package com.mcreport.discord;

import com.mcreport.MCReportPlugin;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.List;

public class DiscordBot {

    private final MCReportPlugin plugin;
    private final String token;
    private JDA jda;

    public DiscordBot(MCReportPlugin plugin, String token) {
        this.plugin = plugin;
        this.token = token;
    }

    public void start() {
        try {
            jda = JDABuilder.createDefault(token)
                    .setStatus(OnlineStatus.ONLINE)
                    .setActivity(Activity.playing("vigilando el servidor"))
                    .setAllowedMentions(List.of())
                    .enableIntents(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER)
                    .addEventListeners(
                            new ReportModal(plugin),
                            new ReportListener(plugin),
                            new AppealListener(plugin),
                            new SlashCommandHandler(plugin)
                    )
                    .build();
            jda.awaitReady();
            registerCommands();
            validateSetup();
            sendReportEmbed();
            sendAppealEmbed();
            plugin.getLogger().info("Bot de Discord conectado correctamente!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            plugin.getLogger().severe("El bot de Discord no pudo iniciarse: " + e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().severe("Error crítico al conectar el bot: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateSetup() {
        String guildId = plugin.getConfig().getString("discord.guild-id", "");
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            plugin.getLogger().severe("NO se encontró el servidor con guild-id='" + guildId + "'. " +
                    "¿El bot está invitado a ese servidor? Los comandos se registrarán globalmente.");
            return;
        }

        Member self = guild.getSelfMember();
        boolean canCreate = self.hasPermission(Permission.MANAGE_CHANNEL);
        boolean canOverride = self.hasPermission(Permission.MANAGE_ROLES);
        if (!canCreate) {
            plugin.getLogger().severe("El bot NO tiene el permiso 'Gestionar canales'. " +
                    "Sin él NO se podrán crear los tickets. Asígnalo al rol del bot.");
        }
        if (!canOverride) {
            plugin.getLogger().severe("El bot NO tiene el permiso 'Gestionar roles'. " +
                    "Se necesita para los overrides de permisos de los tickets.");
        }

        checkCategory(guild, plugin.getConfig().getString("discord.report-ticket-category", ""), "report-ticket-category");
        String appealCat = plugin.getConfig().getString("discord.appeal-ticket-category", "");
        if (appealCat.isEmpty()) {
            appealCat = plugin.getConfig().getString("discord.report-ticket-category", "");
        }
        checkCategory(guild, appealCat, "appeal-ticket-category");

        checkTextChannel(plugin.getConfig().getString("discord.report-embed-channel", ""), "report-embed-channel");
        checkTextChannel(plugin.getConfig().getString("discord.appeal-channel", ""), "appeal-channel");
    }

    private void checkCategory(Guild guild, String id, String configKey) {
        if (id == null || id.isEmpty()) {
            plugin.getLogger().warning("Config '" + configKey + "' está vacía. No se podrán crear tickets.");
            return;
        }
        Category category = guild.getCategoryById(id);
        if (category == null) {
            plugin.getLogger().severe("La categoría de '" + configKey + "' (ID " + id + ") NO existe " +
                    "o el bot no puede verla. Los tickets no se podrán crear ahí.");
        } else {
            plugin.getLogger().info("Categoría de '" + configKey + "' OK: " + category.getName());
        }
    }

    private void checkTextChannel(String id, String configKey) {
        if (id == null || id.isEmpty()) {
            plugin.getLogger().warning("Config '" + configKey + "' está vacía.");
            return;
        }
        if (jda.getTextChannelById(id) == null) {
            plugin.getLogger().severe("El canal de '" + configKey + "' (ID " + id + ") NO existe " +
                    "o el bot no puede verlo.");
        }
    }

    private void registerCommands() {
        var reportCommand = Commands.slash("report", "Reportar a un jugador")
                .addOption(OptionType.STRING, "jugador", "Nombre del jugador a reportar", true)
                .addOption(OptionType.STRING, "razon", "Razón del reporte", true)
                .addOption(OptionType.STRING, "descripcion", "Descripción del incidente", false)
                .addOption(OptionType.STRING, "pruebas", "Links de evidencia", false);

        var reportsCommand = Commands.slash("reports", "Ver la lista de reportes pendientes");

        var statsCommand = Commands.slash("stats", "Ver estadísticas del sistema de reportes");

        var playerCommand = Commands.slash("player", "Ver información de un jugador")
                .addOption(OptionType.STRING, "jugador", "Nombre del jugador", true, true);

        Guild guild = jda.getGuildById(plugin.getConfig().getString("discord.guild-id", ""));
        if (guild == null) {
            plugin.getLogger().warning("No se encontró el servidor de Discord. Registrando comandos globalmente...");
            jda.updateCommands()
                    .addCommands(reportCommand, reportsCommand, statsCommand, playerCommand)
                    .queue(s -> plugin.getLogger().info("Comandos globales registrados correctamente."),
                            e -> plugin.getLogger().warning("Error al registrar comandos: " + e.getMessage()));
            return;
        }
        guild.updateCommands()
                .addCommands(reportCommand, reportsCommand, statsCommand, playerCommand)
                .queue(s -> plugin.getLogger().info("Comandos registrados en el servidor correctamente."),
                        e -> plugin.getLogger().warning("Error al registrar comandos: " + e.getMessage()));
    }

    public void sendReportEmbed() {
        String channelId = plugin.getConfig().getString("discord.report-embed-channel", "");
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            plugin.getLogger().severe("No se encontró el canal de reportes: " + channelId);
            return;
        }

        Button reportButton = Button.primary("mcreport:report", Emoji.fromFormatted("📋")).withLabel("Reportar Jugador");
        sendPermaEmbed(channel, EmbedUtils.createReportButtonEmbed(plugin), reportButton, "reportes");
    }

    public void sendAppealEmbed() {
        String channelId = plugin.getConfig().getString("discord.appeal-channel", "");
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            plugin.getLogger().severe("No se encontró el canal de apelaciones: " + channelId);
            return;
        }

        Button appealButton = Button.primary("mcreport:appeal", Emoji.fromFormatted("📨")).withLabel("Apelar Ban");
        sendPermaEmbed(channel, EmbedUtils.createAppealButtonEmbed(plugin), appealButton, "apelaciones");
    }

    private void sendPermaEmbed(TextChannel channel, MessageEmbed embed,
                                Button button, String label) {
        channel.retrieveMessageById(channel.getLatestMessageIdLong())
                .queue(message -> {
                    if (message.getAuthor().getId().equals(jda.getSelfUser().getId())) {
                        plugin.getLogger().info("Ya existe un embed de " + label + " en el canal, no se duplica.");
                    } else {
                        channel.sendMessageEmbeds(embed)
                                .setAllowedMentions(List.of())
                                .setActionRow(button)
                                .queue();
                    }
                }, failure -> {
                    channel.sendMessageEmbeds(embed)
                            .setAllowedMentions(List.of())
                            .setActionRow(button)
                            .queue();
                });
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            try {
                jda.awaitShutdown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public JDA getJda() {
        return jda;
    }

    public MCReportPlugin getPlugin() {
        return plugin;
    }
}