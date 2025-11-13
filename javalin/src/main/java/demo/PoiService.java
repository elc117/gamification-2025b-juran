package demo;

import io.javalin.Javalin;

import java.util.ArrayList;
import java.util.List;

public class PoiService {

  public record Poi(String poi, double latitude, double longitude) {}

  private static final List<Poi> POIS = List.of(
      new Poi("Centro de Tecnologia", -29.713318, -53.71663),
      new Poi("Biblioteca Central", -29.71566, -53.71523),
      new Poi("Centro de Convenções", -29.72237, -53.71718),
      new Poi("Planetário", -29.72027, -53.71726),
      new Poi("Reitoria da UFSM", -29.72083, -53.71479),
      new Poi("Restaurante Universitário 2", -29.71400, -53.71937),
      new Poi("HUSM", -29.71368, -53.71536),
      new Poi("Pulsar Incubadora Tecnológica - Prédio 2", -29.71101, -53.71634),
      new Poi("Pulsar Incubadora Tecnológica - Prédio 61H", -29.72468, -53.71335),
      new Poi("Casa do Estudante Universitário - CEU II", -29.71801, -53.71465)
  );

    // Haversine
  private static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
    double r = 6371.0;
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat/2) * Math.sin(dLat/2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(dLon/2) * Math.sin(dLon/2);
    return 2 * r * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  public static void main(String[] args) {

    int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "3000"));
    var app = Javalin.create();

    app.get("/poi", ctx -> ctx.json(new Poi("Restaurante Universitário 2", -29.71400, -53.71937)));
    app.get("/poilist", ctx -> ctx.json(POIS));
    app.get("/near/{lat}/{lon}", ctx -> {
      double givenLat = Double.parseDouble(ctx.pathParam("lat"));
      double givenLon = Double.parseDouble(ctx.pathParam("lon"));
      double nearKm = 1.5;
      List<Poi> near = new ArrayList<>();
      for (var p : POIS) if (distanceKm(givenLat, givenLon, p.latitude, p.longitude) <= nearKm) near.add(p);
      ctx.json(near);
    });

    app.start(port);
  }


}
