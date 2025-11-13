package demo;

import io.javalin.Javalin;

public class HelloJavalin {
    public static void main(String[] a) {
        int p = Integer.parseInt(System.getenv().getOrDefault("PORT", "3000"));
        Javalin app = Javalin.create().start(p);
        app.get("/", ctx -> ctx.result("Hello World"));
    }
}
