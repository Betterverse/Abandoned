package de.bananaco.protect.util;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.bananaco.protect.PolygonManager;

public class PlayerUtil {
	
	PolygonManager poly = PolygonManager.getInstance();
	
	public boolean canPlace(Player player, Location location) {
		List<ProtectedPolygon> regions = poly.getList(location);
		
		if(regions.size() == 0)
			return true;
		
		boolean canPlace = false;
		for(ProtectedPolygon poly : regions) {
		if(!canPlace)
		canPlace = poly.canBuild(player);	
		}
		
		return canPlace;
	}

}