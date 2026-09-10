package net.firepixel.fun.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DynamicCommand extends Command {

    private final String action;

    public DynamicCommand(String name, String action) {
        super(name);
        this.action = action;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        player.performCommand(action.startsWith("/") ? action.substring(1) : action);

        player.sendMessage("");

        return true;
    }
}