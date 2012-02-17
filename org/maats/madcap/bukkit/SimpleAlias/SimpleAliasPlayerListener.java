package org.maats.madcap.bukkit.SimpleAlias;

import com.nijiko.permissions.PermissionHandler;
import java.util.HashMap;
import java.util.Map.Entry;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.scheduler.BukkitScheduler;

public class SimpleAliasPlayerListener extends PlayerListener
{
  private final SimpleAlias plugin;

  public SimpleAliasPlayerListener(SimpleAlias instance)
  {
    this.plugin = instance;
  }

  public void onPlayerJoin(PlayerJoinEvent event)
  {
    if (this.plugin.names == null) {
      return;
    }
    Player player = event.getPlayer();
    String alias = (String)this.plugin.names.get(player.getName());

    if (alias != null)
    {
      if ((this.plugin.isPlayerName(alias)) || (this.plugin.isBanned(alias))) {
        this.plugin.names.remove(player.getName());
        this.plugin.saveAliases();

        player.sendMessage("Alias Failure: You are no longer allowed allowed to use " + alias + " as an alias.");
        SimpleAlias.print(player.getName() + " has had their alias " + alias + " cleared because it is no longer allowed.");

        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new DelaySetDisplayName(player), 40L);
      }

      if (SimpleAlias.permissions == null)
      {
        player.setDisplayName(alias);
        SimpleAlias.print(player.getName() + " has connected, aliased as " + alias);
      }
      else if (SimpleAlias.permissions.has(player, "SimpleAlias.*"))
      {
        player.setDisplayName(alias);
        SimpleAlias.print(player.getName() + " has connected, aliased as " + alias);
      }
      else
      {
        SimpleAlias.print(player.getName() + " has connected, alias would be " + alias + " but does not have permission to use the /alias command.");
      }
    }
  }

  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
  {
    String newCmd = "";

    Player player = event.getPlayer();

    String[] args = event.getMessage().split(" ");

    if ((args.length > 0) && (args[0].startsWith("/")) && (!args[0].equalsIgnoreCase("/alias")))
    {
      int count = 0;

      if (!this.plugin.names.isEmpty())
      {
        for (int i = 0; i < args.length; i++)
        {
          String newStr = args[i];

          for (Map.Entry pairs : this.plugin.names.entrySet())
          {
            String alias = (String)pairs.getValue();
            String name = (String)pairs.getKey();

            if (alias.compareToIgnoreCase(newStr) != 0)
              continue;
            newStr = name;
            count++;
            break;
          }

          if (newCmd.length() > 0) {
            newCmd = newCmd + " ";
          }
          newCmd = newCmd + newStr;
        }

      }

      if (count > 0)
      {
        SimpleAlias.print("Translated '" + event.getMessage() + "' into '" + newCmd + "'.");

        event.setCancelled(true);
        player.chat(newCmd);
      }
    }
  }
}