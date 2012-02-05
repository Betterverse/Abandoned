package me.desmin88.mobdisguise;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.desmin88.mobdisguise.commands.MDCommand;
import me.desmin88.mobdisguise.listeners.MDEntityListener;
import me.desmin88.mobdisguise.listeners.MDPlayerListener;
import me.desmin88.mobdisguise.utils.DisguiseTask;
import me.desmin88.mobdisguise.utils.PacketUtils;
import net.minecraft.server.DataWatcher;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class MobDisguise extends JavaPlugin {
    public static Set<String> disList = new HashSet<String>();
    public static Set<String> apiList = new HashSet<String>();
    public static Map<String, Byte> playerMobId = new HashMap<String, Byte>();
    // Player -> Datawatcher
    public static Map<String, DataWatcher> data = new HashMap<String, DataWatcher>();

    public static Set<String> baby = new HashSet<String>();
    // Player disguising -> player disguised as
    public static Map<String, String> p2p = new HashMap<String, String>();
    public static Set<String> playerdislist = new HashSet<String>();
    // end
    public static Set<Integer> playerEntIds = new HashSet<Integer>();
    public static PacketUtils pu = new PacketUtils();
    public static Set<String> telelist = new HashSet<String>();
    // public final PacketListener packetlistener = new PacketListener(this);
    public final MDPlayerListener playerlistener = new MDPlayerListener(this);
    public final MDEntityListener entitylistener = new MDEntityListener(this);
    public static final String pref = "[MobDisguise] ";
    public static Configuration cfg;
    public static boolean perm;
    public static PluginDescriptionFile pdf;

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        System.out.println("[" + pdf.getName() + "]" + " by " + pdf.getAuthors().get(0) + " version " + pdf.getVersion() + " disabled.");

    }

    @Override
    public void onEnable() {
        pdf = this.getDescription();
        // Begin config code
        if (!new File(getDataFolder(), "config.yml").exists()) {
            try {
                getDataFolder().mkdir();
                new File(getDataFolder(), "config.yml").createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(pref + "Error making config.yml?!");
                getServer().getPluginManager().disablePlugin(this); // Cleanup
                return;
            }
        }
        cfg = this.getConfiguration(); // Get config

        if (cfg.getKeys().isEmpty()) { // Config hasn't been made
            System.out.println(pref + "config.yml not found, making with default values");
            cfg.setProperty("RealDrops.enabled", false);
            cfg.setProperty("Permissions.enabled", true);
            cfg.setProperty("MobTarget.enabled", true);
            cfg.setProperty("DisableItemPickup", true);
            for (String mobtype : MobType.types) {
                cfg.setHeader("#Setting a mobtype to false will not allow a player to disguise as that type");
                cfg.setProperty("Blacklist." + mobtype, true); // Just making
            }
            cfg.save();
        }
        if (cfg.getProperty("MobTarget.enabled") == null || cfg.getProperty("DisableItemPickup.enabled") == null) {
            cfg.setProperty("MobTarget.enabled", true);
            cfg.setProperty("DisableItemPickup.enabled", true);
            cfg.save();
        }
        if (cfg.getProperty("Blacklist.enderman") == null) {
            cfg.setProperty("Blacklist.enderman", true);
            cfg.setProperty("Blacklist.silverfish", true);
            cfg.setProperty("Blacklist.cavespider", true);
        }

        cfg.save();
        perm = cfg.getBoolean("Permissions.enabled", true);

        PluginManager pm = getServer().getPluginManager();
        this.getCommand("md").setExecutor(new MDCommand(this));

        pm.registerEvents(playerlistener, this);
        pm.registerEvents(entitylistener, this);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new DisguiseTask(this), 1200, 1200);

        System.out.println("[" + pdf.getName() + "]" + " by " + pdf.getAuthors().get(0) + " version " + pdf.getVersion() + " enabled.");
    }
    
    // Mob Types Enums
    public enum MobType {
		CREEPER(50, "creeper"),
		SKELETON(51, "skeleton"),
		SPIDER(52, "spider"),
		GIANT(53, "giant"),
		ZOMBIE(54, "zombie"),
		SLIME(55, "slime"),
		GHAST(56, "ghast"),
		PIGMAN(57, "zombie pigman"),
		ENDERMAN(58, "enderman"),
		CAVESPIDER(59, "cave spider"),
		SILVERFISH(60, "silverfish"),
		BLAZE(61, "blaze"),
		MAGMACUBE(62, "magma cube"),
		ENDERDRAGON(63, "Ender dragon"),
		PIG(90, "pig"),
		SHEEP(91, "sheep"),
		COW(92, "cow"),
		CHICKEN(93, "chicken"),
		SQUID(94, "squid"),
		WOLF(95, "wolf"),
		MOOSHROOM(96, "mooshroom"),
		SNOWGOLEM(97, "snow golem"),
		VILLAGER(120, "villager");
		
		public final int id;
		public final String name;
		MobType(int i, String n) {
			id = i;
			name = n;
		}
		
		public String toString() {
			return super.toString().toLowerCase();
			
		}
		
		public static MobType getMobType(String name) {
			return MobType.valueOf(name.toUpperCase());
		}
		
		private static String[] enumsToArray() {
			MobType[] vals = MobType.values();
			int i = 0;
			String[] output = new String[vals.length];
			for (MobType mob : vals) {
				output[i++] = mob.toString();
			}
			return output;
		}
		
	    public static String[] types = enumsToArray();
	    
	    public static boolean isMob(String mobName) {
	    	return (Arrays.asList(types).contains(mobName.toLowerCase()));
	    }
	    
	    public static MobType getType(int id) {
	    	for (MobType type : MobType.values()) {
	    		if (type.id == id) {
	    			return type;
	    		}
	    	}
	    	return null;
	    }
	}

}