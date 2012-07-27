package net.betterverse.towns.war;

import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.Material;

import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.config.ConfigNodes;
import net.betterverse.towns.util.TimeTools;

public class TownsWarConfig {
	public static final DyeColor[] woolColors = new DyeColor[]{DyeColor.LIME, DyeColor.GREEN, DyeColor.BLUE, DyeColor.CYAN, DyeColor.LIGHT_BLUE, DyeColor.SILVER, DyeColor.WHITE, DyeColor.PINK, DyeColor.ORANGE, DyeColor.RED};

	private static Material flagBaseMaterial = null;
	private static Material flagLightMaterial = null;
	private static Material beaconWireFrameMaterial = null;

	private static Set<Material> editableMaterialsInWarZone = null;

	public static boolean isAffectedMaterial(Material material) {
		return material == Material.WOOL || material == getFlagBaseMaterial() || material == getFlagLightMaterial() || material == getBeaconWireFrameMaterial();
	}

	public static String parseSingleLineString(String str) {
		return str.replaceAll("&", "\u00A7");
	}

	public static DyeColor[] getWoolColors() {
		return woolColors;
	}

	public static boolean isAllowingAttacks() {
		return TownsSettings.getBoolean(ConfigNodes.WAR_ENEMY_ALLOW_ATTACKS);
	}

	public static long getFlagWaitingTime() {
		return TimeTools.getMillis(TownsSettings.getString(ConfigNodes.WAR_ENEMY_FLAG_WAITING_TIME));
	}

	public static long getTimeBetweenFlagColorChange() {
		return getFlagWaitingTime() / getWoolColors().length;
	}

	public static boolean isDrawingBeacon() {
		return TownsSettings.getBoolean(ConfigNodes.WAR_ENEMY_BEACON_DRAW);
	}

	public static int getMaxActiveFlagsPerPerson() {
		return TownsSettings.getInt(ConfigNodes.WAR_ENEMY_MAX_ACTIVE_FLAGS_PER_PLAYER);
	}

	public static Material getFlagBaseMaterial() {
		return flagBaseMaterial;
	}

	public static Material getFlagLightMaterial() {
		return flagLightMaterial;
	}

	public static Material getBeaconWireFrameMaterial() {
		return beaconWireFrameMaterial;
	}

	public static int getBeaconRadius() {
		return TownsSettings.getInt(ConfigNodes.WAR_ENEMY_BEACON_RADIUS);
	}

	public static int getBeaconSize() {
		return getBeaconRadius() * 2 - 1;
	}

	public static void setFlagBaseMaterial(Material flagBaseMaterial) {
		TownsWarConfig.flagBaseMaterial = flagBaseMaterial;
	}

	public static void setFlagLightMaterial(Material flagLightMaterial) {
		TownsWarConfig.flagLightMaterial = flagLightMaterial;
	}

	public static void setBeaconWireFrameMaterial(Material beaconWireFrameMaterial) {
		TownsWarConfig.beaconWireFrameMaterial = beaconWireFrameMaterial;
	}

	public static int getMinPlayersOnlineInTownForWar() {
		return TownsSettings.getInt(ConfigNodes.WAR_ENEMY_MIN_PLAYERS_ONLINE_IN_TOWN);
	}

	public static int getMinPlayersOnlineInNationForWar() {
		return TownsSettings.getInt(ConfigNodes.WAR_ENEMY_MIN_PLAYERS_ONLINE_IN_NATION);
	}

	public static void setEditableMaterialsInWarZone(Set<Material> editableMaterialsInWarZone) {
		TownsWarConfig.editableMaterialsInWarZone = editableMaterialsInWarZone;
	}

	public static boolean isEditableMaterialInWarZone(Material material) {
		return TownsWarConfig.editableMaterialsInWarZone.contains(material);
	}

	public static boolean isAllowingSwitchesInWarZone() {
		return TownsSettings.getBoolean(ConfigNodes.WAR_WARZONE_SWITCH);
	}

	public static boolean isAllowingFireInWarZone() {
		return TownsSettings.getBoolean(ConfigNodes.WAR_WARZONE_FIRE);
	}

	public static boolean isAllowingItemUseInWarZone() {
		return TownsSettings.getBoolean(ConfigNodes.WAR_WARZONE_ITEM_USE);
	}

	public static boolean isAllowingExplosionsInWarZone() {
		return TownsSettings.getBoolean(ConfigNodes.WAR_WARZONE_EXPLOSIONS);
	}

	public static boolean explosionsBreakBlocksInWarZone() {
		return TownsSettings.getBoolean(ConfigNodes.WAR_WARZONE_EXPLOSIONS_BREAK_BLOCKS);
	}

	public static boolean regenBlocksAfterExplosionInWarZone() {
		return TownsSettings.getBoolean(ConfigNodes.WAR_WARZONE_EXPLOSIONS_REGEN_BLOCKS);
	}
}
