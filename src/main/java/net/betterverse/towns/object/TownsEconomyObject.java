package net.betterverse.towns.object;

import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsLogger;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.util.StringMgmt;

public class TownsEconomyObject extends TownsObject {
	private static Towns plugin;
	private static final String townAccountPrefix = "town-";
	private static final String nationAccountPrefix = "nation-";

	public static void setPlugin(Towns plugin) {
		TownsEconomyObject.plugin = plugin;
	}

	/**
	 * Tries to pay from the players main bank account first, if it fails try their holdings
	 *
	 * @param n
	 * @return if successfully payed amount to 'server'.
	 * @throws EconomyException
	 */
	public boolean pay(double n, String reason)  {
		boolean payed = _pay(n);
		if (payed) {
			TownsLogger.logMoneyTransaction(this, n, null, reason);
		}
		return payed;
	}

	public boolean pay(double n) {
		return pay(n, null);
	}

	private boolean _pay(double n) {
		if (canPayFromHoldings(n)) {
			TownsMessaging.sendDebugMsg("Can Pay: " + n);
			//Towns.economy.withdrawPlayer(townAccountPrefix, n)
			/*
			 * TODO all this, it's wierd
			 */
			Towns.economy.withdrawPlayer(getEconomyName(), n);
			return true;
		}
		return false;
	}

	/**
	 * When collecting money add it to the Accounts bank
	 *
	 * @param n
	 * @throws EconomyException
	 */
	public void collect(double n, String reason)  {
		_collect(n);
		TownsLogger.logMoneyTransaction(null, n, this, reason);
	}

	public void collect(double n)  {
		collect(n, null);
	}

	private void _collect(double n)  {
		Towns.economy.depositPlayer(getEconomyName(), n);
	}

	/**
	 * When one account is paying another account(Taxes/Plot Purchasing)
	 *
	 * @param n
	 * @param collector
	 * @return if successfully payed amount to collector.
	 * @throws EconomyException
	 */
	public boolean payTo(double n, TownsEconomyObject collector, String reason) {
		boolean payed = _payTo(n, collector);
		if (payed) {
			TownsLogger.logMoneyTransaction(this, n, collector, reason);
		}
		return payed;
	}

	public boolean payTo(double n, TownsEconomyObject collector)  {
		return payTo(n, collector, null);
	}

	private boolean _payTo(double n, TownsEconomyObject collector)  {
		if (_pay(n)) {
			collector._collect(n);
			return true;
		} else {
			return false;
		}
	}

	public String getEconomyName() {
		// TODO: Make this less hard coded.
		if (this instanceof Nation) {
			return StringMgmt.trimMaxLength(nationAccountPrefix + getName(), 32);
		} else if (this instanceof Town) {
			return StringMgmt.trimMaxLength(townAccountPrefix + getName(), 32);
		} else {
			return getName();
		}
	}

	public void setBalance(double value) {
		if(Towns.economy.has(getEconomyName(), value))
			Towns.economy.withdrawPlayer(getEconomyName(), Towns.economy.getBalance(getEconomyName())-value);
		else
			Towns.economy.depositPlayer(getEconomyName(), value-Towns.economy.getBalance(getEconomyName()));
	}

	public double getHoldingBalance() {
		return Towns.economy.getBalance(getEconomyName());
	}

	public boolean canPayFromHoldings(double n) {
		if (getHoldingBalance() - n >= 0) {
			return true;
		} else {
			return false;
		}
	}

	public static String getEconomyCurrency() {
		return Towns.economy.format(1).split(" ")[1];
	}

	/* Used To Get Balance of Players holdings in String format for printing*/
	public String getHoldingFormattedBalance()  {
		return Towns.economy.format(getHoldingBalance());
	}

	public static String getFormattedBalance(double balance) {
		return Towns.economy.format(balance);
	}
}
