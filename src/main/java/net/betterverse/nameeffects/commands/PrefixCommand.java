package net.betterverse.nameeffects.commands;

import net.betterverse.creditsshop.PlayerListener;
import net.betterverse.creditsshop.util.SqlConfiguration;
import net.betterverse.nameeffects.NameEffects;
import net.betterverse.nameeffects.objects.AliasPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrefixCommand implements CommandExecutor {
    NameEffects plugin;

    public PrefixCommand(NameEffects instance) {
        plugin = instance;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        if (args.length == 0) {
            AliasPlayer aplr = plugin.players.get(player.getName());
            aplr.setPrefix("");
            player.setDisplayName(aplr.getDisplayName());
            player.sendMessage(ChatColor.GREEN + "Prefix reset!");
        } else {
            if (plugin.hasCreditsShop) {
                int oldamount = SqlConfiguration.getBalanceForUpdate(player.getName());
                if (oldamount >= 0) {
                    int newamount = oldamount - plugin.pprice;
                    if (newamount <= 0) {
                        player.sendMessage("You don't have enough credit!");
                        return true;
                    }

                    PlayerListener.setBalance(player.getName(), newamount);
                }
            } else {
                if (!plugin.economy.has(player.getName(), plugin.pprice)) {
                    player.sendMessage(ChatColor.RED + "Not enough money!");
                    return true;
                }
                plugin.economy.withdrawPlayer(player.getName(), plugin.pprice);
            }

            String arg = args[0];
            AliasPlayer aplr = plugin.players.get(player.getName());
            aplr.setPrefix(arg);
            String prefix = "[" + aplr.getPrefix() + "]";
            if (aplr.getPrefix().equals("")) {
                prefix = "";
            }
            player.setDisplayName(prefix + aplr.getDisplayName());
            player.sendMessage(ChatColor.GREEN + "Prefix set to " + arg + "!");
        }

        return true;
    }
}
