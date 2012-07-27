package net.betterverse.towns.questioner;

import net.betterverse.towns.object.Resident;

public class ResidentQuestionTask extends TownsQuestionTask {
	protected Resident resident;

	public ResidentQuestionTask(Resident resident) {
		this.resident = resident;
	}

	public Resident getResident() {
		return resident;
	}

	@Override
	public void run() {
	}
}
