package net.betterverse.worldmarket;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.betterverse.protect.PolygonManager;
import net.betterverse.protect.utils.ProtectedPolygon;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * WorldMarketManager 1.0
 * 
 * This class is designed to track bought regions and remove bought regions, as
 * well as a couple of other fun functions.
 * 
 * @author codename_B
 */
public class WorldMarketManager {

	private final PolygonManager pm = PolygonManager.getInstance();
	public final WorldMarketEco eco;
	public final Server server;

	private final YamlConfiguration config = new YamlConfiguration();
	private final File file = new File("plugins/WorldMarket/regions.yml");

	WorldMarketManager(WorldMarketEco eco,
			Server server) {
		this.eco = eco;
		this.server = server;
		// I guess we better create that file
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// And now load the config...
		try {
			config.load(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Because its easier than doing it in the code
	 */
	public void save() {
		try {
			config.save(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * I really am that lazy
	 * @param world
	 */
	public void save(World world) {
	try {
		// TODO make save
		Map<String, ProtectedPolygon> polys = pm.getPolygons(world.getName());
		Set<String> keys = polys.keySet();
		for(String key : keys) {
			polys.get(key).save();
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public PolygonManager getPolygonManager() {
		return pm;
	}

	/**
	 * Gets the selection of the player and buys the region if they can afford
	 * it
	 * 
	 * @param player
	 * @return sucess
	 */
	public boolean rentPolygon(String player, World world, ProtectedPolygon region) {
		double cost = getCost(world, region);
		// If they can't afford it, they lose it
		if (!eco.hasOver(player, cost)) {
			if (isOwner(region, player)) {
				// May as well utilise a shared call eh? Save code space!
				clearRegion(world, region);
				save(world);
			}
			return false;
		}
		// Then add the player to owners if he's not already
		if (!isOwner(region, player))
			region.getOwners().add(player);
		// And finally charge the player
		eco.charge(player, cost);
		save(world);
		return true;
	}

	/**
	 * A simple owners wipe
	 * 
	 * @param region
	 */
	public void clearRegion(World world, ProtectedPolygon region) {
		region.getOwners().clear();
		// Clear current flags and add all default flags!
		region.getFlags().clear();
		region.getFlags().addAll(ProtectedPolygon.DEFAULT_FLAGS);
		region.setFlag("sellable", true);
	}

	/**
	 * Used by admins to set the price for regions
	 * 
	 * @param world
	 * @param region
	 * @param price
	 */
	public void setPrice(World world, ProtectedPolygon region, int price) {
		config.set(world.getName() + "." + region.getName(), price);
		save();
	}

	public int getCost(World world, ProtectedPolygon region) {
		return config.getInt(world.getName() + "." + region.getName(),
				Integer.MAX_VALUE);
	}

	/**
	 * Attempts to set a flag for the region
	 * Should be checked against isOwner() first
	 * @param world
	 * @param region
	 * @param flag
	 * @param allow
	 * @return success
	 */
	public boolean setFlag(World world, ProtectedPolygon region, String flag,
			String allow) {
		boolean al = false;
		
		if(allow.equalsIgnoreCase("allow"))
			al = true;
		
		region.setFlag(flag, !al);
		return true;
	}

	/**
	 * This function calculate the number of regions owned by a player in the
	 * world they are currently in.
	 * 
	 * @param player
	 * @return
	 */
	public int regionNumber(Player player) {
		// Use our lovely function
		ProtectedPolygon[] regions = getRentedRegions(player.getWorld());
		// Inital amount
		int amount = 0;
		// Increment for every region
		for (ProtectedPolygon r : regions)
			if (r.isOwner(player.getName()))
				amount++;
		// And return the amount
		return amount;
	}
	/**
	 * Does the player have permission
	 * to rent another region?
	 * @param player
	 * @return true/false
	 */
	public boolean canRent(Player player) {
		int count = regionNumber(player) + 1;
		if(count > WorldMarket.hard)
			return false;
		return (player.hasPermission("worldmarket.zones." + count) || player.hasPermission("worldmarket.zones.*"));
	}

	/**
	 * Gets all the rented regions for a world
	 * 
	 * @param world
	 * @return World
	 */
	public ProtectedPolygon[] getRentedRegions(World world) {
		Map<String, ProtectedPolygon> regions = pm.getPolygons(world.getName());
		LinkedList<ProtectedPolygon> regs = new LinkedList<ProtectedPolygon>();
		for (String key : regions.keySet()) {
			ProtectedPolygon region = regions.get(key);
			if (isRentable(world, region) && !isAvailable(world, region)) {
				regs.add(region);
			}
		}
		return (ProtectedPolygon[]) regs.toArray(new ProtectedPolygon[regs.size()]);
	}

	/**
	 * Makes getting the region the player is in MUCH easier
	 * 
	 * @param player
	 * @return ProtectedPolygon or null
	 */
	public ProtectedPolygon getRegion(Player player) {
		ProtectedPolygon poly = pm.getSellable(player.getLocation());
		return poly;
	}
	/**
	 * Makes getting the owners of a region MUCH easier
	 * 
	 * @param region
	 * @return Set<String>
	 */
	public List<String> getOwners(ProtectedPolygon region) {
		return region.getOwners();
	}

	/**
	 * Convenience method for checking ownership in regions
	 * 
	 * @param region
	 * @param player
	 * @return true/false
	 */
	public boolean isOwner(ProtectedPolygon region, String player) {
		if(region == null)
			return false;
		if(region.getOwners() == null)
			return false;
		return region.isOwner(player);
	}

	/**
	 * Says if the region is available
	 * 
	 * @param player
	 * @param region
	 * @return true/false
	 */
	public boolean isRentable(World world, ProtectedPolygon region) {
		if (config.get(world.getName() + "." + region.getName()) != null)
			return true;
		return false;
	}

	/**
	 * Says if the region is available
	 * 
	 * @param player
	 * @param region
	 * @return true/false
	 */
	public boolean isAvailable(World world, ProtectedPolygon region) {
		if (isRentable(world, region))
			if (region.getOwners().size() == 0)
				return true;
		return false;
	}

}