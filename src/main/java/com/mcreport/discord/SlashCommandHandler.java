package com.mcreport.discord;

import com.mcreport.MCReportPlugin;
import com.mcreport.minecraft.BedrockUtils;
import com.mcreport.storage.Storage;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SlashCommandHandler extends ListenerAdapter {

    private final MCReportPlugin plugin;

    public SlashCommandHandler(MCReportPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "report":
                break;
            case "reports":
                handleReportsList(event);
                break;
            case "stats":
                handleStats(event);
                break;
            case "player":
                handlePlayerInfo(event);
                break;
        }
    }

    private void handleReportsList(SlashCommandInteractionEvent event) {
        if (!PermissionUtils.isStaff(event.getMember(), plugin)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Sin Permisos", "Solo el staff puede ver la lista de reportes."))
                    .setEphemeral(true).queue();
            return;
        }

        List<String> pending = plugin.getStorage().getPendingReportsList();
        int pageSize = 10;
        int totalPages = Math.max(1, (int) Math.ceil(pending.size() / (double) pageSize));
        int to = Math.min(pageSize, pending.size());
        event.replyEmbeds(EmbedUtils.createReportListEmbed(pending.subList(0, to), 1, totalPages))
                .setEphemeral(true).queue();
    }

    private void handleStats(SlashCommandInteractionEvent event) {
        if (!PermissionUtils.isStaff(event.getMember(), plugin)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Sin Permisos", "Solo el staff puede ver las estadísticas."))
                    .setEphemeral(true).queue();
            return;
        }

        Storage storage = plugin.getStorage();
        event.replyEmbeds(EmbedUtils.createStatsEmbed(
                storage.getTotalReports(),
                storage.getPendingReports(),
                storage.getResolvedReports(),
                storage.getActionCount("BAN") + storage.getActionCount("TEMPBAN"),
                storage.getActionCount("KICK"),
                storage.getActionCount("WARN"),
                storage.getActionCount("MUTE")
        )).setEphemeral(true).queue();
    }

    private void handlePlayerInfo(SlashCommandInteractionEvent event) {
        if (!PermissionUtils.isStaff(event.getMember(), plugin)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Sin Permisos", "Solo el staff puede ver info de jugadores."))
                    .setEphemeral(true).queue();
            return;
        }

        String playerName = event.getOption("jugador").getAsString();
        if (playerName.length() > 17) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Nombre Inválido", "El nombre no puede superar 17 caracteres."))
                    .setEphemeral(true).queue();
            return;
        }

        Player online = Bukkit.getPlayerExact(playerName);
        if (online == null) {
            String resolved = BedrockUtils.resolveBedrockName(playerName);
            if (resolved != null) {
                online = Bukkit.getPlayerExact(resolved);
                if (online != null) playerName = online.getName();
            }
        }
        boolean isOnline = online != null && online.isOnline();
        int warns = plugin.getStorage().getPlayerWarns(playerName);
        int reports = plugin.getStorage().getPlayerReports(playerName).size();

        String world = "";
        int x = 0, y = 0, z = 0;
        double health = 0;
        int food = 0;

        if (isOnline) {
            world = online.getWorld().getName();
            x = online.getLocation().getBlockX();
            y = online.getLocation().getBlockY();
            z = online.getLocation().getBlockZ();
            health = online.getHealth();
            food = online.getFoodLevel();
        }

        event.replyEmbeds(EmbedUtils.createPlayerInfoEmbed(
                playerName, isOnline, world, x, y, z, health, food, warns, reports
        )).setEphemeral(true).queue();
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("player")) return;

        String typed = event.getFocusedOption().getValue().toLowerCase();
        List<Command.Choice> choices = new ArrayList<>();
        int count = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (count >= 15) break;
            String name = player.getName();
            String plain = BedrockUtils.stripPrefix(name);
            if (name.toLowerCase().startsWith(typed) || plain.toLowerCase().startsWith(typed)) {
                choices.add(new Command.Choice(name, name));
                count++;
            }
        }

        if (count < 15) {
            for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                if (count >= 15) break;
                String name = offline.getName();
                if (name != null && !name.isEmpty() && name.toLowerCase().startsWith(typed)
                        && choices.stream().noneMatch(c -> c.getName().equalsIgnoreCase(name))) {
                    choices.add(new Command.Choice(name, name));
                    count++;
                }
            }
        }

        event.replyChoices(choices).queue();
    }
}