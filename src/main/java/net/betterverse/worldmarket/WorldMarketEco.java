package net.betterverse.worldmarket;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * WorldMarketEco 1.0
 * 
 * Not even sure if this works.
 * Should theoretically be a very easy to use economy
 * class able to be passed to the other classes for use of economy
 * functions.
 * 
 * @author codename_B
 */
public class WorldMarketEco {
	
	 public static Economy economy = null;
	
	public WorldMarketEco() {
		setupEconomy();
	}

	public boolean hasOver(String player, double amount) {
		if(!economy.hasAccount(player))
			return false;
		
		return economy.has(player, amount);
	}

	public void charge(String player, double amount) {
		economy.withdrawPlayer(player, amount);
	}

	 private Boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

}
