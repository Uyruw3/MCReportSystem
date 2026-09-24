package com.mcreport.storage;

import com.mcreport.MCReportPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SqliteStorage implements Storage {

    private final MCReportPlugin plugin;
    private HikariDataSource dataSource;

    public SqliteStorage(MCReportPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init(MCReportPlugin plugin) {
        String dbPath = plugin.getDataFolder().getAbsolutePath() + "/mcreport.db";
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + dbPath);
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(5000);
        config.setIdleTimeout(300000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(config);

        initializeSchema();
        plugin.getLogger().info("SQLite storage initialized at " + dbPath);
    }

    private void initializeSchema() {
        String[] statements = {
            "CREATE TABLE IF NOT EXISTS reports (" +
            "  id TEXT PRIMARY KEY," +
            "  reporter_tag TEXT NOT NULL," +
            "  reporter_id TEXT NOT NULL," +
            "  reported_player TEXT NOT NULL," +
            "  reason TEXT NOT NULL," +
            "  description TEXT," +
            "  evidence TEXT," +
            "  channel_id TEXT NOT NULL," +
            "  status TEXT NOT NULL DEFAULT 'pending'," +
            "  action_taken TEXT DEFAULT 'PENDIENTE'," +
            "  created_at INTEGER NOT NULL," +
            "  resolved_at INTEGER" +
            ");",
            "CREATE INDEX IF NOT EXISTS idx_reports_reported ON reports(reported_player);",
            "CREATE INDEX IF NOT EXISTS idx_reports_status ON reports(status);",
            "CREATE INDEX IF NOT EXISTS idx_reports_reporter ON reports(reporter_id);",

            "CREATE TABLE IF NOT EXISTS warns (" +
            "  player_name TEXT PRIMARY KEY," +
            "  count INTEGER NOT NULL DEFAULT 0," +
            "  last_warn INTEGER" +
            ");",

            "CREATE TABLE IF NOT EXISTS mutes (" +
            "  uuid TEXT PRIMARY KEY," +
            "  expiry_millis INTEGER NOT NULL" +
            ");",

            "CREATE TABLE IF NOT EXISTS appeals (" +
            "  id TEXT PRIMARY KEY," +
            "  appellant_tag TEXT NOT NULL," +
            "  appellant_id TEXT NOT NULL," +
            "  player_name TEXT NOT NULL," +
            "  reason TEXT NOT NULL," +
            "  evidence TEXT," +
            "  channel_id TEXT NOT NULL," +
            "  status TEXT NOT NULL DEFAULT 'pending'," +
            "  decision TEXT DEFAULT 'PENDIENTE'," +
            "  moderator_tag TEXT," +
            "  note TEXT," +
            "  created_at INTEGER NOT NULL," +
            "  resolved_at INTEGER" +
            ");",
            "CREATE INDEX IF NOT EXISTS idx_appeals_player ON appeals(player_name);",
            "CREATE INDEX IF NOT EXISTS idx_appeals_status ON appeals(status);"
        };

        try (Connection conn = dataSource.getConnection()) {
            for (String sql : statements) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.execute();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error initializing SQLite schema: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload() {
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("SQLite storage closed");
        }
    }

    @Override
    public void createReport(String reportId, String reporterTag, String reporterId,
                             String reportedPlayer, String reason, String description,
                             String evidence, String channelId) {
        String sql = "INSERT INTO reports (id, reporter_tag, reporter_id, reported_player, reason, description, evidence, channel_id, status, action_taken, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'pending', 'PENDIENTE', ?)";
        executeUpdate(sql, reportId, reporterTag, reporterId, reportedPlayer, reason,
                     description != null ? description : "", evidence != null ? evidence : "",
                     channelId, System.currentTimeMillis());
    }

    @Override
    public void resolveReport(String reportId) {
        String sql = "UPDATE reports SET status = 'resolved', resolved_at = ? WHERE id = ?";
        executeUpdate(sql, System.currentTimeMillis(), reportId);
    }

    @Override
    public void setActionTaken(String reportId, String action) {
        String sql = "UPDATE reports SET action_taken = ? WHERE id = ?";
        executeUpdate(sql, action, reportId);
    }

    @Override
    public String getReportActionTaken(String reportId) {
        String sql = "SELECT action_taken FROM reports WHERE id = ?";
        return querySingle(sql, rs -> rs.getString("action_taken"), reportId);
    }

    @Override
    public int getPlayerWarns(String playerName) {
        String sql = "SELECT count FROM warns WHERE player_name = ?";
        return querySingle(sql, rs -> rs.getInt("count"), playerName.toLowerCase());
    }

    @Override
    public void setPlayerWarns(String playerName, int warns) {
        String sql = "INSERT INTO warns (player_name, count, last_warn) VALUES (?, ?, ?) " +
                     "ON CONFLICT(player_name) DO UPDATE SET count = ?, last_warn = ?";
        long now = System.currentTimeMillis();
        executeUpdate(sql, playerName.toLowerCase(), warns, now, warns, now);
    }

    @Override
    public Map<String, Long> getMutes() {
        Map<String, Long> mutes = new HashMap<>();
        String sql = "SELECT uuid, expiry_millis FROM mutes";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                mutes.put(rs.getString("uuid"), rs.getLong("expiry_millis"));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error loading mutes: " + e.getMessage());
        }
        return mutes;
    }

    @Override
    public void saveMute(String uuid, long expiryMillis) {
        String sql = "INSERT INTO mutes (uuid, expiry_millis) VALUES (?, ?) " +
                     "ON CONFLICT(uuid) DO UPDATE SET expiry_millis = ?";
        executeUpdate(sql, uuid, expiryMillis, expiryMillis);
    }

    @Override
    public void removeMute(String uuid) {
        String sql = "DELETE FROM mutes WHERE uuid = ?";
        executeUpdate(sql, uuid);
    }

    @Override
    public String getReportReporterId(String channelId) {
        String sql = "SELECT reporter_id FROM reports WHERE channel_id = ?";
        return querySingle(sql, rs -> rs.getString("reporter_id"), channelId);
    }

    @Override
    public Map<String, Object> getReport(String reportId) {
        Map<String, Object> report = new HashMap<>();
        String sql = "SELECT * FROM reports WHERE id = ?";
        querySingle(sql, rs -> {
            report.put("reporter-tag", rs.getString("reporter_tag"));
            report.put("reporter-id", rs.getString("reporter_id"));
            report.put("reported-player", rs.getString("reported_player"));
            report.put("reason", rs.getString("reason"));
            report.put("description", rs.getString("description"));
            report.put("evidence", rs.getString("evidence"));
            report.put("channel-id", rs.getString("channel_id"));
            report.put("status", rs.getString("status"));
            report.put("action-taken", rs.getString("action_taken"));
            return report;
        }, reportId);
        return report;
    }

    @Override
    public List<String> getPendingReportsList() {
        List<String> reports = new ArrayList<>();
        String sql = "SELECT id, reported_player, reason, created_at FROM reports WHERE status = 'pending' ORDER BY created_at DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String key = rs.getString("id");
                String player = rs.getString("reported_player");
                String reason = rs.getString("reason");
                String created = formatTimestamp(rs.getLong("created_at"));
                reports.add(key + "|" + player + "|" + reason + "|" + created);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error loading pending reports: " + e.getMessage());
        }
        return reports;
    }

    @Override
    public int getTotalReports() {
        return querySingle("SELECT COUNT(*) as cnt FROM reports", rs -> rs.getInt("cnt"));
    }

    @Override
    public int getPendingReports() {
        return querySingle("SELECT COUNT(*) as cnt FROM reports WHERE status = 'pending'", rs -> rs.getInt("cnt"));
    }

    @Override
    public int getResolvedReports() {
        return querySingle("SELECT COUNT(*) as cnt FROM reports WHERE status = 'resolved'", rs -> rs.getInt("cnt"));
    }

    @Override
    public int getActionCount(String action) {
        String sql = "SELECT COUNT(*) as cnt FROM reports WHERE action_taken = ?";
        return querySingle(sql, rs -> rs.getInt("cnt"), action);
    }

    @Override
    public List<String> getPlayerReports(String playerName) {
        List<String> reports = new ArrayList<>();
        String sql = "SELECT id FROM reports WHERE reported_player = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(rs.getString("id"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error loading player reports: " + e.getMessage());
        }
        return reports;
    }

    @Override
    public void createAppeal(String appealId, String appellantTag, String appellantId,
                             String playerName, String reason, String evidence, String channelId) {
        String sql = "INSERT INTO appeals (id, appellant_tag, appellant_id, player_name, reason, evidence, channel_id, status, decision, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 'pending', 'PENDIENTE', ?)";
        executeUpdate(sql, appealId, appellantTag, appellantId, playerName,
                     reason, evidence != null ? evidence : "", channelId, System.currentTimeMillis());
    }

    @Override
    public boolean hasPendingAppeal(String appellantId, String playerName) {
        String sql = "SELECT 1 FROM appeals WHERE appellant_id = ? AND player_name = ? AND status = 'pending' LIMIT 1";
        return querySingle(sql, rs -> true, appellantId, playerName) != null;
    }

    @Override
    public void setAppealDecision(String appealId, String decision, String moderatorTag, String note) {
        String sql = "UPDATE appeals SET status = 'resolved', decision = ?, moderator_tag = ?, note = ?, resolved_at = ? WHERE id = ?";
        executeUpdate(sql, decision, moderatorTag, note != null ? note : "", System.currentTimeMillis(), appealId);
    }

    @Override
    public String getAppealPlayer(String appealId) {
        String sql = "SELECT player_name FROM appeals WHERE id = ?";
        return querySingle(sql, rs -> rs.getString("player_name"), appealId);
    }

    @Override
    public String getAppealAppellantId(String appealId) {
        String sql = "SELECT appellant_id FROM appeals WHERE id = ?";
        return querySingle(sql, rs -> rs.getString("appellant_id"), appealId);
    }

    @Override
    public List<String> getPendingAppealsList() {
        List<String> appeals = new ArrayList<>();
        String sql = "SELECT id, player_name, created_at FROM appeals WHERE status = 'pending' ORDER BY created_at DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String key = rs.getString("id");
                String player = rs.getString("player_name");
                String created = formatTimestamp(rs.getLong("created_at"));
                appeals.add(key + "|" + player + "|" + created);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error loading pending appeals: " + e.getMessage());
        }
        return appeals;
    }

    @FunctionalInterface
    private interface ResultMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private <T> T querySingle(String sql, ResultMapper<T> mapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapper.map(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("SQL error: " + e.getMessage() + " | " + sql);
        }
        return null;
    }

    private void executeUpdate(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL update error: " + e.getMessage() + " | " + sql);
            throw new RuntimeException(e);
        }
    }

    private String formatTimestamp(long millis) {
        java.time.Instant instant = java.time.Instant.ofEpochMilli(millis);
        java.time.ZonedDateTime zdt = instant.atZone(java.time.ZoneId.systemDefault());
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(zdt);
    }

    public CompletableFuture<Void> asyncExecute(Runnable task) {
        return CompletableFuture.runAsync(task);
    }
}