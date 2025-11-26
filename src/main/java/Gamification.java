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

// Enum para tipos de treino
enum TipoTreino {
    ARREMESSO, CORRIDA, SALTOS, ABDOMINAIS
}

public class Gamification {

    private static final String DB_URL;

    static {
        if (System.getenv("RENDER") != null) {
            DB_URL = "jdbc:sqlite:/var/data/treinos.db";
        } else {
            DB_URL = "jdbc:sqlite:treinos.db";
        }
    }
    // Lista de desafios atualizada
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
            
        }).start(Integer.parseInt(System.getenv().getOrDefault("PORT", "3000")));

        app.get("/", ctx -> serveStaticFile(ctx, "index.html", "text/html"));
        app.get("/assets/{file}", ctx -> {
            String fileName = ctx.pathParam("file");
            serveStaticFile(ctx, "assets/" + fileName, getContentType(fileName));
        });

        // Inicializar banco com melhor estrutura
        initDatabase();

        // Rotas
        app.get("/healthz", ctx -> ctx.result("OK"));
        app.get("/treinos", Gamification::listTreinos);
        app.post("/treinos", Gamification::createTreino);
        app.get("/treinos/{id}", Gamification::getTreino);
        app.put("/treinos/{id}", Gamification::updateTreino);
        app.delete("/treinos/{id}", Gamification::deleteTreino);
        app.get("/usuario", Gamification::getUsuarioStatus);
        app.get("/desafios/diario", Gamification::getDesafioDiario);
        app.post("/desafios/concluir", Gamification::concluirDesafio);
        app.get("/quadra/ponto-aleatorio", Gamification::getPontoAleatorio);
        app.get("/quadra/sequencia-habilidades", Gamification::getSequenciaHabilidades);
        app.get("/estatisticas", Gamification::getEstatisticas);
    }

    private static void serveStaticFile(Context ctx, String filePath, String contentType) {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("conteudo-pagina", filePath);
            if (java.nio.file.Files.exists(path)) {
                byte[] fileBytes = java.nio.file.Files.readAllBytes(path);
                ctx.contentType(contentType);
                ctx.result(fileBytes);
            } else {
                ctx.status(404).result("Arquivo não encontrado: " + filePath);
            }
        } catch (Exception e) {
            ctx.status(500).result("Erro ao carregar arquivo: " + e.getMessage());
        }
    }

    private static String getContentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html";
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".png")) return "image/png";
        return "text/plain";
    }

    private static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
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
                    ctx.result("Treino criado. Pontos ganhos: " + pontos);
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
            int totalDesafios = 0;
            Map<String, Integer> somasPorTipo = new HashMap<>();
            try (Connection conn = DriverManager.getConnection(DB_URL);
                ResultSet rs = conn.createStatement().executeQuery("SELECT tipo, SUM(quantidade) AS total, COUNT(*) AS treinos FROM treinos GROUP BY tipo")) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo").toLowerCase();
                    int total = rs.getInt("total");
                    somasPorTipo.put(tipo, total);
                    totalTreinos += rs.getInt("treinos");
                    totalPontos += calcularPontos(new Treino(0, tipo, total, ""));
                }
            }
            try (Connection conn = DriverManager.getConnection(DB_URL);
                ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) AS desafios FROM treinos WHERE data LIKE '%desafio%' OR tipo = 'desafio'")) {
                if (rs.next()) totalDesafios = rs.getInt("desafios");
            }

            List<Conquista> conquistas = new ArrayList<>();
            conquistas.add(new Conquista("Recruta Fitness", "Registre o décimo treino", totalTreinos >= 10));
            conquistas.add(new Conquista("Arremessador Estrela ", "Acumule 50 arremessos", somasPorTipo.getOrDefault("arremesso", 0) >= 50));
            conquistas.add(new Conquista("Arremessador Fera ", "Acumule 500 arremessos", somasPorTipo.getOrDefault("arremesso", 0) >= 500));
            conquistas.add(new Conquista("Arremessador Supremacia ", "Acumule 2500 arremessos", somasPorTipo.getOrDefault("arremesso", 0) >= 2500));
            conquistas.add(new Conquista("Corredor de Pés Leves", "Complete 25 km de corrida", somasPorTipo.getOrDefault("corrida", 0) >= 25));
            conquistas.add(new Conquista("Corredor Veloz como o Vento", "Complete 100 km de corrida", somasPorTipo.getOrDefault("corrida", 0) >= 100));
            conquistas.add(new Conquista("Corredor Lendário", "Complete 500 km de corrida", somasPorTipo.getOrDefault("corrida", 0) >= 500));
            conquistas.add(new Conquista("Saltador Destemido ", "Acumule 100 saltos", somasPorTipo.getOrDefault("saltos", 0) >= 100));
            conquistas.add(new Conquista("Saltador Elástico", "Acumule 500 saltos", somasPorTipo.getOrDefault("saltos", 0) >= 500));
            conquistas.add(new Conquista("Saltador Olímpico", "Acumule 2000 saltos", somasPorTipo.getOrDefault("saltos", 0) >= 2000));
            conquistas.add(new Conquista("Abdômen Imbatível", "Faça 350 abdominais", somasPorTipo.getOrDefault("abdominais", 0) >= 350));
            conquistas.add(new Conquista("Abdômen de Ferro", "Faça 2000 abdominais", somasPorTipo.getOrDefault("abdominais", 0) >= 2000));
            conquistas.add(new Conquista("Abdômen Titânico", "Faça 20000 abdominais", somasPorTipo.getOrDefault("abdominais", 0) >= 20000));
            conquistas.add(new Conquista("Desafiador Heróico", "Conclua 15 desafios", totalDesafios >= 15));
            conquistas.add(new Conquista("Treinador de 3 Estrelas", "Treine por 3 dias consecutivos", totalTreinos >= 3));
            conquistas.add(new Conquista("Treinador Constante", "Treine por 7 dias consecutivos", totalTreinos >= 7));
            conquistas.add(new Conquista("Treinador Mestral", "Treine por 14 dias consecutivos", totalTreinos >= 14));
            conquistas.add(new Conquista("Treinador Supremum", "Treine por 25 dias consecutivos", totalTreinos >= 25));
            conquistas.add(new Conquista("Pontuador Radiante", "Atinga 500 pontos", totalPontos >= 500));
            conquistas.add(new Conquista("Mestre da Quadra", "Acumule 2000 pontos", totalPontos >= 2000));
            conquistas.add(new Conquista("Pontuador Gigante", "Atinga 5000 pontos", totalPontos >= 5000));
            conquistas.add(new Conquista("Mestre da Quadra Suprema", "Acumule 20000 pontos", totalPontos >= 20000));
            conquistas.add(new Conquista("Pontuador Elite", "Atinga 50000 pontos", totalPontos >= 50000));
            conquistas.add(new Conquista("Mestre da Quadra Galáctica", "Acumule 100000 pontos", totalPontos >= 100000));

            long desbloqueadas = conquistas.stream().filter(Conquista::isDesbloqueada).count();
            conquistas.add(new Conquista("Campeão em Ascensão", "Desbloqueie metade das conquistas", desbloqueadas >= 12));
            conquistas.add(new Conquista("Campeão Absoluto", "Desbloqueie todas as conquistas", desbloqueadas >= 24));

            String status = totalPontos >= 100 ? "Conquista desbloqueada!" : "Continue treinando";
            ctx.json(Map.of("pontos", totalPontos, "status", status, "totalTreinos", totalTreinos, "conquistas", conquistas));
        } catch (Exception e) {
            ctx.status(500).result("Erro ao obter status: " + e.getMessage());
        }
    }

    private static int calcularPontos(Treino treino) {
        try {
            TipoTreino tipo = TipoTreino.valueOf(treino.getTipo().toUpperCase());
            return switch (tipo) {
                case ARREMESSO -> treino.getQuantidade() * 1;
                case CORRIDA -> calcularPontosCorrida(treino.getQuantidade());
                case SALTOS -> treino.getQuantidade() * 4;
                case ABDOMINAIS -> treino.getQuantidade() / 2;
            };
        } catch (IllegalArgumentException e) {
            return treino.getQuantidade() * 1;
        }
    }

    private static int calcularPontosCorrida(int km) {
        if (km <= 0) return 0;
        for (int i = 1; i <= km; i++) {
            int pontos = 0;
            if (i == 1) pontos += 1;
            else if (i == 2) pontos += 3;
            else if (i == 3) pontos += 6;
            else if (i == 4) pontos += 10;
            else if (i == 5) pontos += 15;
            else pontos = (km - 2) * 5;
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

    private static void getSequenciaHabilidades(Context ctx) {
        List<Map<String, Integer>> pontos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int x = ThreadLocalRandom.current().nextInt(0, 355);
            int y = ThreadLocalRandom.current().nextInt(0, 289);
            pontos.add(Map.of("x", x, "y", y));
        }
        ctx.json(pontos);
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

    public static class Conquista {
        private String nome;
        private String descricao;
        private boolean desbloqueada;

        public Conquista(String nome, String descricao, boolean desbloqueada) {
            this.nome = nome;
            this.descricao = descricao;
            this.desbloqueada = desbloqueada;
        }

        public String getNome() { return nome; }
        public String getDescricao() { return descricao; }
        public boolean isDesbloqueada() { return desbloqueada; }
    }

}



