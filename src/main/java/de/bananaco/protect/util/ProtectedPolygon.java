package de.bananaco.protect.util;

import java.awt.Polygon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;


public class ProtectedPolygon {
	
	private final Polygon polygon;
	/*
	 * META
	 */
	private final Map<String, Object> meta;
	public static final List<String> DEFAULT_FLAGS = new ArrayList<String>();
	
	static {
		List<String> flags = DEFAULT_FLAGS;
		flags.add("block_ignite");
		flags.add("block_burn");
		flags.add("player_interact_chest");
		flags.add("player_damage_pvp");
		flags.add("creature_spawn");
	}
	
	private final static Object NULL_META = new NullMetaObject();
	
	public ProtectedPolygon(String name, String world, Polygon polygon, int y, int height, List<String> flags) {
		Map<String, Object> meta = new HashMap<String, Object>();
		
		/*
		 * DEFAULT FLAGS
		 */	
		flags.addAll(DEFAULT_FLAGS);
		
		meta.put(Meta.WORLD, world);
		meta.put(Meta.NAME, name);
		meta.put(Meta.OWNERS, new ArrayList<String>());
		meta.put(Meta.HEIGHT, height);
		meta.put(Meta.START_Y, y);
		meta.put(Meta.FLAGS, flags);
		
		this.meta = meta;
		this.polygon = polygon;
		// And because some people are stupid
		this.save();
	}
	
	public ProtectedPolygon(Polygon polygon, Map<String, Object> meta) {
		this.meta = meta;
		this.polygon = polygon;
	}
	
	/**
	 * Access the meta map directly
	 * @return Map<String, Object>
	 */
	public Map<String, Object> getMeta() {
		return meta;
	}
	
	/**
	 * If the key exists, return it
	 * Otherwise return NullMetaObject
	 * @param key
	 * @return Object
	 */
	public Object getMeta(String key) {
		if(meta.containsKey(key))
			return meta.get(key);
		
		else return NULL_META;
	}
	
	public void setMeta(String key, Object value) {
		meta.put(key, value);
	}
	
	static class NullMetaObject {
		
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getFlags() {
		return (List<String>) meta.get(Meta.FLAGS);
	}
	
	public boolean getFlag(String flag) {
		// flag
		List<String> flags = getFlags();
		flag = flag.toLowerCase();
		// if the flag is true it will be in the list
		if(flags.contains(flag))
			return true;
		// otherwise it will be false
		return false;
	}
	
	public void setFlag(String flag, boolean value) {
		// flag
		flag = flag.toLowerCase();
		List<String> flags = getFlags();
		// set the value to true
		if(value)
			if(!flags.contains(flag))
				flags.add(flag);
		// or set the value to false
		if(!value)
			if(flags.contains(flag))
				flags.remove(flag);
	}
	
	@SuppressWarnings("unchecked")
	public boolean canOverride(Player player) {
		if(player.hasPermission("polygon.admin"))
			return true;
		
		List<String> owners = (List<String>) meta.get(Meta.OWNERS);
		for(String o : owners)
			if(o.equalsIgnoreCase(player.getName()))
				return true;
		
		return false;
	}
	
	public boolean isOwner(String player) {
		for(String owner : getOwners()) {
			if(owner.equalsIgnoreCase(player))
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean canBuild(Player player) {
		if(player.hasPermission("polygon.admin"))
			return true;
		
		List<String> owners = (List<String>) meta.get(Meta.OWNERS);
		for(String o : owners)
			if(o.equalsIgnoreCase(player.getName()))
				return true;
		
		return getFlag("build");
	}
	
	@SuppressWarnings("unchecked")
	public boolean canFlag(Player player) {
		if(player.hasPermission("polygon.admin"))
			return true;
		List<String> owners = (List<String>) meta.get(Meta.OWNERS);
		for(String o : owners)
			if(o.equalsIgnoreCase(player.getName()))
				return true;
		
		return false;
	}
	
	public boolean contains(int x, int y, int z) {
		int thisy = (Integer) meta.get(Meta.START_Y);
		int height = (Integer) meta.get(Meta.HEIGHT);
		
		if(y < thisy)
			return false;
		if(y >= thisy+height)
			return false;
		return polygon.contains(x, z);
	}
	
	public String getWorld() {
		return (String) meta.get(Meta.WORLD);
	}
	
	public String getName() {
		return (String) meta.get(Meta.NAME);
	}
	
	public Polygon getPolygon() {
		return polygon;
	}
	
	public int getHeight() {
		return (Integer) meta.get(Meta.HEIGHT);
	}
	
	public int getY() {
		return (Integer) meta.get(Meta.START_Y);
	}
	
	@Override
	public int hashCode() {
		return meta.get(Meta.NAME).hashCode();
	}
	
	public void setHeight(int height) {
		meta.put(Meta.HEIGHT, height);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getOwners() {
		return (List<String>) meta.get(Meta.OWNERS);
	}
	
	public void save() {
		try {
			serialize(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void delete() {
		try {
			ProtectedPolygon polygon = this;
			String ro = "plugins/PolygonProtect/";
			
			File world = new File(ro+polygon.getWorld());
			File root = new File(world, polygon.getName()+".poly");
			File meta = new File(world, polygon.getName()+".yml");
			// Mark for deletion
			if(!root.delete())
				root.deleteOnExit();
			if(!meta.delete())
				meta.deleteOnExit();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void serialize(ProtectedPolygon polygon) throws Exception {
		String ro = "plugins/PolygonProtect/";
		
		File world = new File(ro+polygon.getWorld());
		File root = new File(world, polygon.getName()+".poly");
		File meta = new File(world, polygon.getName()+".yml");
		
		if(!world.exists()) {
			world.mkdirs();
		}
		if(!root.exists()) {
			root.createNewFile();
		}
		if(!meta.exists()) {
			meta.createNewFile();
		}
		
		FileOutputStream fos = new FileOutputStream(root);
		GZIPOutputStream gzos = new GZIPOutputStream(fos);
		ObjectOutputStream oos = new ObjectOutputStream(gzos);
		// Write the polygon - this class implements Serializable
		oos.writeObject(polygon.getPolygon());
		oos.close();
		gzos.close();
		fos.close();
		
		// Write the metadata - this is saved in yaml
		YamlConfiguration data = new YamlConfiguration();
		
		Map<String, Object> met = polygon.getMeta();
		Set<String> keys = met.keySet();
		
		for(String key : keys) {
			data.set("meta."+key, met.get(key));
		}
		
		data.save(meta);
	}
	
	public static ProtectedPolygon deserialize(File root) throws Exception {
		String name = root.getPath().replace(".poly", "");
		File meta = new File(name+".yml");
		// Load the polygon - this class implements Serializable
		FileInputStream fis = new FileInputStream(root);
		GZIPInputStream gzis = new GZIPInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(gzis);
		Object o = ois.readObject();
		Polygon polygon = (Polygon) o;
		ois.close();
		gzis.close();
		fis.close();
		
		// Load the metadata - this is saved in yaml
		YamlConfiguration data = new YamlConfiguration();
		data.load(meta);
		
		Map<String, Object> met = new HashMap<String, Object>();
		Set<String> keys = data.getConfigurationSection("meta").getKeys(false);
		for(String key : keys) {
			Object mo = data.get("meta."+key);
			met.put(key, mo);
		}
		// Deserialize and add the owners
		ProtectedPolygon p = new ProtectedPolygon(polygon, met);

		return p;
	}

}
