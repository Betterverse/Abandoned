package net.betterverse.worldmarket;

import java.text.SimpleDateFormat;
import java.util.Date;
import net.betterverse.protect.utils.ProtectedPolygon;
import org.bukkit.Server;
import org.bukkit.World;

/**
 * WorldMarketTick 1.0
 * 
 * A simple midnight checker as requested by Asphodan
 * 
 * @author codename_B
 */
public class WorldMarketTick extends Thread {

	private final WorldMarketManager wmm;
	private final Server server;
	private boolean isRunning = true;

	public WorldMarketTick(WorldMarketManager wmm) {
		this.wmm = wmm;
		this.server = wmm.server;
	}

	/**
	 * Used to disable the thread neatly
	 * 
	 * @param running
	 */
	public void setRunning(boolean running) {
		this.isRunning = running;
	}

	@Override
	public void run() {
		while (isRunning) {
			try {
				System.out.println("checking for rent!");
				if (chargeRent())
					doRent();
				for (int i=0; i<600 && isRunning; i++)
				if (isRunning)
				sleep(100);
			} catch (Exception e) {

			}
		}
		interrupt();
	}

	/**
	 * Loop through all worlds and charge rent accordingly
	 */
	public void doRent() {
		for (World world : server.getWorlds()) {
			ProtectedPolygon[] regions = wmm.getRentedRegions(world);
			for (ProtectedPolygon region : regions) {
				String player = region.getOwners().get(0);
				wmm.rentPolygon(player, world, region);
			}
		}
	}

	/**
	 * Is it that time again?
	 * 
	 * @return true/false
	 */
	public boolean chargeRent() {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		String date = format.format(new Date());
		System.out.println(date);
		if (date.equals("00:00"))
			return true;
		else
			return false;
	}
}
