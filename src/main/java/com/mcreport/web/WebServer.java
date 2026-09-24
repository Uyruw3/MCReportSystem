package com.mcreport.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mcreport.MCReportPlugin;
import com.mcreport.storage.Storage;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class WebServer {

    private final MCReportPlugin plugin;
    private final Storage storage;
    private HttpServer server;
    private final ObjectMapper objectMapper;

    public WebServer(MCReportPlugin plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newFixedThreadPool(4));

            server.createContext("/api/reports", new ReportsHandler());
            server.createContext("/api/appeals", new AppealsHandler());
            server.createContext("/api/players", new PlayersHandler());
            server.createContext("/api/stats", new StatsHandler());
            server.createContext("/api/health", new HealthHandler());

            server.start();
            plugin.getLogger().info("Web API started on port " + port);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start web server: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("Web API stopped");
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        String json = objectMapper.writeValueAsString(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, json.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, Map.of("error", message));
    }

    private class ReportsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                if ("GET".equals(method)) {
                    if ("/api/reports".equals(path)) {
                        List<String> pending = storage.getPendingReportsList();
                        sendJson(exchange, 200, Map.of(
                            "pending", pending,
                            "total", storage.getTotalReports(),
                            "resolved", storage.getResolvedReports()
                        ));
                    } else if (path.startsWith("/api/reports/")) {
                        String reportId = path.substring("/api/reports/".length());
                        Map<String, Object> report = storage.getReport(reportId);
                        if (report.isEmpty()) {
                            sendError(exchange, 404, "Report not found");
                        } else {
                            sendJson(exchange, 200, report);
                        }
                    } else {
                        sendError(exchange, 404, "Not found");
                    }
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("API error: " + e.getMessage());
                sendError(exchange, 500, "Internal server error");
            }
        }
    }

    private class AppealsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                if ("GET".equals(method)) {
                    if ("/api/appeals".equals(path)) {
                        List<String> pending = storage.getPendingAppealsList();
                        sendJson(exchange, 200, Map.of("pending", pending));
                    } else if (path.startsWith("/api/appeals/")) {
                        String appealId = path.substring("/api/appeals/".length());
                        String player = storage.getAppealPlayer(appealId);
                        if (player == null || player.isEmpty()) {
                            sendError(exchange, 404, "Appeal not found");
                        } else {
                            sendJson(exchange, 200, Map.of("id", appealId, "player", player));
                        }
                    } else {
                        sendError(exchange, 404, "Not found");
                    }
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("API error: " + e.getMessage());
                sendError(exchange, 500, "Internal server error");
            }
        }
    }

    private class PlayersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                if ("GET".equals(method) && path.startsWith("/api/players/")) {
                    String playerName = path.substring("/api/players/".length());
                    int warns = storage.getPlayerWarns(playerName);
                    List<String> reports = storage.getPlayerReports(playerName);
                    sendJson(exchange, 200, Map.of(
                        "name", playerName,
                        "warns", warns,
                        "reports", reports,
                        "reportCount", reports.size()
                    ));
                } else {
                    sendError(exchange, 404, "Not found");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("API error: " + e.getMessage());
                sendError(exchange, 500, "Internal server error");
            }
        }
    }

    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("GET".equals(exchange.getRequestMethod())) {
                    sendJson(exchange, 200, Map.of(
                        "totalReports", storage.getTotalReports(),
                        "pendingReports", storage.getPendingReports(),
                        "resolvedReports", storage.getResolvedReports(),
                        "bans", storage.getActionCount("BAN"),
                        "tempbans", storage.getActionCount("TEMPBAN"),
                        "kicks", storage.getActionCount("KICK"),
                        "warns", storage.getActionCount("WARN"),
                        "mutes", storage.getActionCount("MUTE")
                    ));
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("API error: " + e.getMessage());
                sendError(exchange, 500, "Internal server error");
            }
        }
    }

    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 200, Map.of("status", "ok", "plugin", "MCReportPlugin"));
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        }
    }
}