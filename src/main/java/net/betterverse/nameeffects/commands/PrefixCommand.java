package net.betterverse.nameeffects.commands;

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
        if (args.length == 0) {
            AliasPlayer aplr = plugin.players.get(sender.getName());
            aplr.setPrefix("");
            ((Player) sender).setDisplayName(aplr.getDisplayName());
            sender.sendMessage(ChatColor.GREEN + "Prefix reset!");
        } else {
            if (!plugin.economy.has(sender.getName(), plugin.pprice)) {
                sender.sendMessage(ChatColor.RED + "Not enough money!");
                return true;
            }
            plugin.economy.withdrawPlayer(sender.getName(), plugin.pprice);
            String arg = args[0];
            AliasPlayer aplr = plugin.players.get(sender.getName());
            aplr.setPrefix(arg);
            String prefix = "[" + aplr.getPrefix() + "]";
            if (aplr.getPrefix().equals("")) {
                prefix = "";
            }
            ((Player) sender).setDisplayName(prefix + aplr.getDisplayName());
            sender.sendMessage(ChatColor.GREEN + "Prefix set to " + arg + "!");
        }

        return true;
    }
}
