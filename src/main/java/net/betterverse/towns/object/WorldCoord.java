package net.betterverse.towns.object;

import net.betterverse.towns.NotRegisteredException;

//TODO: have toString() include the worlds?
public class WorldCoord extends Coord {
	private TownsWorld world;

	public TownsWorld getWorld() {
		return world;
	}

	public void setWorld(TownsWorld world) {
		this.world = world;
	}

	public WorldCoord(TownsWorld world, int x, int z) {
		super(x, z);
		this.world = world;
	}

	public WorldCoord(TownsWorld world, Coord coord) {
		super(coord);
		this.world = world;
	}

	public WorldCoord(WorldCoord worldCoord) {
		super(worldCoord);
		this.world = worldCoord.getWorld();
	}

	public Coord getCoord() {
		return new Coord(x, z);
	}

	public TownBlock getTownBlock() throws NotRegisteredException {
		return getWorld().getTownBlock(getCoord());
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + x;
		result = 31 * result + z;
		result = 31 * result + world.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (! (obj instanceof Coord)) {
			return false;
		}

		if (! (obj instanceof WorldCoord)) {
			Coord o = (Coord) obj;
			return this.x == o.x && this.z == o.z;
		}

		WorldCoord o = (WorldCoord) obj;
		return this.x == o.x && this.z == o.z && this.world.equals(o.world);
	}

	@Override
	public String toString() {
		return world.getName() + "," + super.toString();
	}
}
