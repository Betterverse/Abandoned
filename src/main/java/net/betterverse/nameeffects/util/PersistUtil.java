package net.betterverse.nameeffects.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.betterverse.nameeffects.objects.AliasPlayer;
import org.bukkit.configuration.file.YamlConfiguration;

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

    public static boolean save() {
        try {
            config.save(file);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static void saveAliasPlayers(Map<String, AliasPlayer> map) {
        if (map == null) {
            return;
        }

        for (Map.Entry<String, AliasPlayer> values : map.entrySet()) {
            config.set(values.getKey(), values.getValue());
        }

        save();
    }

    public static Map<String, AliasPlayer> getAliasPlayers() {
        Map<String, AliasPlayer> map = new HashMap<String, AliasPlayer>();
        for (String values : config.getKeys(false)) {
            AliasPlayer player = (AliasPlayer) config.get(values);
            player.setName(values);
            map.put(values, player);
        }
        return map;
    }
}
