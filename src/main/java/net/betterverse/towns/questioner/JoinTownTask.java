package net.betterverse.towns.questioner;

import net.betterverse.towns.AlreadyRegisteredException;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.util.ChatTools;

public class JoinTownTask extends ResidentTownQuestionTask {
	public JoinTownTask(Resident resident, Town town) {
		super(resident, town);
	}

	@Override
	public void run() {
		try {
			town.addResident(resident);
			towns.deleteCache(resident.getName());
			TownsUniverse.getDataSource().saveResident(resident);
			TownsUniverse.getDataSource().saveTown(town);

			TownsMessaging.sendTownMessage(town, ChatTools.color(String.format(TownsSettings.getLangString("msg_join_town"), resident.getName())));
		} catch (AlreadyRegisteredException e) {
			try {
				TownsMessaging.sendResidentMessage(resident, e.getError());
			} catch (TownsException e1) {
			}
		}
	}
}
