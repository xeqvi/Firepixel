package net.firepixel.fun.command;

import net.firepixel.fun.Firepixel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScoreboardCommand implements CommandExecutor {

    private final Firepixel plugin;

    public ScoreboardCommand(Firepixel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());
        boolean enabled = plugin.getScoreboardManager().toggle(player);
        player.sendMessage(plugin.getLanguageManager().getMessage(language, enabled ? "scoreboard.enabled" : "scoreboard.disabled"));

        return true;
    }
}