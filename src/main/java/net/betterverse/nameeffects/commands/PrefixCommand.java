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
		if (!(sender instanceof Player)) {
			return true;
		}

		Player player = (Player) sender;

		if (args.length == 0) {
			AliasPlayer aplr = plugin.getAliasPlayer(sender.getName());
			if (aplr.getPrefix() == null) {
				return true;
			}
			aplr.resetPrefix();
			player.sendMessage(ChatColor.GREEN + "Prefix reset!");
		} else {
			try {
				int oldamount = SqlConfiguration.getBalanceForUpdate(player.getName());
				if (oldamount >= 0) {
					int newamount = oldamount - plugin.pprice;
					if (newamount <= 0) {
						player.sendMessage("You don't have enough credit!");
						return true;
					}
					PlayerListener.setBalance(player.getName(), newamount);
				} else {
					player.sendMessage("You don't have enough credit!");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			String arg = args[0];
			plugin.getAliasPlayer(sender.getName()).setPrefix(arg);
			player.sendMessage(ChatColor.GREEN + "Prefix set to " + arg + "!");
		}

		return true;
	}
}
