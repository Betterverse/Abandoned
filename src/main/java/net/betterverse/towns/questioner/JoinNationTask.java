package net.betterverse.towns.questioner;

import net.betterverse.towns.AlreadyRegisteredException;
import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.util.ChatTools;

public class JoinNationTask extends ResidentNationQuestionTask {
	public JoinNationTask(Resident resident, Nation nation) {
		super(resident, nation);
	}

	@Override
	public void run() {
		try {
			nation.addTown(resident.getTown());
			//towns.deleteCache(resident.getName());
			TownsUniverse.getDataSource().saveResident(resident);
			TownsUniverse.getDataSource().saveTown(resident.getTown());
			TownsUniverse.getDataSource().saveNation(nation);

			TownsMessaging.sendNationMessage(nation, ChatTools.color(String.format(TownsSettings.getLangString("msg_join_nation"), resident.getTown().getName())));
		} catch (AlreadyRegisteredException e) {
			try {
				TownsMessaging.sendResidentMessage(resident, e.getError());
			} catch (TownsException e1) {
			}
		} catch (NotRegisteredException e) {
			// TODO somehow this person is not the town mayor
			e.printStackTrace();
		}
	}
}
