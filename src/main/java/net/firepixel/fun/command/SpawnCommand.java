package net.firepixel.fun.command;

import net.firepixel.fun.Firepixel;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    private final Firepixel plugin;

    public SpawnCommand(Firepixel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());
        Location spawn = plugin.getSpawnManager().getSpawn();

        if (spawn == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(language, "spawn.not-set"));
            return true;
        }

        player.teleport(spawn);
        player.sendMessage(plugin.getLanguageManager().getMessage(language, "spawn.teleported"));

        return true;
    }
}