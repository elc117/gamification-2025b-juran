package demo.model;

public class Conquista {
    private int id;
    private String nome;
    private String descricao;
    private String criterio;  // Ex.: "pontosTotais > 100"
    private boolean desbloqueada;

    public Conquista(int id, String nome, String descricao, String criterio) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.criterio = criterio;
        this.desbloqueada = false;
    }

    // Getters e setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getCriterio() { return criterio; }
    public boolean isDesbloqueada() { return desbloqueada; }
    public void setDesbloqueada(boolean desbloqueada) { this.desbloqueada = desbloqueada; }

    // Método OO: verifica se a conquista é desbloqueada baseado no usuário
    public boolean verificarSeDesbloqueada(Usuario usuario) {
        // Lógica simples: avalia o critério (expanda para mais complexidade)
        if ("pontosTotais > 100".equals(criterio)) {
            return usuario.getPontosTotais() > 100;
        }
        return false;  // Adicione mais critérios conforme necessário
    }
}