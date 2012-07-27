package net.betterverse.towns.war;

import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.tasks.TownsTimerTask;

public class StartWarTimerTask extends TownsTimerTask {
	public StartWarTimerTask(TownsUniverse universe) {
		super(universe);
	}

	@Override
	public void run() {
		universe.getWarEvent().start();
	}
}
