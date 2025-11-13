package demo.model;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private int id;
    private String nome;
    private int nivel;
    private int pontosTotais;
    private List<Treino> treinos;

    public Usuario(int id, String nome) {
        this.id = id;
        this.nome = nome;
        this.nivel = 1;  // Inicia no nível 1
        this.pontosTotais = 0;
        this.treinos = new ArrayList<>();
    }

    // Getters e setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public int getNivel() { return nivel; }
    public int getPontosTotais() { return pontosTotais; }
    public List<Treino> getTreinos() { return treinos; }

    // Métodos OO para gamificação
    public void adicionarTreino(Treino treino) {
        treinos.add(treino);
        calcularPontosTotais();
        atualizarNivel();  // Ex.: nível baseado em pontos
    }

    public void calcularPontosTotais() {
        pontosTotais = treinos.stream().mapToInt(Treino::getPontosGanhos).sum();
    }

    private void atualizarNivel() {
        nivel = (pontosTotais / 100) + 1;  // Ex.: nível aumenta a cada 100 pontos
    }

    public void verificarConquistas(List<Conquista> conquistas) {
        for (Conquista c : conquistas) {
            if (!c.isDesbloqueada() && c.verificarSeDesbloqueada(this)) {
                c.setDesbloqueada(true);
            }
        }
    }
}