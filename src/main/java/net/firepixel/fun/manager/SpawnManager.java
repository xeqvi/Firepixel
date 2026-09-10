package net.firepixel.fun.manager;

import net.firepixel.fun.Firepixel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

public class SpawnManager {

    private final Firepixel plugin;

    public SpawnManager(Firepixel plugin) {
        this.plugin = plugin;
    }

    public Location getSpawn() {
        String worldName = plugin.getConfig().getString("spawn.world");

        if (worldName == null) {
            return null;
        }

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            return null;
        }

        double x = plugin.getConfig().getDouble("spawn.x");
        double y = plugin.getConfig().getDouble("spawn.y");
        double z = plugin.getConfig().getDouble("spawn.z");
        float yaw = (float) plugin.getConfig().getDouble("spawn.yaw");
        float pitch = (float) plugin.getConfig().getDouble("spawn.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setSpawn(Location location) {
        plugin.getConfig().set("spawn.world", location.getWorld().getName());
        plugin.getConfig().set("spawn.x", location.getX());
        plugin.getConfig().set("spawn.y", location.getY());
        plugin.getConfig().set("spawn.z", location.getZ());
        plugin.getConfig().set("spawn.yaw", (double) location.getYaw());
        plugin.getConfig().set("spawn.pitch", (double) location.getPitch());
        plugin.saveConfig();
    }

    public boolean isEnabledForWorld(String worldName) {
        List<String> worlds = plugin.getConfig().getStringList("spawn.enabled_spawn_worlds");

        if (worldName == null) {
            return false;
        }

        for (String entry : worlds) {
            if (entry.equals("*") || entry.equalsIgnoreCase(worldName)) {
                return true;
            }
        }

        return false;
    }
}