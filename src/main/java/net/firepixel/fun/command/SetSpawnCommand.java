package net.firepixel.fun.command;

import net.firepixel.fun.Firepixel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {

    private final Firepixel plugin;

    public SetSpawnCommand(Firepixel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());

        if (!player.hasPermission("firepixel.setspawn")) {
            player.sendMessage(plugin.getLanguageManager().getMessage(language, "spawn.no-permission"));
            return true;
        }

        plugin.getSpawnManager().setSpawn(player.getLocation());
        player.sendMessage(plugin.getLanguageManager().getMessage(language, "spawn.set"));

        return true;
    }
}