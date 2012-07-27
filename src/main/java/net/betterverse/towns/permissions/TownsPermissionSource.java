package net.betterverse.towns.permissions;

import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.Resident;

import org.bukkit.entity.Player;

/**
 * @author ElgarL
 *
 * Manager for Permission provider plugins
 */
public abstract class TownsPermissionSource {
	protected TownsSettings settings;
	protected Towns plugin;

	/**
	 * getPermissionNode
	 *
	 * returns the specified prefix/suffix nodes from permissionsEX
	 *
	 * @param resident
	 * @param node Should be "prefix" or "suffix"
	 * @return
	 */
	abstract public String getPrefixSuffix(Resident resident, String node);

	/**
	 * @param playerName
	 * @param node
	 * @return -1 = can't find
	 */
	abstract public int getGroupPermissionIntNode(String playerName, String node);

	/**
	 * hasPermission
	 *
	 * returns if a player has a certain permission node.
	 *
	 * @param player
	 * @param node
	 * @return
	 */
	abstract public boolean hasPermission(Player player, String node);

	/**
	 * Returns the players Group name.
	 *
	 * @param player
	 * @return
	 */
	abstract public String getPlayerGroup(Player player);
}
