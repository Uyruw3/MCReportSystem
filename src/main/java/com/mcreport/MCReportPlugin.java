package com.mcreport;

import com.mcreport.discord.DiscordBot;
import com.mcreport.minecraft.BanManager;
import com.mcreport.minecraft.ChatLogger;
import com.mcreport.storage.ReportStorage;
import com.mcreport.storage.SqliteStorage;
import com.mcreport.storage.Storage;
import com.mcreport.update.AutoUpdater;
import com.mcreport.web.WebServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MCReportPlugin extends JavaPlugin {

    private static MCReportPlugin instance;
    private DiscordBot discordBot;
    private BanManager banManager;
    private ChatLogger chatLogger;
    private Storage storage;
    private WebServer webServer;
    private AutoUpdater autoUpdater;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        String storageType = getConfig().getString("storage.type", "yaml").toLowerCase();
        if ("sqlite".equals(storageType)) {
            this.storage = new SqliteStorage(this);
        } else {
            this.storage = new ReportStorage(this);
        }
        storage.init(this);

        this.banManager = new BanManager(this);
        this.chatLogger = new ChatLogger(this);
        this.autoUpdater = new AutoUpdater(this);

        getServer().getPluginManager().registerEvents(chatLogger, this);

        getCommand("mcreport").setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("mcreport.admin")) {
                sender.sendMessage("§cNo tienes permisos para usar este comando.");
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                storage.reload();
                sender.sendMessage("§aMCReportPlugin recargado correctamente.");
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("webreload")) {
                restartWebServer();
                sender.sendMessage("§aServidor web reiniciado.");
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("update")) {
                autoUpdater.checkForUpdates();
                sender.sendMessage("§aBuscando actualizaciones... revisa la consola.");
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("updatestatus")) {
                File pending = new File(getDataFolder().getParentFile(), AutoUpdater.UPDATE_JAR);
                if (autoUpdater.isUpdateAvailable()) {
                    sender.sendMessage("§eHay una actualización disponible:" + autoUpdater.getLatestVersion());
                } else {
                    sender.sendMessage("§aEl plugin está actualizado.");
                }
                if (pending.exists()) {
                    sender.sendMessage("§eHay un " + AutoUpdater.UPDATE_JAR + " por aplicar. Detén el servidor y ejecuta aplicar-update.bat.");
                }
                return true;
            }
            sender.sendMessage("§eMCReportPlugin v" + getDescription().getVersion() + " está activo.");
            return true;
        });

        String token = getConfig().getString("discord.token", "");
        if (token.isEmpty() || token.equals("TU_TOKEN_AQUI")) {
            getLogger().severe("No se ha configurado el token de Discord! Desactivando plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        validateConfig();
        startWebServer();

        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            discordBot = new DiscordBot(this, token);
            discordBot.start();
            autoUpdater.checkForUpdates();
        });

        autoUpdater.remindPending();

        getLogger().info("MCReportPlugin v" + getDescription().getVersion() + " activado correctamente! (Storage: " + storageType + ")");
    }

    private void startWebServer() {
        if (!getConfig().getBoolean("web.enabled", false)) return;
        int port = getConfig().getInt("web.port", 8123);
        webServer = new WebServer(this, storage);
        webServer.start(port);
    }

    private void restartWebServer() {
        if (webServer != null) {
            webServer.stop();
            webServer = null;
        }
        startWebServer();
    }

    private void validateConfig() {
        String[] required = {
            "discord.token",
            "discord.guild-id",
            "discord.report-embed-channel",
            "discord.report-ticket-category",
            "discord.staff-roles"
        };
        for (String key : required) {
            if (getConfig().getString(key) == null && getConfig().getList(key) == null) {
                getLogger().warning("Configuración faltante: " + key);
            }
        }
        if (getConfig().getLongList("discord.staff-roles").isEmpty()) {
            getLogger().warning("No hay roles de staff configurados en discord.staff-roles");
        }
    }

    @Override
    public void onDisable() {
        if (discordBot != null) {
            discordBot.shutdown();
        }
        if (webServer != null) {
            webServer.stop();
            webServer = null;
        }
        if (storage != null) {
            storage.close();
        }
        getLogger().info("MCReportPlugin desactivado.");
    }

    public static MCReportPlugin getInstance() {
        return instance;
    }

    public DiscordBot getDiscordBot() {
        return discordBot;
    }

    public BanManager getBanManager() {
        return banManager;
    }

    public ChatLogger getChatLogger() {
        return chatLogger;
    }

    public AutoUpdater getAutoUpdater() {
        return autoUpdater;
    }

    public Storage getStorage() {
        return storage;
    }
}
