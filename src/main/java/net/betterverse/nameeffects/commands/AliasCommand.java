package net.betterverse.nameeffects.commands;

import net.betterverse.nameeffects.NameEffects;
import net.betterverse.nameeffects.objects.AliasPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AliasCommand implements CommandExecutor {
    NameEffects plugin;

    public AliasCommand(NameEffects instance) {
        plugin = instance;
    }

    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.hasPermission("nameeffects.alias"))) {
            return false;
        }
        if (plugin.expired.contains(sender.getName())) {
            sender.sendMessage(ChatColor.RED + "You can't do that again this soon!");
            return true;
        }
        if (args.length == 0) {
            AliasPlayer aplr = plugin.getAliasPlayer(sender.getName());
            if (aplr.getAlias() == null)
                return true;
            aplr.resetAlias();
            sender.sendMessage(ChatColor.GREEN + "Alias reset!");
        } else {
            String arg = args[0];
            arg = filter(arg);
            if (plugin.blocked.contains(ChatColor.stripColor(arg).toLowerCase())) {
                sender.sendMessage(ChatColor.RED + "That name is blocked!");
                return true;
            }
            if (Bukkit.getPlayer(arg) != null) {
                sender.sendMessage(ChatColor.RED + "A player currently has that name!");
                return true;
            }
            plugin.getAliasPlayer(sender.getName()).setAlias(arg);
            sender.sendMessage(ChatColor.GREEN + "Alias set to " + arg + "!");
            plugin.expired.add(sender.getName());
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    plugin.expired.remove(sender.getName());
                }
            }, 20L * 60 * 24);

        }

        return true;
    }

    private String filter(String arg) {
        for (String color : plugin.ccodes) {
            arg = arg.replaceAll(color, "");
        }

        return arg;
    }
}
