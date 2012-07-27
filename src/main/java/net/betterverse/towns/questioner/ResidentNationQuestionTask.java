package net.betterverse.towns.questioner;

import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.Resident;

public class ResidentNationQuestionTask extends TownsQuestionTask {
	protected Resident resident;
	protected Nation nation;

	public ResidentNationQuestionTask(Resident resident, Nation nation) {
		this.resident = resident;
		this.nation = nation;
	}

	public Resident getResident() {
		return resident;
	}

	public Nation getNation() {
		return nation;
	}

	@Override
	public void run() {
	}
}
