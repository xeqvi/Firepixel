package net.firepixel.fun.command;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.manager.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class RankCommand implements CommandExecutor, TabCompleter {

    private final Firepixel plugin;

    public RankCommand(Firepixel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String language = plugin.getLanguageManager().getDefaultLanguage();

        if (!sender.hasPermission("firepixel.rank") && !sender.hasPermission("firepixel.rank.manage") && !sender.isOp()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(language, "rank.no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(language, "rank.usage"));
            return true;
        }

        RankManager rankManager = plugin.getRankManager();

        if (rankManager == null) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(language, "rank.no-rank-manager"));
            return true;
        }

        String playerName = args[0];
        String requestedRank = args[1].toLowerCase(Locale.ROOT);
        OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);

        if (offline == null || (!offline.isOnline() && !offline.hasPlayedBefore())) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(language, "rank.player-not-found"));
            return true;
        }

        RankManager.RankDefinition definition = rankManager.getRank(requestedRank);

        if (definition == null || (!definition.matches(requestedRank) && !"default".equalsIgnoreCase(requestedRank))) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(language, "rank.group-not-found"));
            return true;
        }

        String rankId = definition.getId();
        String primaryGroup = definition.getPrimaryGroup();

        applyLuckPermsGroup(offline.getUniqueId(), primaryGroup);

        String name = offline.getName() == null ? playerName : offline.getName();

        String success = plugin.getLanguageManager().getMessage(language, "rank.success");
        success = success.replace("%player%", name).replace("%group%", rankId);
        sender.sendMessage(success);

        if (offline.isOnline()) {
            Player online = (Player) offline;
            String assigned = plugin.getLanguageManager().getMessage(language, "rank.assigned");
            assigned = assigned.replace("%group%", rankId);
            online.sendMessage(assigned);
            plugin.getScoreboardManager().refreshPlayer(online);
        }

        return true;
    }

    private boolean applyLuckPermsGroup(java.util.UUID uuid, String groupName) {
        try {
            Object luckPerms = Class.forName("net.luckperms.api.LuckPermsProvider").getMethod("get").invoke(null);

            if (luckPerms == null) {
                return false;
            }

            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object loaded = userManager.getClass().getMethod("loadUser", java.util.UUID.class).invoke(userManager, uuid);
            Object user = loaded instanceof CompletableFuture ? ((CompletableFuture<?>) loaded).join() : loaded;

            if (user == null) {
                return false;
            }

            Object data = user.getClass().getMethod("data").invoke(user);
            final Object inheritanceType = Class.forName("net.luckperms.api.node.NodeType").getField("INHERITANCE").get(null);
            final Class<?> nodeClass = Class.forName("net.luckperms.api.node.Node");
            Predicate<Object> predicate = new Predicate<Object>() {
                public boolean test(Object node) {
                    try {
                        Method matches = inheritanceType.getClass().getMethod("matches", nodeClass);
                        Object value = matches.invoke(inheritanceType, node);
                        return value instanceof Boolean ? ((Boolean) value).booleanValue() : false;
                    } catch (Throwable ignored) {
                        return false;
                    }
                }
            };

            try {
                Method clear = data.getClass().getMethod("clear", Predicate.class);
                clear.invoke(data, predicate);
            } catch (Throwable ignored) {
            }

            Object builder = Class.forName("net.luckperms.api.node.types.InheritanceNode").getMethod("builder", String.class).invoke(null, groupName);
            Object node = builder.getClass().getMethod("build").invoke(builder);
            data.getClass().getMethod("add", nodeClass).invoke(data, node);

            try {
                Method primary = user.getClass().getMethod("setPrimaryGroup", String.class);
                primary.invoke(user, groupName);
            } catch (Throwable ignored) {
            }

            userManager.getClass().getMethod("saveUser", Class.forName("net.luckperms.api.model.user.User")).invoke(userManager, user);
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("firepixel.rank") && !sender.hasPermission("firepixel.rank.manage") && !sender.isOp()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> completions = new ArrayList<String>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    completions.add(player.getName());
                }
            }

            return completions;
        }

        if (args.length == 2) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            List<String> completions = new ArrayList<String>();
            RankManager rankManager = plugin.getRankManager();

            if (rankManager != null) {
                for (RankManager.RankDefinition definition : rankManager.getSortedRanks()) {
                    if (definition.getId().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                        completions.add(definition.getId());
                    }
                }
            }

            return completions;
        }

        return Collections.emptyList();
    }
}