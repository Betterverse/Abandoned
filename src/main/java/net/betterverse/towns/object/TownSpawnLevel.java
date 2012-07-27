package net.betterverse.towns.object;

import org.bukkit.entity.Player;

import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.config.ConfigNodes;

public enum TownSpawnLevel {
	TOWN_RESIDENT(ConfigNodes.GTOWN_SETTINGS_ALLOW_TOWN_SPAWN, "msg_err_town_spawn_forbidden", ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL, "towns.town.spawn.town"),
	PART_OF_NATION(ConfigNodes.GTOWN_SETTINGS_ALLOW_TOWN_SPAWN_TRAVEL_NATION, "msg_err_town_spawn_nation_forbidden", ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL_NATION, "towns.town.spawn.nation"),
	NATION_ALLY(ConfigNodes.GTOWN_SETTINGS_ALLOW_TOWN_SPAWN_TRAVEL_ALLY, "msg_err_town_spawn_ally_forbidden", ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL_ALLY, "towns.town.spawn.ally"),
	UNAFFILIATED(ConfigNodes.GTOWN_SETTINGS_ALLOW_TOWN_SPAWN_TRAVEL, "msg_err_public_spawn_forbidden", ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL_PUBLIC, "towns.town.spawn.public"),
	ADMIN(null, null, null, null);

	private ConfigNodes isAllowingConfigNode, ecoPriceConfigNode;
	private String permissionNode, notAllowedLangNode;

	private TownSpawnLevel(ConfigNodes isAllowingConfigNode, String notAllowedLangNode, ConfigNodes ecoPriceConfigNode, String permissionNode) {
		this.isAllowingConfigNode = isAllowingConfigNode;
		this.notAllowedLangNode = notAllowedLangNode;
		this.ecoPriceConfigNode = ecoPriceConfigNode;
		this.permissionNode = permissionNode;
	}

	public void checkIfAllowed(Towns plugin, Player player) throws TownsException {
		if (! (isAllowed() && hasPermissionNode(plugin, player))) {
			throw new TownsException(TownsSettings.getLangString(notAllowedLangNode));
		}
	}

	public boolean isAllowed() {
		return this == TownSpawnLevel.ADMIN ? true : TownsSettings.getBoolean(this.isAllowingConfigNode);
	}

	public boolean hasPermissionNode(Towns plugin, Player player) {
		return this == TownSpawnLevel.ADMIN ? true : (plugin.isPermissions() && TownsUniverse.getPermissionSource().hasPermission(player, this.permissionNode)) || ((! plugin.isPermissions()) && (TownsSettings.isAllowingTownSpawn()));
	}

	public double getCost() {
		return this == TownSpawnLevel.ADMIN ? 0 : TownsSettings.getDouble(ecoPriceConfigNode);
	}
}
