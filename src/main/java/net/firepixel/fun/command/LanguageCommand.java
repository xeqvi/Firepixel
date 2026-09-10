package net.firepixel.fun.command;

import net.firepixel.fun.Firepixel;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LanguageCommand implements CommandExecutor {

    private final Firepixel plugin;

    public LanguageCommand(Firepixel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        String current = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());

        if (!plugin.getLanguageManager().isEnabledForWorld(player.getWorld().getName())) {
            player.sendMessage(plugin.getLanguageManager().getMessage(current, "language.world-disabled"));
            return true;
        }

        if (args.length == 0) {
            plugin.getLanguageMenuManager().open(player);
            return true;
        }

        String language = plugin.getLanguageManager().resolveLanguage(args[0]);

        if (language == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(current, "language.invalid"));
            return true;
        }

        plugin.getPlayerDataManager().setLanguage(player.getUniqueId(), language);
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);

        String message = plugin.getLanguageManager().getMessage(language, "language.changed");
        message = message.replace("%language%", plugin.getLanguageManager().getLanguageName(language));
        player.sendMessage(message);

        return true;
    }
}