package net.betterverse.towns.permissions;

import org.bukkit.entity.Player;

import net.betterverse.towns.Towns;
import net.betterverse.towns.object.Resident;

public class NullPermSource extends TownsPermissionSource {
	public NullPermSource(Towns towns) {
		this.plugin = towns;
	}

	@Override
	public String getPrefixSuffix(Resident resident, String node) {
		// using no permissions provider
		return "";
	}

	/**
	 * @param playerName
	 * @param node
	 * @return -1 = can't find
	 */
	@Override
	public int getGroupPermissionIntNode(String playerName, String node) {
		// // using no permissions provider
		return - 1;
	}

	/**
	 * hasPermission
	 *
	 * returns if a player has a certain permission node.
	 *
	 * @param player
	 * @param node
	 * @return
	 */
	@Override
	public boolean hasPermission(Player player, String node) {
		// using no permissions provider
		return false;
	}

	/**
	 * Returns the players Group name.
	 *
	 * @param player
	 * @return
	 */
	@Override
	public String getPlayerGroup(Player player) {
		// using no permissions provider
		return "";
	}
}
