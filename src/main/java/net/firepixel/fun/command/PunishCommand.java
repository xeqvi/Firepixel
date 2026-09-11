package net.firepixel.fun.command;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.manager.PunishmentManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PunishCommand implements CommandExecutor {

    private final Firepixel plugin;
    private final PunishmentManager.PunishmentType type;

    public PunishCommand(Firepixel plugin, PunishmentManager.PunishmentType type) {
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

        boolean silent = args.length > 0 && args[0].equalsIgnoreCase("-s");
        int start = silent ? 1 : 0;

        if (args.length < start + 2) {
            sender.sendMessage(manager.getMessage("invalid-duration"));
            return true;
        }

        String playerName = args[start];
        long duration = 0;
        int reasonIndex = start + 1;

        if (type == PunishmentManager.PunishmentType.TEMPBAN || type == PunishmentManager.PunishmentType.TEMPMUTE) {
            if (args.length < start + 3) {
                sender.sendMessage(manager.getMessage("invalid-duration"));
                return true;
            }

            duration = manager.parseDuration(args[start + 1]);

            if (duration <= 0) {
                sender.sendMessage(manager.getMessage("invalid-duration"));
                return true;
            }

            reasonIndex = start + 2;
        }

        StringBuilder reasonBuilder = new StringBuilder();

        for (int i = reasonIndex; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }

        String reason = reasonBuilder.toString().trim();

        if (reason.isEmpty()) {
            reason = "No reason provided";
        }

        String lower = type.name().toLowerCase();

        if ((type == PunishmentManager.PunishmentType.BAN || type == PunishmentManager.PunishmentType.TEMPBAN) && manager.isPlayerBanned(playerName)) {
            sender.sendMessage(manager.getMessage("already-banned").replace("%player%", playerName));
            return true;
        }

        if ((type == PunishmentManager.PunishmentType.MUTE || type == PunishmentManager.PunishmentType.TEMPMUTE) && manager.isPlayerMuted(playerName)) {
            sender.sendMessage(manager.getMessage("already-muted").replace("%player%", playerName));
            return true;
        }

        if (!manager.addPunishment(playerName, type, reason, sender.getName(), duration)) {
            return true;
        }

        String successKey = lower + "d";

        if (type == PunishmentManager.PunishmentType.TEMPBAN) {
            successKey = "temp-banned";
        } else if (type == PunishmentManager.PunishmentType.TEMPMUTE) {
            successKey = "temp-muted";
        }

        String success = manager.getMessage(successKey);
        success = success.replace("%player%", playerName).replace("%duration%", manager.formatDuration(duration));
        sender.sendMessage(success);

        String broadcastKey = lower + "-broadcast";

        if (type == PunishmentManager.PunishmentType.TEMPBAN) {
            broadcastKey = "temp-banned-broadcast";
        } else if (type == PunishmentManager.PunishmentType.TEMPMUTE) {
            broadcastKey = "temp-muted-broadcast";
        }

        String broadcast = manager.getMessage(broadcastKey);
        broadcast = broadcast.replace("%player%", playerName).replace("%staff%", sender.getName()).replace("%reason%", reason).replace("%duration%", manager.formatDuration(duration));
        manager.broadcast(broadcast, silent);

        manager.applyPunishment(playerName, type, reason, duration);

        return true;
    }
}