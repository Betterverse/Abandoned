package net.betterverse.towns.object;

import java.util.ArrayList;
import java.util.List;

import net.betterverse.towns.AlreadyRegisteredException;
import net.betterverse.towns.NotRegisteredException;

public class TownBlockOwner extends TownsEconomyObject {
	protected List<TownBlock> townBlocks = new ArrayList<TownBlock>();
	protected TownsPermission permissions = new TownsPermission();

	public void setTownblocks(List<TownBlock> townblocks) {
		this.townBlocks = townblocks;
	}

	public List<TownBlock> getTownBlocks() {
		return townBlocks;
	}

	public boolean hasTownBlock(TownBlock townBlock) {
		return townBlocks.contains(townBlock);
	}

	public void addTownBlock(TownBlock townBlock) throws AlreadyRegisteredException {
		if (hasTownBlock(townBlock)) {
			throw new AlreadyRegisteredException();
		} else {
			townBlocks.add(townBlock);
		}
	}

	public void removeTownBlock(TownBlock townBlock) throws NotRegisteredException {
		if (! hasTownBlock(townBlock)) {
			throw new NotRegisteredException();
		} else {
			townBlocks.remove(townBlock);
		}
	}

	public void setPermissions(String line) {
		//permissions.reset(); not needed, already done in permissions.load()
		permissions.load(line);
	}

	public TownsPermission getPermissions() {
		return permissions;
	}
}
