package com.mcreport.update;

import com.mcreport.MCReportPlugin;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AutoUpdater {

    public static final String UPDATE_JAR = "MCReportPlugin-new.jar";
    public static final String PLUGIN_JAR = "MCReportPlugin.jar";
    private static final int TIMEOUT = 15000;

    private final MCReportPlugin plugin;
    private volatile boolean updateAvailable;
    private volatile String latestVersion = "";

    public AutoUpdater(MCReportPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public File getPendingFile() {
        return new File(plugin.getDataFolder().getParentFile(), UPDATE_JAR);
    }

    public boolean hasPendingUpdate() {
        return getPendingFile().isFile();
    }

    public String getPendingSha256() {
        File pending = getPendingFile();
        if (!pending.isFile()) {
            return "";
        }
        try {
            return sha256(pending);
        } catch (IOException e) {
            plugin.getLogger().warning("[Updater] No se pudo calcular el SHA-256 del JAR pendiente: " +
                    e.getMessage());
            return "";
        }
    }

    public void remindPending() {
        if (hasPendingUpdate()) {
            plugin.getLogger().info("[Updater] Hay un " + UPDATE_JAR + " pendiente por aplicar. " +
                    "Detén el servidor desde el panel, sustituye plugins/" + PLUGIN_JAR +
                    " por plugins/" + UPDATE_JAR + " y vuelve a iniciarlo.");
        }
    }

    public void checkForUpdates() {
        if (!plugin.getConfig().getBoolean("update.enabled", false)) return;

        String versionUrl = plugin.getConfig().getString("update.version-url", "");
        String jarUrl = plugin.getConfig().getString("update.jar-url", "");
        if (versionUrl.isEmpty() || jarUrl.isEmpty()) {
            plugin.getLogger().warning("[Updater] Falta update.version-url o update.jar-url en config.yml.");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> checkAsync(versionUrl, jarUrl));
    }

    private void checkAsync(String versionUrl, String jarUrl) {
        String current = plugin.getDescription().getVersion();
        try {
            String remote = fetchText(versionUrl).trim();
            if (remote.isEmpty()) {
                plugin.getLogger().warning("[Updater] La URL de versión no devolvió ningún dato.");
                return;
            }
            latestVersion = remote.replaceFirst("^[vV]", "");
            updateAvailable = compareVersions(latestVersion, current) > 0;

            if (!updateAvailable) {
                plugin.getLogger().info("[Updater] Estás en la última versión (" + current + "). No hay actualizaciones.");
                return;
            }

            plugin.getLogger().info("[Updater] Nueva versión disponible: " + latestVersion +
                    " (actual: " + current + "). Descargando...");

            File pluginsDir = plugin.getDataFolder().getParentFile();
            File pending = new File(pluginsDir, UPDATE_JAR);
            File tmp = new File(pluginsDir, "." + UPDATE_JAR + ".tmp");

            downloadJar(jarUrl, tmp);
            Files.move(tmp.toPath(), pending.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("[Updater] JAR descargado de forma segura en plugins/" + UPDATE_JAR +
                    " (" + pending.length() + " bytes, SHA-256: " + getPendingSha256() + "). " +
                    "No se sustituye el JAR cargado. Detén el servidor y aplica el cambio desde el panel.");
        } catch (Exception e) {
            plugin.getLogger().warning("[Updater] Error al comprobar la actualización: " + e.getMessage());
        }
    }

    private String fetchText(String urlStr) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setRequestProperty("User-Agent", "MCReportPlugin-Updater");
        conn.setInstanceFollowRedirects(true);
        try (InputStream in = conn.getInputStream()) {
            if (conn.getResponseCode() >= 400) {
                throw new IOException("HTTP " + conn.getResponseCode());
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            conn.disconnect();
        }
    }

    private void downloadJar(String urlStr, File dest) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setRequestProperty("User-Agent", "MCReportPlugin-Updater");
        conn.setInstanceFollowRedirects(true);
        try (InputStream in = conn.getInputStream()) {
            if (conn.getResponseCode() >= 400) {
                throw new IOException("HTTP " + conn.getResponseCode());
            }
            Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            conn.disconnect();
        }
    }

    private String sha256(File file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(file.toPath())) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            StringBuilder result = new StringBuilder();
            for (byte value : digest.digest()) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 no disponible", e);
        }
    }

    public static int compareVersions(String a, String b) {
        String[] pa = a.replaceAll("[^0-9.]", "").split("\\.");
        String[] pb = b.replaceAll("[^0-9.]", "").split("\\.");
        int len = Math.max(pa.length, pb.length);
        for (int i = 0; i < len; i++) {
            int an = (i < pa.length && !pa[i].isEmpty()) ? Integer.parseInt(pa[i]) : 0;
            int bn = (i < pb.length && !pb[i].isEmpty()) ? Integer.parseInt(pb[i]) : 0;
            if (an != bn) return Integer.compare(an, bn);
        }
        return 0;
    }
}