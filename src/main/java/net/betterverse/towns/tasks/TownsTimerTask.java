package net.betterverse.towns.tasks;

import java.util.TimerTask;

import net.betterverse.towns.Towns;
import net.betterverse.towns.object.TownsUniverse;

public abstract class TownsTimerTask extends TimerTask {
	protected TownsUniverse universe;
	protected Towns plugin;

	public TownsTimerTask(TownsUniverse universe) {
		this.universe = universe;
		this.plugin = universe.getPlugin();
	}

	//@Override
	//public void run() {
	//}
}
