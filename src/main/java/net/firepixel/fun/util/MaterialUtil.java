package net.firepixel.fun.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public final class MaterialUtil {

    private static final boolean LEGACY;

    static {
        String version = "";
        try {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            String[] parts = name.split("\\.");
            if (parts.length > 3) {
                version = parts[3];
            }
        } catch (Throwable ignored) {
        }
        LEGACY = version.startsWith("v1_8") || version.startsWith("v1_9") || version.startsWith("v1_10") || version.startsWith("v1_11") || version.startsWith("v1_12");
    }

    private MaterialUtil() {
    }

    public static boolean isLegacy() {
        return LEGACY;
    }

    public static Material getSkullMaterial() {
        Material material = Material.getMaterial(LEGACY ? "SKULL_ITEM" : "PLAYER_HEAD");

        if (material != null) {
            return material;
        }

        material = Material.getMaterial("SKULL_ITEM");

        if (material != null) {
            return material;
        }

        material = Material.getMaterial("PLAYER_HEAD");

        return material == null ? Material.STONE : material;
    }

    public static ItemStack createSkull() {
        return LEGACY ? new ItemStack(getSkullMaterial(), 1, (short) 3) : new ItemStack(getSkullMaterial(), 1);
    }

    public static void applyTexture(SkullMeta meta, String texture) {
        if (meta == null || texture == null || texture.trim().isEmpty()) {
            return;
        }

        String value = texture.trim();

        try {
            if (value.regionMatches(true, 0, "owner:", 0, 6) || value.regionMatches(true, 0, "player:", 0, 7) || value.regionMatches(true, 0, "skull:", 0, 6)) {
                String owner = value.substring(value.indexOf(':') + 1).trim();

                if (!owner.isEmpty()) {
                    meta.setOwner(owner);
                }

                return;
            }

            if (value.startsWith("http://") || value.startsWith("https://")) {
                String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + value + "\"}}}";
                value = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            }

            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            Object profile = gameProfileClass.getConstructor(UUID.class, String.class).newInstance(UUID.randomUUID(), null);
            Object property = propertyClass.getConstructor(String.class, String.class).newInstance("textures", value);
            Object properties = gameProfileClass.getMethod("getProperties").invoke(profile);
            properties.getClass().getMethod("put", Object.class, Object.class).invoke(properties, "textures", property);

            Field field = null;
            Class<?> type = meta.getClass();

            while (type != null && field == null) {
                for (String fieldName : new String[] {"profile", "gameProfile"}) {
                    try {
                        field = type.getDeclaredField(fieldName);
                        break;
                    } catch (NoSuchFieldException ignored) {
                    }
                }
                type = type.getSuperclass();
            }

            if (field != null) {
                field.setAccessible(true);
                field.set(meta, profile);
            }
        } catch (Throwable ignored) {
        }
    }
}