package net.betterverse.alias;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class AliasCommand implements CommandExecutor {
    private final Alias plugin;
    private ArrayList<Player> dun = new ArrayList<Player>();

    public AliasCommand(Alias plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("alias")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }

            Player player = (Player) sender;
            String name = player.getName();

            if (!player.hasPermission("alias.use")) {
                player.sendMessage("You do not have permission to use this!");
                return true;
            }

            if (args.length == 0) {
                if (this.plugin.aliases.remove(player.getName()) != null) {
                    player.setDisplayName(player.getName());
                    this.plugin.saveAliases();
                    this.plugin.getServer().broadcastMessage("Player " + ChatColor.DARK_RED + name + ChatColor.WHITE +
                                                                " is no longer using an alias.");
                } else {
                    player.sendMessage("No current alias to clear. Use /alias <NICKNAME> to set your alias.");
                }
                return true;
            } else if (args.length == 1) {
                String alias = args[0];

                if (alias.length() > 14) {
                    player.sendMessage("Aliases are limited to 14 characters.");
                    return true;
                } else if (this.plugin.aliases.containsValue(alias)) {
                    if (this.plugin.aliases.get(name).equalsIgnoreCase(alias)) {
                        player.sendMessage("You are already using " + alias + " as an alias.");
                        return true;
                    }

                    player.sendMessage("That alias is already in use.");
                    return true;
                } else if ((this.plugin.playerDir != null) && (this.plugin.playerDir.exists()) &&
                    (this.plugin.isPlayerName(alias))) {
                    player.sendMessage("You are not allowed to use that alias.");
                    return true;
                } else if (this.plugin.isBanned(alias)) {
                    player.sendMessage("You are not allowed to use that alias.");
                    return true;
                } else if (dun.contains(player)) {
                    player.sendMessage("You have already set your alias recently!");
                    return true;
                } else if (alias.matches("[a-zA-Z0-9 ]*")) {
                    dun.add(player);
                    player.setDisplayName(alias);
                    this.plugin.aliases.put(name, alias);
                    this.plugin.saveAliases();
                    this.plugin.getServer().broadcastMessage("Player " + name + " is now aliased as " + alias);
                    Alias.print(ChatColor.RED + name + ChatColor.WHITE + " has set their alias to " +
                                ChatColor.DARK_RED + alias);
                    return true;
                }

                player.sendMessage("Aliases must be only one word and must be alphanumeric.");
                return true;
            } else if (args.length > 1) {
                player.sendMessage("Unrecognized /alias use. Use /alias to clear your alias");
                player.sendMessage(" or say /alias <NICKNAME> to set your alias.");
                player.sendMessage(" Aliases must be one word and must be alphanumeric.");
                return true;
            }
            return true;
        }
        return false;
    }
}