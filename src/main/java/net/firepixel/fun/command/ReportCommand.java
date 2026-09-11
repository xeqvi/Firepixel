package net.firepixel.fun.command;

import net.firepixel.fun.Firepixel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportCommand implements CommandExecutor {

    private final Firepixel plugin;

    public ReportCommand(Firepixel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(plugin.getLanguageManager().getDefaultLanguage(), "report.player-only"));
            return true;
        }

        Player player = (Player) sender;
        plugin.getReportManager().openReasonMenu(player);

        return true;
    }
}