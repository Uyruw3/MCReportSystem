package com.mcreport.discord;

import com.mcreport.MCReportPlugin;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AppealListener extends ListenerAdapter {

    private final MCReportPlugin plugin;
    private final ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PendingAppealDecision> pendingDecisions = new ConcurrentHashMap<>();

    public AppealListener(MCReportPlugin plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    private void startCleanupTask() {
        long minWindow = Math.max(TimeUnit.SECONDS.toMillis(
                        Math.max(1, plugin.getConfig().getInt("appeals.cooldown-seconds", 300))),
                TimeUnit.MINUTES.toMillis(5));
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            cooldowns.entrySet().removeIf(entry -> now - entry.getValue() > minWindow);
            pendingDecisions.entrySet().removeIf(entry ->
                    now - entry.getValue().createdAt > TimeUnit.MINUTES.toMillis(15));
        }, 20L * 60 * 5, 20L * 60 * 5);
    }

    private static class PendingAppealDecision {
        String appealId;
        String playerName;
        String appellantId;
        String moderatorId;
        boolean approved;
        long createdAt;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        if (componentId.equals("mcreport:appeal")) {
            handleAppealButton(event);
            return;
        }

        String[] parts = componentId.split(":");
        if (parts.length < 5 || !parts[0].equals("mcreport")) return;

        if (parts[1].equals("appeal-approve") || parts[1].equals("appeal-reject")) {
            if (!PermissionUtils.isStaff(event.getMember(), plugin)) {
                event.replyEmbeds(EmbedUtils.createErrorEmbed(
                        "Sin Permisos",
                        "No tienes permiso para realizar esta acción."
                )).setEphemeral(true).queue();
                return;
            }
            openDecisionModal(event, parts[1].equals("appeal-approve"), parts[2], parts[3], parts[4]);
        }
    }

    private void handleAppealButton(ButtonInteractionEvent event) {
        String userId = event.getUser().getId();
        int cooldownSeconds = plugin.getConfig().getInt("appeals.cooldown-seconds", 300);

        Long last = cooldowns.get(userId);
        if (last != null) {
            long elapsed = (System.currentTimeMillis() - last) / 1000;
            if (elapsed < cooldownSeconds) {
                long remaining = cooldownSeconds - elapsed;
                event.replyEmbeds(EmbedUtils.createErrorEmbed(
                        "Cooldown Activo",
                        "Debes esperar **" + remaining + "** segundos antes de hacer otra apelación."
                )).setEphemeral(true).queue();
                return;
            }
        }

        Modal modal = Modal.create("mcreport:appeal-submit", "📨 Apelar Ban")
                .addActionRows(
                        ActionRow.of(
                                TextInput.create("jugador", "Usuario de Minecraft", TextInputStyle.SHORT)
                                        .setPlaceholder("El usuario que fue baneado")
                                        .setRequired(true)
                                        .setRequiredRange(1, 17)
                                        .build()
                        ),
                        ActionRow.of(
                                TextInput.create("motivo", "¿Por qué deberían desbanearte?", TextInputStyle.PARAGRAPH)
                                        .setPlaceholder("Explica tu situación con detalle...")
                                        .setRequired(true)
                                        .setRequiredRange(10, 1000)
                                        .build()
                        ),
                        ActionRow.of(
                                TextInput.create("pruebas", "Evidencia (Links)", TextInputStyle.PARAGRAPH)
                                        .setPlaceholder("Links de screenshots, pruebas, etc.")
                                        .setRequired(false)
                                        .setRequiredRange(0, 500)
                                        .build()
                        )
                )
                .build();

        event.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        String modalId = event.getModalId();

        if (modalId.equals("mcreport:appeal-submit")) {
            handleAppealSubmit(event);
            return;
        }

        if (modalId.startsWith("mcreport:appeal-decision:")) {
            String token = modalId.replace("mcreport:appeal-decision:", "");
            PendingAppealDecision pending = pendingDecisions.remove(token);
            if (pending == null) return;

            if (!pending.moderatorId.equals(event.getUser().getId())) {
                event.replyEmbeds(EmbedUtils.createErrorEmbed(
                        "Acción Inválida",
                        "Esta apelación fue iniciada por otro moderador."
                )).setEphemeral(true).queue();
                return;
            }

            String note = event.getValue("razon") != null ? event.getValue("razon").getAsString() : "";
            if (pending.approved) {
                handleApprove(event, pending, note);
            } else {
                handleReject(event, pending, note);
            }
        }
    }

    private void handleAppealSubmit(ModalInteractionEvent event) {
        String jugador = event.getValue("jugador").getAsString();
        String motivo = event.getValue("motivo").getAsString();
        String pruebas = event.getValue("pruebas") != null ? event.getValue("pruebas").getAsString() : "";

        if (jugador.length() > 17) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Nombre Inválido",
                    "El nombre del jugador no puede tener más de 17 caracteres."
            )).setEphemeral(true).queue();
            return;
        }

        Guild guild = event.getGuild();
        String appealId = "appeal-" + System.currentTimeMillis();

        String ticketCategoryId = plugin.getConfig().getString("discord.appeal-ticket-category", "");
        if (ticketCategoryId.isEmpty()) {
            ticketCategoryId = plugin.getConfig().getString("discord.report-ticket-category", "");
        }
        if (ticketCategoryId.isEmpty()) {
            plugin.getLogger().severe("No se ha configurado la categoría de tickets (appeal ni report)!");
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Error del Servidor",
                    "El sistema aún no está configurado. Contacta con el staff."
            )).setEphemeral(true).queue();
            return;
        }

        Category category = guild.getChannelById(Category.class, ticketCategoryId);
        if (category == null) {
            plugin.getLogger().severe("No se pudo encontrar la categoría de apelaciones (ID '" +
                    ticketCategoryId + "'). ¿Existe y el bot la puede ver?");
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Error del Servidor",
                    "El sistema aún no está configurado. Contacta con el staff."
            )).setEphemeral(true).queue();
            return;
        }

        String channelName = "apelacion-" + jugador.toLowerCase().replaceAll("[^a-z0-9_.*-]", "");
        String appellantTag = event.getUser().getAsTag();
        String appellantId = event.getUser().getId();

        if (plugin.getStorage().hasPendingAppeal(appellantId, jugador)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Apelación Pendiente",
                    "Ya tienes una apelación pendiente para este jugador."
            )).setEphemeral(true).queue();
            return;
        }

        guild.createTextChannel(channelName)
                .setParent(category)
                .setTopic("📨 " + jugador + " • Apelación • ID: " + appealId)
                .addPermissionOverride(guild.getPublicRole(), List.of(), List.of(Permission.VIEW_CHANNEL))
                .addPermissionOverride(event.getMember(),
                        List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), List.of())
                .queue(ticketChannel -> {
                    for (Long roleId : plugin.getConfig().getLongList("discord.staff-roles")) {
                        Role role = guild.getRoleById(roleId);
                        if (role != null) {
                            ticketChannel.upsertPermissionOverride(role)
                                    .grant(Permission.VIEW_CHANNEL)
                                    .grant(Permission.MESSAGE_SEND)
                                    .queue();
                        }
                    }

                    net.dv8tion.jda.api.entities.MessageEmbed appealEmbed = EmbedUtils.createAppealTicketEmbed(
                            jugador, appellantTag, appellantId, motivo, pruebas, appealId);

                    Button approveButton = Button.success("mcreport:appeal-approve:" + appealId + ":" + jugador + ":" + appellantId,
                            Emoji.fromFormatted("✅")).withLabel("Aprobar");
                    Button rejectButton = Button.danger("mcreport:appeal-reject:" + appealId + ":" + jugador + ":" + appellantId,
                            Emoji.fromFormatted("❌")).withLabel("Rechazar");

                    ticketChannel.sendMessageEmbeds(appealEmbed)
                            .setActionRow(approveButton, rejectButton)
                            .queue();

                    try {
                        plugin.getStorage().createAppeal(appealId, appellantTag,
                                appellantId, jugador, motivo, pruebas, ticketChannel.getId());
                    } catch (RuntimeException storageFailure) {
                        plugin.getLogger().severe("No se pudo guardar la apelación " + appealId
                                + "; eliminando el ticket incompleto: " + storageFailure.getMessage());
                        ticketChannel.delete().queue();
                        event.replyEmbeds(EmbedUtils.createErrorEmbed(
                                "Error del Servidor",
                                "No se pudo guardar la apelación. Inténtalo de nuevo."
                        )).setEphemeral(true).queue();
                        return;
                    }

                    cooldowns.put(appellantId, System.currentTimeMillis());

                    event.replyEmbeds(EmbedUtils.createSuccessEmbed(
                            "Apelación Enviada",
                            "Tu apelación **#" + appealId.replace("appeal-", "") + "** ha sido enviada correctamente.\n\n" +
                            "**Jugador:** `" + jugador + "`\n\n" +
                            "El equipo de staff la revisará y recibirás la respuesta por DM."
                    )).setEphemeral(true).queue();
                }, failure -> {
                    logChannelCreationFailure(failure, channelName);
                    event.replyEmbeds(EmbedUtils.createErrorEmbed(
                            "Error del Servidor",
                            "No se pudo crear el ticket de apelación. Inténtalo de nuevo."
                    )).setEphemeral(true).queue();
                });
    }

    private void logChannelCreationFailure(Throwable failure, String channelName) {
        if (failure instanceof net.dv8tion.jda.api.exceptions.ErrorResponseException ere) {
            plugin.getLogger().severe("No se pudo crear el canal '" + channelName + "': "
                    + ere.getErrorResponse() + " (" + ere.getMeaning() + "). " +
                    "Revisa que el bot tenga los permisos 'Gestionar canales' y 'Gestionar roles', " +
                    "y que su rol esté por encima de los roles de staff.");
        } else {
            plugin.getLogger().severe("No se pudo crear el canal '" + channelName + "': " + failure);
        }
    }

    private void openDecisionModal(ButtonInteractionEvent event, boolean approved,
                                   String appealId, String playerName, String appellantId) {
        String token = UUID.randomUUID().toString().substring(0, 20);

        PendingAppealDecision pending = new PendingAppealDecision();
        pending.appealId = appealId;
        pending.playerName = playerName;
        pending.appellantId = appellantId;
        pending.moderatorId = event.getUser().getId();
        pending.approved = approved;
        pending.createdAt = System.currentTimeMillis();
        pendingDecisions.put(token, pending);

        Modal modal = Modal.create("mcreport:appeal-decision:" + token,
                approved ? "✅ Aprobar Apelación" : "❌ Rechazar Apelación")
                .addActionRows(
                        ActionRow.of(
                                TextInput.create("razon", approved ? "Mensaje al jugador" : "Motivo del rechazo", TextInputStyle.PARAGRAPH)
                                        .setPlaceholder(approved ? "Mensaje que recibirá por DM..." : "Explica por qué se rechaza la apelación...")
                                        .setRequired(true)
                                        .setRequiredRange(1, 1000)
                                        .build()
                        )
                )
                .build();

        event.replyModal(modal).queue();
    }

    private void handleApprove(ModalInteractionEvent event, PendingAppealDecision pending, String note) {
        plugin.getBanManager().unbanPlayer(pending.playerName);
        event.replyEmbeds(EmbedUtils.createAppealDecisionEmbed(
                "approved", pending.playerName, event.getUser().getAsTag(), note
        )).setEphemeral(false).queue();

        plugin.getStorage().setAppealDecision(pending.appealId, "APPROVED", event.getUser().getAsTag(), note);
        notifyAppellant(pending.appellantId, pending.playerName, "approved", note);
        plugin.getLogger().info("[MCReport] Apelación " + pending.appealId + " APROBADA por " +
                event.getUser().getAsTag() + ". Jugador: " + pending.playerName);
        closeChannel(event);
    }

    private void handleReject(ModalInteractionEvent event, PendingAppealDecision pending, String note) {
        event.replyEmbeds(EmbedUtils.createAppealDecisionEmbed(
                "rejected", pending.playerName, event.getUser().getAsTag(), note
        )).setEphemeral(false).queue();

        plugin.getStorage().setAppealDecision(pending.appealId, "REJECTED", event.getUser().getAsTag(), note);
        notifyAppellant(pending.appellantId, pending.playerName, "rejected", note);
        plugin.getLogger().info("[MCReport] Apelación " + pending.appealId + " RECHAZADA por " +
                event.getUser().getAsTag() + ". Jugador: " + pending.playerName);
        closeChannel(event);
    }

    private void notifyAppellant(String appellantId, String playerName, String decision, String note) {
        if (appellantId == null || appellantId.isEmpty()) return;

        try {
            User appellant = plugin.getDiscordBot().getJda().getUserById(appellantId);
            if (appellant != null) {
                appellant.openPrivateChannel().queue(channel -> {
                    channel.sendMessageEmbeds(EmbedUtils.createAppealDMEmbed(
                            decision, playerName, note
                    )).queue();
                }, failure -> {
                    plugin.getLogger().warning("No se pudo enviar DM al apelante " + appellantId);
                });
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error al notificar al apelante: " + e.getMessage());
        }
    }

    private void closeChannel(ModalInteractionEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                event.getChannel().delete().queue();
            } catch (Exception e) {
                plugin.getLogger().warning("Error al eliminar canal de apelación: " + e.getMessage());
            }
        }, 20L * Math.max(1, plugin.getConfig().getInt("appeals.close-delay-seconds", 10)));
    }
}