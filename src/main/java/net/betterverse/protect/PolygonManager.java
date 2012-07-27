package net.betterverse.protect;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;

import net.betterverse.protect.utils.ProtectedPolygon;
import net.betterverse.protect.utils.StringFilenameFilter;

public class PolygonManager {
	
	private final Map<String, Map<String, ProtectedPolygon>> polygons = new HashMap<String, Map<String, ProtectedPolygon>>();
	private final StringFilenameFilter filter = new StringFilenameFilter(".poly");
	private static PolygonManager instance = null;
	
	public PolygonManager() {
		PolygonManager.instance = this;
	}
	
	public static PolygonManager getInstance() {
		if(instance == null)
			new PolygonManager();
		return instance;
	}
	
	public void add(ProtectedPolygon poly) {
		getPolygons(poly.getWorld()).put(poly.getName(), poly);
	}
	
	public ProtectedPolygon get(String world, String name) {
		Map<String, ProtectedPolygon> polys = getPolygons(world);
		Set<String> keys = polys.keySet();
		for(String key : keys) {
			if(key.equalsIgnoreCase(name))
				return polys.get(key);
		}
		return null;
	}
	
	public boolean contains(String world, String name) {
		return getPolygons(world).containsKey(name);
	}
	/**
	 * Iterates through all the ProtectedPolygons in the world and returns every polygon that contains the area
	 * @param loc
	 * @return ProtectedPolygon(s)
	 */
	public List<ProtectedPolygon> getList(Location loc) {
		List<ProtectedPolygon> polyList = new ArrayList<ProtectedPolygon>();
		Map<String, ProtectedPolygon> polySet = getPolygons(loc.getWorld().getName());
		ProtectedPolygon poly;
		for(String key : polySet.keySet()) {
			poly = polySet.get(key);
			if(poly.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
				polyList.add(poly);
		}
		return polyList;
	}
	/**
	 * Iterates through all the ProtectedPolygons in the world and returns the first true result
	 * @param loc
	 * @return ProtectedPolygon
	 */
	public ProtectedPolygon get(Location loc) {
		Map<String, ProtectedPolygon> polySet = getPolygons(loc.getWorld().getName());
		ProtectedPolygon poly;
		for(String key : polySet.keySet()) {
			poly = polySet.get(key);
			if(poly.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
				return poly;
		}
		return null;
	}
	
	public ProtectedPolygon getSellable(Location loc) {
		Map<String, ProtectedPolygon> polySet = getPolygons(loc.getWorld().getName());
		ProtectedPolygon poly;
		for(String key : polySet.keySet()) {
			poly = polySet.get(key);
			if(poly.getFlag("sellable") &&
					poly.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
				return poly;
		}
		return null;
	}
	
	public Map<String, ProtectedPolygon> getPolygons(String world) {
		Map<String, ProtectedPolygon> poly;
		if(!polygons.containsKey(world)) {
			poly = new HashMap<String, ProtectedPolygon>();
			polygons.put(world, poly);
		} else {
			poly = polygons.get(world);
		}
		return poly;
	}
	
	/**
	 * Iterate through all the saved polygons and load them!
	 */
	public void load() {
		System.out.println("Loading...");
		List<File> files = new ArrayList<File>();
		
		File parent = new File("plugins/PolygonProtect/");
		File[] filel = parent.listFiles();
		
		if(filel == null) {
			System.err.println("Err 315");
			return;
		}
		
		for(File f : filel) {
			if(f.isDirectory()) {
				for(File fi : f.listFiles(filter)) {
					System.out.println(fi.getPath());
					files.add(fi);
				}
			}
		}
		
		for(File file : files) {
			try {
				ProtectedPolygon poly = ProtectedPolygon.deserialize(file);
				add(poly);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Iterate through all the loaded polygons and save them!
	 */
	public void save() {
		Set<String> entrySet = polygons.keySet();
		for(String key : entrySet) {
			Set<String> keySet = polygons.get(key).keySet();
			for(String k : keySet) {
				ProtectedPolygon p = polygons.get(key).get(k);
				p.save();
			}
		}
	}
}