package net.firepixel.fun.manager;

import net.firepixel.fun.Firepixel;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LanguageManager {

    private final Firepixel plugin;
    private final Map<String, YamlConfiguration> languages = new HashMap<String, YamlConfiguration>();

    public LanguageManager(Firepixel plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        languages.clear();

        File folder = new File(plugin.getDataFolder(), "languages");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        plugin.saveResource("languages/messages_en.yml", false);

        File[] files = folder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            String fileName = file.getName();

            if (!fileName.startsWith("messages_") || !fileName.endsWith(".yml")) {
                continue;
            }

            String code = fileName.substring("messages_".length(), fileName.length() - ".yml".length()).toLowerCase(Locale.ROOT);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            mergeDefaults(config, fileName, file);
            languages.put(code, config);
        }
    }

    private void mergeDefaults(YamlConfiguration target, String fileName, File file) {
        try (InputStream stream = plugin.getResource("languages/" + fileName)) {
            if (stream == null) {
                return;
            }

            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));

            for (String key : defaults.getKeys(true)) {
                if (!defaults.isConfigurationSection(key) && !target.contains(key)) {
                    target.set(key, defaults.get(key));
                }
            }

            target.save(file);
        } catch (Exception ignored) {
        }
    }

    public String getDefaultLanguage() {
        String configured = plugin.getConfig().getString("language.default-language", "en");
        String resolved = resolveLanguage(configured);

        if (resolved != null) {
            return resolved;
        }

        return "en";
    }

    public String resolveLanguage(String input) {
        if (input == null) {
            return null;
        }

        String normalized = input.trim().toLowerCase(Locale.ROOT);

        if (languages.containsKey(normalized)) {
            return normalized;
        }

        return null;
    }

    public boolean isSupported(String language) {
        return language != null && languages.containsKey(language.toLowerCase(Locale.ROOT));
    }

    public List<String> getSupportedLanguages() {
        return new ArrayList<String>(languages.keySet());
    }

    public String getLanguageName(String language) {
        String key = resolveLanguage(language);

        if (key == null) {
            key = getDefaultLanguage();
        }

        YamlConfiguration config = languages.get(key);

        if (config == null) {
            return key;
        }

        return config.getString("language_name", key);
    }

    public String getMessage(String language, String path) {
        String key = resolveLanguage(language);

        if (key == null) {
            key = getDefaultLanguage();
        }

        YamlConfiguration config = languages.get(key);

        if (config != null) {
            String value = config.getString(path);

            if (value != null) {
                return ChatColor.translateAlternateColorCodes('&', value);
            }
        }

        YamlConfiguration fallback = languages.get(getDefaultLanguage());

        if (fallback != null) {
            String value = fallback.getString(path);

            if (value != null) {
                return ChatColor.translateAlternateColorCodes('&', value);
            }
        }

        return "";
    }

    public boolean isEnabledForWorld(String worldName) {
        List<String> disabled = plugin.getConfig().getStringList("language.disabled_language_worlds");

        if (worldName == null) {
            return true;
        }

        for (String entry : disabled) {
            if (entry.equals("*") || entry.equalsIgnoreCase(worldName)) {
                return false;
            }
        }

        return true;
    }
}