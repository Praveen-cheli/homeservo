package com.homeservo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/** HomeServo web app: a lightweight Java HTTP server and static booking experience. */
public class HomeServoApplication {
    private static final List<String> bookings = new CopyOnWriteArrayList<>();
    private static final AtomicInteger ids = new AtomicInteger(1000);
    private static final String SERVICES = """
      [{"id":"cleaning","name":"Home cleaning","detail":"A fresh, tidy home","price":149,"unit":"hour","icon":"🧹","tone":"mint","tag":"MOST BOOKED"},
       {"id":"dishes","name":"Dishwashing","detail":"Sink cleared, kitchen reset","price":99,"unit":"hour","icon":"🍽️","tone":"peach","tag":"QUICK FIX"},
       {"id":"laundry","name":"Laundry & folding","detail":"Wash, dry and neatly fold","price":129,"unit":"hour","icon":"🧺","tone":"lavender","tag":"EASY DAY"},
       {"id":"bathroom","name":"Bathroom refresh","detail":"A sparkling clean bathroom","price":199,"unit":"visit","icon":"🫧","tone":"blue","tag":"POPULAR"},
       {"id":"kitchen","name":"Kitchen cleaning","detail":"Grease-free counters & hob","price":179,"unit":"hour","icon":"✨","tone":"yellow","tag":"FRESH PICK"},
       {"id":"dusting","name":"Dusting & surfaces","detail":"The little details, handled","price":99,"unit":"hour","icon":"🪴","tone":"rose","tag":"FEELS GOOD"}]
      """;

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/services", e -> respond(e, 200, SERVICES, "application/json"));
        server.createContext("/api/bookings", HomeServoApplication::handleBookings);
        server.createContext("/", HomeServoApplication::serveSite);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("HomeServo is running at http://localhost:" + port);
    }

    private static void handleBookings(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) { respond(exchange, 204, "", "text/plain"); return; }
        if (!"POST".equals(exchange.getRequestMethod())) { respond(exchange, 405, "{\"error\":\"Use POST to create a booking\"}", "application/json"); return; }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String id = "HS-" + ids.incrementAndGet();
        String safe = body.replace("\\", "\\\\").replace("\"", "\\\"");
        bookings.add("{\"id\":\"" + id + "\",\"createdAt\":\"" + Instant.now() + "\",\"details\":\"" + safe + "\"}");
        respond(exchange, 201, "{\"id\":\"" + id + "\",\"message\":\"Your home helper is being matched\"}", "application/json");
    }

    private static void serveSite(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) { respond(exchange, 405, "Method not allowed", "text/plain"); return; }
        Path page = Path.of("src/main/resources/static/index.html");
        if (!Files.exists(page)) { respond(exchange, 404, "HomeServo page not found", "text/plain"); return; }
        respond(exchange, 200, Files.readString(page), "text/html; charset=utf-8");
    }

    private static void respond(HttpExchange exchange, int status, String body, String type) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var out = exchange.getResponseBody()) { out.write(bytes); }
    }
}
