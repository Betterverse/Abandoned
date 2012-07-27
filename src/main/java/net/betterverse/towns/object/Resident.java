package net.betterverse.towns.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.betterverse.towns.AlreadyRegisteredException;
import net.betterverse.towns.EmptyTownException;
import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.TownsException;

public class Resident extends TownBlockOwner {
	private List<Resident> friends = new ArrayList<Resident>();
	private Town town;
	private long lastOnline, registered;
	private boolean isNPC = false;
	private String title, surname;
	private long teleportRequestTime;
	private Town teleportDestination;
	private double teleportCost;
	private String chatFormattedName;

	public Resident(String name) {
		setChatFormattedName(name);
		setName(name);
		setTitle("");
		setSurname("");
		permissions.loadDefault(this);
		teleportRequestTime = - 1;
		teleportCost = 0.0;
	}

	public void setLastOnline(long lastOnline) {
		this.lastOnline = lastOnline;
	}

	public long getLastOnline() {
		return lastOnline;
	}

	public void setNPC(boolean isNPC) {
		this.isNPC = isNPC;
	}

	public boolean isNPC() {
		return isNPC;
	}

	public void setTitle(String title) {
		if (title.matches(" ")) {
			title = "";
		}
		this.title = title;
		setChangedName(true);
	}

	public String getTitle() {
		return title;
	}

	public boolean hasTitle() {
		return ! title.isEmpty();
	}

	public void setSurname(String surname) {
		if (surname.matches(" ")) {
			surname = "";
		}
		this.surname = surname;
		setChangedName(true);
	}

	public String getSurname() {
		return surname;
	}

	public boolean hasSurname() {
		return ! surname.isEmpty();
	}

	public boolean isKing() {
		try {
			return getTown().getNation().isKing(this);
		} catch (TownsException e) {
			return false;
		}
	}

	public boolean isMayor() {
		return hasTown() ? town.isMayor(this) : false;
	}

	public boolean hasTown() {
		return ! (town == null);
	}

	public boolean hasNation() {
		return hasTown() ? town.hasNation() : false;
	}

	public Town getTown() throws NotRegisteredException {
		if (hasTown()) {
			return town;
		} else {
			throw new NotRegisteredException("Resident doesn't belong to any town");
		}
	}

	public void setTown(Town town) throws AlreadyRegisteredException {
		if (town == null) {
			this.town = null;
			setTitle("");
			setSurname("");
			return;
		}
		if (this.town == town) {
			return;
		}
		if (hasTown()) {
			throw new AlreadyRegisteredException();
		}
		this.town = town;
		setTitle("");
		setSurname("");
	}

	public void setFriends(List<Resident> newFriends) {
		friends = newFriends;
	}

	public List<Resident> getFriends() {
		return friends;
	}

	public boolean removeFriend(Resident resident) throws NotRegisteredException {
		if (hasFriend(resident)) {
			return friends.remove(resident);
		} else {
			throw new NotRegisteredException();
		}
	}

	public boolean hasFriend(Resident resident) {
		return friends.contains(resident);
	}

	public void addFriend(Resident resident) throws AlreadyRegisteredException {
		if (hasFriend(resident)) {
			throw new AlreadyRegisteredException();
		} else {
			friends.add(resident);
		}
	}

	public void removeAllFriends() {
		for (Resident resident : new ArrayList<Resident>(friends)) {
			try {
				removeFriend(resident);
			} catch (NotRegisteredException e) {
			}
		}
	}

	public void clear() throws EmptyTownException {
		removeAllFriends();
		setLastOnline(0);

		if (hasTown()) {
			try {
				town.removeResident(this);
				setTitle("");
				setSurname("");
			} catch (NotRegisteredException e) {
			}
		}
	}

	public void setRegistered(long registered) {
		this.registered = registered;
	}

	public long getRegistered() {
		return registered;
	}

	@Override
	public List<String> getTreeString(int depth) {
		List<String> out = new ArrayList<String>();
		out.add(getTreeDepth(depth) + "Resident (" + getName() + ")");
		out.add(getTreeDepth(depth + 1) + "Registered: " + getRegistered());
		out.add(getTreeDepth(depth + 1) + "Last Online: " + getLastOnline());
		if (getFriends().size() > 0) {
			out.add(getTreeDepth(depth + 1) + "Friends (" + getFriends().size() + "): " + Arrays.toString(getFriends().toArray(new Resident[0])));
		}
		return out;
	}

	public void clearTeleportRequest() {
		teleportRequestTime = - 1;
	}

	public void setTeleportRequestTime() {
		teleportRequestTime = System.currentTimeMillis();
	}

	public long getTeleportRequestTime() {
		return teleportRequestTime;
	}

	public void setTeleportDestination(Town town) {
		teleportDestination = town;
	}

	public Town getTeleportDestination() {
		return teleportDestination;
	}

	public boolean hasRequestedTeleport() {
		return teleportRequestTime != - 1;
	}

	public void setTeleportCost(double cost) {
		teleportCost = cost;
	}

	public double getTeleportCost() {
		return teleportCost;
	}

	/**
	 * @return the chatFormattedName
	 */
	public String getChatFormattedName() {
		return chatFormattedName;
	}

	/**
	 * @param chatFormattedName the chatFormattedName to set
	 */
	public void setChatFormattedName(String chatFormattedName) {
		this.chatFormattedName = chatFormattedName;
		setChangedName(false);
	}
}
