package net.betterverse.nameeffects.util;

import net.betterverse.nameeffects.NameEffects;
import net.betterverse.nameeffects.objects.AliasPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistUtil {
    static YamlConfiguration config;
    static File file;

    public static void initialize() {
        load();
    }

    public static void load() {
        file = new File("plugins/NameEffects/persist.yml");

        config = YamlConfiguration.loadConfiguration(file);

        config.options().indent(4);
        config.options().header("Persistence Config");
    }

    public static void set(String key, Object obj) {
        config.set(key, obj);
    }

    public static Boolean save() {
        try {
            config.save(file);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static void saveAliasPlayers(Map<String, AliasPlayer> map) {
        if (map == null)
            return;

        for (Map.Entry<String, AliasPlayer> values : map.entrySet()) {
            ArrayList<String> list = new ArrayList<String>();

            list.add(values.getValue().getDisplayName());
            list.add(values.getValue().getPrefix());

            set(values.getKey(), list);
        }

        save();
    }

    public static Map<String, AliasPlayer> getAliasPlayers() {
        Map<String, AliasPlayer> map = new HashMap<String, AliasPlayer>();

        for (Map.Entry<String, Object> values : config.getValues(false).entrySet()) {
            List<String> list = config.getStringList(values.getKey());

            if (list.isEmpty())
                continue;

            map.put(values.getKey(), new AliasPlayer(list.get(0), list.get(1)));
        }

        return map;
    }

    public static void addPlayerToAliases(final NameEffects plugin, final Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                AliasPlayer aplr = plugin.players.get(player.getName());

                String group;

                try {
                    group = plugin.chat.getGroupPrefix(player.getWorld(), plugin.permission.getPrimaryGroup(player)) + player.getName();
                } catch (UnsupportedOperationException ignored) {
                    group = player.getName();
                }

                if (aplr == null) {
                    aplr = new AliasPlayer(group, "");
                    plugin.players.put(player.getName(), aplr);
                } else {
                    aplr.setDisplayName(group);
                }
                String prefix = "[" + aplr.getPrefix() + "]";
                if (aplr.getPrefix().equals("")) {
                    prefix = "";
                }
                player.setDisplayName(prefix + aplr.getDisplayName());
            }
        }, 20L);
    }
}
