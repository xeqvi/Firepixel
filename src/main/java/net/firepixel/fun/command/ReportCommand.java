package net.firepixel.fun.command;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.util.ColorUtil;
import org.bukkit.Bukkit;
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

        final Player player = (Player) sender;
        String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());

        if (args.length == 0) {
            openLater(player, null);
            return true;
        }

        String target = args[0];

        if (target.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.self")));
            return true;
        }

        Player online = Bukkit.getPlayerExact(target);

        if (online == null) {
            player.sendMessage(ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.player-not-found")));
            return true;
        }

        openLater(player, online.getName());

        return true;
    }

    private void openLater(final Player player, final String target) {
        String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());
        player.sendMessage(ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.wait")));

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                if (!player.isOnline()) {
                    return;
                }

                if (target == null) {
                    plugin.getReportManager().openReasonMenu(player);
                } else {
                    plugin.getReportManager().openReasonMenu(player, target);
                }
            }
        }, 30L);
    }
}