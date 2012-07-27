package net.betterverse.towns.questioner;

import net.betterverse.towns.object.Town;

public class TownQuestionTask extends TownsQuestionTask {
	protected Town town;

	public TownQuestionTask(Town town) {
		this.town = town;
	}

	public Town getTown() {
		return town;
	}

	@Override
	public void run() {
	}
}
