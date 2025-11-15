package demo;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.json.JavalinJackson;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.nio.file.Files;
import java.nio.file.Paths;

// Enum para tipos de treino (atualizado: adicionado SALTOS, removidos DEFESA e PASSES)
enum TipoTreino {
    ARREMESSO, CORRIDA, SALTOS, ABDOMINAIS
}

public class Gamification {

    private static final String DB_URL = "jdbc:sqlite:treinos.db";
    // Lista de desafios atualizada (apenas corrida, abdominais, saltos e arremessos)
    private static final List<String> DESAFIOS = List.of(
        "Corrida de 5km em menos de 30 minutos",
        "Fazer 50 abdominais",
        "Saltar 20 vezes seguidas",
        "Acertar 10 arremessos seguidos de três pontos",
        "Corrida de 10km",
        "Fazer 100 abdominais",
        "Saltar 50 vezes",
        "Acertar 20 arremessos de meia quadra",
        "Corrida de 15km",
        "Fazer 150 abdominais"
    );

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson());
            // CORS: Para habilitar, adicione dependência 'javalin-cors' e use:
            // config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
        }).start(Integer.parseInt(System.getenv().getOrDefault("PORT", "3000")));

        app.get("/", ctx -> {
            try {
                String html = new String(Files.readAllBytes(Paths.get("src/main/resources/public/index.html")));
                ctx.html(html);
            } catch (Exception e) {
                ctx.status(500).result("Erro ao carregar index.html: " + e.getMessage());
            }
        });

        // Inicializar banco com melhor estrutura
        initDatabase();

        // Rotas (agrupadas logicamente)
        app.get("/healthz", ctx -> ctx.result("OK"));
        app.get("/treinos", Gamification::listTreinos);
        app.post("/treinos", Gamification::createTreino);
        app.get("/treinos/{id}", Gamification::getTreino);
        app.put("/treinos/{id}", Gamification::updateTreino);
        app.delete("/treinos/{id}", Gamification::deleteTreino);
        app.get("/usuario", Gamification::getUsuarioStatus);
        app.get("/desafios/diario", Gamification::getDesafioDiario);
        app.post("/desafios/concluir", Gamification::concluirDesafio);  // Nova rota para concluir desafio
        app.get("/quadra/ponto-aleatorio", Gamification::getPontoAleatorio);
        app.get("/estatisticas", Gamification::getEstatisticas);
        app.get("/assets/*", ctx -> {
            String path = ctx.path().substring(1);
            try {
                byte[] bytes = Files.readAllBytes(Paths.get("src/main/resources/public/" + path));
                ctx.result(bytes).contentType("image/jpeg");
            } catch (Exception e) {
                ctx.status(404);
            }
        });
    }

    private static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Adicionar índice para performance
            String sqlTreinos = "CREATE TABLE IF NOT EXISTS treinos (id INTEGER PRIMARY KEY AUTOINCREMENT, tipo TEXT, quantidade INTEGER, data TEXT)";
            conn.createStatement().execute(sqlTreinos);
            String sqlIndex = "CREATE INDEX IF NOT EXISTS idx_tipo ON treinos (tipo)";
            conn.createStatement().execute(sqlIndex);
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar DB: " + e.getMessage());
        }
    }

    private static void listTreinos(Context ctx) {
        try {
            List<Treino> treinos = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM treinos ORDER BY data DESC")) {
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
            Treino treino = ctx.bodyAsClass(Treino.class);
            if (!isValidTreino(treino)) {
                ctx.status(400).result("Dados inválidos: tipo deve ser válido e quantidade > 0");
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

    private static boolean isValidTreino(Treino treino) {
        if (treino == null || treino.getTipo() == null || treino.getQuantidade() <= 0) return false;
        try {
            TipoTreino.valueOf(treino.getTipo().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void getUsuarioStatus(Context ctx) {
        try {
            int totalPontos = 0;
            int totalTreinos = 0;
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 ResultSet rs = conn.createStatement().executeQuery("SELECT SUM(quantidade * CASE WHEN tipo = 'arremesso' THEN 10 ELSE 5 END) AS pontos, COUNT(*) AS treinos FROM treinos")) {
                if (rs.next()) {
                    totalPontos = rs.getInt("pontos");
                    totalTreinos = rs.getInt("treinos");
                }
            }
            String status = totalPontos >= 100 ? "Conquista desbloqueada!" : "Continue treinando";
            ctx.json(new UsuarioStatus(totalPontos, status, totalTreinos));
        } catch (Exception e) {
            ctx.status(500).result("Erro ao obter status: " + e.getMessage());
        }
    }

    private static int calcularPontos(Treino treino) {
        try {
            TipoTreino tipo = TipoTreino.valueOf(treino.getTipo().toUpperCase());
            return switch (tipo) {
                case ARREMESSO -> treino.getQuantidade() * 1;  // 1 ponto por unidade
                case CORRIDA -> calcularPontosCorrida(treino.getQuantidade());  // Progressivo por km
                case SALTOS -> treino.getQuantidade() * 4;  // 4 pontos por unidade
                case ABDOMINAIS -> treino.getQuantidade() / 2;  // 0.5 pontos por unidade (arredonda para baixo)
            };
        } catch (IllegalArgumentException e) {
            return treino.getQuantidade() * 1;  // Fallback para 1 ponto por unidade
        }
    }

    // Função auxiliar para pontos de corrida (progressivos por km)
    private static int calcularPontosCorrida(int km) {
        if (km <= 0) return 0;
        int pontos = 0;
        for (int i = 1; i <= km; i++) {
            if (i == 1) pontos += 1;
            else if (i == 2) pontos += 3;
            else if (i == 3) pontos += 6;
            else if (i == 4) pontos += 10;
            else if (i == 5) pontos += 15;
            else pontos += 10;  // +10 por km adicional
        }
        return pontos;
    }

    private static void getDesafioDiario(Context ctx) {
        String dateParam = ctx.queryParam("date");
        LocalDate date = dateParam != null ? LocalDate.parse(dateParam) : LocalDate.now();
        int diaDoAno = date.getDayOfYear();
        String desafio = DESAFIOS.get(diaDoAno % DESAFIOS.size());
        ctx.json(Map.of("data", date.toString(), "desafio", desafio));
    }

    // Nova rota para concluir desafio e cadastrar treino
    private static void concluirDesafio(Context ctx) {
        try {
            String dateParam = ctx.queryParam("date");
            LocalDate date = dateParam != null ? LocalDate.parse(dateParam) : LocalDate.now();
            int diaDoAno = date.getDayOfYear();
            String desafio = DESAFIOS.get(diaDoAno % DESAFIOS.size());
            
            String tipo = extrairTipoDoDesafio(desafio);
            int quantidade = extrairQuantidadeDoDesafio(desafio);
            
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO treinos (tipo, quantidade, data) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, tipo);
                stmt.setInt(2, quantidade);
                stmt.setString(3, date.toString());
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    ctx.result("Desafio concluído e cadastrado como treino!");
                }
            }
        } catch (Exception e) {
            ctx.status(500).result("Erro ao concluir desafio: " + e.getMessage());
        }
    }

    // Funções auxiliares para extrair tipo e quantidade do desafio
    private static String extrairTipoDoDesafio(String desafio) {
        if (desafio.contains("Corrida")) return "corrida";
        if (desafio.contains("abdominais")) return "abdominais";
        if (desafio.contains("Saltar")) return "saltos";
        if (desafio.contains("arremessos")) return "arremesso";
        return "corrida";
    }

    private static int extrairQuantidadeDoDesafio(String desafio) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+");
        java.util.regex.Matcher matcher = pattern.matcher(desafio);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 1;
    }

    private static void getPontoAleatorio(Context ctx) {
        int x = ThreadLocalRandom.current().nextInt(0, 355);
        int y = ThreadLocalRandom.current().nextInt(0, 289);
        ctx.json(Map.of("x", x, "y", y));
    }

    private static void getEstatisticas(Context ctx) {
        try {
            Map<String, List<Map<String, Object>>> stats = new HashMap<>();
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 ResultSet rs = conn.createStatement().executeQuery("SELECT tipo, data, SUM(quantidade) AS total FROM treinos GROUP BY tipo, data ORDER BY data")) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo").toLowerCase();
                    String data = rs.getString("data");
                    int total = rs.getInt("total");
                    stats.computeIfAbsent(tipo, k -> new ArrayList<>()).add(Map.of("data", data, "quantidade", total));
                }
            }
            ctx.json(stats);
        } catch (Exception e) {
            ctx.status(500).result("Erro ao obter estatísticas: " + e.getMessage());
        }
    }

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

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public int getQuantidade() { return quantidade; }
        public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    public static class UsuarioStatus {
        private int pontos;
        private String status;
        private int totalTreinos;

        public UsuarioStatus(int pontos, String status, int totalTreinos) {
            this.pontos = pontos;
            this.status = status;
            this.totalTreinos = totalTreinos;
        }

        public int getPontos() { return pontos; }
        public void setPontos(int pontos) { this.pontos = pontos; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getTotalTreinos() { return totalTreinos; }
        public void setTotalTreinos(int totalTreinos) { this.totalTreinos = totalTreinos; }
    }
}