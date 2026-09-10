package net.firepixel.fun.command;

import net.firepixel.fun.Firepixel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLanguageCommand implements CommandExecutor {

    private final Firepixel plugin;

    public SetLanguageCommand(Firepixel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String senderLanguage = sender instanceof Player
                ? plugin.getPlayerDataManager().getLanguage(((Player) sender).getUniqueId())
                : plugin.getLanguageManager().getDefaultLanguage();

        if (args.length == 0) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(senderLanguage, "language.invalid"));
            return true;
        }

        String language = plugin.getLanguageManager().resolveLanguage(args[0]);

        if (language == null) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(senderLanguage, "language.invalid"));
            return true;
        }

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getLanguageManager().getMessage(senderLanguage, "language.player-only"));
                return true;
            }

            Player player = (Player) sender;

            if (!plugin.getLanguageManager().isEnabledForWorld(player.getWorld().getName())) {
                player.sendMessage(plugin.getLanguageManager().getMessage(senderLanguage, "language.world-disabled"));
                return true;
            }

            plugin.getPlayerDataManager().setLanguage(player.getUniqueId(), language);
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);

            String message = plugin.getLanguageManager().getMessage(language, "language.changed");
            message = message.replace("%language%", plugin.getLanguageManager().getLanguageName(language));
            player.sendMessage(message);

            return true;
        }

        if (!sender.hasPermission("firepixel.setlanguage")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(senderLanguage, "spawn.no-permission"));
            return true;
        }

        Player onlineTarget = Bukkit.getPlayerExact(args[1]);

        if (onlineTarget != null) {
            plugin.getPlayerDataManager().setLanguage(onlineTarget.getUniqueId(), language);
            onlineTarget.playSound(onlineTarget.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);

            String targetMessage = plugin.getLanguageManager().getMessage(language, "language.changed");
            targetMessage = targetMessage.replace("%language%", plugin.getLanguageManager().getLanguageName(language));
            onlineTarget.sendMessage(targetMessage);

            String senderMessage = plugin.getLanguageManager().getMessage(senderLanguage, "language.changed");
            senderMessage = senderMessage.replace("%language%", plugin.getLanguageManager().getLanguageName(language));
            sender.sendMessage(senderMessage);

            return true;
        }

        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(args[1]);

        if (offlineTarget == null || !offlineTarget.hasPlayedBefore()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(senderLanguage, "language.player-not-found"));
            return true;
        }

        plugin.getPlayerDataManager().setLanguage(offlineTarget.getUniqueId(), language);

        String senderMessage = plugin.getLanguageManager().getMessage(senderLanguage, "language.changed");
        senderMessage = senderMessage.replace("%language%", plugin.getLanguageManager().getLanguageName(language));
        sender.sendMessage(senderMessage);

        return true;
    }
}