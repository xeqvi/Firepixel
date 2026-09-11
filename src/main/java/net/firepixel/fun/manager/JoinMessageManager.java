package net.firepixel.fun.manager;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.util.RankUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class JoinMessageManager {

    private static final String[] RANK_PRIORITY = new String[] {
            "owner", "coowner", "founder", "mainadmin", "admin", "mod", "gm", "helper",
            "jrhelper", "youtube", "mvpplusplus", "mvpplus", "mvp", "vipplus", "vip"
    };

    private final Firepixel plugin;

    public JoinMessageManager(Firepixel plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("join-message.enabled", true);
    }

    public void handleJoin(Player player) {
        if (!isEnabled() || player == null) {
            return;
        }

        String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());
        String message = getRankMessage(player, language);

        if (message == null || message.trim().isEmpty()) {
            message = plugin.getConfig().getString("join-message.default", "");
        }

        if (message == null || message.trim().isEmpty()) {
            return;
        }

        String click = plugin.getConfig().getString("join-message.click_command", "/viewprofile %player%");
        click = click == null ? "" : click.replace("%player%", player.getName()).replace("{player}", player.getName());

        BaseComponent[] parts = TextComponent.fromLegacyText(resolve(player, message));
        TextComponent base = new TextComponent("");

        for (BaseComponent component : parts) {
            base.addExtra(component);
        }

        String hover = buildHover(player, language);

        if (hover != null && !hover.isEmpty()) {
            base.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        }

        if (!click.isEmpty()) {
            base.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, click));
        }

        for (Player recipient : plugin.getServer().getOnlinePlayers()) {
            recipient.spigot().sendMessage(base);
        }
    }

    private String getRankMessage(Player player, String language) {
        for (String key : RANK_PRIORITY) {
            String permission = plugin.getLanguageManager().getMessage(language, "join-message.ranks." + key + ".permission");
            String message = plugin.getLanguageManager().getMessage(language, "join-message.ranks." + key + ".message");

            if (permission == null || permission.trim().isEmpty() || message == null || message.trim().isEmpty()) {
                continue;
            }

            if (player.hasPermission(permission)) {
                return message;
            }
        }

        return "";
    }

    private String buildHover(Player player, String language) {
        List<String> lines = plugin.getLanguageManager().getStringList(language, "join-message.hover_text");

        if (lines.isEmpty()) {
            return "";
        }

        List<String> rendered = new ArrayList<String>();

        for (String line : lines) {
            rendered.add(resolve(player, line));
        }

        return String.join("\n", rendered);
    }

    private String resolve(Player player, String text) {
        if (text == null) {
            return "";
        }

        String rank = RankUtil.getTag(player);
        String result = text.replace("%player%", player.getName()).replace("{player}", player.getName());
        result = result.replace("%rank%", rank).replace("{rank}", rank);
        result = result.replace("%prefix%", rank).replace("{prefix}", rank);
        return ChatColor.translateAlternateColorCodes('&', result);
    }
}