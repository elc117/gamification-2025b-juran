package demo;

import io.javalin.Javalin;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomAdviceService {
    private static final List<String> ADVICES = List.of(
            "It always seems impossible, until it's done.",
            "To cleanly remove the seed from an Avocado, lay a knife firmly across it, and twist.",
            "Fail. Fail again. Fail better.",
            "Play is the true mother of invention.",
            "Remedy tickly coughs with a drink of honey, lemon and water as hot as you can take.");

    public static void main(String[] a) {
        int p = Integer.parseInt(System.getenv().getOrDefault("PORT", "3000"));
        var app = Javalin.create();
        app.get("/advice", ctx -> {
            var i = ThreadLocalRandom.current().nextInt(ADVICES.size());
            ctx.result(ADVICES.get(i));
        });
        app.start(p);
    }
}