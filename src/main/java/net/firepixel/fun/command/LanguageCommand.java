package net.firepixel.fun.command;

import net.firepixel.fun.Firepixel;
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
        String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());

        if (!plugin.getLanguageManager().isEnabledForWorld(player.getWorld().getName())) {
            player.sendMessage(plugin.getLanguageManager().getMessage(language, "language.world-disabled"));
            return true;
        }

        plugin.getLanguageMenuManager().open(player);

        return true;
    }
}