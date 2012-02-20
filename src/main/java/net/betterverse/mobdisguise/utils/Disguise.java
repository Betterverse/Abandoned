package net.betterverse.mobdisguise.utils;

import java.util.Arrays;

public class Disguise {
	// Mob Types Enums
	public enum MobType {
		CREEPER(50, "creeper", "Creeper"),
		SKELETON(51, "skeleton", "Skeleton"),
		SPIDER(52, "spider", "Spider"),
		GIANT(53, "giant", "Giant"),
		ZOMBIE(54, "zombie", "Zombie"),
		SLIME(55, "slime", "Slime"),
		GHAST(56, "ghast", "Ghast"),
		PIGMAN(57, "zombie pigman", "PigZombie"),
		ENDERMAN(58, "enderman", "Enderman"),
		CAVESPIDER(59, "cave spider", "CaveSpider"),
		SILVERFISH(60, "silverfish", "Silverfish"),
		BLAZE(61, "blaze", "Blaze"),
		MAGMACUBE(62, "magma cube", "MagmaCube"),
		ENDERDRAGON(63, "Ender dragon", "EnderDragon"),
		PIG(90, "pig", "Pig"),
		SHEEP(91, "sheep", "Sheep"),
		COW(92, "cow", "Cow"),
		CHICKEN(93, "chicken", "Chicken"),
		SQUID(94, "squid", "Squid"),
		WOLF(95, "wolf", "Wolf"),
		MOOSHROOM(96, "mooshroom", "MushroomCow"),
		SNOWGOLEM(97, "snow golem", "Snowman"),
		VILLAGER(120, "villager", "Villager");
		
		public final byte id;
		public final String name;
		public Class typeClass = null;
		MobType(int i, String n, String className) {
			id = (byte) i;
			name = n;
			try {
				typeClass = Class.forName("org.bukkit.entity." + className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
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

	public MobType mob;
	public String data;
	
	public Disguise(MobType mob, String data) {
		this.mob = mob;
		this.data = data;
	}
	
	public void setData(String newData) {
		data = newData;
	}
}
