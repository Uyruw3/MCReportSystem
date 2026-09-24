package com.mcreport.minecraft;

import com.mcreport.MCReportPlugin;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BanManager {

    private final MCReportPlugin plugin;
    private final ConcurrentHashMap<UUID, Long> mutes = new ConcurrentHashMap<>();

    public BanManager(MCReportPlugin plugin) {
        this.plugin = plugin;
        loadMutes();
        MSchedulerTask();
    }

    private void MSchedulerTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            mutes.entrySet().removeIf(entry -> now > entry.getValue());
        }, 20L * 60, 20L * 60);
    }

    private void loadMutes() {
        plugin.getStorage().getMutes().forEach((uuidString, expiryMillis) -> {
            try {
                mutes.put(UUID.fromString(uuidString), expiryMillis);
            } catch (IllegalArgumentException ignored) {
            }
        });
    }

    public void banPlayer(String playerName, String reason) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            OfflinePlayer player = resolveOfflinePlayer(playerName);
            if (player != null) {
                ProfileBanList banList = (ProfileBanList) Bukkit.getBanList(BanList.Type.PROFILE);
                banList.addBan(player.getPlayerProfile(), reason, (Date) null, "MCReportBot");
                plugin.getLogger().info("[MCReport] Jugador " + playerName + " baneado. Razón: " + reason);
            }
        });
    }

    public void tempBanPlayer(String playerName, int hours, String reason) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            OfflinePlayer player = resolveOfflinePlayer(playerName);
            if (player != null) {
                ProfileBanList banList = (ProfileBanList) Bukkit.getBanList(BanList.Type.PROFILE);
                Instant expiry = Instant.now().plus(Duration.ofHours(hours));
                banList.addBan(player.getPlayerProfile(), reason, Date.from(expiry), "MCReportBot");
                plugin.getLogger().info("[MCReport] Jugador " + playerName + " baneado por " + hours + " horas. Razón: " + reason);
            }
        });
    }

    public void unbanPlayer(String playerName) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            OfflinePlayer player = resolveOfflinePlayer(playerName);
            if (player != null) {
                ProfileBanList banList = (ProfileBanList) Bukkit.getBanList(BanList.Type.PROFILE);
                banList.pardon(player.getPlayerProfile());
                plugin.getLogger().info("[MCReport] Jugador " + playerName + " ha sido desbaneado.");
            }
        });
    }

    public void kickPlayer(String playerName, String reason) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player == null) {
                String resolved = BedrockUtils.resolveBedrockName(playerName);
                if (resolved != null) {
                    player = Bukkit.getPlayerExact(resolved);
                }
            }
            if (player != null && player.isOnline()) {
                player.kickPlayer(plugin.getLocalization().message("messages.player-kicked")
                        + "\n\n§7" + plugin.getLocalization().message("chat.reason") + ": " + reason);
                plugin.getLogger().info("[MCReport] Jugador " + playerName + " expulsado. Razón: " + reason);
            }
        });
    }

    public void mutePlayer(String playerName, int minutes) {
        OfflinePlayer player = resolveOfflinePlayer(playerName);
        if (player == null) return;

        long expiry = System.currentTimeMillis() + Duration.ofMinutes(minutes).toMillis();
        mutes.put(player.getUniqueId(), expiry);
        plugin.getStorage().saveMute(player.getUniqueId().toString(), expiry);

        Player online = findOnlinePlayer(playerName);
        if (online != null && online.isOnline()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                online.sendMessage(plugin.getLocalization().message("chat.muted-for",
                        "{minutes}", minutes));
            });
        }
        plugin.getLogger().info("[MCReport] Jugador " + playerName + " silenciado por " + minutes + " minutos.");
    }

    public void unmutePlayer(String playerName) {
        OfflinePlayer player = resolveOfflinePlayer(playerName);
        if (player == null) return;

        mutes.remove(player.getUniqueId());
        plugin.getStorage().removeMute(player.getUniqueId().toString());
        plugin.getLogger().info("[MCReport] Jugador " + playerName + " ha sido desilenciado.");
    }

    public boolean isPlayerMuted(String playerName) {
        OfflinePlayer player = resolveOfflinePlayer(playerName);
        if (player == null) return false;
        Long expiry = mutes.get(player.getUniqueId());
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            mutes.remove(player.getUniqueId());
            plugin.getStorage().removeMute(player.getUniqueId().toString());
            return false;
        }
        return true;
    }

    public boolean isPlayerBanned(String playerName) {
        OfflinePlayer player = resolveOfflinePlayer(playerName);
        if (player != null) {
            ProfileBanList banList = (ProfileBanList) Bukkit.getBanList(BanList.Type.PROFILE);
            return banList.isBanned(player.getPlayerProfile());
        }
        return false;
    }

    public boolean isPlayerOnline(String playerName) {
        return findOnlinePlayer(playerName) != null;
    }

    public String getPlayerUuidString(String playerName) {
        OfflinePlayer player = resolveOfflinePlayer(playerName);
        return player != null ? player.getUniqueId().toString() : null;
    }

    private OfflinePlayer resolveOfflinePlayer(String playerName) {
        Player online = findOnlinePlayer(playerName);
        if (online != null) return online;

        String resolved = BedrockUtils.resolveBedrockName(playerName);
        if (resolved != null) {
            Player bedrockOnline = Bukkit.getPlayerExact(resolved);
            if (bedrockOnline != null) return bedrockOnline;
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
        if (offline != null) return offline;

        String exact = BedrockUtils.stripPrefix(playerName);
        if (!exact.equals(playerName)) {
            return Bukkit.getOfflinePlayer(exact);
        }
        return null;
    }

    private Player findOnlinePlayer(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null) return player;
        String resolved = BedrockUtils.resolveBedrockName(playerName);
        if (resolved != null) {
            return Bukkit.getPlayerExact(resolved);
        }
        return null;
    }
}