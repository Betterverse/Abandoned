package net.betterverse.towns.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownsEconomyObject;
import net.betterverse.towns.war.War;
import net.betterverse.towns.war.WarSpoils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TownsEntityMonitorListener implements Listener {

	private final Towns plugin;

	public TownsEntityMonitorListener(Towns plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void entityDamage(EntityDamageEvent event) {
		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
			Entity attackerEntity = entityEvent.getDamager();
			Entity defenderEntity = entityEvent.getEntity();

			if (defenderEntity instanceof Player) {
				Player defenderPlayer = (Player) defenderEntity;
				Player attackerPlayer = null;
				if (defenderPlayer.getHealth() > 0) {
					return;
				}

				Resident attackerResident = null;
				Resident defenderResident = null;

				try {
					defenderResident = plugin.getTownsUniverse().getResident(defenderPlayer.getName());
				} catch (NotRegisteredException e) {
					return;
				}

				if (attackerEntity instanceof Player) {
					attackerPlayer = (Player) attackerEntity;
					try {
						attackerResident = plugin.getTownsUniverse().getResident(attackerPlayer.getName());
					} catch (NotRegisteredException e) {
					}
				}

				deathPayment(attackerPlayer, defenderPlayer, attackerResident, defenderResident);
				wartimeDeathPoints(attackerPlayer, defenderPlayer, attackerResident, defenderResident);

				if (TownsSettings.isRemovingOnMonarchDeath()) {
					monarchDeath(attackerPlayer, defenderPlayer, attackerResident, defenderResident);
				}
			}
		}
	}

	private void wartimeDeathPoints(Player attackerPlayer, Player defenderPlayer, Resident attackerResident, Resident defenderResident) {
		if (attackerPlayer != null && plugin.getTownsUniverse().isWarTime()) {
			try {
				if (attackerResident == null) {
					throw new NotRegisteredException();
				}

				Town town = attackerResident.getTown();
				if (TownsSettings.getWarPointsForKill() > 0) {
					plugin.getTownsUniverse().getWarEvent().townScored(town, TownsSettings.getWarPointsForKill());
				}
			} catch (NotRegisteredException e) {
			}
		}
	}

	private void monarchDeath(Player attackerPlayer, Player defenderPlayer, Resident attackerResident, Resident defenderResident) {
		if (plugin.getTownsUniverse().isWarTime()) {
			War warEvent = plugin.getTownsUniverse().getWarEvent();
			try {
				Nation defenderNation = defenderResident.getTown().getNation();
				if (warEvent.isWarringNation(defenderNation)) {
					if (defenderResident.isMayor()) {
						if (defenderResident.isKing()) {
							if (attackerResident != null && attackerResident.hasTown()) {
								warEvent.remove(attackerResident.getTown(), defenderNation);
							} else {
								warEvent.remove(defenderNation);
							}
							TownsMessaging.sendGlobalMessage(defenderNation.getName() + "'s king was killed. Nation removed from war.");
						} else {
							if (attackerResident != null && attackerResident.hasTown()) {
								warEvent.remove(attackerResident.getTown(), defenderResident.getTown());
							} else {
								warEvent.remove(defenderResident.getTown());
							}
							TownsMessaging.sendGlobalMessage(defenderResident.getTown() + "'s mayor was killed. Town removed from war.");
						}
					}
				}
			} catch (NotRegisteredException e) {
			}
		}
	}

	public void deathPayment(Player attackerPlayer, Player defenderPlayer, Resident attackerResident, Resident defenderResident) {
		if (attackerPlayer != null && plugin.getTownsUniverse().isWarTime() && TownsSettings.getWartimeDeathPrice() > 0) {
			try {
				if (attackerResident == null) {
					throw new NotRegisteredException();
				}

				double price = TownsSettings.getWartimeDeathPrice();
				double townPrice = 0;
				if (!defenderResident.canPayFromHoldings(price)) {
					townPrice = price - defenderResident.getHoldingBalance();
					price = defenderResident.getHoldingBalance();
				}

				if (price > 0) {
					defenderResident.payTo(price, attackerResident, "Death Payment (War)");
					TownsMessaging.sendMsg(attackerPlayer, "You robbed " + defenderResident.getName() + " of " + price + " " + TownsEconomyObject.getEconomyCurrency() + ".");
					TownsMessaging.sendMsg(defenderPlayer, attackerResident.getName() + " robbed you of " + price + " " + TownsEconomyObject.getEconomyCurrency() + ".");
				}

				// Resident doesn't have enough funds.
				if (townPrice > 0) {
					Town town = defenderResident.getTown();
					if (!town.canPayFromHoldings(townPrice)) {
						// Town doesn't have enough funds.
						townPrice = town.getHoldingBalance();
						try {
							plugin.getTownsUniverse().getWarEvent().remove(attackerResident.getTown(), town);
						} catch (NotRegisteredException e) {
							plugin.getTownsUniverse().getWarEvent().remove(town);
						}
					} else {
						TownsMessaging.sendTownMessage(town, defenderResident.getName() + "'s wallet couldn't satisfy " + attackerResident.getName() + ". " + townPrice + " taken from town bank.");
					}
					town.payTo(townPrice, attackerResident, String.format("Death Payment (War) (%s couldn't pay)", defenderResident.getName()));
				}
			} catch (NotRegisteredException e) {
			}
		} else if (TownsSettings.getDeathPrice() > 0) {
			double price = TownsSettings.getDeathPrice();
			if (!defenderResident.canPayFromHoldings(price)) {
				price = defenderResident.getHoldingBalance();
			}

			defenderResident.payTo(price, new WarSpoils(), "Death Payment");
			TownsMessaging.sendMsg(defenderPlayer, "You lost " + price + " " + TownsEconomyObject.getEconomyCurrency() + ".");

		}
	}
}
