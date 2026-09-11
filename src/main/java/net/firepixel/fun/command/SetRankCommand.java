package net.firepixel.fun.command;

import net.firepixel.fun.Firepixel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetRankCommand implements CommandExecutor {

    private final Firepixel plugin;
    private final RankCommand delegate;

    public SetRankCommand(Firepixel plugin) {
        this.plugin = plugin;
        this.delegate = new RankCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return delegate.onCommand(sender, command, label, args);
    }
}