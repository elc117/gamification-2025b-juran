package demo;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.json.JavalinJackson;  // Import corrigido para JavalinJackson
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;  // Import adicionado para ArrayList
import java.util.List;      // Import adicionado para List

public class Gamification {

    private static final String DB_URL = "jdbc:sqlite:treinos.db";

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson());  // Configura Jackson para JSON (usando import corrigido)
        }).start(Integer.parseInt(System.getenv().getOrDefault("PORT", "3000")));

        // Inicializar banco
        initDatabase();

        // Rotas
        app.get("/healthz", ctx -> ctx.result("OK"));
        app.get("/treinos", Gamification::listTreinos);
        app.post("/treinos", Gamification::createTreino);
        app.get("/treinos/{id}", Gamification::getTreino);
        app.put("/treinos/{id}", Gamification::updateTreino);
        app.delete("/treinos/{id}", Gamification::deleteTreino);
        app.get("/usuario", Gamification::getUsuarioStatus);
    }

    private static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "CREATE TABLE IF NOT EXISTS treinos (id INTEGER PRIMARY KEY AUTOINCREMENT, tipo TEXT, quantidade INTEGER, data TEXT)";
            conn.createStatement().execute(sql);
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar DB: " + e.getMessage());
        }
    }

    private static void listTreinos(Context ctx) {
        try {
            List<Treino> treinos = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM treinos")) {
                while (rs.next()) {
                    treinos.add(new Treino(rs.getInt("id"), rs.getString("tipo"), rs.getInt("quantidade"), rs.getString("data")));
                }
            }
            ctx.json(treinos);
        } catch (Exception e) {
            ctx.status(500).result("Erro ao listar treinos: " + e.getMessage());
        }
    }

    private static void createTreino(Context ctx) {
        try {
            Treino treino = ctx.bodyAsClass(Treino.class);  // Mapeia JSON para Treino
            if (treino.getTipo() == null || treino.getQuantidade() <= 0) {
                ctx.status(400).result("Dados inválidos: tipo não pode ser null e quantidade deve ser > 0");
                return;
            }
            int pontos = calcularPontos(treino);
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO treinos (tipo, quantidade, data) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, treino.getTipo());
                stmt.setInt(2, treino.getQuantidade());
                stmt.setString(3, treino.getData());
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    ctx.result("Treino created with id " + id + ". Pontos ganhos: " + pontos);
                }
            }
        } catch (Exception e) {
            ctx.status(500).result("Erro ao criar treino: " + e.getMessage());
        }
    }

    private static void getTreino(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM treinos WHERE id = ?")) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    ctx.json(new Treino(rs.getInt("id"), rs.getString("tipo"), rs.getInt("quantidade"), rs.getString("data")));
                } else {
                    ctx.status(404).result("Treino não encontrado");
                }
            }
        } catch (Exception e) {
            ctx.status(500).result("Erro ao buscar treino: " + e.getMessage());
        }
    }

    private static void updateTreino(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Treino treino = ctx.bodyAsClass(Treino.class);
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement stmt = conn.prepareStatement("UPDATE treinos SET tipo = ?, quantidade = ?, data = ? WHERE id = ?")) {
                stmt.setString(1, treino.getTipo());
                stmt.setInt(2, treino.getQuantidade());
                stmt.setString(3, treino.getData());
                stmt.setInt(4, id);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    ctx.result("Treino updated");
                } else {
                    ctx.status(404).result("Treino não encontrado");
                }
            }
        } catch (Exception e) {
            ctx.status(500).result("Erro ao atualizar treino: " + e.getMessage());
        }
    }

    private static void deleteTreino(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM treinos WHERE id = ?")) {
                stmt.setInt(1, id);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    ctx.result("Treino deleted");
                } else {
                    ctx.status(404).result("Treino não encontrado");
                }
            }
        } catch (Exception e) {
            ctx.status(500).result("Erro ao deletar treino: " + e.getMessage());
        }
    }

    private static void getUsuarioStatus(Context ctx) {
        try {
            int totalPontos = 0;
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 ResultSet rs = conn.createStatement().executeQuery("SELECT SUM(quantidade * CASE WHEN tipo = 'arremesso' THEN 10 ELSE 5 END) AS pontos FROM treinos")) {
                if (rs.next()) {
                    totalPontos = rs.getInt("pontos");
                }
            }
            ctx.json(new UsuarioStatus(totalPontos, totalPontos >= 100 ? "Conquista desbloqueada!" : "Continue treinando"));
        } catch (Exception e) {
            ctx.status(500).result("Erro ao obter status: " + e.getMessage());
        }
    }

    private static int calcularPontos(Treino treino) {
        return treino.getTipo().equals("arremesso") ? treino.getQuantidade() * 10 : treino.getQuantidade() * 5;
    }

    // Classe Treino (deve estar em model/Treino.java, mas incluída aqui para simplicidade)
    public static class Treino {
        private int id;
        private String tipo;
        private int quantidade;
        private String data;

        public Treino() {}
        public Treino(int id, String tipo, int quantidade, String data) {
            this.id = id;
            this.tipo = tipo;
            this.quantidade = quantidade;
            this.data = data;
        }

        // Getters e setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public int getQuantidade() { return quantidade; }
        public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    // Classe UsuarioStatus
    public static class UsuarioStatus {
        private int pontos;
        private String status;

        public UsuarioStatus(int pontos, String status) {
            this.pontos = pontos;
            this.status = status;
        }

        public int getPontos() { return pontos; }
        public String getStatus() { return status; }
    }
}