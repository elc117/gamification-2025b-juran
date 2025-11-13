package demo;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import demo.model.Treino;
import demo.model.Usuario;
import demo.model.Conquista;
import demo.service.TreinoDAO;
import demo.service.GamificacaoManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Gamification {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "3000"));

        // Instâncias OO para DAO e gamificação
        TreinoDAO dao = new TreinoDAO();
        List<Conquista> conquistas = Arrays.asList(
            new Conquista(1, "Iniciante em Basquete", "Ganhar 100 pontos totais", "pontosTotais > 100")
        );
        GamificacaoManager manager = new GamificacaoManager(conquistas);
        Usuario usuario = new Usuario(1, "Jogador1");  // Exemplo de usuário (em produção, gerencie via DAO)

        var app = Javalin.create(cfg -> cfg.showJavalinBanner = false);

        app.get("/healthz", ctx -> ctx.result("ok"));

        // CRUD de Treinos (adaptado de users)
        app.get("/treinos", ctx -> {
            try {
                ctx.json(dao.listarTodos());
            } catch (SQLException e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Erro ao listar treinos");
            }
        });

        app.get("/treinos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            try {
                Treino t = dao.buscarPorId(id);
                if (t == null) ctx.status(HttpStatus.NOT_FOUND).json("Treino not found");
                else ctx.json(t);
            } catch (SQLException e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Erro ao buscar treino");
            }
        });

        app.post("/treinos", ctx -> {
            try {
                Treino t = ctx.bodyAsClass(Treino.class);
                t.calcularPontos();  // Calcula pontos automaticamente
                int id = dao.salvar(t);
                manager.registrarTreino(usuario, t);  // Integra gamificação
                ctx.json("Treino created with id " + id + ". Pontos ganhos: " + t.getPontosGanhos());
            } catch (SQLException e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Erro ao salvar treino");
            }
        });

        app.put("/treinos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            try {
                Treino t = ctx.bodyAsClass(Treino.class);
                t.setId(id);
                t.calcularPontos();
                dao.atualizar(t);
                manager.registrarTreino(usuario, t);  // Recalcula gamificação
                ctx.json("Treino updated. Pontos ganhos: " + t.getPontosGanhos());
            } catch (SQLException e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Erro ao atualizar treino");
            }
        });

        app.delete("/treinos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            try {
                dao.deletar(id);
                ctx.json("Treino deleted");
            } catch (SQLException e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Erro ao deletar treino");
            }
        });

        // Rota adicional para ver status do usuário (gamificação)
        app.get("/usuario", ctx -> ctx.json(Map.of(
            "nome", usuario.getNome(),
            "pontosTotais", usuario.getPontosTotais(),
            "nivel", usuario.getNivel(),
            "conquistas", conquistas.stream().filter(Conquista::isDesbloqueada).map(Conquista::getNome).toList()
        )));

        app.start(port);
    }
}