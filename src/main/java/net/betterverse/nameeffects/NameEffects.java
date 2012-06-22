package net.betterverse.nameeffects;

import java.util.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class NameEffects extends JavaPlugin implements Listener {

    private Map<String, AliasPlayer> players = new HashMap<String, AliasPlayer>();
    private Economy economy;
    private int pprice;
    private List<String> blocked = new ArrayList<String>();
    private Set<String> expired = new HashSet<String>();
    private List<String> ccodes = new ArrayList<String>();

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this, this);
        setupEconomy();

        blocked.add("notch");
        ccodes.add("&1");

        getConfig().addDefault("PrefixPrice", 10);
        getConfig().addDefault("BlockedStuff", blocked);
        getConfig().addDefault("BlockedColorCodes", ccodes);
        getConfig().options().copyDefaults(true);
        saveConfig();

        pprice = getConfig().getInt("PrefixPrice");
        blocked = getConfig().getStringList("BlockedStuff");
        ccodes = getConfig().getStringList("BlockedColorCodes");
    }

    private Boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        AliasPlayer aplr = players.get(player.getName());
        if (aplr == null) {
            aplr = new AliasPlayer(player.getDisplayName(), "");
            players.put(player.getName(), aplr);
        }
        player.setDisplayName("[" + aplr.getPrefix() + "]" + aplr.getDisplayName());
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("alias")) {
            if (!(sender.hasPermission("nameeffects.alias"))) {
                return false;
            }
            if (expired.contains(sender.getName())) {
                sender.sendMessage(ChatColor.RED + "You can't do that again this soon!");
                return true;
            }
            if (args.length == 0) {
                AliasPlayer aplr = players.get(sender.getName());
                aplr.setDisplayName(sender.getName());
                sender.sendMessage(ChatColor.GREEN + "Alias reset!");
            } else {
                String arg = args[0];
                if (blocked.contains(ChatColor.stripColor(arg).toLowerCase())) {
                    sender.sendMessage(ChatColor.RED + "That name is blocked!");
                    return true;
                }
                if (Bukkit.getOfflinePlayer(arg) != null) {
                    sender.sendMessage(ChatColor.RED + "A player currently has that name!");
                    return true;
                }
                arg = filter(arg);
                AliasPlayer aplr = players.get(sender.getName());
                aplr.setDisplayName(arg);
                String prefix = "[" + aplr.getPrefix() + "]";
                if(aplr.getPrefix().equals("")) {
                    prefix = "";
                }
                ((Player) sender).setDisplayName(prefix + aplr.getDisplayName());
                sender.sendMessage(ChatColor.GREEN + "Alias set to " + arg + "!");
                expired.add(sender.getName());
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

                    @Override
                    public void run() {
                        expired.remove(sender.getName());
                    }
                }, 20L * 60 * 24);

            }
        }
        if (label.equalsIgnoreCase("prefix")) {
            if (!economy.has(sender.getName(), pprice)) {
                sender.sendMessage(ChatColor.RED + "Not enough money!");
                return true;
            }
            economy.withdrawPlayer(sender.getName(), pprice);
            if (args.length == 0) {
                AliasPlayer aplr = players.get(sender.getName());
                aplr.setPrefix("");
                ((Player) sender).setDisplayName(aplr.getDisplayName());
                sender.sendMessage(ChatColor.GREEN + "Prefix reset!");
            } else {
                String arg = args[0];
                AliasPlayer aplr = players.get(sender.getName());
                aplr.setPrefix(arg);
                String prefix = "[" + aplr.getPrefix() + "]";
                if(aplr.getPrefix().equals("")) {
                    prefix = "";
                }
                ((Player) sender).setDisplayName(prefix + aplr.getDisplayName());
                sender.sendMessage(ChatColor.GREEN + "Prefix set to " + arg + "!");
            }
        }
        return true;
    }

    private String filter(String arg) {
        for(String color : ccodes) {
            arg = arg.replaceAll(color, "");
        }
        return arg;
    }
}
