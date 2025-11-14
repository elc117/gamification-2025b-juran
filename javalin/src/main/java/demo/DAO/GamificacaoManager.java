package demo.dao;

import demo.model.Conquista;
import demo.model.Treino;
import demo.model.Usuario;
import java.util.List;

public class GamificacaoManager {
    private List<Conquista> conquistas;

    public GamificacaoManager(List<Conquista> conquistas) {
        this.conquistas = conquistas;
    }

    public void registrarTreino(Usuario usuario, Treino treino) {
        usuario.adicionarTreino(treino);
        atualizarPontos(usuario);
        checarConquistas(usuario);
    }

    public void atualizarPontos(Usuario usuario) {
        usuario.calcularPontosTotais();
    }

    public void checarConquistas(Usuario usuario) {
        usuario.verificarConquistas(conquistas);
    }
}