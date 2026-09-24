package com.mcreport.discord;

import com.mcreport.MCReportPlugin;
import com.mcreport.storage.Storage;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ReportListener extends ListenerAdapter {

    private final MCReportPlugin plugin;
    private final ConcurrentHashMap<String, PendingAction> pendingActions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> processingReports = new ConcurrentHashMap<>();

    public ReportListener(MCReportPlugin plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    private void startCleanupTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            pendingActions.entrySet().removeIf(entry ->
                    now - entry.getValue().createdAt > TimeUnit.MINUTES.toMillis(15));
        }, 20L * 60 * 5, 20L * 60 * 5);
    }

    private static class PendingAction {
        String action;
        String reportId;
        String playerName;
        String reporterId;
        String moderatorId;
        String guildId;
        long createdAt;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] parts = event.getComponentId().split(":");
        if (parts.length < 4 || !parts[0].equals("mcreport")) return;

        String action = parts[1];
        String reportId = parts[2];
        String playerName = parts[3];
        String reporterId = parts.length > 4 ? parts[4] : null;

        if (!PermissionUtils.isStaff(event.getMember(), plugin)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Sin Permisos",
                    "No tienes permiso para realizar esta acción."
            )).setEphemeral(true).queue();
            return;
        }

        openReasonModal(event, action, reportId, playerName, reporterId);
    }

    private void openReasonModal(ButtonInteractionEvent event, String action,
                                  String reportId, String playerName, String reporterId) {
        String token = UUID.randomUUID().toString().substring(0, 20);

        PendingAction pending = new PendingAction();
        pending.action = action;
        pending.reportId = reportId;
        pending.playerName = playerName;
        pending.reporterId = reporterId;
        pending.moderatorId = event.getUser().getId();
        pending.guildId = event.getGuild().getId();
        pending.createdAt = System.currentTimeMillis();
        pendingActions.put(token, pending);

        switch (action) {
            case "ban":
                openBanModal(event, token, false);
                break;
            case "tempban":
                openBanModal(event, token, true);
                break;
            case "kick":
                openSimpleReasonModal(event, token, "🚪 Razón del Kick",
                        "Haz sido expulsado (razón personalizada)");
                break;
            case "warn":
                openSimpleReasonModal(event, token, "⚠️ Razón del Warn",
                        "Motivo de la advertencia");
                break;
            case "mute":
                openMuteModal(event, token);
                break;
            case "resolve":
                openResolveModal(event, token);
                break;
        }
    }

    private void openBanModal(ButtonInteractionEvent event, String token, boolean temp) {
        Modal.Builder modal = Modal.create("mcreport:reason:" + token,
                temp ? "⏰ Ban Temporal" : "🔨 Ban Permanente");

        if (temp) {
            modal.addActionRows(
                    ActionRow.of(
                            TextInput.create("razon", "Razón del ban", TextInputStyle.PARAGRAPH)
                                    .setPlaceholder("Motivo del ban temporal...")
                                    .setRequired(true)
                                    .setRequiredRange(1, 500)
                                    .build()
                    ),
                    ActionRow.of(
                            TextInput.create("duracion", "Duración (horas)", TextInputStyle.SHORT)
                                    .setPlaceholder("Ej: 24 (por defecto: " + plugin.getConfig().getInt("reports.temp-ban-duration-hours", 24) + "h)")
                                    .setRequired(true)
                                    .setRequiredRange(1, 6)
                                    .build()
                    )
            );
        } else {
            modal.addActionRows(
                    ActionRow.of(
                            TextInput.create("razon", "Razón del ban", TextInputStyle.PARAGRAPH)
                                    .setPlaceholder("Motivo del ban permanente...")
                                    .setRequired(true)
                                    .setRequiredRange(1, 500)
                                    .build()
                    )
            );
        }

        event.replyModal(modal.build()).queue();
    }

    private void openMuteModal(ButtonInteractionEvent event, String token) {
        Modal modal = Modal.create("mcreport:reason:" + token, "🔇 Silenciar Jugador")
                .addActionRows(
                        ActionRow.of(
                                TextInput.create("razon", "Razón del mute", TextInputStyle.PARAGRAPH)
                                        .setPlaceholder("Motivo del silencio...")
                                        .setRequired(true)
                                        .setRequiredRange(1, 500)
                                        .build()
                        ),
                        ActionRow.of(
                                TextInput.create("duracion", "Duración (minutos)", TextInputStyle.SHORT)
                                        .setPlaceholder("Ej: 60 (por defecto: " + plugin.getConfig().getInt("reports.mute-duration-minutes", 30) + "min)")
                                        .setRequired(true)
                                        .setRequiredRange(1, 6)
                                        .build()
                        )
                )
                .build();
        event.replyModal(modal).queue();
    }

    private void openSimpleReasonModal(ButtonInteractionEvent event, String token, String title, String placeholder) {
        Modal modal = Modal.create("mcreport:reason:" + token, title)
                .addActionRows(
                        ActionRow.of(
                                TextInput.create("razon", title, TextInputStyle.PARAGRAPH)
                                        .setPlaceholder(placeholder)
                                        .setRequired(true)
                                        .setRequiredRange(1, 500)
                                        .build()
                        )
                )
                .build();
        event.replyModal(modal).queue();
    }

    private void openResolveModal(ButtonInteractionEvent event, String token) {
        Modal modal = Modal.create("mcreport:reason:" + token, "✅ Resolver Ticket")
                .addActionRows(
                        ActionRow.of(
                                TextInput.create("razon", "Nota para el reportero", TextInputStyle.PARAGRAPH)
                                        .setPlaceholder("Mensaje que recibirá el reportero por DM...")
                                        .setRequired(false)
                                        .setRequiredRange(0, 1000)
                                        .build()
                        )
                )
                .build();
        event.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().startsWith("mcreport:reason:")) return;

        String token = event.getModalId().replace("mcreport:reason:", "");
        PendingAction pending = pendingActions.remove(token);
        if (pending == null) return;

        if (!pending.moderatorId.equals(event.getUser().getId())) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Acción Inválida",
                    "Esta acción fue iniciada por otro moderador."
            )).setEphemeral(true).queue();
            return;
        }

        if (!plugin.getStorage().isReportPending(pending.reportId)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Reporte Cerrado", "Este reporte ya fue procesado por otro moderador."
            )).setEphemeral(true).queue();
            return;
        }
        if (processingReports.putIfAbsent(pending.reportId, Boolean.TRUE) != null) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Acción en curso", "Otro moderador ya está procesando este reporte."
            )).setEphemeral(true).queue();
            return;
        }

        String reason = event.getValue("razon") != null ? event.getValue("razon").getAsString() : "";
        String duration = event.getValue("duracion") != null ? event.getValue("duracion").getAsString() : "";

        try {
            switch (pending.action) {
                case "ban":
                    handleBan(event, pending, reason);
                    break;
                case "tempban":
                    handleTempBan(event, pending, reason, duration);
                    break;
                case "kick":
                    handleKick(event, pending, reason);
                    break;
                case "warn":
                    handleWarn(event, pending, reason);
                    break;
                case "mute":
                    handleMute(event, pending, reason, duration);
                    break;
                case "resolve":
                    handleResolve(event, pending, reason);
                    break;
                default:
                    event.replyEmbeds(EmbedUtils.createErrorEmbed(
                            "Acción Inválida", "La acción solicitada no existe."
                    )).setEphemeral(true).queue();
            }
        } finally {
            processingReports.remove(pending.reportId);
        }
    }

    private void handleBan(ModalInteractionEvent event, PendingAction pending, String reason) {
        plugin.getBanManager().banPlayer(pending.playerName, reason);
        event.replyEmbeds(EmbedUtils.createActionEmbed(
                "BAN", pending.playerName, event.getUser().getAsTag(), reason
        )).setEphemeral(false).queue();

        plugin.getStorage().setActionTaken(pending.reportId, "BAN", event.getUser().getAsTag(), reason);
        notifyReporter(pending.reporterId, pending.playerName, "BAN", reason);
        logAction(event, pending.playerName, "BAN", reason);
    }

    private void handleTempBan(ModalInteractionEvent event, PendingAction pending, String reason, String duration) {
        int hours;
        try {
            hours = Integer.parseInt(duration.trim());
        } catch (NumberFormatException e) {
            hours = plugin.getConfig().getInt("reports.temp-ban-duration-hours", 24);
        }
        if (hours <= 0) hours = 1;

        String fullReason = reason + " (§e" + hours + "h)";
        plugin.getBanManager().tempBanPlayer(pending.playerName, hours, fullReason);
        event.replyEmbeds(EmbedUtils.createActionEmbed(
                "TEMPBAN", pending.playerName, event.getUser().getAsTag(),
                reason + " • ⏰ " + hours + " horas"
        )).setEphemeral(false).queue();

        plugin.getStorage().setActionTaken(pending.reportId, "TEMPBAN", event.getUser().getAsTag(), reason);
        notifyReporter(pending.reporterId, pending.playerName, "TEMPBAN",
                reason + " • " + hours + " horas");
        logAction(event, pending.playerName, "TEMPBAN", reason + " (" + hours + "h)");
    }

    private void handleKick(ModalInteractionEvent event, PendingAction pending, String reason) {
        plugin.getBanManager().kickPlayer(pending.playerName, reason);
        event.replyEmbeds(EmbedUtils.createActionEmbed(
                "KICK", pending.playerName, event.getUser().getAsTag(), reason
        )).setEphemeral(false).queue();

        plugin.getStorage().setActionTaken(pending.reportId, "KICK", event.getUser().getAsTag(), reason);
        notifyReporter(pending.reporterId, pending.playerName, "KICK", reason);
        logAction(event, pending.playerName, "KICK", reason);
    }

    private void handleWarn(ModalInteractionEvent event, PendingAction pending, String reason) {
        Storage storage = plugin.getStorage();
        int maxWarns = plugin.getConfig().getInt("reports.max-warns-before-ban", 3);

        int currentWarns = storage.getPlayerWarns(pending.playerName) + 1;
        storage.setPlayerWarns(pending.playerName, currentWarns);

        if (currentWarns >= maxWarns) {
            storage.setPlayerWarns(pending.playerName, 0);
            plugin.getBanManager().banPlayer(pending.playerName,
                    "Máximo de warnings alcanzado (" + maxWarns + "/" + maxWarns + ")");
            event.replyEmbeds(EmbedUtils.createActionEmbed(
                    "BAN", pending.playerName, "Sistema Automático",
                    "Ban automático por alcanzar " + maxWarns + " warnings"
            )).setEphemeral(false).queue();

            storage.setActionTaken(pending.reportId, "BAN", event.getUser().getAsTag(),
                    "Ban automático por alcanzar " + maxWarns + " warnings");
            notifyReporter(pending.reporterId, pending.playerName, "BAN",
                    "Ban automático por alcanzar " + maxWarns + " warnings");
            logAction(event, pending.playerName, "BAN",
                    "Ban automático por alcanzar " + maxWarns + " warnings");
            return;
        }

        event.replyEmbeds(EmbedUtils.createActionEmbed(
                "WARN", pending.playerName, event.getUser().getAsTag(),
                reason + " • (" + currentWarns + "/" + maxWarns + " warnings)"
        )).setEphemeral(false).queue();

        storage.setActionTaken(pending.reportId, "WARN", event.getUser().getAsTag(), reason);
        notifyReporter(pending.reporterId, pending.playerName, "WARN",
                reason + " • " + currentWarns + "/" + maxWarns + " warnings");
        logAction(event, pending.playerName, "WARN", reason);
    }

    private void handleMute(ModalInteractionEvent event, PendingAction pending, String reason, String duration) {
        int minutes;
        try {
            minutes = Integer.parseInt(duration.trim());
        } catch (NumberFormatException e) {
            minutes = plugin.getConfig().getInt("reports.mute-duration-minutes", 30);
        }
        if (minutes <= 0) minutes = 1;

        plugin.getBanManager().mutePlayer(pending.playerName, minutes);
        event.replyEmbeds(EmbedUtils.createActionEmbed(
                "MUTE", pending.playerName, event.getUser().getAsTag(),
                reason + " • 🔇 " + minutes + " minutos"
        )).setEphemeral(false).queue();

        plugin.getStorage().setActionTaken(pending.reportId, "MUTE", event.getUser().getAsTag(), reason);
        notifyReporter(pending.reporterId, pending.playerName, "MUTE",
                reason + " • " + minutes + " minutos");
        logAction(event, pending.playerName, "MUTE", reason + " (" + minutes + "min)");
    }

    private void handleResolve(ModalInteractionEvent event, PendingAction pending, String note) {
        Storage storage = plugin.getStorage();
        String actionTaken = storage.getReportActionTaken(pending.reportId);
        storage.resolveReport(pending.reportId);

        String message = note != null && !note.isEmpty() ? note : "Sin nota adicional";
        event.replyEmbeds(EmbedUtils.createClosedTicketEmbed(
                event.getUser().getAsTag(),
                actionTaken != null ? actionTaken + "\n📝 Nota: " + message : "Ninguna acción tomada\n📝 Nota: " + message
        )).setEphemeral(false).queue();

        notifyReporter(pending.reporterId, pending.playerName, "RESOLVED", message);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                event.getChannel().delete().queue();
            } catch (Exception e) {
                plugin.getLogger().warning("Error al eliminar canal: " + e.getMessage());
            }
        }, 20L * 3);
    }

    private void notifyReporter(String reporterId, String playerName, String action, String reason) {
        if (reporterId == null || reporterId.isEmpty()) return;

        try {
            User reporter = plugin.getDiscordBot().getJda().getUserById(reporterId);
            if (reporter != null) {
                reporter.openPrivateChannel().queue(channel -> {
                    channel.sendMessageEmbeds(EmbedUtils.createDMNotificationEmbed(
                            playerName, action, reason,
                            plugin.getStorage().getPlayerWarns(playerName),
                            plugin.getConfig().getInt("reports.max-warns-before-ban", 3)
                    )).queue();
                }, failure -> {
                    plugin.getLogger().warning("No se pudo enviar DM al reporter " + reporterId);
                });
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error al notificar al reporter: " + e.getMessage());
        }
    }

    private void logAction(net.dv8tion.jda.api.entities.User moderator, String playerName, String action, String reason) {
        plugin.getLogger().info("[MCReport] " + moderator.getAsTag() + " realizó " + action + " a " + playerName + ". Razón: " + reason);
    }

    private void logAction(ModalInteractionEvent event, String playerName, String action, String reason) {
        logAction(event.getUser(), playerName, action, reason);
    }
}