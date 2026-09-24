package com.mcreport.discord;

import com.mcreport.MCReportPlugin;
import com.mcreport.minecraft.PlayerDataManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ReportModal extends ListenerAdapter {

    private static final int MAX_MENU_OPTIONS = 25;
    private static final List<String> DEFAULT_REASONS = List.of(
            "Cheating/Hacks",
            "Toxicidad/Insultos",
            "Spam",
            "Evadir Ban",
            "Bug Abuse",
            "Skin Inapropiada",
            "Otro"
    );

    private final MCReportPlugin plugin;
    private final ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastReportAt = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> reportCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PendingReason> pendingReasons = new ConcurrentHashMap<>();

    private static class PendingReason {
        final String reason;
        final long createdAt;

        PendingReason(String reason, long createdAt) {
            this.reason = reason;
            this.createdAt = createdAt;
        }
    }

    public ReportModal(MCReportPlugin plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    private int cooldownSeconds() {
        return Math.max(1, plugin.getConfig().getInt("reports.cooldown-seconds", 60));
    }

    private void startCleanupTask() {
        long minWindow = Math.max(TimeUnit.SECONDS.toMillis(cooldownSeconds()), TimeUnit.MINUTES.toMillis(5));
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            cooldowns.entrySet().removeIf(entry -> now - entry.getValue() > minWindow);
            lastReportAt.entrySet().removeIf(entry -> now - entry.getValue() > minWindow);
            reportCounts.entrySet().removeIf(entry -> now - lastReportAt.getOrDefault(entry.getKey(), 0L) > minWindow);
            pendingReasons.entrySet().removeIf(entry -> now - entry.getValue().createdAt > TimeUnit.MINUTES.toMillis(5));
        }, 20L * 60, 20L * 60);
    }

    private long remainingCooldown(String userId) {
        Long last = cooldowns.get(userId);
        if (last == null) return 0;
        long elapsed = (System.currentTimeMillis() - last) / 1000;
        return elapsed < cooldownSeconds() ? cooldownSeconds() - elapsed : 0;
    }

    private List<String> loadReasons() {
        List<String> configured = plugin.getConfig().getStringList("reports.reasons");
        Set<String> reasons = new LinkedHashSet<>(configured.isEmpty() ? DEFAULT_REASONS : configured);
        List<String> result = new ArrayList<>(reasons);
        return result.size() > MAX_MENU_OPTIONS ? result.subList(0, MAX_MENU_OPTIONS) : result;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().equals("mcreport:report")) return;

        String userId = event.getUser().getId();
        long remaining = remainingCooldown(userId);
        if (remaining > 0) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Cooldown Activo",
                    "Debes esperar **" + remaining + "** segundos antes de hacer otro reporte."
            )).setEphemeral(true).queue();
            return;
        }

        StringSelectMenu.Builder reasonMenu = StringSelectMenu.create("mcreport:reason:" + userId)
                .setPlaceholder("Selecciona la razón del reporte...")
                .setMinValues(1)
                .setMaxValues(1);

        boolean anyOption = false;
        for (String reason : loadReasons()) {
            reasonMenu.addOption(EmbedUtils.getReasonEmoji(reason) + " " + reason, reason);
            anyOption = true;
        }

        if (!anyOption) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Error del Servidor",
                    "No hay categorías de reporte configuradas."
            )).setEphemeral(true).queue();
            return;
        }

        event.reply("Selecciona la razón del reporte:")
                .setComponents(ActionRow.of(reasonMenu.build()))
                .setEphemeral(true)
                .queue();
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!event.getComponentId().startsWith("mcreport:reason:")) return;

        String userId = event.getComponentId().replace("mcreport:reason:", "");
        String selectedReason = event.getValues().get(0);
        pendingReasons.put(userId, new PendingReason(selectedReason, System.currentTimeMillis()));

        Modal modal = Modal.create("mcreport:submit:" + userId, "📋 Reportar Jugador")
                .addActionRows(
                        ActionRow.of(
                                TextInput.create("jugador", "Nombre del Jugador", TextInputStyle.SHORT)
                                        .setPlaceholder("Escribe el nombre exacto del jugador")
                                        .setRequired(true)
                                        .setRequiredRange(1, 17)
                                        .build()
                        ),
                        ActionRow.of(
                                TextInput.create("descripcion", "Descripción del Incidente", TextInputStyle.PARAGRAPH)
                                        .setPlaceholder("Describe lo que pasó con detalle")
                                        .setRequired(true)
                                        .setRequiredRange(10, 1000)
                                        .build()
                        ),
                        ActionRow.of(
                                TextInput.create("pruebas", "Evidencia (Links)", TextInputStyle.PARAGRAPH)
                                        .setPlaceholder("Links de screenshots, videos, etc.")
                                        .setRequired(false)
                                        .setRequiredRange(0, 500)
                                        .build()
                        )
                )
                .build();

        event.replyModal(modal).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("report")) return;

        String userId = event.getUser().getId();
        long remaining = remainingCooldown(userId);
        if (remaining > 0) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Cooldown Activo",
                    "Debes esperar **" + remaining + "** segundos antes de hacer otro reporte."
            )).setEphemeral(true).queue();
            return;
        }

        if (event.getGuild() == null) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Error del Servidor",
                    "Este comando solo funciona dentro del servidor."
            )).setEphemeral(true).queue();
            return;
        }

        String jugador = event.getOption("jugador") != null ? event.getOption("jugador").getAsString() : null;
        String razon = event.getOption("razon") != null ? event.getOption("razon").getAsString() : "Otro";
        String descripcion = event.getOption("descripcion") != null ? event.getOption("descripcion").getAsString() : "";
        String pruebas = event.getOption("pruebas") != null ? event.getOption("pruebas").getAsString() : "";

        if (!InputValidator.isValidPlayerName(jugador)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Nombre Inválido",
                    "El nombre del jugador no es válido (máximo 17 caracteres, solo letras, números, _ y . o * para Bedrock)."
            )).setEphemeral(true).queue();
            return;
        }

        event.deferReply().setEphemeral(true).queue(hook ->
                submitReport(hook, event.getGuild(),
                        event.getUser().getAsTag(), event.getUser().getId(),
                        jugador, razon, descripcion, pruebas));
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().startsWith("mcreport:submit:")) return;

        String userId = event.getModalId().split(":")[2];
        PendingReason pending = pendingReasons.remove(userId);
        String reason = pending != null ? pending.reason : "Otro";

        if (event.getGuild() == null) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Error del Servidor",
                    "Este formulario solo funciona dentro del servidor."
            )).setEphemeral(true).queue();
            return;
        }

        String jugador = event.getValue("jugador") != null ? event.getValue("jugador").getAsString() : null;
        String descripcion = event.getValue("descripcion") != null ? event.getValue("descripcion").getAsString() : "";
        String pruebas = event.getValue("pruebas") != null ? event.getValue("pruebas").getAsString() : "";

        if (!InputValidator.isValidPlayerName(jugador)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed(
                    "Nombre Inválido",
                    "El nombre del jugador no es válido (máximo 17 caracteres, solo letras, números, _ y . o * para Bedrock)."
            )).setEphemeral(true).queue();
            return;
        }

        event.deferReply().setEphemeral(true).queue(hook ->
                submitReport(hook, event.getGuild(),
                        event.getUser().getAsTag(), event.getUser().getId(),
                        jugador, reason, descripcion, pruebas));
    }

    private void submitReport(InteractionHook hook, Guild guild, String reporterTag, String reporterId,
                              String jugador, String razon, String descripcion, String pruebas) {
        jugador = jugador.trim();
        razon = razon.trim();
        descripcion = InputValidator.normalizeText(descripcion, 1000);
        pruebas = InputValidator.normalizeEvidence(pruebas);
        String targetPlayer = jugador;
        try {
            String serverEvidence = plugin.getServer().getScheduler()
                    .callSyncMethod(plugin, () -> new PlayerDataManager(plugin).formatEvidence(targetPlayer))
                    .get(3, TimeUnit.SECONDS);
            if (!serverEvidence.isBlank()) {
                pruebas = pruebas.isBlank() ? serverEvidence : pruebas + "\n\n" + serverEvidence;
            }
            pruebas = InputValidator.normalizeText(pruebas, 1000);
        } catch (Exception exception) {
            plugin.getLogger().warning("No se pudo capturar evidencia del servidor para " + jugador
                    + ": " + exception.getMessage());
        }
        if (!InputValidator.isConfiguredReason(razon, loadReasons())) {
            hook.sendMessageEmbeds(EmbedUtils.createErrorEmbed(
                    "Categoría Inválida", "Selecciona una categoría válida para el reporte."
            )).queue();
            return;
        }
        if (descripcion.length() < 10) {
            hook.sendMessageEmbeds(EmbedUtils.createErrorEmbed(
                    "Descripción Insuficiente", "Describe el incidente con al menos 10 caracteres."
            )).queue();
            return;
        }
        if (!reserveReportTimestamp(reporterId)) {
            hook.sendMessageEmbeds(EmbedUtils.createErrorEmbed(
                    "Cooldown Activo", "Debes esperar antes de enviar otro reporte."
            )).queue();
            return;
        }
        String ticketCategoryId = plugin.getConfig().getString("discord.report-ticket-category", "");
        Category category = ticketCategoryId.isEmpty() ? null : guild.getChannelById(Category.class, ticketCategoryId);
        if (category == null) {
            plugin.getLogger().severe("No se pudo encontrar la categoría de tickets de reporte (ID '" +
                    ticketCategoryId + "'). ¿Existe y el bot la puede ver?");
            hook.sendMessageEmbeds(EmbedUtils.createErrorEmbed(
                    "Error del Servidor",
                    "El sistema aún no está configurado. Contacta con el staff."
            )).queue();
            releaseReportTimestamp(reporterId);
            return;
        }

        String reportId = "report-" + System.currentTimeMillis();
        String safeName = jugador.toLowerCase().replaceAll("[^a-z0-9_*-]", "");
        String channelName = "reporte-" + (safeName.isEmpty() ? "jugador" : safeName);

        guild.createTextChannel(channelName)
                .setParent(category)
                .setTopic("📋 " + jugador + " • " + razon + " • ID: " + reportId)
                .addPermissionOverride(guild.getPublicRole(), List.of(), List.of(Permission.VIEW_CHANNEL))
                .queue(channel -> {
                    grantStaffAccess(guild, channel);

                    net.dv8tion.jda.api.entities.MessageEmbed reportEmbed = EmbedUtils.createReportTicketEmbed(
                            reporterTag, reporterId, jugador, razon, descripcion, pruebas, reportId);

                    Button banButton = Button.danger("mcreport:ban:" + reportId + ":" + jugador + ":" + reporterId, Emoji.fromFormatted("🔨")).withLabel("Ban");
                    Button tempBanButton = Button.danger("mcreport:tempban:" + reportId + ":" + jugador + ":" + reporterId, Emoji.fromFormatted("⏰")).withLabel("TempBan");
                    Button kickButton = Button.secondary("mcreport:kick:" + reportId + ":" + jugador + ":" + reporterId, Emoji.fromFormatted("🚪")).withLabel("Kick");
                    Button warnButton = Button.primary("mcreport:warn:" + reportId + ":" + jugador + ":" + reporterId, Emoji.fromFormatted("⚠️")).withLabel("Warn");
                    Button muteButton = Button.secondary("mcreport:mute:" + reportId + ":" + jugador + ":" + reporterId, Emoji.fromFormatted("🔇")).withLabel("Mute");
                    Button resolveButton = Button.success("mcreport:resolve:" + reportId + ":" + jugador + ":" + reporterId, Emoji.fromFormatted("✅")).withLabel("Resolver");

                    channel.sendMessageEmbeds(reportEmbed)
                            .setActionRow(banButton, tempBanButton, kickButton)
                            .addActionRow(warnButton, muteButton, resolveButton)
                            .queue();

                    try {
                        plugin.getStorage().createReport(reportId, reporterTag, reporterId,
                                jugador, razon, descripcion, pruebas, channel.getId());
                    } catch (Exception e) {
                        plugin.getLogger().severe("Error al guardar el reporte: " + e.getMessage());
                        channel.delete().queue();
                        releaseReportTimestamp(reporterId);
                        hook.sendMessageEmbeds(EmbedUtils.createErrorEmbed(
                                "Error de Persistencia", "No se pudo guardar el reporte. El ticket fue cancelado."
                        )).queue();
                        return;
                    }

                    hook.sendMessageEmbeds(EmbedUtils.createSuccessEmbed(
                            "Reporte Enviado",
                            "Tu reporte **#" + reportId.replace("report-", "") + "** ha sido enviado correctamente.\n\n" +
                            "**Jugador:** `" + jugador + "`\n" +
                            "**Categoría:** " + razon + "\n\n" +
                            "Si se toma alguna acción, serás notificado por DM."
                    )).queue();
                }, failure -> {
                    logChannelCreationFailure(failure, channelName);
                    releaseReportTimestamp(reporterId);
                    hook.sendMessageEmbeds(EmbedUtils.createErrorEmbed(
                            "Error del Servidor",
                            "No se pudo crear el ticket de reporte. Inténtalo de nuevo."
                    )).queue();
                });
    }

    private boolean reserveReportTimestamp(String userId) {
        long now = System.currentTimeMillis();
        Long previous = cooldowns.putIfAbsent(userId, now);
        if (previous == null || (now - previous) / 1000 >= cooldownSeconds()) {
            if (previous != null) cooldowns.replace(userId, previous, now);
            lastReportAt.put(userId, now);
            reportCounts.merge(userId, 1, Integer::sum);
            return true;
        }
        return false;
    }

    private void releaseReportTimestamp(String userId) {
        cooldowns.remove(userId);
        lastReportAt.remove(userId);
        reportCounts.computeIfPresent(userId, (key, count) -> Math.max(0, count - 1));
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

    private void grantStaffAccess(Guild guild, TextChannel channel) {
        for (Long roleId : plugin.getConfig().getLongList("discord.staff-roles")) {
            Role role = guild.getRoleById(roleId);
            if (role != null) {
                channel.upsertPermissionOverride(role)
                        .grant(Permission.VIEW_CHANNEL)
                        .grant(Permission.MESSAGE_SEND)
                        .queue();
            }
        }
    }

    public int getUserReportCount(String userId) {
        return reportCounts.getOrDefault(userId, 0);
    }
}