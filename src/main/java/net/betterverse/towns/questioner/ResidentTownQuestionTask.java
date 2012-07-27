package net.betterverse.towns.questioner;

import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;

public class ResidentTownQuestionTask extends TownsQuestionTask {
	protected Resident resident;
	protected Town town;

	public ResidentTownQuestionTask(Resident resident, Town town) {
		this.resident = resident;
		this.town = town;
	}

	public Resident getResident() {
		return resident;
	}

	public Town getTown() {
		return town;
	}

	@Override
	public void run() {
	}
}
