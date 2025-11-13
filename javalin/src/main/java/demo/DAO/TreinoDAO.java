package demo.service;

import demo.model.Treino;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TreinoDAO {
    private Connection conn;

    public TreinoDAO() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:treinos.db");
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS treinos(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tipo TEXT,
                    quantidade INTEGER,
                    data TEXT,
                    pontos INTEGER
                )
            """);
        }
    }

    public List<Treino> listarTodos() throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, tipo, quantidade, data, pontos FROM treinos");
             ResultSet rs = ps.executeQuery()) {
            List<Treino> treinos = new ArrayList<>();
            while (rs.next()) {
                treinos.add(new Treino(rs.getInt(1), rs.getString(2), rs.getInt(3), LocalDate.parse(rs.getString(4))));
            }
            return treinos;
        }
    }

    public Treino buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, tipo, quantidade, data, pontos FROM treinos WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Treino(rs.getInt(1), rs.getString(2), rs.getInt(3), LocalDate.parse(rs.getString(4)));
            }
        }
    }

    public int salvar(Treino treino) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO treinos(tipo, quantidade, data, pontos) VALUES(?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, treino.getTipo());
            ps.setInt(2, treino.getQuantidade());
            ps.setString(3, treino.getData().toString());
            ps.setInt(4, treino.getPontosGanhos());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public void atualizar(Treino treino) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE treinos SET tipo=?, quantidade=?, data=?, pontos=? WHERE id=?")) {
            ps.setString(1, treino.getTipo());
            ps.setInt(2, treino.getQuantidade());
            ps.setString(3, treino.getData().toString());
            ps.setInt(4, treino.getPontosGanhos());
            ps.setInt(5, treino.getId());
            ps.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM treinos WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}