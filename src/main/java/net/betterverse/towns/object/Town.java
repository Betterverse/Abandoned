package net.betterverse.towns.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;

import net.betterverse.towns.AlreadyRegisteredException;
import net.betterverse.towns.EmptyNationException;
import net.betterverse.towns.EmptyTownException;
import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.lwc.TownsModule;
import net.betterverse.towns.wallgen.Wall;
import net.betterverse.towns.wallgen.WallSection;
import net.betterverse.towns.wallgen.Walled;

public class Town extends TownBlockOwner implements Walled, ResidentList {
	private List<Resident> residents = new ArrayList<Resident>();
	private List<Resident> assistants = new ArrayList<Resident>();
	private Wall wall = new Wall();
	private Resident mayor;
	private int bonusBlocks, purchasedBlocks;
	private double taxes, plotTax, commercialPlotTax, embassyPlotTax, plotPrice, commercialPlotPrice, embassyPlotPrice;
	private Nation nation;
	private boolean hasUpkeep, isPublic, isTaxPercentage;
	private String townBoard = "/town set board [msg]", tag;
	private TownBlock homeBlock;
	private TownsWorld world;
	private Location spawn;

	public Town(String name) {
		setName(name);
		tag = "";
		bonusBlocks = 0;
		purchasedBlocks = 0;
		taxes = 0.0;
		plotTax = 0.0;
		commercialPlotTax = 0;
		plotPrice = 0.0;
		hasUpkeep = true;
		isPublic = true;
		isTaxPercentage = false;
		permissions.loadDefault(this);
	}

	@Override
	public void addTownBlock(TownBlock townBlock) throws AlreadyRegisteredException {
		if (hasTownBlock(townBlock)) {
			throw new AlreadyRegisteredException();
		} else {
			townBlocks.add(townBlock);
			if (townBlocks.size() == 1 && ! hasHomeBlock()) {
				try {
					setHomeBlock(townBlock);
				} catch (TownsException e) {
				}
			}
		}
	}

	public void setTag(String text) throws TownsException {
		if (text.length() > 4) {
			throw new TownsException("Tag too long");
		}
		this.tag = text.toUpperCase();
		if (this.tag.matches(" ")) {
			this.tag = "";
		}
		setChangedName(true);
	}

	public String getTag() {
		return tag;
	}

	public boolean hasTag() {
		return ! tag.isEmpty();
	}

	public Resident getMayor() {
		return mayor;
	}

	public void setTaxes(double taxes) {
		if (isTaxPercentage) {
			if (taxes > TownsSettings.getMaxTaxPercent()) {
				this.taxes = TownsSettings.getMaxTaxPercent();
			} else {
				this.taxes = taxes;
			}
		} else if (taxes > TownsSettings.getMaxTax()) {
			this.taxes = TownsSettings.getMaxTax();
		} else {
			this.taxes = taxes;
		}
	}

	public double getTaxes() {
		setTaxes(taxes); //make sure the tax level is right.
		return taxes;
	}

	public void setMayor(Resident mayor) throws TownsException {
		if (! hasResident(mayor)) {
			throw new TownsException("Mayor doesn't belong to town.");
		}
		this.mayor = mayor;
	}

	public Nation getNation() throws NotRegisteredException {
		if (hasNation()) {
			return nation;
		} else {
			throw new NotRegisteredException("Town doesn't belong to any nation.");
		}
	}

	public void setNation(Nation nation) throws AlreadyRegisteredException {
		if (nation == null) {
			this.nation = null;
			return;
		}
		if (this.nation == nation) {
			return;
		}
		if (hasNation()) {
			throw new AlreadyRegisteredException();
		}
		this.nation = nation;
	}

	public List<Resident> getResidents() {
		return residents;
	}

	public List<Resident> getAssistants() {
		return assistants;
	}

