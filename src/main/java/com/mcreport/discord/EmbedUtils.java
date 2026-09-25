package com.mcreport.discord;

import com.mcreport.MCReportPlugin;
import com.mcreport.minecraft.BedrockUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.Color;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EmbedUtils {

    private static final Color COLOR_PRIMARY = new Color(59, 130, 246);
    private static final Color COLOR_SUCCESS = new Color(34, 197, 94);
    private static final Color COLOR_WARNING = new Color(245, 158, 11);
    private static final Color COLOR_DANGER = new Color(239, 68, 68);
    private static final Color COLOR_INFO = new Color(139, 92, 246);
    private static final Color COLOR_MUTED = new Color(107, 114, 128);

    private static final String FOOTER_TEXT = "MCReport • Sistema de Moderación";
    private static final String SKIN_URL = "https://mc-heads.net/avatar/%s/128";
    private static final String SKIN_URL_FULL = "https://mc-heads.net/body/%s/128";
    private static final String SKIN_URL_BEDROCK = "https://mc-heads.net/avatar/steve/128";
    private static final String SKIN_URL_FULL_BEDROCK = "https://mc-heads.net/body/steve/128";

    private static String skin(String playerName) {
        return BedrockUtils.isBedrockPlayer(playerName) ? SKIN_URL_BEDROCK : String.format(SKIN_URL, playerName);
    }

    private static String skinFull(String playerName) {
        return BedrockUtils.isBedrockPlayer(playerName) ? SKIN_URL_FULL_BEDROCK : String.format(SKIN_URL_FULL, playerName);
    }

    private static void addEditionField(EmbedBuilder embed, String playerName) {
        if (BedrockUtils.isBedrockPlayer(playerName)) {
            embed.addField("🎮 **Edición**", "🟢 **Bedrock**", true);
        }
    }

    public static MessageEmbed createReportButtonEmbed(MCReportPlugin plugin) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("📋 Sistema de Reportes", null, null);
        embed.setDescription(
                "**¿Has visto a un jugador rompiendo las reglas?**\n" +
                "Utiliza el botón de abajo para reportarlo de forma rápida y anónima.\n\n" +
                "**Cómo funciona:**\n" +
                "1️⃣ Haz clic en el botón **📋 Reportar**\n" +
                "2️⃣ Completa el formulario con los detalles\n" +
                "3️⃣ El equipo de staff revisará tu reporte\n" +
                "4️⃣ Recibirás una notificación por DM cuando se tome acción\n\n" +
                "**Categorías de reporte:**\n" +
                "🔴 **Cheating/Hacks** - Uso de trampas o modificaciones\n" +
                "🟡 **Toxicidad** - Insultos, acoso o comportamiento ofensivo\n" +
                "⚪ **Spam** - Mensajes repetitivos o publicidad\n" +
                "🟣 **Evadir Ban** - Intentar evitar una sanción\n" +
                "🔵 **Bug Abuse** - Explotar errores del juego\n" +
                "🎨 **Skin Inapropiada** - Skins con contenido ofensivo\n" +
                "⚫ **Otro** - Cualquier otra infracción\n\n" +
                "**⚠️ Importante:** Los reportes falsos serán sancionados."
        );
        embed.setColor(COLOR_PRIMARY);
        embed.setFooter(FOOTER_TEXT + " • Versión 2.0.5");
        embed.setTimestamp(Instant.now());
        return embed.build();
    }

    public static MessageEmbed createReportTicketEmbed(String reporterTag, String reporterId,
                                                        String reportedPlayer, String reason,
                                                        String description, String evidence,
                                                        String reportId) {
        EmbedBuilder embed = new EmbedBuilder();

        String reasonEmoji = getReasonEmoji(reason);
        embed.setAuthor(reasonEmoji + " Nuevo Reporte • #" + reportId.replace("report-", ""), null, skin(reportedPlayer));

        embed.setDescription("Se ha abierto un ticket de reporte. Revisa la información y toma la acción correspondiente.");

        embed.addField("👤 **Jugador Reportado**", "`" + reportedPlayer + "`", true);
        addEditionField(embed, reportedPlayer);
        embed.addField("📋 **Categoría**", reasonEmoji + " " + reason, true);
        embed.addField("📝 **Reportado por**", reporterTag, true);

        if (description != null && !description.isEmpty()) {
            embed.addField("📄 **Descripción del Incidente**", description, false);
        }
        if (evidence != null && !evidence.isEmpty()) {
            embed.addField("🔗 **Pruebas / Evidencia**", evidence, false);
        }

        embed.addField("⏰ **Momento**", "<t:" + (System.currentTimeMillis() / 1000) + ":R>", true);
        embed.addField("🆔 **Reportado ID**", "`" + reporterId + "`", true);

        embed.setColor(COLOR_DANGER);
        embed.setThumbnail(skin(reportedPlayer));
        embed.setImage(skinFull(reportedPlayer));
        embed.setFooter(FOOTER_TEXT + " • Ticket Abierto");
        embed.setTimestamp(Instant.now());

        return embed.build();
    }

    public static MessageEmbed createActionEmbed(String action, String playerName, String moderatorTag, String reason) {
        EmbedBuilder embed = new EmbedBuilder();

        String emoji;
        Color color;
        String title;

        switch (action.toUpperCase()) {
            case "BAN":
                emoji = "🔨";
                color = COLOR_DANGER;
                title = "Jugador Baneado";
                break;
            case "TEMPBAN":
                emoji = "⏰";
                color = COLOR_DANGER;
                title = "Jugador Baneado Temporalmente";
                break;
            case "KICK":
                emoji = "🚪";
                color = COLOR_WARNING;
                title = "Jugador Expulsado";
                break;
            case "WARN":
                emoji = "⚠️";
                color = COLOR_WARNING;
                title = "Advertencia Enviada";
                break;
            case "MUTE":
                emoji = "🔇";
                color = COLOR_MUTED;
                title = "Jugador Silenciado";
                break;
            default:
                emoji = "✅";
                color = COLOR_SUCCESS;
                title = "Acción Completada";
        }

        embed.setAuthor(emoji + " " + title, null, skin(playerName));
        embed.setDescription("**Acción realizada por** " + moderatorTag);

        embed.addField("👤 **Jugador**", "`" + playerName + "`", true);
        addEditionField(embed, playerName);
        embed.addField("📋 **Acción**", action, true);
        embed.addField("📝 **Razón**", reason != null ? reason : "No especificada", true);

        embed.setColor(color);
        embed.setThumbnail(skin(playerName));
        embed.setFooter(FOOTER_TEXT);
        embed.setTimestamp(Instant.now());

        return embed.build();
    }

    public static MessageEmbed createClosedTicketEmbed(String closedBy, String actionTaken) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("✅ Ticket Cerrado", null, null);
        embed.setDescription("Este ticket ha sido cerrado por **" + closedBy + "**.");

        if (actionTaken != null && !actionTaken.isEmpty()) {
            embed.addField("📋 **Acción tomada**", actionTaken, false);
        }

        embed.setColor(COLOR_SUCCESS);
        embed.setFooter(FOOTER_TEXT);
        embed.setTimestamp(Instant.now());
        return embed.build();
    }

    public static MessageEmbed createDMNotificationEmbed(String playerName, String action, String reason, int warns, int maxWarns) {
        EmbedBuilder embed = new EmbedBuilder();

        String emoji;
        Color color;
        String title;

        switch (action.toUpperCase()) {
            case "BAN":
                emoji = "🔨";
                color = COLOR_DANGER;
                title = "Tu reporte ha resultado en un BAN";
                break;
            case "TEMPBAN":
                emoji = "⏰";
                color = COLOR_DANGER;
                title = "Tu reporte ha resultado en un ban temporal";
                break;
            case "KICK":
                emoji = "🚪";
                color = COLOR_WARNING;
                title = "Tu reporte ha resultado en un KICK";
                break;
            case "WARN":
                emoji = "⚠️";
                color = COLOR_WARNING;
                title = "Tu reporte ha resultado en una ADVERTENCIA";
                break;
            case "RESOLVED":
                emoji = "✅";
                color = COLOR_SUCCESS;
                title = "Tu reporte ha sido resuelto";
                break;
            default:
                emoji = "📢";
                color = COLOR_PRIMARY;
                title = "Actualización de Reporte";
        }

        embed.setAuthor(emoji + " " + title, null, skin(playerName));
        embed.setDescription("El equipo de staff ha procesado tu reporte sobre **" + playerName + "**.");

        embed.addField("👤 **Jugador Reportado**", "`" + playerName + "`", true);
        addEditionField(embed, playerName);
        embed.addField("📋 **Acción Tomada**", emoji + " " + action, true);

        if (reason != null && !reason.isEmpty()) {
            embed.addField("📝 **Razón**", reason, false);
        }

        if (action.equalsIgnoreCase("WARN") && maxWarns > 0) {
            embed.addField("⚠️ **Warnings**", warns + "/" + maxWarns + " warnings", true);
            if (warns >= maxWarns) {
                embed.addField("🔨 **Resultado**", "El jugador ha sido baneado automáticamente por alcanzar el máximo de warnings", false);
            }
        }

        embed.setColor(color);
        embed.setThumbnail(skin(playerName));
        embed.setFooter(FOOTER_TEXT);
        embed.setTimestamp(Instant.now());

        return embed.build();
    }

    public static MessageEmbed createAppealButtonEmbed(MCReportPlugin plugin) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("📨 Sistema de Apelaciones", null, null);
        embed.setDescription(
                "**¿Has sido baneado y crees que fue un error?**\n" +
                "Utiliza el botón de abajo para apelar tu ban.\n\n" +
                "**Cómo funciona:**\n" +
                "1️⃣ Haz clic en el botón **📨 Apelar Ban**\n" +
                "2️⃣ Indica tu usuario de Minecraft y explica tu situación\n" +
                "3️⃣ El equipo de staff revisará tu apelación\n" +
                "4️⃣ Recibirás la respuesta por DM\n\n" +
                "**⚠️ Importante:** Las apelaciones falsas o repetidas pueden conllevar sanciones."
        );
        embed.setColor(COLOR_PRIMARY);
        embed.setFooter(FOOTER_TEXT + " • Apelaciones");
        embed.setTimestamp(Instant.now());
        return embed.build();
    }

    public static MessageEmbed createAppealTicketEmbed(String playerName, String appellantTag,
                                                       String appellantId, String appealText,
                                                       String evidence, String appealId) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("📨 Nueva Apelación • #" + appealId.replace("appeal-", ""),
                null, skin(playerName));
        embed.setDescription("Se ha abierto un ticket de apelación. Revisa la información y toma la decisión correspondiente.");

        embed.addField("👤 **Jugador**", "`" + playerName + "`", true);
        addEditionField(embed, playerName);
        embed.addField("📝 **Apelante**", appellantTag, true);
        embed.addField("⏰ **Momento**", "<t:" + (System.currentTimeMillis() / 1000) + ":R>", true);

        if (appealText != null && !appealText.isEmpty()) {
            embed.addField("📋 **Motivo de la apelación**", appealText, false);
        }
        if (evidence != null && !evidence.isEmpty()) {
            embed.addField("🔗 **Pruebas / Evidencia**", evidence, false);
        }

        embed.addField("🆔 **Apelante ID**", "`" + appellantId + "`", true);

        embed.setColor(COLOR_WARNING);
        embed.setThumbnail(skin(playerName));
        embed.setImage(skinFull(playerName));
        embed.setFooter(FOOTER_TEXT + " • Ticket Abierto");
        embed.setTimestamp(Instant.now());

        return embed.build();
    }

    public static MessageEmbed createAppealDecisionEmbed(String decision, String playerName,
                                                         String moderatorTag, String note) {
        boolean approved = decision.equalsIgnoreCase("approved");
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor((approved ? "✅" : "❌") + " Apelación " + (approved ? "Aprobada" : "Rechazada"),
                null, skin(playerName));
        embed.setDescription("Decisión tomada por **" + moderatorTag + "**.");

        embed.addField("👤 **Jugador**", "`" + playerName + "`", true);
        addEditionField(embed, playerName);
        embed.addField("📋 **Decisión**", approved ? "✅ Aprobada" : "❌ Rechazada", true);

        if (note != null && !note.isEmpty()) {
            embed.addField("📝 **Nota / Mensaje**", note, false);
        }

        embed.setColor(approved ? COLOR_SUCCESS : COLOR_DANGER);
        embed.setThumbnail(skin(playerName));
        embed.setFooter(FOOTER_TEXT);
        embed.setTimestamp(Instant.now());

        return embed.build();
    }

    public static MessageEmbed createAppealDMEmbed(String decision, String playerName, String note) {
        boolean approved = decision.equalsIgnoreCase("approved");
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor((approved ? "✅" : "❌") + " Resultado de tu Apelación",
                null, skin(playerName));
        embed.setDescription("Tu apelación para **" + playerName + "** ha sido " +
                (approved ? "**APROBADA**" : "**RECHAZADA**") + ".");

        addEditionField(embed, playerName);

        if (approved) {
            embed.addField("🔓 **Desbloqueado**", "Tu ban ha sido retirado. ¡Ya puedes volver a entrar al servidor!", false);
        }

        if (note != null && !note.isEmpty()) {
            embed.addField("📝 **Mensaje del staff**", note, false);
        }

        embed.setColor(approved ? COLOR_SUCCESS : COLOR_DANGER);
        embed.setThumbnail(skin(playerName));
        embed.setFooter(FOOTER_TEXT + " • Apelaciones");
        embed.setTimestamp(Instant.now());

        return embed.build();
    }

    public static MessageEmbed createPlayerInfoEmbed(String playerName, boolean isOnline,
                                                      String world, int x, int y, int z,
                                                      double health, int foodLevel,
                                                      int warnCount, int reportCount) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("ℹ️ Información de Jugador • " + playerName, null, skin(playerName));

        embed.addField("🎮 **Estado**", isOnline ? "🟢 En línea" : "🔴 Fuera de línea", true);
        addEditionField(embed, playerName);
        embed.addField("❤️ **Salud**", String.format("%.1f / 20", health), true);
        embed.addField("🍖 **Hambre**", foodLevel + " / 20", true);

        if (isOnline) {
            embed.addField("🌍 **Mundo**", world, true);
            embed.addField("📍 **Ubicación**", x + ", " + y + ", " + z, true);
        }

        embed.addField("⚠️ **Warnings**", warnCount + " / " + getMaxWarns(), true);
        embed.addField("📋 **Reportes**", reportCount + " reportes recibidos", true);

        embed.setColor(isOnline ? COLOR_PRIMARY : COLOR_MUTED);
        embed.setThumbnail(skin(playerName));
        embed.setImage(skinFull(playerName));
        embed.setFooter(FOOTER_TEXT);
        embed.setTimestamp(Instant.now());

        return embed.build();
    }

    public static MessageEmbed createStatsEmbed(int totalReports, int pendingReports,
                                                  int resolvedReports, int totalBans,
                                                  int totalKicks, int totalWarns,
                                                  int totalMutes) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("📊 Estadísticas del Sistema de Reportes", null, null);

        embed.addField("📋 **Reportes Totales**", String.valueOf(totalReports), true);
        embed.addField("⏳ **Pendientes**", String.valueOf(pendingReports), true);
        embed.addField("✅ **Resueltos**", String.valueOf(resolvedReports), true);

        embed.addBlankField(false);

        embed.addField("🔨 **Bans**", String.valueOf(totalBans), true);
        embed.addField("🚪 **Kicks**", String.valueOf(totalKicks), true);
        embed.addField("⚠️ **Warns**", String.valueOf(totalWarns), true);
        embed.addField("🔇 **Mutes**", String.valueOf(totalMutes), true);

        embed.setColor(COLOR_PRIMARY);
        embed.setFooter(FOOTER_TEXT);
        embed.setTimestamp(Instant.now());

        return embed.build();
    }

    public static MessageEmbed createReportListEmbed(List<String> reports, int page, int totalPages) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("📋 Lista de Reportes • Página " + page + "/" + totalPages, null, null);

        if (reports.isEmpty()) {
            embed.setDescription("No hay reportes pendientes.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < reports.size(); i++) {
                String report = reports.get(i);
                String[] parts = report.split("\\|");
                if (parts.length >= 3) {
                    sb.append("**").append(i + 1).append(".** ");
                    sb.append("`").append(parts[0]).append("` ");
                    sb.append("• ").append(parts[1]).append(" ");
                    sb.append("• ").append(parts[2]).append("\n");
                }
            }
            embed.setDescription(sb.toString());
        }

        embed.setColor(COLOR_PRIMARY);
        embed.setFooter(FOOTER_TEXT + " • Página " + page + "/" + totalPages);
        embed.setTimestamp(Instant.now());

        return embed.build();
    }

    public static MessageEmbed createConfirmationEmbed(String title, String description, Color color) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(title, null, null);
        embed.setDescription(description);
        embed.setColor(color);
        embed.setFooter(FOOTER_TEXT);
        embed.setTimestamp(Instant.now());
        return embed.build();
    }

    public static MessageEmbed createErrorEmbed(String title, String description) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("❌ " + title, null, null);
        embed.setDescription(description);
        embed.setColor(COLOR_DANGER);
        embed.setFooter(FOOTER_TEXT);
        embed.setTimestamp(Instant.now());
        return embed.build();
    }

    public static MessageEmbed createSuccessEmbed(String title, String description) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("✅ " + title, null, null);
        embed.setDescription(description);
        embed.setColor(COLOR_SUCCESS);
        embed.setFooter(FOOTER_TEXT);
        embed.setTimestamp(Instant.now());
        return embed.build();
    }

    public static String getReasonEmoji(String reason) {
        if (reason == null) return "⚫";
        String lower = reason.toLowerCase();
        if (lower.contains("cheat") || lower.contains("hack")) return "🔴";
        if (lower.contains("toxic") || lower.contains("insult")) return "🟡";
        if (lower.contains("spam")) return "⚪";
        if (lower.contains("ban") || lower.contains("evadir")) return "🟣";
        if (lower.contains("bug") || lower.contains("exploit")) return "🔵";
        if (lower.contains("skin")) return "🎨";
        return "⚫";
    }

    private static int getMaxWarns() {
        MCReportPlugin plugin = MCReportPlugin.getInstance();
        if (plugin != null) {
            return plugin.getConfig().getInt("reports.max-warns-before-ban", 3);
        }
        return 3;
    }
}
