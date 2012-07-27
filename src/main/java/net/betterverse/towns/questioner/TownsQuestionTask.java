package net.betterverse.towns.questioner;

import net.betterverse.questioner.BukkitQuestionTask;
import net.betterverse.towns.Towns;
import net.betterverse.towns.object.TownsUniverse;

public abstract class TownsQuestionTask extends BukkitQuestionTask {
	protected Towns towns;
	protected TownsUniverse universe;

	public TownsUniverse getUniverse() {
		return universe;
	}

	public void setTowns(Towns towns) {
		this.towns = towns;
		this.universe = towns.getTownsUniverse();
	}

	@Override
	public abstract void run();
}
