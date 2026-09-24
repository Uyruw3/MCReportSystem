package com.mcreport.storage;

import com.mcreport.MCReportPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportStorage implements Storage {

    private final MCReportPlugin plugin;
    private File reportsFile;
    private FileConfiguration reportsConfig;
    private File warnsFile;
    private FileConfiguration warnsConfig;
    private File mutesFile;
    private FileConfiguration mutesConfig;
    private File appealsFile;
    private FileConfiguration appealsConfig;

    public ReportStorage(MCReportPlugin plugin) {
        this.plugin = plugin;
        initFiles();
    }

    @Override
    public void init(MCReportPlugin plugin) {
        initFiles();
    }

    @Override
    public void reload() {
        reportsConfig = YamlConfiguration.loadConfiguration(reportsFile);
        warnsConfig = YamlConfiguration.loadConfiguration(warnsFile);
        mutesConfig = YamlConfiguration.loadConfiguration(mutesFile);
        appealsConfig = YamlConfiguration.loadConfiguration(appealsFile);
    }

    @Override
    public void close() {
        saveReports();
        saveWarns();
        saveMutes();
        saveAppeals();
    }

    private void initFiles() {
        reportsFile = new File(plugin.getDataFolder(), "reports.yml");
        if (!reportsFile.exists()) {
            try {
                reportsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear reports.yml: " + e.getMessage());
            }
        }
        reportsConfig = YamlConfiguration.loadConfiguration(reportsFile);

        warnsFile = new File(plugin.getDataFolder(), "warns.yml");
        if (!warnsFile.exists()) {
            try {
                warnsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear warns.yml: " + e.getMessage());
            }
        }
        warnsConfig = YamlConfiguration.loadConfiguration(warnsFile);

        mutesFile = new File(plugin.getDataFolder(), "mutes.yml");
        if (!mutesFile.exists()) {
            try {
                mutesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear mutes.yml: " + e.getMessage());
            }
        }
        mutesConfig = YamlConfiguration.loadConfiguration(mutesFile);

        appealsFile = new File(plugin.getDataFolder(), "appeals.yml");
        if (!appealsFile.exists()) {
            try {
                appealsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear appeals.yml: " + e.getMessage());
            }
        }
        appealsConfig = YamlConfiguration.loadConfiguration(appealsFile);
    }

    public void createReport(String reportId, String reporterTag, String reporterId,
                             String reportedPlayer, String reason, String description,
                             String evidence, String channelId) {
        String path = "reports." + reportId;
        reportsConfig.set(path + ".reporter-tag", reporterTag);
        reportsConfig.set(path + ".reporter-id", reporterId);
        reportsConfig.set(path + ".reported-player", reportedPlayer);
        reportsConfig.set(path + ".reason", reason);
        reportsConfig.set(path + ".description", description);
        reportsConfig.set(path + ".evidence", evidence);
        reportsConfig.set(path + ".channel-id", channelId);
        reportsConfig.set(path + ".status", "pending");
        reportsConfig.set(path + ".action-taken", "PENDIENTE");
        reportsConfig.set(path + ".created-at", System.currentTimeMillis());
        saveReports();
    }

    public void resolveReport(String reportId) {
        if (reportsConfig.getConfigurationSection("reports") == null) return;
        String path = "reports." + reportId;
        reportsConfig.set(path + ".status", "resolved");
        reportsConfig.set(path + ".resolved-at", System.currentTimeMillis());
        saveReports();
    }

    public void setActionTaken(String reportId, String action) {
        String path = "reports." + reportId + ".action-taken";
        reportsConfig.set(path, action);
        saveReports();
    }

    public String getReportActionTaken(String reportId) {
        return reportsConfig.getString("reports." + reportId + ".action-taken", null);
    }

    public int getPlayerWarns(String playerName) {
        return warnsConfig.getInt("warns." + playerName.toLowerCase(), 0);
    }

    public void setPlayerWarns(String playerName, int warns) {
        warnsConfig.set("warns." + playerName.toLowerCase(), warns);
        saveWarns();
    }

    public Map<String, Long> getMutes() {
        Map<String, Long> mutes = new HashMap<>();
        if (mutesConfig.getConfigurationSection("mutes") != null) {
            for (String uuid : mutesConfig.getConfigurationSection("mutes").getKeys(false)) {
                mutes.put(uuid, mutesConfig.getLong("mutes." + uuid));
            }
        }
        return mutes;
    }

    public void saveMute(String uuid, long expiryMillis) {
        mutesConfig.set("mutes." + uuid, expiryMillis);
        saveMutes();
    }

    public void removeMute(String uuid) {
        mutesConfig.set("mutes." + uuid, null);
        saveMutes();
    }

    public String getReportReporterId(String channelId) {
        if (reportsConfig.getConfigurationSection("reports") == null) return null;
        for (String key : reportsConfig.getConfigurationSection("reports").getKeys(false)) {
            String channel = reportsConfig.getString("reports." + key + ".channel-id", "");
            if (channel.equalsIgnoreCase(channelId)) {
                return reportsConfig.getString("reports." + key + ".reporter-id", "");
            }
        }
        return null;
    }

    public Map<String, Object> getReport(String reportId) {
        Map<String, Object> report = new HashMap<>();
        String path = "reports." + reportId;
        if (reportsConfig.contains(path)) {
            report.put("reporter-tag", reportsConfig.getString(path + ".reporter-tag", ""));
            report.put("reporter-id", reportsConfig.getString(path + ".reporter-id", ""));
            report.put("reported-player", reportsConfig.getString(path + ".reported-player", ""));
            report.put("reason", reportsConfig.getString(path + ".reason", ""));
            report.put("description", reportsConfig.getString(path + ".description", ""));
            report.put("evidence", reportsConfig.getString(path + ".evidence", ""));
            report.put("channel-id", reportsConfig.getString(path + ".channel-id", ""));
            report.put("status", reportsConfig.getString(path + ".status", "pending"));
            report.put("action-taken", reportsConfig.getString(path + ".action-taken", "PENDIENTE"));
        }
        return report;
    }

    public List<String> getPendingReportsList() {
        List<String> reports = new ArrayList<>();
        if (reportsConfig.getConfigurationSection("reports") == null) return reports;
        for (String key : reportsConfig.getConfigurationSection("reports").getKeys(false)) {
            if ("pending".equalsIgnoreCase(reportsConfig.getString("reports." + key + ".status"))) {
                String player = reportsConfig.getString("reports." + key + ".reported-player", "");
                String reason = reportsConfig.getString("reports." + key + ".reason", "");
                String created = formatTimestamp(reportsConfig.getLong("reports." + key + ".created-at"));
                reports.add(key + "|" + player + "|" + reason + "|" + created);
            }
        }
        return reports;
    }

    public int getTotalReports() {
        return reportsConfig.getConfigurationSection("reports") != null
                ? reportsConfig.getConfigurationSection("reports").getKeys(false).size() : 0;
    }

    public int getPendingReports() {
        int count = 0;
        if (reportsConfig.getConfigurationSection("reports") != null) {
            for (String key : reportsConfig.getConfigurationSection("reports").getKeys(false)) {
                if ("pending".equals(reportsConfig.getString("reports." + key + ".status"))) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getResolvedReports() {
        int count = 0;
        if (reportsConfig.getConfigurationSection("reports") != null) {
            for (String key : reportsConfig.getConfigurationSection("reports").getKeys(false)) {
                if ("resolved".equals(reportsConfig.getString("reports." + key + ".status"))) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getActionCount(String action) {
        int count = 0;
        if (reportsConfig.getConfigurationSection("reports") != null) {
            for (String key : reportsConfig.getConfigurationSection("reports").getKeys(false)) {
                if (action.equalsIgnoreCase(reportsConfig.getString("reports." + key + ".action-taken"))) {
                    count++;
                }
            }
        }
        return count;
    }

    public List<String> getPlayerReports(String playerName) {
        List<String> reports = new ArrayList<>();
        if (reportsConfig.getConfigurationSection("reports") == null) return reports;
        for (String key : reportsConfig.getConfigurationSection("reports").getKeys(false)) {
            if (playerName.equalsIgnoreCase(reportsConfig.getString("reports." + key + ".reported-player", ""))) {
                reports.add(key);
            }
        }
        return reports;
    }

    private String formatTimestamp(long millis) {
        java.time.Instant instant = java.time.Instant.ofEpochMilli(millis);
        java.time.ZonedDateTime zdt = instant.atZone(java.time.ZoneId.systemDefault());
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(zdt);
    }

    public void createAppeal(String appealId, String appellantTag, String appellantId,
                         String playerName, String reason, String evidence, String channelId) {
        String path = "appeals." + appealId;
        appealsConfig.set(path + ".appellant-tag", appellantTag);
        appealsConfig.set(path + ".appellant-id", appellantId);
        appealsConfig.set(path + ".player-name", playerName);
        appealsConfig.set(path + ".reason", reason);
        appealsConfig.set(path + ".evidence", evidence);
        appealsConfig.set(path + ".channel-id", channelId);
        appealsConfig.set(path + ".status", "pending");
        appealsConfig.set(path + ".decision", "PENDIENTE");
        appealsConfig.set(path + ".created-at", System.currentTimeMillis());
        saveAppeals();
    }

    @Override
    public boolean hasPendingAppeal(String appellantId, String playerName) {
        if (appealsConfig.getConfigurationSection("appeals") == null) return false;
        for (String key : appealsConfig.getConfigurationSection("appeals").getKeys(false)) {
            String path = "appeals." + key;
            if ("pending".equalsIgnoreCase(appealsConfig.getString(path + ".status"))
                    && appellantId.equals(appealsConfig.getString(path + ".appellant-id", ""))
                    && playerName.equalsIgnoreCase(appealsConfig.getString(path + ".player-name", ""))) {
                return true;
            }
        }
        return false;
    }

    public void setAppealDecision(String appealId, String decision, String moderatorTag, String note) {
        String path = "appeals." + appealId;
        appealsConfig.set(path + ".status", "resolved");
        appealsConfig.set(path + ".decision", decision);
        appealsConfig.set(path + ".moderator-tag", moderatorTag);
        appealsConfig.set(path + ".note", note);
        appealsConfig.set(path + ".resolved-at", System.currentTimeMillis());
        saveAppeals();
    }

    public String getAppealPlayer(String appealId) {
        return appealsConfig.getString("appeals." + appealId + ".player-name", "");
    }

    public String getAppealAppellantId(String appealId) {
        return appealsConfig.getString("appeals." + appealId + ".appellant-id", "");
    }

    public List<String> getPendingAppealsList() {
        List<String> appeals = new ArrayList<>();
        if (appealsConfig.getConfigurationSection("appeals") == null) return appeals;
        for (String key : appealsConfig.getConfigurationSection("appeals").getKeys(false)) {
            if ("pending".equalsIgnoreCase(appealsConfig.getString("appeals." + key + ".status"))) {
                String player = appealsConfig.getString("appeals." + key + ".player-name", "");
                String created = formatTimestamp(appealsConfig.getLong("appeals." + key + ".created-at"));
                appeals.add(key + "|" + player + "|" + created);
            }
        }
        return appeals;
    }

    private void saveAppeals() {
        try {
            appealsConfig.save(appealsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar appeals.yml: " + e.getMessage());
        }
    }

    private void saveReports() {
        try {
            reportsConfig.save(reportsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar reports.yml: " + e.getMessage());
        }
    }

    private void saveWarns() {
        try {
            warnsConfig.save(warnsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar warns.yml: " + e.getMessage());
        }
    }

    private void saveMutes() {
        try {
            mutesConfig.save(mutesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar mutes.yml: " + e.getMessage());
        }
    }
}