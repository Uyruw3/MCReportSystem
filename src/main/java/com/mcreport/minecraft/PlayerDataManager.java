package com.mcreport.minecraft;

import com.mcreport.MCReportPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class PlayerDataManager {

    private final MCReportPlugin plugin;

    public PlayerDataManager(MCReportPlugin plugin) {
        this.plugin = plugin;
    }

    public Map<String, Object> capturePlayerData(String playerName) {
        Map<String, Object> data = new HashMap<>();
        Player player = Bukkit.getPlayerExact(playerName);

        if (player == null || !player.isOnline()) {
            data.put("online", false);
            return data;
        }

        data.put("online", true);
        data.put("name", player.getName());
        data.put("uuid", player.getUniqueId().toString());
        data.put("health", player.getHealth());
        data.put("foodLevel", player.getFoodLevel());
        data.put("gamemode", player.getGameMode().name());
        data.put("level", player.getLevel());
        data.put("exp", player.getExp());

        Location loc = player.getLocation();
        data.put("world", loc.getWorld().getName());
        data.put("x", loc.getBlockX());
        data.put("y", loc.getBlockY());
        data.put("z", loc.getBlockZ());
        data.put("yaw", loc.getYaw());
        data.put("pitch", loc.getPitch());

        if (plugin.getConfig().getBoolean("evidence.save-inventory", true)) {
            data.put("inventory", captureInventory(player.getInventory()));
            data.put("armor", captureArmor(player.getInventory()));
            data.put("enderchest", captureEnderChest(player));
        }

        data.put("ip", player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown");

        return data;
    }

    private List<Map<String, Object>> captureInventory(PlayerInventory inventory) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (ItemStack item : inventory.getContents()) {
            items.add(captureItem(item));
        }
        return items;
    }

    private List<Map<String, Object>> captureArmor(PlayerInventory inventory) {
        List<Map<String, Object>> armor = new ArrayList<>();
        for (ItemStack item : inventory.getArmorContents()) {
            armor.add(captureItem(item));
        }
        return armor;
    }

    private List<Map<String, Object>> captureEnderChest(Player player) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (player.getEnderChest() != null) {
            for (ItemStack item : player.getEnderChest().getContents()) {
                items.add(captureItem(item));
            }
        }
        return items;
    }

    private Map<String, Object> captureItem(ItemStack item) {
        Map<String, Object> itemData = new HashMap<>();
        if (item == null || item.getType() == Material.AIR) {
            itemData.put("type", "AIR");
            return itemData;
        }
        itemData.put("type", item.getType().name());
        itemData.put("amount", item.getAmount());
        itemData.put("durability", item.getDurability());
        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName()) {
                itemData.put("name", item.getItemMeta().getDisplayName());
            }
            if (item.getItemMeta().hasLore()) {
                itemData.put("lore", item.getItemMeta().getLore());
            }
        }
        return itemData;
    }

    public String formatPlayerData(Map<String, Object> data) {
        if (!(Boolean) data.getOrDefault("online", false)) {
            return "El jugador no está en línea.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("**Jugador:** ").append(data.get("name")).append("\n");
        sb.append("**UUID:** ").append(data.get("uuid")).append("\n");
        sb.append("**Salud:** ").append(String.format("%.1f", data.get("health"))).append("/20\n");
        sb.append("**Hambre:** ").append(data.get("foodLevel")).append("/20\n");
        sb.append("**Modo de juego:** ").append(data.get("gamemode")).append("\n");
        sb.append("**Nivel:** ").append(data.get("level")).append("\n");
        sb.append("**Mundo:** ").append(data.get("world")).append("\n");
        sb.append("**Ubicación:** (").append(data.get("x")).append(", ").append(data.get("y")).append(", ").append(data.get("z")).append(")\n");

        return sb.toString();
    }

    public String formatEvidence(String playerName) {
        Map<String, Object> data = capturePlayerData(playerName);
        if (!(Boolean) data.getOrDefault("online", false)) {
            return "📌 **Evidencia del servidor:** el jugador no estaba conectado.";
        }

        StringJoiner evidence = new StringJoiner("\n");
        evidence.add("📌 **Evidencia del servidor**");
        evidence.add("Jugador: `" + data.get("name") + "`");
        if (plugin.getConfig().getBoolean("evidence.save-location", true)) {
            evidence.add("Ubicación: `" + data.get("world") + " " + data.get("x") + ", "
                    + data.get("y") + ", " + data.get("z") + "`");
        }
        if (plugin.getConfig().getBoolean("evidence.save-inventory", true)) {
            evidence.add("Inventario capturado: `" + ((List<?>) data.get("inventory")).size() + " objetos`");
        }
        if (plugin.getConfig().getBoolean("evidence.save-chat-log", true)) {
            String chat = plugin.getChatLogger().getPlayerChatLogFormatted(playerName);
            evidence.add("Chat reciente:\n" + truncate(chat, 500));
        }
        return truncate(evidence.toString(), 900);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 1) + "…";
    }
}
