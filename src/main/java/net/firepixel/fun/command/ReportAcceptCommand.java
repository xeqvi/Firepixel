package net.firepixel.fun.command;

import net.firepixel.fun.Firepixel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReportAcceptCommand implements CommandExecutor {

    private final Firepixel plugin;

    public ReportAcceptCommand(Firepixel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String language = plugin.getLanguageManager().getDefaultLanguage();

        if (!sender.hasPermission(plugin.getConfig().getString("report.staff-permission", "firepixel.report.staff"))) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(language, "report.no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(language, "report.usage"));
            return true;
        }

        if (plugin.getReportManager().accept(args[0])) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(language, "report.accepted").replace("%reported%", args[0]));
        } else {
            sender.sendMessage(plugin.getLanguageManager().getMessage(language, "report.no-players"));
        }

        return true;
    }
}