package de.bananaco.protect.util;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;

import de.bananaco.protect.PolygonManager;

public class PolygonBuilder {
	
	private final List<Location> points = new ArrayList<Location>();
	private boolean finalised = false;
	private boolean sellable = false;
	private int height = 128;
	
	public PolygonBuilder() {
		// points?
	}
	
	public void setSellable(boolean sellable) {
		this.sellable = sellable;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public boolean isSellable() {
		return sellable;
	}
	
	public PolygonBuilder(Location location) {
		points.add(location);
	}
	
	public void add(Location loc) {
		if(isFinal())
			return;
		points.add(loc);
	}
	
	private List<String> getFlags() {
		// TODO Auto-generated method stub
		return new ArrayList<String>();
	}
	
	public void finalise(String name) {
		if(isFinal())
			return;
		finalised = true;
		// Assign them here, saves a touch of speed
		int x, z;
		Polygon polygon = new Polygon();
		for(Location loc : points) {
			x = loc.getBlockX();
			z = loc.getBlockZ();
			polygon.addPoint(x, z);
		}
		Location first = points.get(0);
		ProtectedPolygon pPolygon = new ProtectedPolygon(name, first.getWorld().getName(), polygon, first.getBlockY(), getHeight(), getFlags());
		PolygonManager.getInstance().add(pPolygon);
		points.clear();
	}

	public boolean isFinal() {
		return finalised;
	}

}
