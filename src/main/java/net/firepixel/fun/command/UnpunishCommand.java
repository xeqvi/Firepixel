package net.firepixel.fun.command;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.manager.PunishmentManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UnpunishCommand implements CommandExecutor {

    private final Firepixel plugin;
    private final PunishmentManager.PunishmentType type;

    public UnpunishCommand(Firepixel plugin, PunishmentManager.PunishmentType type) {
        this.plugin = plugin;
        this.type = type;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PunishmentManager manager = plugin.getPunishmentManager();

        if (!sender.hasPermission("firepixel.punishment." + type.name().toLowerCase())) {
            sender.sendMessage(manager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(manager.getMessage("invalid-duration"));
            return true;
        }

        String playerName = args[0];
        boolean banned = type == PunishmentManager.PunishmentType.BAN;

        if (banned && !manager.isPlayerBanned(playerName)) {
            sender.sendMessage(manager.getMessage("not-banned").replace("%player%", playerName));
            return true;
        }

        if (!banned && !manager.isPlayerMuted(playerName)) {
            sender.sendMessage(manager.getMessage("not-muted").replace("%player%", playerName));
            return true;
        }

        manager.removePunishment(playerName, type.name());

        String key = banned ? "unbanned" : "unmuted";
        String broadcastKey = banned ? "unbanned-broadcast" : "unmuted-broadcast";

        sender.sendMessage(manager.getMessage(key).replace("%player%", playerName));

        String broadcast = manager.getMessage(broadcastKey);
        broadcast = broadcast.replace("%player%", playerName).replace("%staff%", sender.getName());
        manager.broadcast(broadcast, false);

        return true;
    }
}