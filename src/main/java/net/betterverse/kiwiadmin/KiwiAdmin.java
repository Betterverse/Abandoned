package net.betterverse.kiwiadmin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class KiwiAdmin extends JavaPlugin
{
  public static final Logger log = Logger.getLogger("Minecraft");

  static Database db;
  static String maindir = "plugins/KiwiAdmin/";
  static File Settings = new File(maindir + "config.properties");
  static ArrayList<String> bannedPlayers = new ArrayList();
  static ArrayList<String> bannedIPs = new ArrayList();
  static Map<String, Long> tempBans = new HashMap();
  private final KiwiAdminPlayerListener playerListener = new KiwiAdminPlayerListener(this);
  public static boolean useMysql;
  public static String mysqlDatabase;
  public static String mysqlUser;
  public static String mysqlPassword;
  public static String mysqlTable;
  public static boolean autoComplete;
  public static String mysqlTableIp;
  public void onDisable() {
    tempBans.clear();
    bannedPlayers.clear();
    System.out.println("KiwiAdmin disabled.");
  }

  protected void createDefaultConfiguration(String name)
  {
    File actual = new File(getDataFolder(), name);
    if (!actual.exists())
    {
      InputStream input = 
        getClass().getResourceAsStream("/defaults/" + name);
      if (input != null) {
        FileOutputStream output = null;
        try
        {
          output = new FileOutputStream(actual);
          byte[] buf = new byte[8192];
          int length = 0;
          while ((length = input.read(buf)) > 0) {
            output.write(buf, 0, length);
          }

          System.out.println(getDescription().getName() + 
            ": Default configuration file written: " + name);
        } catch (IOException e) {
          e.printStackTrace();
          try
          {
            if (input != null)
              input.close();
          } catch (IOException localIOException1) {
          }
          try {
            if (output != null)
              output.close();
          }
          catch (IOException localIOException2)
          {
          }
        }
        finally
        {
          try
          {
            if (input != null)
              input.close();
          } catch (IOException localIOException3) {
          }
          try {
            if (output != null)
              output.close(); 
          } catch (IOException localIOException4) {
          }
        }
      }
    }
  }

  public void onEnable() {
    new File(maindir).mkdir();

    createDefaultConfiguration("config.yml");

    useMysql = getConfig().getBoolean("mysql", false);
    mysqlTable = getConfig().getString("mysql-table", "banlist");
    mysqlTableIp = getConfig().getString("mysql-table-ip", "banlistip");
    autoComplete = getConfig().getBoolean("auto-complete", true);

    db = new Database(this);

    PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(playerListener,this);

    PluginDescriptionFile pdfFile = getDescription();
    log.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
  }

  public static String combineSplit(int startIndex, String[] string, String seperator) {
    StringBuilder builder = new StringBuilder();

    for (int i = startIndex; i < string.length; i++) {
      builder.append(string[i]);
      builder.append(seperator);
    }

    builder.deleteCharAt(builder.length() - seperator.length());
    return builder.toString();
  }

  static long parseTimeSpec(String time, String unit)
  {
   long sec;
		try {
      sec = Integer.parseInt(time) * 60;
    }
    catch (NumberFormatException ex)
    {
      return 0L;
    }
    if (unit.startsWith("hour"))
      sec *= 60L;
    else if (unit.startsWith("day"))
      sec *= 1440L;
    else if (unit.startsWith("week"))
      sec *= 10080L;
    else if (unit.startsWith("month"))
      sec *= 43200L;
    else if (unit.startsWith("min"))
      sec *= 1L;
    else if (unit.startsWith("sec"))
      sec /= 60L;
    return sec * 1000L;
  }

  public String expandName(String Name) {
    int m = 0;
    String Result = "";
    for (int n = 0; n < getServer().getOnlinePlayers().length; n++) {
      String str = getServer().getOnlinePlayers()[n].getName();
      if (str.matches("(?i).*" + Name + ".*")) {
        m++;
        Result = str;
        if (m == 2) {
          return null;
        }
      }
      if (str.equalsIgnoreCase(Name))
        return str;
    }
    if (m == 1)
      return Result;
    if (m > 1) {
      return null;
    }
    if (m < 1) {
      return Name;
    }
    return Name;
  }

  public String formatMessage(String str) {
    String funnyChar = new Character('§').toString();
    str = str.replaceAll("&", funnyChar);
    return str;
  }

  public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
  {
    String commandName = command.getName().toLowerCase();
    String[] trimmedArgs = args;

    if (commandName.equals("reloadka")) {
      return reloadKA(sender);
    }
    if (commandName.equals("unban")) {
      return unBanPlayer(sender, trimmedArgs);
    }
    if (commandName.equals("ban")) {
      return banPlayer(sender, trimmedArgs);
    }
    if (commandName.equals("kick")) {
      return kickPlayer(sender, trimmedArgs);
    }
    if (commandName.equals("tempban")) {
      return tempbanPlayer(sender, trimmedArgs);
    }
    if (commandName.equals("checkban")) {
      return checkBan(sender, trimmedArgs);
    }
    if (commandName.equals("ipban")) {
      return ipBan(sender, trimmedArgs);
    }
    if (commandName.equals("exportbans")) {
      return exportBans(sender);
    }
    return false;
  }

  private boolean unBanPlayer(CommandSender sender, String[] args) {
    boolean auth = false;
    Player player = null;
    String kicker = "server";
    if ((sender instanceof Player)) {
      player = (Player)sender;
      if (player.hasPermission("kiwiadmin.unban")) auth = true;
      kicker = player.getName();
    } else {
      auth = true;
    }

    if (!auth) return false;

    if (args.length < 1) return false;

    String p = args[0];

    if (Database.removeFromBanlist(p))
    {
      bannedPlayers.remove(p.toLowerCase());

      if (tempBans.containsKey(p.toLowerCase())) {
        tempBans.remove(p.toLowerCase());
      }
      log.log(Level.INFO, "[KiwiAdmin] " + kicker + " unbanned player " + p + ".");

      String kickerMsg = this.getConfig().getString("messages.unbanMsg");
      String globalMsg = this.getConfig().getString("messages.unbanMsgGlobal");
      kickerMsg = kickerMsg.replaceAll("%victim%", p);
      globalMsg = globalMsg.replaceAll("%victim%", p);
      globalMsg = globalMsg.replaceAll("%player%", kicker);

      sender.sendMessage(formatMessage(kickerMsg));

      getServer().broadcastMessage(formatMessage(globalMsg));
      return true;
    }
    String kickerMsg = this.getConfig().getString("messages.unbanMsgFailed");
    kickerMsg = kickerMsg.replaceAll("%victim%", p);
    sender.sendMessage(formatMessage(kickerMsg));
    return true;
  }

  private boolean kickPlayer(CommandSender sender, String[] args) {
    boolean auth = false;
    Player player = null;
    String kicker = "server";
    if ((sender instanceof Player)) {
      player = (Player)sender;
      if (player.hasPermission( "kiwiadmin.kick")) auth = true;
      kicker = player.getName();
    } else {
      auth = true;
    }

    if (!auth) return false;

    if (args.length < 1) return false;

    String p = args[0].toLowerCase();

    String reason = "undefined";
    if (args.length > 1) reason = combineSplit(1, args, " ");

    if (p.equals("*")) {
      if (((sender instanceof Player)) && 
        (!player.hasPermission("kiwiadmin.kick.all"))) return false;

      String kickerMsg = this.getConfig().getString("messages.kickAllMsg");
      kickerMsg = kickerMsg.replaceAll("%player%", kicker);
      kickerMsg = kickerMsg.replaceAll("%reason%", reason);
      log.log(Level.INFO, "[KiwiAdmin] " + formatMessage(kickerMsg));
      Player[] arrayOfPlayer;
      if ((arrayOfPlayer = getServer().getOnlinePlayers()).length != 0) { Player pl = arrayOfPlayer[0];
        pl.kickPlayer(formatMessage(kickerMsg));
        return true;
      }
    }
    if (autoComplete)
      p = expandName(p);
    Player victim = getServer().getPlayer(p);
    if (victim == null) {
      String kickerMsg = this.getConfig().getString("messages.kickMsgFailed");
      kickerMsg = kickerMsg.replaceAll("%victim%", p);
      sender.sendMessage(formatMessage(kickerMsg));
      return true;
    }

    log.log(Level.INFO, "[KiwiAdmin] " + kicker + " kicked player " + p + ". Reason: " + reason);

    String kickerMsg = this.getConfig().getString("messages.kickMsgVictim");
    kickerMsg = kickerMsg.replaceAll("%player%", kicker);
    kickerMsg = kickerMsg.replaceAll("%reason%", reason);
    victim.kickPlayer(formatMessage(kickerMsg));

    String kickerMsgAll = this.getConfig().getString("messages.kickMsgBroadcast");
    kickerMsgAll = kickerMsgAll.replaceAll("%player%", kicker);
    kickerMsgAll = kickerMsgAll.replaceAll("%reason%", reason);
    kickerMsgAll = kickerMsgAll.replaceAll("%victim%", p);
    getServer().broadcastMessage(formatMessage(kickerMsgAll));
    return true;
  }
  private boolean banPlayer(CommandSender sender, String[] args) {
    boolean auth = false;
    Player player = null;
    String kicker = "server";
    if ((sender instanceof Player)) {
      player = (Player)sender;
      if (player.hasPermission( "kiwiadmin.ban")) auth = true;
      kicker = player.getName();
    } else {
      auth = true;
    }

    if (!auth) return false;

    if (args.length < 1) return false;

    String p = args[0];
    if (autoComplete)
      p = expandName(p);
    Player victim = getServer().getPlayer(p);

    String reason = "undefined";
    if (args.length > 1) reason = combineSplit(1, args, " ");

    if (bannedPlayers.contains(p.toLowerCase())) {
      String kickerMsg = this.getConfig().getString("messages.banMsgFailed");
      kickerMsg = kickerMsg.replaceAll("%victim%", p);
      sender.sendMessage(formatMessage(kickerMsg));
      return true;
    }

    bannedPlayers.add(p.toLowerCase());

    db.addPlayer(p, reason, kicker);

    log.log(Level.INFO, "[KiwiAdmin] " + kicker + " banned player " + p + ".");

    if (victim != null)
    {
      String kickerMsg = this.getConfig().getString("messages.banMsgVictim");
      kickerMsg = kickerMsg.replaceAll("%player%", kicker);
      kickerMsg = kickerMsg.replaceAll("%reason%", reason);
      victim.kickPlayer(formatMessage(kickerMsg));
    }

    String kickerMsgAll = this.getConfig().getString("messages.banMsgBroadcast");
    kickerMsgAll = kickerMsgAll.replaceAll("%player%", kicker);
    kickerMsgAll = kickerMsgAll.replaceAll("%reason%", reason);
    kickerMsgAll = kickerMsgAll.replaceAll("%victim%", p);
    getServer().broadcastMessage(formatMessage(kickerMsgAll));

    return true;
  }

  private boolean tempbanPlayer(CommandSender sender, String[] args) {
    boolean auth = false;
    Player player;
    String kicker = "server";
    if ((sender instanceof Player)) {
      player = (Player)sender;
      if (player.hasPermission("kiwiadmin.tempban")) auth = true;
      kicker = player.getName();
    } else {
      auth = true;
    }
    if (!auth) return false;

    if (args.length < 3) return false;

    String p = args[0];
    if (autoComplete)
      p = expandName(p);
    Player victim = getServer().getPlayer(p);

    String reason = "undefined";
    if (args.length > 3) reason = combineSplit(3, args, " ");

    if (bannedPlayers.contains(p.toLowerCase())) {
      String kickerMsg = this.getConfig().getString("messages.banMsgFailed");
      kickerMsg = kickerMsg.replaceAll("%victim%", p);
      sender.sendMessage(formatMessage(kickerMsg));
      return true;
    }

    bannedPlayers.add(p.toLowerCase());
    long tempTime = parseTimeSpec(args[1], args[2]);
    tempTime = System.currentTimeMillis() + tempTime;
    tempBans.put(p.toLowerCase(), Long.valueOf(tempTime));

    db.addPlayer(p, reason, kicker, tempTime);

    log.log(Level.INFO, "[KiwiAdmin] " + kicker + " tempbanned player " + p + ".");

    if (victim != null)
    {
      String kickerMsg = this.getConfig().getString("messages.tempbanMsgVictim");
      kickerMsg = kickerMsg.replaceAll("%player%", kicker);
      kickerMsg = kickerMsg.replaceAll("%reason%", reason);
      victim.kickPlayer(formatMessage(kickerMsg));
    }

    String kickerMsgAll = this.getConfig().getString("messages.tempbanMsgBroadcast");
    kickerMsgAll = kickerMsgAll.replaceAll("%player%", kicker);
    kickerMsgAll = kickerMsgAll.replaceAll("%reason%", reason);
    kickerMsgAll = kickerMsgAll.replaceAll("%victim%", p);
    getServer().broadcastMessage(formatMessage(kickerMsgAll));

    return true;
  }

  private boolean checkBan(CommandSender sender, String[] args) {
    String p = args[0];
    if (bannedPlayers.contains(p.toLowerCase()))
      sender.sendMessage(ChatColor.RED + "Player " + p + " is banned.");
    else
      sender.sendMessage(ChatColor.GREEN + "Player " + p + " is not banned.");
    return true;
  }
  private boolean ipBan(CommandSender sender, String[] args) {
    boolean auth = false;
    Player player = null;
    String kicker = "server";
    if ((sender instanceof Player)) {
      player = (Player)sender;
      if (player.hasPermission( "kiwiadmin.ipban")) auth = true;
      kicker = player.getName();
    } else {
      auth = true;
    }
    if (!auth) return false;

    if (args.length < 1) return false;

    String p = args[0];
    if (autoComplete)
      p = expandName(p);
    Player victim = getServer().getPlayer(p);
    if (victim == null) {
      sender.sendMessage("Couldn't find player.");
      return true;
    }

    String reason = "undefined";
    if (args.length > 1) reason = combineSplit(1, args, " ");

    if (bannedPlayers.contains(p.toLowerCase())) {
      String kickerMsg = this.getConfig().getString("messages.banMsgFailed");
      kickerMsg = kickerMsg.replaceAll("%victim%", p);
      sender.sendMessage(formatMessage(kickerMsg));
      return true;
    }

    bannedPlayers.add(p.toLowerCase());
    bannedIPs.add(victim.getAddress().getAddress().getHostAddress());

    db.addPlayer(p, reason, kicker);
    db.addAddress(p, victim.getAddress());

    log.log(Level.INFO, "[KiwiAdmin] " + kicker + " banned player " + p + ".");

    String kickerMsg = this.getConfig().getString("messages.banMsgVictim");
    kickerMsg = kickerMsg.replaceAll("%player%", kicker);
    kickerMsg = kickerMsg.replaceAll("%reason%", reason);
    victim.kickPlayer(formatMessage(kickerMsg));

    String kickerMsgAll = this.getConfig().getString("messages.banMsgBroadcast");
    kickerMsgAll = kickerMsgAll.replaceAll("%player%", kicker);
    kickerMsgAll = kickerMsgAll.replaceAll("%reason%", reason);
    kickerMsgAll = kickerMsgAll.replaceAll("%victim%", p);
    getServer().broadcastMessage(formatMessage(kickerMsgAll));

    return true;
  }
  private boolean reloadKA(CommandSender sender) {
    boolean auth = false;
    Player player = null;
    String kicker = "server";
    if ((sender instanceof Player)) {
      player = (Player)sender;
      if (player.hasPermission("kiwiadmin.reload"))
        auth = true;
      kicker = player.getName();
    } else {
      auth = true;
    }
    if (auth)
    {
      bannedPlayers.clear();
      tempBans.clear();

      db = new Database(this);

      log.log(Level.INFO, "[KiwiAdmin] " + kicker + " reloaded the banlist.");
      sender.sendMessage("§2Reloaded banlist.");
      return true;
    }
    return false;
  }

  private boolean exportBans(CommandSender sender) {
    boolean auth = false;
    Player player = null;
    if ((sender instanceof Player)) {
      player = (Player)sender;
      if (player.hasPermission("kiwiadmin.export"))
        auth = true;
    } else {
      auth = true;
    }
    if (auth) {
      db.exportBans();
      sender.sendMessage("§2Exported banlist to banned-players.txt.");
      return true;
    }
    return false;
  }
}