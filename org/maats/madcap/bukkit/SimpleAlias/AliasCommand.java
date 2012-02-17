package org.maats.madcap.bukkit.SimpleAlias;

import com.nijiko.permissions.PermissionHandler;
import java.io.File;
import java.util.HashMap;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AliasCommand
  implements CommandExecutor
{
  private final SimpleAlias plugin;

  public AliasCommand(SimpleAlias plugin)
  {
    this.plugin = plugin;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
  {
    Player player = null;
    String name = "";
    String alias = "";

    if (cmd.getName().compareToIgnoreCase("alias") == 0)
    {
      if (!(sender instanceof Player)) {
        sender.sendMessage("Only players can use this command!");
        return true;
      }

      player = (Player)sender;
      name = player.getName();

      if (SimpleAlias.permissions != null)
      {
        if (!SimpleAlias.permissions.has(player, "SimpleAlias.*")) {
          player.sendMessage("You do not have permission to use " + commandLabel + ".");
          return true;
        }

      }

      if (args.length == 0)
      {
        if (this.plugin.names.remove(player.getName()) != null) {
          player.setDisplayName(player.getName());
          this.plugin.saveAliases();
          this.plugin.getServer().broadcastMessage("Player " + name + " is no longer using an alias.");
          SimpleAlias.print(name + " has cleared their alias.");
        }
        else {
          player.sendMessage("No current alias to clear. Use /alias <NICKNAME> to set your alias.");
        }
        return true;
      }

      if (args.length == 1)
      {
        alias = args[0];

        if (alias.length() > 12) {
          player.sendMessage("Alias Failure: Aliases are limited to 12 characters.");
          return true;
        }

        if (this.plugin.names.containsValue(alias))
        {
          if (((String)this.plugin.names.get(name)).equalsIgnoreCase(alias)) {
            player.sendMessage("Alias Failure: You are already using " + alias + " as an alias.");
            return true;
          }

          player.sendMessage("Alias Failure: That alias is already in use.");
          return true;
        }

        if ((this.plugin.playerDir != null) && (this.plugin.playerDir.exists()) && (this.plugin.isPlayerName(alias))) {
          player.sendMessage("Alias Failure: You are not allowed to use that alias.");
          return true;
        }

        if (this.plugin.isBanned(alias)) {
          player.sendMessage("Alias Failure: You are not allowed to use that alias.");
          return true;
        }

        if (alias.matches("[a-zA-Z0-9]*")) {
          player.setDisplayName(alias);
          this.plugin.names.put(name, alias);
          this.plugin.saveAliases();
          this.plugin.getServer().broadcastMessage("Player " + name + " is now aliased as " + alias);

          SimpleAlias.print(name + " has set their alias to " + alias);
          return true;
        }

        player.sendMessage("Alias Failure: Aliases must be only one word and must be alphanumeric.");
        return true;
      }

      if (args.length > 1) {
        player.sendMessage("Unrecognized /alias use. Use /alias to clear your alias");
        player.sendMessage(" or say /alias <NICKNAME> to set your alias.");
        player.sendMessage(" Aliases must be one word and must be alphanumeric.");
        return true;
      }

      SimpleAlias.print("this shouldn't happen, please notify developer");
      return true;
    }

    return false;
  }
}