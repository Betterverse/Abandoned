package net.betterverse.signshop;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SSShop {

	Location loc;
	String player;


	SSShop(String s) throws NumberFormatException, ArrayIndexOutOfBoundsException {
		this.loc = new Location(Bukkit.getServer().getWorld(s.split(":")[0]), (double)Integer.parseInt(s.split(":")[1]), (double)Integer.parseInt(s.split(":")[2]), (double)Integer.parseInt(s.split(":")[3]));
		this.player = s.split(":")[4];
	}

	SSShop(Location loc, String player) {
		this.loc = loc;
		this.player = player;
	}

	public String toString() {
		return this.loc.getWorld().getName() + ":" + this.loc.getBlockX() + ":" + this.loc.getBlockY() + ":" + this.loc.getBlockZ() + ":" + this.player;
	}

	public boolean equals(Object other) {
		return other instanceof SSShop && ((SSShop)other).loc.equals(this.loc);
	}
}
