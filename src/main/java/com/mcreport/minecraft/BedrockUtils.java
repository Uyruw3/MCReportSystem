package com.mcreport.minecraft;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilidades de compatibilidad con Bedrock (Geyser/Floodgate).
 *
 * Sirven para detectar de forma fiable a los jugadores de Bedrock que
 * entran por Geyser/Floodgate, tanto por nombre (prefijo "." o "*")
 * como por UUID (los UUID de Floodgate empiezan por 00000000-0000-0000-0000-).
 */
public final class BedrockUtils {

    public static final String FLOODGATE_UUID_PREFIX = "00000000-0000-0000-0000-";
    public static final String LEGACY_PREFIX = "*";

    private static final ConcurrentHashMap<String, Boolean> PLUGIN_PRESENCE = new ConcurrentHashMap<>();

    private BedrockUtils() {
    }

    public static boolean isGeyserInstalled() {
        return getPluginPresence("Geyser-Spigot") || getPluginPresence("Geyser");
    }

    public static boolean isFloodgateInstalled() {
        return getPluginPresence("floodgate");
    }

    private static boolean getPluginPresence(String name) {
        return PLUGIN_PRESENCE.computeIfAbsent(name,
                k -> Bukkit.getPluginManager().getPlugin(k) != null);
    }

    /**
     * Los nombres de Java solo admiten letras, números y "_", así que
     * cualquier nombre que empiece por "." o "*" es de Bedrock con total
     * seguridad (Geyser usa "." por defecto).
     */
    public static boolean hasBedrockPrefix(String playerName) {
        if (playerName == null || playerName.isEmpty()) return false;
        char first = playerName.charAt(0);
        return first == '.' || first == LEGACY_PREFIX.charAt(0);
    }

    /**
     * Quita el prefijo de Bedrock para mostrar el "nombre limpio"
     * (sin el prefijo "." / "*") en los embeds de Discord.
     */
    public static String stripPrefix(String playerName) {
        if (!hasBedrockPrefix(playerName)) return playerName;
        return playerName.substring(1);
    }

    public static boolean isBedrockPlayer(Player player) {
        if (player == null) return false;
        UUID uuid = player.getUniqueId();
        return hasBedrockPrefix(player.getName())
                || isFloodgateUuid(uuid)
                || isFloodgatePlayer(uuid)
                || isGeyserPlayer(uuid);
    }

    public static boolean isBedrockPlayer(String playerName) {
        if (playerName == null) return false;
        if (hasBedrockPrefix(playerName)) return true;

        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null) {
            return isBedrockPlayer(player);
        }
        return false;
    }

    /**
     * Los UUID generados por Floodgate tienen esta forma exacta:
     * 00000000-0000-0000-0000-<XUID en hex>. Es la detección más rápida
     * y no depende de que las API estén cargadas.
     */
    public static boolean isFloodgateUuid(UUID uuid) {
        return uuid != null && uuid.toString().startsWith(FLOODGATE_UUID_PREFIX);
    }

    public static boolean isFloodgatePlayer(UUID uuid) {
        if (uuid == null) return false;
        if (isFloodgateUuid(uuid)) return true;
        if (!isFloodgateInstalled()) return false;

        return safeApiCall("org.geysermc.floodgate.api.FloodgateApi",
                "isFloodgatePlayer", UUID.class, uuid);
    }

    public static boolean isGeyserPlayer(UUID uuid) {
        if (uuid == null) return false;
        if (!isGeyserInstalled()) return false;

        try {
            Class<?> geyserApiClass = Class.forName("com.geysermc.geyser.api.GeyserApi");
            Method apiMethod = geyserApiClass.getMethod("api");
            Object api = apiMethod.invoke(null);
            Method connectionByUuid = api.getClass().getMethod("connectionByUuid", UUID.class);
            Object connection = connectionByUuid.invoke(api, uuid);
            return connection != null;
        } catch (Exception e) {
            return isFloodgateUuid(uuid);
        }
    }

    /**
     * Si hay un jugador de Bedrock online cuyo prefijo coincida con el
     * configurado, devolvemos su nombre exacto (con prefijo). Sirve para
     * resolver reportes donde el reportero escribe el nombre sin prefijo.
     */
    public static String resolveBedrockName(String plainName) {
        if (plainName == null) return null;
        String cleaned = stripPrefix(plainName);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isBedrockPlayer(player)) continue;
            String name = player.getName();
            if (stripPrefix(name).equalsIgnoreCase(cleaned)) {
                return name;
            }
        }
        return null;
    }

    /**
     * UUID "de servidor" correcto para baneos/kicks de jugadores bedrock.
     * Si el jugador está online, devuelve su UUID real (el de Floodgate),
     * que es el que Geyser registra en el servidor.
     */
    public static UUID getServerUuid(String playerName) {
        Player online = Bukkit.getPlayerExact(playerName);
        if (online != null) return online.getUniqueId();

        String resolved = resolveBedrockName(playerName);
        if (resolved != null) {
            Player player = Bukkit.getPlayerExact(resolved);
            if (player != null) return player.getUniqueId();
        }
        return null;
    }

    private static boolean safeApiCall(String className, String methodName, Class<?> paramType, Object arg) {
        try {
            Class<?> apiClass = Class.forName(className);
            Method getInstance = apiClass.getMethod("getInstance");
            Object api = getInstance.invoke(null);
            Method method = apiClass.getMethod(methodName, paramType);
            return Boolean.TRUE.equals(method.invoke(api, arg));
        } catch (Exception e) {
            return false;
        }
    }

    public static void refresh() {
        PLUGIN_PRESENCE.clear();
    }
}