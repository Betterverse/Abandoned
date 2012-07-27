package net.betterverse.towns;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.ResidentList;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownBlock;
import net.betterverse.towns.object.TownBlockOwner;
import net.betterverse.towns.object.TownsEconomyObject;
import net.betterverse.towns.object.TownsObject;
import net.betterverse.towns.object.TownsWorld;
import net.betterverse.towns.util.ChatTools;
import net.betterverse.towns.util.Colors;
import net.betterverse.towns.util.MinecraftTools;
import net.betterverse.towns.util.StringMgmt;

public class TownsFormatter {
	public static final SimpleDateFormat lastOnlineFormat = new SimpleDateFormat("MMMMM dd '@' HH:mm");
	public static final SimpleDateFormat registeredFormat = new SimpleDateFormat("MMM d yyyy");

	/**
	 * 1 = Description 2 = Count
	 *
	 * Colours: 3 = Description and : 4 = Count 5 = Colour for the start of the
	 * list
	 */
	public static final String residentListPrefixFormat = "%3$s%1$s %4$s[%2$d]%3$s:%5$s ";

	public static List<String> getFormattedOnlineResidents(Towns plugin, String prefix, ResidentList residentList) {
		List<Resident> onlineResidents = plugin.getTownsUniverse().getOnlineResidents(residentList);
		return getFormattedResidents(prefix, onlineResidents);
	}

	public static List<String> getFormattedResidents(String prefix, List<Resident> residentList) {
		return ChatTools.listArr(getFormattedNames(residentList), String.format(residentListPrefixFormat, prefix, residentList.size(), Colors.Green, Colors.LightGreen, Colors.White));
	}

	public static String[] getFormattedNames(List<Resident> residentList) {
		return getFormattedNames(residentList.toArray(new Resident[0]));
	}

