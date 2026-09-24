package com.mcreport.discord;

import com.mcreport.MCReportPlugin;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public final class PermissionUtils {

    private PermissionUtils() {
    }

    public static boolean isStaff(Member member, MCReportPlugin plugin) {
        if (member == null) return false;
        if (member.isOwner()) return true;

        List<Long> staffRoleIds = plugin.getConfig().getLongList("discord.staff-roles");
        for (Long roleId : staffRoleIds) {
            if (member.getRoles().stream().anyMatch(role -> role.getIdLong() == roleId)) {
                return true;
            }
        }

        String adminRoleId = plugin.getConfig().getString("discord.admin-role-id", "");
        if (!adminRoleId.isEmpty()) {
            return member.getRoles().stream().anyMatch(role -> role.getId().equals(adminRoleId));
        }

        return member.hasPermission(Permission.ADMINISTRATOR);
    }
}