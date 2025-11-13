package demo;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteService {

  public static class User {
    public Integer userId;  // nullable on create
    public String name;
    public String email;
    public User() {}
    public User(Integer id, String n, String e) { this.userId = id; this.name = n; this.email = e; }
  }

  private static Connection conn;

  public static void main(String[] args) throws Exception {
    int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "3000"));

    // Open/create DB file users.db
    conn = DriverManager.getConnection("jdbc:sqlite:users.db");
    try (Statement st = conn.createStatement()) {
      st.execute("""
        CREATE TABLE IF NOT EXISTS users(
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          name TEXT,
          email TEXT
        )
      """);
    }

    var app = Javalin.create(cfg -> cfg.showJavalinBanner = false);

    app.get("/healthz", ctx -> ctx.result("ok"));

    app.get("/users", ctx -> ctx.json(listUsers()));

    app.get("/users/{id}", ctx -> {
      int id = Integer.parseInt(ctx.pathParam("id"));
      var u = getUser(id);
      if (u == null) ctx.status(HttpStatus.NOT_FOUND).json("User not found");
      else ctx.json(u);
    });

    app.post("/users", ctx -> {
      var u = ctx.bodyAsClass(User.class); // works via javalin-bundle (Jackson)
      int id = insertUser(u);
      ctx.json("User created with id " + id);
    });

    app.put("/users/{id}", ctx -> {
      int id = Integer.parseInt(ctx.pathParam("id"));
      var u = ctx.bodyAsClass(User.class);
      u.userId = id;
      updateUser(u);
      ctx.json("User updated");
    });

    app.delete("/users/{id}", ctx -> {
      int id = Integer.parseInt(ctx.pathParam("id"));
      deleteUser(id);
      ctx.json("User deleted");
    });

    app.start(port);
  }

  private static List<User> listUsers() throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("SELECT id,name,email FROM users");
         ResultSet rs = ps.executeQuery()) {
      List<User> out = new ArrayList<>();
      while (rs.next()) out.add(new User(rs.getInt(1), rs.getString(2), rs.getString(3)));
      return out;
    }
  }

  private static User getUser(int id) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("SELECT id,name,email FROM users WHERE id=?")) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        return new User(rs.getInt(1), rs.getString(2), rs.getString(3));
      }
    }
  }

  private static int insertUser(User u) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO users(name,email) VALUES(?,?)",
        Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, u.name);
      ps.setString(2, u.email);
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        return keys.next() ? keys.getInt(1) : -1;
      }
    }
  }

  private static void updateUser(User u) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET name=?, email=? WHERE id=?")) {
      ps.setString(1, u.name);
      ps.setString(2, u.email);
      ps.setInt(3, u.userId);
      ps.executeUpdate();
    }
  }

  private static void deleteUser(int id) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
      ps.setInt(1, id);
      ps.executeUpdate();
    }
  }
}