	public boolean hasResident(String name) {
		for (Resident resident : residents) {
			if (resident.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasResident(Resident resident) {
		return residents.contains(resident);
	}

	public boolean hasAssistant(Resident resident) {
		return assistants.contains(resident);
	}

	public void addResident(Resident resident) throws AlreadyRegisteredException {
		addResidentCheck(resident);
		residents.add(resident);
		resident.setTown(this);
	}

	public void addResidentCheck(Resident resident) throws AlreadyRegisteredException {
		if (hasResident(resident)) {
			throw new AlreadyRegisteredException(resident.getName() + " already belongs to town.");
		} else if (resident.hasTown()) {
			try {
				if (! resident.getTown().equals(this)) {
					throw new AlreadyRegisteredException(resident.getName() + " already belongs to another town.");
				}
			} catch (NotRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void addAssistant(Resident resident) throws AlreadyRegisteredException {
		if (hasAssistant(resident)) {
			throw new AlreadyRegisteredException();
		} else {
			assistants.add(resident);
		}
	}

	public boolean isMayor(Resident resident) {
		return resident == mayor;
	}

	public boolean hasNation() {
		return nation != null;
	}

	public int getNumResidents() {
		return residents.size();
	}

	public boolean isCapital() {
		return hasNation() ? nation.isCapital(this) : false;
	}

	public void setHasUpkeep(boolean hasUpkeep) {
		this.hasUpkeep = hasUpkeep;
	}

	public boolean hasUpkeep() {
		return hasUpkeep;
	}

	public void setHasMobs(boolean hasMobs) {
		this.permissions.mobs = hasMobs;
	}

	public boolean hasMobs() {
		return this.permissions.mobs;
	}

	public void setPVP(boolean isPVP) {
		this.permissions.pvp = isPVP;
	}

	public boolean isPVP() {
		return this.permissions.pvp;
	}

	public void setBANG(boolean isBANG) {
		this.permissions.explosion = isBANG;
	}

	public boolean isBANG() {
		return this.permissions.explosion;
	}

	public void setTaxPercentage(boolean isPercentage) {
		this.isTaxPercentage = isPercentage;
		if (this.getTaxes() > 100) {
			this.setTaxes(0);
		}
	}

	public boolean isTaxPercentage() {
		return isTaxPercentage;
	}

	public void setFire(boolean isFire) {
		this.permissions.fire = isFire;
	}

	public boolean isFire() {
		return this.permissions.fire;
	}

	public void setTownBoard(String townBoard) {
		this.townBoard = townBoard;
	}

	public String getTownBoard() {
		return townBoard;
	}

	public void setBonusBlocks(int bonusBlocks) {
		this.bonusBlocks = bonusBlocks;
	}

	public int getTotalBlocks() {
		return TownsSettings.getMaxTownBlocks(this);
	}

	public int getBonusBlocks() {
		return bonusBlocks;
	}

	public void addBonusBlocks(int bonusBlocks) {
		this.bonusBlocks += bonusBlocks;
	}

	public void setPurchasedBlocks(int purchasedBlocks) {
		this.purchasedBlocks = purchasedBlocks;
	}

	public int getPurchasedBlocks() {
		return purchasedBlocks;
	}

	public void addPurchasedBlocks(int purchasedBlocks) {
		this.purchasedBlocks += purchasedBlocks;
	}

	/**
	 * @param homeBlock
	 * @return true if the world/homeblock has changed
	 * @throws TownsException
	 */
	public boolean setHomeBlock(TownBlock homeBlock) throws TownsException {
		if (homeBlock == null) {
			this.homeBlock = null;
			return false;
		}
		if (! hasTownBlock(homeBlock)) {
			throw new TownsException("Town has no claim over this town block.");
		}
		this.homeBlock = homeBlock;

		// Set the world as it may have changed
		if (this.world != homeBlock.getWorld()) {
			if ((world != null) && (world.hasTown(this))) {
				world.removeTown(this);
			}

			setWorld(homeBlock.getWorld());
		}

		try {
			setSpawn(spawn);
		} catch (TownsException e) {
			spawn = null;
		} catch (NullPointerException e) {
			// In the event that spawn is already null
		}

		return true;
	}

	public TownBlock getHomeBlock() throws TownsException {
		if (hasHomeBlock()) {
			return homeBlock;
		} else {
			throw new TownsException("Town has not set a home block.");
		}
	}

	/**
	 * Sets the world this town belongs to. If it's a world change it will
	 * remove the town from the old world and place in the new.
	 *
	 * @param world
	 */
	public void setWorld(TownsWorld world) {
		if (world == null) {
			this.world = null;
			return;
		}
		if (this.world == world) {
			return;
		}

		if (hasWorld()) {
			try {
				world.removeTown(this);
			} catch (NotRegisteredException e) {
			}
		}

		this.world = world;

		try {
			this.world.addTown(this);
		} catch (AlreadyRegisteredException e) {
		}
	}

	/**
	 * Fetch the World this town is registered too. If (for any reason) it's
	 * null it will attempt to find the owning world from TownsUniverse.
	 *
	 * @return world or null
	 */
	public TownsWorld getWorld() {
		if (world != null) {
			return world;
		}

		return TownsUniverse.getTownWorld(this.getName());
	}

	public boolean hasMayor() {
		return mayor != null;
	}

	public void removeResident(Resident resident) throws EmptyTownException, NotRegisteredException {
		if (! hasResident(resident)) {
			throw new NotRegisteredException();
		} else {
			remove(resident);

			if (getNumResidents() == 0) {
				try {
					clear();
					throw new EmptyTownException(this);
				} catch (EmptyNationException e) {
					throw new EmptyTownException(this, e);
				}
			}
		}
	}

	private void removeAllResidents() {
		for (Resident resident : new ArrayList<Resident>(residents)) {
			remove(resident);
		}
	}

	private void remove(Resident resident) {
		//LWC <-> Towns Hook
		if (TownsModule.hooked) {
			TownsModule.onResidentRemoved(this, resident);
		}

		for (TownBlock townBlock : new ArrayList<TownBlock>(resident.getTownBlocks())) {
			townBlock.setResident(null);
			try {
				townBlock.setPlotPrice(townBlock.getTown().getPlotPrice());
			} catch (NotRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//getPlugin().getTownsUniverse().getDataSource().saveResident(resident); //TODO: BAD!
		}

		if (isMayor(resident)) {
			if (residents.size() > 1) {
				for (Resident assistant : new ArrayList<Resident>(getAssistants())) {
					if (assistant != resident) {
						try {
							setMayor(assistant);
							continue;
						} catch (TownsException e) {
							// Error setting mayor.
							e.printStackTrace();
						}
					}
				}
				if (isMayor(resident)) {
					// Still mayor and no assistants so pick a resident to be mayor
					for (Resident newMayor : new ArrayList<Resident>(getResidents())) {
						if (newMayor != resident) {
							try {
								setMayor(newMayor);
								continue;
							} catch (TownsException e) {
								// Error setting mayor.
								e.printStackTrace();
							}
						}
					}
				}
			}
		}

		if (hasNation() && nation.hasAssistant(resident)) {
			try {
				nation.removeAssistant(resident);
			} catch (NotRegisteredException e) {
			}
		}
		if (hasAssistant(resident)) {
			try {
				removeAssistant(resident);
			} catch (NotRegisteredException e) {
			}
		}

		try {
			resident.setTown(null);
		} catch (AlreadyRegisteredException e) {
		}
		residents.remove(resident);
	}

	public void removeAssistant(Resident resident) throws NotRegisteredException {
		if (! hasAssistant(resident)) {
			throw new NotRegisteredException();
		} else {
			assistants.remove(resident);
		}
	}

	public void setSpawn(Location spawn) throws TownsException {
		if (! hasHomeBlock()) {
			throw new TownsException("Home Block has not been set");
		}
		Coord spawnBlock = Coord.parseCoord(spawn);
		if (homeBlock.getX() == spawnBlock.getX() && homeBlock.getZ() == spawnBlock.getZ()) {
			this.spawn = spawn;
		} else {
			throw new TownsException("Spawn is not within the homeBlock.");
		}
	}

	public Location getSpawn() throws TownsException {
		if (hasHomeBlock() && spawn != null) {
			return spawn;
		} else {
			this.spawn = null;
			throw new TownsException("Town has not set a spawn location.");
		}
	}

	public boolean hasSpawn() {
		return (hasHomeBlock() && spawn != null);
	}

	public boolean hasHomeBlock() {
		return homeBlock != null;
	}

	public void clear() throws EmptyNationException {
		//Cleanup
		removeAllResidents();
		mayor = null;
		residents.clear();
		assistants.clear();
		homeBlock = null;

		try {
			if (hasWorld()) {
				world.removeTownBlocks(getTownBlocks());
				world.removeTown(this);
			}
		} catch (NotRegisteredException e) {
		}
		if (hasNation()) {
			try {
				nation.removeTown(this);
			} catch (NotRegisteredException e) {
			}
		}
	}

	private boolean hasWorld() {
		return world != null;
	}

	@Override
	public void removeTownBlock(TownBlock townBlock) throws NotRegisteredException {
		if (! hasTownBlock(townBlock)) {
			throw new NotRegisteredException();
		} else {
			try {
				if (getHomeBlock() == townBlock) {
					setHomeBlock(null);
				}
			} catch (TownsException e) {
			}
			townBlocks.remove(townBlock);
		}
	}

	public void setPlotPrice(double plotPrice) {
		this.plotPrice = plotPrice;
	}

	public double getPlotPrice() {
		return plotPrice;
	}

	public void setCommercialPlotPrice(double commercialPlotPrice) {
		this.commercialPlotPrice = commercialPlotPrice;
	}

	public double getCommercialPlotPrice() {
		return commercialPlotPrice;
	}

	public void setEmbassyPlotPrice(double embassyPlotPrice) {
		this.embassyPlotPrice = embassyPlotPrice;
	}

	public double getEmbassyPlotPrice() {
		return embassyPlotPrice;
	}

	public Wall getWall() {
		return wall;
	}

	public List<WallSection> getWallSections() {
		return getWall().getWallSections();
	}

	public void setWallSections(List<WallSection> wallSections) {
		getWall().setWallSections(wallSections);
	}

	public boolean hasWallSection(WallSection wallSection) {
		return getWall().hasWallSection(wallSection);
	}

	public void addWallSection(WallSection wallSection) {
		getWall().addWallSection(wallSection);
	}

	public void removeWallSection(WallSection wallSection) {
		getWall().removeWallSection(wallSection);
	}

	public boolean isHomeBlock(TownBlock townBlock) {
		return hasHomeBlock() ? townBlock == homeBlock : false;
	}

	public void setPlotTax(double plotTax) {
		this.plotTax = plotTax;
	}

	public double getPlotTax() {
		return plotTax;
	}

	public void setCommercialPlotTax(double commercialTax) {
		this.commercialPlotTax = commercialTax;
	}

	public double getCommercialPlotTax() {
		return commercialPlotTax;
	}

	public void setEmbassyPlotTax(double embassyPlotTax) {
		this.embassyPlotTax = embassyPlotTax;
	}

	public double getEmbassyPlotTax() {
		return embassyPlotTax;
	}

	public void withdrawFromBank(Resident resident, int amount) throws TownsException {
		if (! isMayor(resident) && ! hasAssistant(resident)) {
			throw new TownsException("You don't have access to the town's bank.");
		}

		if (TownsSettings.isUsingEconomy()) {
			if (! payTo(amount, resident, "Town Widthdraw")) {
				throw new TownsException("There is not enough money in the bank.");
			}
		} else {
			throw new TownsException("Economy has not been turned on.");
		}
	}

	@Override
	public List<String> getTreeString(int depth) {
		List<String> out = new ArrayList<String>();
		out.add(getTreeDepth(depth) + "Town (" + getName() + ")");
		out.add(getTreeDepth(depth + 1) + "Mayor: " + (hasMayor() ? getMayor().getName() : "None"));
		out.add(getTreeDepth(depth + 1) + "Home: " + homeBlock);
		out.add(getTreeDepth(depth + 1) + "Bonus: " + bonusBlocks);
		out.add(getTreeDepth(depth + 1) + "TownBlocks (" + getTownBlocks().size() + "): " /*+ getTownBlocks()*/);
		if (getAssistants().size() > 0) {
			out.add(getTreeDepth(depth + 1) + "Assistants (" + getAssistants().size() + "): " + Arrays.toString(getAssistants().toArray(new Resident[0])));
		}
		out.add(getTreeDepth(depth + 1) + "Residents (" + getResidents().size() + "):");
		for (Resident resident : getResidents()) {
			out.addAll(resident.getTreeString(depth + 2));
		}
		return out;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public boolean isPublic() {
		return isPublic;
	}
}