	public static String getTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
		return sdf.format(System.currentTimeMillis());
	}

	public static List<String> getStatus(TownBlock townBlock) {
		List<String> out = new ArrayList<String>();

		try {
			TownBlockOwner owner;
			Town town = townBlock.getTown();
			TownsWorld world = townBlock.getWorld();

			if (townBlock.hasResident()) {
				owner = townBlock.getResident();
			} else {
				owner = townBlock.getTown();
			}

			out.add(ChatTools.formatTitle(TownsFormatter.getFormattedName(owner) + ((MinecraftTools.isOnline(owner.getName())) ? Colors.LightGreen + " (Online)" : "")));
			out.add(Colors.Green + " Perm: " + townBlock.getPermissions().getColourString());
			out.add(Colors.Green + "PvP: " + ((town.isPVP() || world.isForcePVP() || townBlock.getPermissions().pvp) ? Colors.Red + "ON" : Colors.LightGreen + "OFF") + Colors.Green + "  Explosions: " + ((town.isBANG() || world.isForceExpl() || townBlock.getPermissions().explosion) ? Colors.Red + "ON" : Colors.LightGreen + "OFF") + Colors.Green + "  Firespread: " + ((town.isFire() || world.isForceFire() || townBlock.getPermissions().fire) ? Colors.Red + "ON" : Colors.LightGreen + "OFF") + Colors.Green + "  Mob Spawns: " + ((town.hasMobs() || world.isForceTownMobs() || townBlock.getPermissions().mobs) ? Colors.Red + "ON" : Colors.LightGreen + "OFF"));
		} catch (NotRegisteredException e) {
			out.add("Error: " + e.getError());
		}

		return out;
	}

	public static List<String> getStatus(Resident resident) {
		List<String> out = new ArrayList<String>();

		// ___[ King Harlus ]___
		out.add(ChatTools.formatTitle(getFormattedName(resident) + ((MinecraftTools.isOnline(resident.getName())) ? Colors.LightGreen + " (Online)" : "")));

		// Registered: Sept 3 2009 | Last Online: March 7 @ 14:30
		out.add(Colors.Green + "Registered: " + Colors.LightGreen + registeredFormat.format(resident.getRegistered()) + Colors.Gray + " | " + Colors.Green + "Last Online: " + Colors.LightGreen + lastOnlineFormat.format(resident.getLastOnline()));

		// Owner of: 4 plots
		// Perm: Build = f-- Destroy = fa- Switch = fao Item = ---
		//if (resident.getTownBlocks().size() > 0) {
		out.add(Colors.Green + "Owner of: " + Colors.LightGreen + resident.getTownBlocks().size() + " plots");
		out.add(Colors.Green + "	Perm: " + resident.getPermissions().getColourString());
		out.add(Colors.Green + "PVP: " + ((resident.getPermissions().pvp) ? Colors.Red + "ON" : Colors.LightGreen + "OFF") + Colors.Green + "  Explosions: " + ((resident.getPermissions().explosion) ? Colors.Red + "ON" : Colors.LightGreen + "OFF") + Colors.Green + "  Firespread: " + ((resident.getPermissions().fire) ? Colors.Red + "ON" : Colors.LightGreen + "OFF") + Colors.Green + "  Mob Spawns: " + ((resident.getPermissions().mobs) ? Colors.Red + "ON" : Colors.LightGreen + "OFF"));
		//}

		// Bank: 534 coins
		if (TownsSettings.isUsingEconomy()) {
				out.add(Colors.Green + "Bank: " + Colors.LightGreen + resident.getHoldingFormattedBalance());
		}

		// Town: Camelot
		String line = Colors.Green + "Town: " + Colors.LightGreen;
		if (! resident.hasTown()) {
			line += "None";
		} else {
			try {
				line += getFormattedName(resident.getTown());
			} catch (TownsException e) {
				line += "Error: " + e.getError();
			}
		}
		out.add(line);

		// Friends [12]: James, Carry, Mason
		List<Resident> friends = resident.getFriends();
		out.addAll(getFormattedResidents("Friends", friends));

		return out;
	}

	public static List<String> getStatus(Town town) {
		List<String> out = new ArrayList<String>();

        // I can't replicate asphodan's error, and this is all I can really do for now :c -rat
        TownsWorld world;
        try {
            world = town.getWorld();
        } catch (NullPointerException e) {
            out.add("Failed to get town world!");
            return out;
        }

		// ___[ Raccoon City (PvP) ]___
		out.add(ChatTools.formatTitle(getFormattedName(town) + ((town.isPVP() || town.getWorld().isForcePVP()) ? Colors.Red + " (PvP)" : "")));

		// Lord: Mayor Quimby
		// Board: Get your fried chicken
		try {
			out.add(Colors.Green + "Board: " + Colors.LightGreen + town.getTownBoard());
		} catch (NullPointerException e) {
		}

		// Town Size: 0 / 16 [Bought: 0/48] [Bonus: 0] [Home: 33,44]
		try {
			out.add(Colors.Green + "Town Size: " + Colors.LightGreen + town.getTownBlocks().size() + " / " + TownsSettings.getMaxTownBlocks(town) + (TownsSettings.isSellingBonusBlocks() ? Colors.LightBlue + " [Bought: " + town.getPurchasedBlocks() + "/" + TownsSettings.getMaxPurchedBlocks() + "]" : "") + (town.getBonusBlocks() > 0 ? Colors.LightBlue + " [Bonus: " + town.getBonusBlocks() + "]" : "") + (town.isPublic() ? Colors.LightGray + " [Home: " + (town.hasHomeBlock() ? town.getHomeBlock().getCoord().toString() : "None") + "]" : ""));
		} catch (TownsException e) {
		}

		// Permissions: B=rao D=--- S=ra-
		out.add(Colors.Green + "Permissions: " + town.getPermissions().getColourString().replace("f", "r"));
		out.add(Colors.Green + "Explosions: " + ((town.isBANG() || world.isForceExpl()) ? Colors.Red + "ON" : Colors.LightGreen + "OFF") + Colors.Green + "  Firespread: " + ((town.isFire() || world.isForceFire()) ? Colors.Red + "ON" : Colors.LightGreen + "OFF") + Colors.Green + "  Mob Spawns: " + ((town.hasMobs() || world.isForceTownMobs()) ? Colors.Red + "ON" : Colors.LightGreen + "OFF") + Colors.Green + "  Tax: " + Colors.Red + town.getTaxes() + (town.isTaxPercentage() ? "%" : ""));

		// | Bank: 534 coins
		String bankString = "";
		if (TownsSettings.isUsingEconomy()) {
				bankString = Colors.Gray + " | " + Colors.Green + "Bank: " + Colors.LightGreen + town.getHoldingFormattedBalance();
		}

		// Mayor: MrSand | Bank: 534 coins
		out.add(Colors.Green + "Mayor: " + Colors.LightGreen + getFormattedName(town.getMayor()) + bankString);

		// Assistants [2]: Sammy, Ginger
		if (town.getAssistants().size() > 0) {
			out.addAll(getFormattedResidents("Assistants", town.getAssistants()));
		}

		// Nation: Azur Empire
		try {
			out.add(Colors.Green + "Nation: " + Colors.LightGreen + getFormattedName(town.getNation()));
		} catch (TownsException e) {
		}

		// Residents [12]: James, Carry, Mason

		String[] residents = getFormattedNames(town.getResidents().toArray(new Resident[0]));
		if (residents.length > 34) {
			String[] entire = residents;
			residents = new String[36];
			System.arraycopy(entire, 0, residents, 0, 35);
			residents[35] = "and more...";
		}
		out.addAll(ChatTools.listArr(residents, Colors.Green + "Residents " + Colors.LightGreen + "[" + town.getNumResidents() + "]" + Colors.Green + ":" + Colors.White + " "));
		return out;
	}

	public static List<String> getStatus(Nation nation) {
		List<String> out = new ArrayList<String>();

		// ___[ Azur Empire ]___
		out.add(ChatTools.formatTitle(getFormattedName(nation)));

		// Bank: 534 coins
		String line = "";
		if (TownsSettings.isUsingEconomy()) {
				line = Colors.Green + "Bank: " + Colors.LightGreen + nation.getHoldingFormattedBalance();
		}

		if (nation.isNeutral()) {
			if (line.length() > 0) {
				line += Colors.Gray + " | ";
			}
			line += Colors.LightGray + "Neutral";
		}
		// Bank: 534 coins | Neutral
		if (line.length() > 0) {
			out.add(line);
		}

		// King: King Harlus
		if (nation.getNumTowns() > 0 && nation.hasCapital() && nation.getCapital().hasMayor()) {
			out.add(Colors.Green + "King: " + Colors.LightGreen + getFormattedName(nation.getCapital().getMayor()) + Colors.Green + "  NationTax: " + Colors.Red + nation.getTaxes());
		}
		// Assistants: Mayor Rockefel, Sammy, Ginger
		if (nation.getAssistants().size() > 0) {
			out.addAll(ChatTools.listArr(getFormattedNames(nation.getAssistants().toArray(new Resident[0])), Colors.Green + "Assistants:" + Colors.White + " "));
		}
		// Towns [44]: James City, Carry Grove, Mason Town
		out.addAll(ChatTools.listArr(getFormattedNames(nation.getTowns().toArray(new Town[0])), Colors.Green + "Towns " + Colors.LightGreen + "[" + nation.getNumTowns() + "]" + Colors.Green + ":" + Colors.White + " "));
		// Allies [4]: James Nation, Carry Territory, Mason Country
		out.addAll(ChatTools.listArr(getFormattedNames(nation.getAllies().toArray(new Nation[0])), Colors.Green + "Allies " + Colors.LightGreen + "[" + nation.getAllies().size() + "]" + Colors.Green + ":" + Colors.White + " "));
		// Enemies [4]: James Nation, Carry Territory, Mason Country
		out.addAll(ChatTools.listArr(getFormattedNames(nation.getEnemies().toArray(new Nation[0])), Colors.Green + "Enemies " + Colors.LightGreen + "[" + nation.getEnemies().size() + "]" + Colors.Green + ":" + Colors.White + " "));

		return out;
	}

	public static List<String> getStatus(TownsWorld world) {
		List<String> out = new ArrayList<String>();

		// ___[ World ]___
		out.add(ChatTools.formatTitle(getFormattedName(world)));

		// Claimable: No | PvP: Off
		out.add(Colors.Green + "Claimable: " + (world.isClaimable() ? Colors.LightGreen + "Yes" : Colors.Rose + "No") + Colors.Gray + " | " + Colors.Green + "PvP: " + (world.isPVP() ? Colors.Rose + "On" : Colors.LightGreen + "Off") + Colors.Gray + " | " + Colors.Green + "ForcePvP: " + (world.isForcePVP() ? Colors.Rose + "On" : Colors.LightGreen + "Off") + Colors.Gray + " | " + Colors.Green + "Fire: " + (world.isForceFire() ? Colors.Rose + "On" : Colors.LightGreen + "Off"));

		out.add(Colors.Green + "Explosions: " + (world.isForceExpl() ? Colors.Rose + "On" : Colors.LightGreen + "Off") + Colors.Gray + " | " + Colors.Green + "World Mobs: " + (world.hasWorldMobs() ? Colors.Rose + "On" : Colors.LightGreen + "Off") + Colors.Gray + " | " + Colors.Green + "ForceTownMobs: " + (world.isForceTownMobs() ? Colors.Rose + "On" : Colors.LightGreen + "Off"));
		// Using Default Settings: Yes
		//out.add(Colors.Green + "Using Default Settings: " + (world.isUsingDefault() ? Colors.LightGreen + "Yes" : Colors.Rose + "No"));
		// Wilderness:
		//	 Build, Destroy, Switch
		//	 Ignored Blocks: 34, 45, 64
		out.add(Colors.Green + world.getUnclaimedZoneName() + ":");
		out.add("	" + (world.getUnclaimedZoneBuild() ? Colors.LightGreen : Colors.Rose) + "Build" + Colors.Gray + ", " + (world.getUnclaimedZoneDestroy() ? Colors.LightGreen : Colors.Rose) + "Destroy" + Colors.Gray + ", " + (world.getUnclaimedZoneSwitch() ? Colors.LightGreen : Colors.Rose) + "Switch" + Colors.Gray + ", " + (world.getUnclaimedZoneItemUse() ? Colors.LightGreen : Colors.Rose) + "ItemUse");
		out.add("	" + Colors.Green + "Ignored Blocks:" + Colors.LightGreen + " " + StringMgmt.join(world.getUnclaimedZoneIgnoreIds(), ", "));

		return out;
	}

	public static String getNamePrefix(Resident resident) {
		if (resident == null) {
			return "";
		}
		if (resident.isKing()) {
			return TownsSettings.getKingPrefix(resident);
		} else if (resident.isMayor()) {
			return TownsSettings.getMayorPrefix(resident);
		}
		return "";
	}

	public static String getNamePostfix(Resident resident) {
		if (resident == null) {
			return "";
		}
		if (resident.isKing()) {
			return TownsSettings.getKingPostfix(resident);
		} else if (resident.isMayor()) {
			return TownsSettings.getMayorPostfix(resident);
		}
		return "";
	}

	public static String getFormattedName(TownsObject obj) {
		if (obj == null) {
			return "Null";
		} else if (obj instanceof Resident) {
			return getFormattedResidentName((Resident) obj);
		} else if (obj instanceof Town) {
			return getFormattedTownName((Town) obj);
		} else if (obj instanceof Nation) {
			return getFormattedNationName((Nation) obj);
		}
		//System.out.println("just name: " + obj.getName());
		return obj.getName().replaceAll("_", " ");
	}

	public static String getFormattedResidentName(Resident resident) {
		if (resident == null) {
			return "null";
		}
		if (resident.isKing()) {
			return TownsSettings.getKingPrefix(resident) + resident.getName().replaceAll("_", " ") + TownsSettings.getKingPostfix(resident);
		} else if (resident.isMayor()) {
			return TownsSettings.getMayorPrefix(resident) + resident.getName().replaceAll("_", " ") + TownsSettings.getMayorPostfix(resident);
		}
		return resident.getName().replaceAll("_", " ");
	}

	public static String getFormattedTownName(Town town) {
		if (town.isCapital()) {
			return TownsSettings.getCapitalPrefix(town) + town.getName().replaceAll("_", " ") + TownsSettings.getCapitalPostfix(town);
		}
		return TownsSettings.getTownPrefix(town) + town.getName().replaceAll("_", " ") + TownsSettings.getTownPostfix(town);
	}

	public static String getFormattedNationName(Nation nation) {
		return TownsSettings.getNationPrefix(nation) + nation.getName().replaceAll("_", " ") + TownsSettings.getNationPostfix(nation);
	}

	public static String[] getFormattedNames(Resident[] residents) {
		List<String> names = new ArrayList<String>();
		for (Resident resident : residents) {
			names.add(getFormattedName(resident));
		}
		return names.toArray(new String[0]);
	}

	public static String[] getFormattedNames(Town[] towns) {
		List<String> names = new ArrayList<String>();
		for (Town town : towns) {
			names.add(getFormattedName(town));
		}
		return names.toArray(new String[0]);
	}

	public static String[] getFormattedNames(Nation[] nations) {
		List<String> names = new ArrayList<String>();
		for (Nation nation : nations) {
			names.add(getFormattedName(nation));
		}
		return names.toArray(new String[0]);
	}

	public static String formatMoney(double amount) {
		try {
			return TownsEconomyObject.getFormattedBalance(amount);
		} catch (Exception e) {
			return Double.toString(amount);
		}
	}
}
