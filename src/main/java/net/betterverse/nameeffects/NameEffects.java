package net.betterverse.nameeffects;

import net.betterverse.nameeffects.commands.AliasCommand;
import net.betterverse.nameeffects.commands.PrefixCommand;
import net.betterverse.nameeffects.objects.AliasPlayer;
import net.betterverse.nameeffects.util.PersistUtil;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class NameEffects extends JavaPlugin {
    private PluginManager pm;
    private static NameEffects instance;

    public Chat chat;
    public Permission permission;
    public Boolean hasCreditsShop;

    public int pprice;

    public Set<String> expired = new HashSet<String>();

    public Map<String, AliasPlayer> players = null;

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

        instance = this;

        players = PersistUtil.getAliasPlayers();

        scanForInvalid();
    }

    public AliasPlayer getAliasPlayer(String player) {
        if (players == null) {
            players = new HashMap<String, AliasPlayer>();
        }
        if (players.containsKey(player)) {
            return players.get(player);
        } else {
            return new AliasPlayer(player, null, null);
        }
    }

    public AliasPlayer getAliasPlayer(Player player) {
        return getAliasPlayer(player.getName());
    }

    public static NameEffects getInstance() {
        return instance;
    }

    /**
     * Clean the alias to make it consist of only letters, numbers, and the underscore
     *
     * @param offer Offered input to be scanned
     * @return String that should be used based upon the offer
     */
    public String sanitizeAlias(String offer) {
        return offer.replaceAll("[^\\w]", "");
    }

    /**
     * Clean the prefix to make it consist of only letters, numbers, underscores, and the & symbol
     *
     * @param offer Offered input to be scanned
     * @return String that should be used based upon the offer
     */
    public String sanitizePrefix(String offer) {
        return offer.replaceAll("[^\\w&]", "");
    }

    private void scanForInvalid() {
        Iterator<String> iterator = players.keySet().iterator();
        while(iterator.hasNext()) {
            String playerName = iterator.next();
            AliasPlayer aliasPlayer = players.get(playerName);

            aliasPlayer.setAlias(sanitizeAlias(aliasPlayer.getAlias()));
            aliasPlayer.setPrefix(sanitizePrefix(aliasPlayer.getPrefix()));
        }
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
        }

        hasCreditsShop = pm.getPlugin("CreditsShop") != null;

        return hasCreditsShop;
    }
}