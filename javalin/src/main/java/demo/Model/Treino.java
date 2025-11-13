package demo.model;

import java.time.LocalDate;

public class Treino {
    private Integer id;  // nullable on create
    private String tipo;  // ex.: "arremesso" ou "corrida"
    private int quantidade;  // ex.: tentativas ou km
    private LocalDate data;
    private int pontosGanhos;

    public Treino() {}

    public Treino(Integer id, String tipo, int quantidade, LocalDate data) {
        this.id = id;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.data = data;
        calcularPontos();  // Calcula automaticamente
    }

    // Getters e setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public int getPontosGanhos() { return pontosGanhos; }

    // Método OO: calcula pontos baseado no tipo (gamificação)
    public void calcularPontos() {
        if ("arremesso".equals(tipo)) {
            pontosGanhos = quantidade * 10;  // 10 pontos por tentativa
        } else if ("corrida".equals(tipo)) {
            pontosGanhos = quantidade * 5;  // 5 pontos por km
        } else {
            pontosGanhos = 0;
        }
    }
}