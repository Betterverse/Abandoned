package net.betterverse.nameeffects;

import net.betterverse.nameeffects.commands.AliasCommand;
import net.betterverse.nameeffects.commands.PrefixCommand;
import net.betterverse.nameeffects.listeners.PlayerListener;
import net.betterverse.nameeffects.objects.AliasPlayer;
import net.betterverse.nameeffects.util.PersistUtil;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class NameEffects extends JavaPlugin {
    private PluginManager pm;

    public Economy economy;
    public Chat chat;
    public Permission permission;
    public Boolean hasCreditsShop;

    public int pprice;

    public Set<String> expired = new HashSet<String>();

    public Map<String, AliasPlayer> players;

    public List<String> blocked = new ArrayList<String>();
    public List<String> ccodes = new ArrayList<String>();

    @Override
    public void onDisable() {
        PersistUtil.saveAliasPlayers(players);
    }

    @Override
    public void onEnable() {
        pm = getServer().getPluginManager();

        if (!setupPlugins()) {
            pm.disablePlugin(this);
            return;
        }

        pm.registerEvents(new PlayerListener(this), this);

        getCommand("prefix").setExecutor(new PrefixCommand(this));
        getCommand("alias").setExecutor(new AliasCommand(this));

        blocked.add("notch");
        ccodes.add("&1");

        getConfig().addDefault("PrefixPrice", 10);
        getConfig().addDefault("BlockedAliases", blocked);
        getConfig().addDefault("BlockedColorCodes", ccodes);
        getConfig().options().copyDefaults(true);
        saveConfig();

        pprice = getConfig().getInt("PrefixPrice");
        blocked = getConfig().getStringList("BlockedAliases");
        ccodes = getConfig().getStringList("BlockedColorCodes");

        PersistUtil.initialize();

        players = PersistUtil.getAliasPlayers();

        for (Player player : getServer().getOnlinePlayers()) {
            PersistUtil.addPlayerToAliases(this, player);
        }
    }

    public String getPrefix(String player) {
        if (players.containsKey(player)) {
            return players.get(player).getPrefix();
        } else {
            return null;
        }
    }

    public String getPrefix(Player player) {
        return getPrefix(player.getName());
    }

    public AliasPlayer getAliasPlayer(String player) {
        if (players.containsKey(player)) {
            return players.get(player);
        } else {
            return null;
        }
    }

    public AliasPlayer getAliasPlayer(Player player) {
        return getAliasPlayer(player.getName());
    }

    private Boolean setupPlugins() {
        if (pm.getPlugin("Vault") != null) {
            RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
            if (chatProvider != null) {
                chat = chatProvider.getProvider();
            }

            RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
            if (permissionProvider != null) {
                permission = permissionProvider.getProvider();
            }

            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
            }
        }

        hasCreditsShop = pm.getPlugin("CreditsShop") != null;

        return economy != null || hasCreditsShop;
    }
}