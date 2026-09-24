package com.mcreport.minecraft;

import com.mcreport.MCReportPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatLogger implements Listener {

    private final MCReportPlugin plugin;
    private final Map<String, LinkedList<String>> chatLogs = new ConcurrentHashMap<>();
    private final int maxMessages;

    public ChatLogger(MCReportPlugin plugin) {
        this.plugin = plugin;
        this.maxMessages = Math.max(1, plugin.getConfig().getInt("evidence.chat-log-size", 50));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (plugin.getBanManager().isPlayerMuted(player.getName())) {
            event.setCancelled(true);
            player.sendMessage("§c🔇 Has sido silenciado. No puedes hablar.");
            return;
        }

        String playerName = player.getName();
        String message = "[" + playerName + "] " + event.getMessage();

        chatLogs.computeIfAbsent(playerName.toLowerCase(), k -> new LinkedList<>());
        LinkedList<String> logs = chatLogs.get(playerName.toLowerCase());
        synchronized (logs) {
            logs.addLast(message);
            while (logs.size() > maxMessages) {
                logs.removeFirst();
            }
        }
    }

    public List<String> getPlayerChatLog(String playerName) {
        LinkedList<String> logs = chatLogs.get(playerName.toLowerCase());
        if (logs == null) return new ArrayList<>();
        synchronized (logs) {
            return new ArrayList<>(logs);
        }
    }

    public String getPlayerChatLogFormatted(String playerName) {
        List<String> logs = getPlayerChatLog(playerName);
        if (logs.isEmpty()) return "No hay mensajes registrados.";

        StringBuilder sb = new StringBuilder();
        for (String msg : logs) {
            sb.append(msg).append("\n");
        }
        return sb.toString();
    }

    public void clearPlayerChatLog(String playerName) {
        chatLogs.remove(playerName.toLowerCase());
    }
}