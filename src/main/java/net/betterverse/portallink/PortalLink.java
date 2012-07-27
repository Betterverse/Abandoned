package net.betterverse.portallink;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PortalLink extends JavaPlugin
{
  private PluginDescriptionFile pdfFile;
  private final PortalLinkPlayerListener plPlayerListener = new PortalLinkPlayerListener(this);
  private final PortalLinkConfig plConfig = new PortalLinkConfig(this);
  private Method allowNetherMethod = null;
  private Map<World, Boolean> storedAllowNether = new HashMap();
  Logger log = Logger.getLogger("Minecraft");

  public void onEnable()
  {
    getCommand("pl").setExecutor(new CommandExecutor() {
      public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        if ((args[0].equalsIgnoreCase("link")) && (args.length > 1)) {
          if (!sender.hasPermission("portallink.link")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to define PortalLinks.");
            return true;
          }
          boolean twoway = false;
          int whichNether = 0;
          int index = 1;
          if ((args[1].startsWith("-")) && (args[1].length() <= 3)) {
            if (args[1].contains("b")) {
              twoway = true;
              index = 2;
            }
            if (args[1].contains("0")) {
              index = 2;
            } else if (args[1].contains("1")) {
              index = 2;
              whichNether = 1;
            } else if (args[1].contains("2")) {
              index = 2;
              whichNether = 2;
            } else if (args[1].contains("3")) {
              index = 2;
              whichNether = 3;
            }

          }

          if (args.length > 2) {
            if (args[2].equalsIgnoreCase("-b")) {
              twoway = true;
              index = 3;
            } else if (args[2].startsWith("-")) {
              String numberStr = String.valueOf(args[2].charAt(2));
              if ("0123".contains(numberStr)) {
                whichNether = Integer.parseInt(numberStr);
              }
              index = 3;
            }

          }

          if (args.length <= index) {
            return false;
          }
          String str1 = args[index];
          str1 = str1.trim();
          boolean stillEndsInBckSlsh = str1.endsWith("\\");
          while (stillEndsInBckSlsh) {
            index++;
            if (args.length > index) {
              str1 = str1.substring(0, str1.length() - 1);
              str1 = str1.concat(" " + args[index]);
              stillEndsInBckSlsh = str1.endsWith("\\");
            } else {
              return false;
            }
          }
          String str2;
          if (args.length <= index + 1) {
            str2 = "";
          } else {
            str2 = args[(index + 1)];
            str2 = str2.trim();
            stillEndsInBckSlsh = str2.endsWith("\\");
            while (stillEndsInBckSlsh) {
              index++;
              if (args.length > index + 1) {
                str2 = str2.substring(0, str2.length() - 1);
                str2 = str2.concat(" " + args[(index + 1)]);
                stillEndsInBckSlsh = str2.endsWith("\\");
              }
            }
          }
          PortalLink.this.plConfig.addLink(str1, str2, (sender instanceof Player) ? sender : null, twoway, whichNether);
          return true;
        }if ((args[0].equals("unlink")) && (args.length > 1)) {
          if (!sender.hasPermission("portallink.unlink")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to remove PortalLinks.");
            return true;
          }
          int index = 1;

          String str1 = args[index];
          str1 = str1.trim();
          boolean stillEndsInBckSlsh = str1.endsWith("\\");
          while (stillEndsInBckSlsh) {
            index++;
            if (args.length > index) {
              str1 = str1.substring(0, str1.length() - 1);
              str1 = str1.concat(" " + args[index]);
              stillEndsInBckSlsh = str1.endsWith("\\");
            } else {
              return false;
            }
          }
          String str2;
          if (args.length <= index + 1) {
            str2 = "";
          } else {
            str2 = args[(index + 1)];
            str2 = str2.trim();
            boolean stillEndsInBckSlsh1 = str2.endsWith("\\");
            while (stillEndsInBckSlsh1) {
              index++;
              if (args.length > index + 1) {
                str2 = str2.substring(0, str2.length() - 1);
                str2 = str2.concat(" " + args[(index + 1)]);
                stillEndsInBckSlsh1 = str2.endsWith("\\");
              }
            }
          }
          PortalLink.this.plConfig.removeLink(str1, str2, (sender instanceof Player) ? sender : null);
          return true;
        }if ((args[0].equals("reload")) && (args.length == 1)) {
          if (!sender.hasPermission("portallink.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to reload PortalLink.");
            return true;
          }
          PortalLink.this.plConfig.loadUserDefinedLinks();
          if ((sender instanceof Player)) {
            sender.sendMessage("PortalLink has been reloaded!");
          }
          PortalLink.this.logInfo("PortalLink has been reloaded!");
          return true;
        }
        return false;
      }
    });
    this.pdfFile = getDescription();
    this.plConfig.loadUserDefinedLinks();
    Method m = null;
    try {
      m = World.class.getMethod("getAllowNether", new Class[0]);
    }
    catch (NoSuchMethodException localNoSuchMethodException) {
    }
    this.allowNetherMethod = m;
    PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(plPlayerListener,this);
    this.log.info(this.pdfFile.getName() + " version " + this.pdfFile.getVersion() + " is enabled!");
  }

  public boolean getAllowNether(World world) {
    if (this.storedAllowNether.containsKey(world)) {
      return ((Boolean)this.storedAllowNether.get(world)).booleanValue();
    }
    if (this.allowNetherMethod != null)
      try {
        Boolean bool = (Boolean)this.allowNetherMethod.invoke(world, new Object[0]);
        this.storedAllowNether.put(world, bool);
        return bool.booleanValue();
      }
      catch (Exception localException)
      {
      }
    this.storedAllowNether.put(world, Boolean.valueOf(true));
    return true;
  }

  public void onDisable()
  {
    this.storedAllowNether.clear();
    this.log.info(this.pdfFile.getName() + " is disabled!");
  }

  public PortalLinkConfig getPortalLinkConfig() {
    return this.plConfig;
  }

  public void logInfo(String string) {
    this.log.info("[" + this.pdfFile.getName() + "] " + string);
  }

  public void logWarning(String string) {
    this.log.warning("[" + this.pdfFile.getName() + "] " + string);
  }

  public void logError(String string) {
    this.log.severe("[" + this.pdfFile.getName() + "] " + string);
  }
}