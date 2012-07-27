package me.kalmanolah.os;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OKCmd
  implements CommandExecutor
{
  private static OKmain plugin;

  public OKCmd(OKmain instance)
  {
    plugin = instance;
  }

  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    boolean handled = false;
    if (is(label, "os")) {
      if ((args.length == 1) && (is(args[0], "reload"))) {
        handled = true;
        if (!isPlayer(sender)) {
          OKConfig.loadkeys();
          OKLogger.info("Configuration reloaded!");
        }
        else if (OKmain.CheckPermission(getPlayer(sender), "okspamsecurity.reload")) {
          OKConfig.loadkeys();
          sendMessage(sender, ChatColor.GOLD + "Notice: " + ChatColor.GRAY + "Configuration reloaded!");
        } else {
          sendMessage(sender, ChatColor.LIGHT_PURPLE + "You do not have permission to do this.");
        }
      }
      else if ((args.length == 2) && (is(args[0], "punish"))) {
        handled = true;
        if (!isPlayer(sender)) {
          Player plr = plugin.getServer().getPlayer(args[1]);
          if (plr != null) {
            if (!OKmain.manuallypunishedplayers.contains(plr)) {
              OKmain.manuallypunishedplayers.add(plr);
              OKLogger.info("Notice: '" + args[1] + "' is now being punished.");
            } else {
              OKLogger.info("Error: '" + args[1] + "' is already being punished.");
            }
          }
          else OKLogger.info("Error: '" + args[1] + "' is not a valid player.");

        }
        else if (OKmain.CheckPermission(getPlayer(sender), "okspamsecurity.punish")) {
          Player plr = plugin.getServer().getPlayer(args[1]);
          if (plr != null) {
            if (!OKmain.manuallypunishedplayers.contains(plr)) {
              OKmain.manuallypunishedplayers.add(plr);
              OKLogger.info(getName(sender) + " initiated punishment against " + args[1] + ".");
              sendMessage(sender, ChatColor.GOLD + "Notice: " + ChatColor.GRAY + "'" + ChatColor.WHITE + args[1] + ChatColor.GRAY + "' is now being punished.");
            } else {
              sendMessage(sender, ChatColor.RED + "Error: " + ChatColor.GRAY + "'" + ChatColor.WHITE + args[1] + ChatColor.GRAY + "' is already being punished.");
            }
          }
          else sendMessage(sender, ChatColor.RED + "Error: " + ChatColor.GRAY + "'" + ChatColor.WHITE + args[1] + ChatColor.GRAY + "' is not a valid player."); 
        }
        else
        {
          sendMessage(sender, ChatColor.LIGHT_PURPLE + "You do not have permission to do this.");
        }
      }
      else if ((args.length == 2) && (is(args[0], "forgive"))) {
        handled = true;
        if (!isPlayer(sender)) {
          Player plr = plugin.getServer().getPlayer(args[1]);
          if (plr != null) {
            if (OKmain.manuallypunishedplayers.contains(plr)) {
              OKmain.manuallypunishedplayers.remove(plr);
              OKLogger.info("Notice: '" + args[1] + "' is no longer being punished.");
            } else {
              OKLogger.info("Error: '" + args[1] + "' is not being punished.");
            }
          }
          else OKLogger.info("Error: '" + args[1] + "' is not a valid player.");

        }
        else if (OKmain.CheckPermission(getPlayer(sender), "okspamsecurity.forgive")) {
          Player plr = plugin.getServer().getPlayer(args[1]);
          if (plr != null) {
            if (OKmain.manuallypunishedplayers.contains(plr)) {
              OKmain.manuallypunishedplayers.remove(plr);
              OKLogger.info(getName(sender) + " stopped punishment against " + args[1] + ".");
              sendMessage(sender, ChatColor.GOLD + "Notice: " + ChatColor.GRAY + "'" + ChatColor.WHITE + args[1] + ChatColor.GRAY + "' is no longer being punished.");
            } else {
              sendMessage(sender, ChatColor.RED + "Error: " + ChatColor.GRAY + "'" + ChatColor.WHITE + args[1] + ChatColor.GRAY + "' is not being punished.");
            }
          }
          else sendMessage(sender, ChatColor.RED + "Error: " + ChatColor.GRAY + "'" + ChatColor.WHITE + args[1] + ChatColor.GRAY + "' is not a valid player."); 
        }
        else
        {
          sendMessage(sender, ChatColor.LIGHT_PURPLE + "You do not have permission to do this.");
        }
      }
      else {
        handled = true;
        sendMessage(sender, "Incorrect command usage: /" + label + " " + join(args, 0));
      }
    }
    return handled;
  }

  private boolean is(String entered, String label) {
    return entered.equalsIgnoreCase(label);
  }

  private boolean sendMessage(CommandSender sender, String message) {
    boolean sent = false;
    if (isPlayer(sender)) {
      Player player = (Player)sender;
      player.sendMessage(message);
      sent = true;
    }
    return sent;
  }

  private String getName(CommandSender sender) {
    String name = "";
    if (isPlayer(sender)) {
      Player player = (Player)sender;
      name = player.getName();
    }
    return name;
  }

  private boolean isPlayer(CommandSender sender) {
    return sender instanceof Player;
  }

  private Player getPlayer(CommandSender sender) {
    Player player = null;
    if (isPlayer(sender)) {
      player = (Player)sender;
    }
    return player;
  }

  private String join(String[] split, int delimiter) {
    String joined = "";
    int length = split.length;
    int i = delimiter;
    while (i < length - 1) {
      joined = joined + split[i] + " ";
      i++;
    }
    while (i == length - 1) {
      joined = joined + split[i];
      i++;
    }
    return joined;
  }
}