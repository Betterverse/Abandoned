package net.blockheaven.kaipr.heavenactivity;

import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.Holdings;
import com.nijiko.permissions.PermissionHandler;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HeavenActivity extends JavaPlugin
{
  protected static final Logger logger = Logger.getLogger("Minecraft.HeavenActivity");
  public HeavenActivityConfig config;
  public static PermissionHandler Permissions;
  public static iConomy iConomy;
  public Map<Integer, Map<String, Double>> playersActivities = new HashMap();

  public static Timer updateTimer = null;

  public int currentSequence = 0;
  public Double chatPointsGiven;
  public Double chatCharPointsGiven;
  public Double commandPointsGiven;
  public Double commandCharPointsGiven;
  public Double movePointsGiven;
  public Double blockPlacePointsGiven;
  public Double blockBreakPointsGiven;

  public void onEnable()
  {
    logger.info(getDescription().getName() + " " + 
      getDescription().getVersion() + " enabled.");

    this.config = new HeavenActivityConfig(this);

    startUpdateTimer();

    PlayerListener playerListener = new HeavenActivityPlayerListener(this);
    BlockListener blockListener = new HeavenActivityBlockListener(this);
    ServerListener serverListener = new HeavenActivityServerListener(this);

    PluginManager pm = getServer().getPluginManager();
    if (this.config.moveTracking)
      pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Monitor, this);
    if ((this.config.commandTracking) || (this.config.logCommands))
      pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Event.Priority.Monitor, this);
    if (this.config.chatTracking)
      pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Monitor, this);
    if (this.config.blockTracking) {
      pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Monitor, this);
      pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Monitor, this);
    }
    pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Event.Priority.Monitor, this);
    pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Event.Priority.Monitor, this);
  }

  public void onDisable()
  {
    this.config.reloadAndSave();
    stopUpdateTimer();
  }

  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
  {
    if ((!(sender instanceof Player)) && (args.length == 0)) {
      sender.sendMessage(ChatColor.RED + "[Activity] Activity is only tracked for players!");
      return false;
    }

    if (args.length == 0) {
      int activity = getActivity((Player)sender);
      sendMessage(sender, "Your current activity is: " + activityColor(activity) + activity + "%");
    } else if ((args[0].compareToIgnoreCase("list") == 0) || (args[0].compareToIgnoreCase("listall") == 0)) {
      if (hasPermission(sender, "activity.view.list", true)) {
        StringBuilder res = new StringBuilder();
        for (Player player : getServer().getOnlinePlayers()) {
          int activity = getActivity(player);
          res.append(activityColor(activity) + player.getName() + " " + activity + "%");
          res.append(ChatColor.GRAY + ", ");
        }
        sendMessage(sender, res.substring(0, res.length() - 2));
      } else {
        sendMessage(sender, ChatColor.RED + "You have no permission to see a list of online players' activity.");
      }
    } else if ((args[0].compareToIgnoreCase("admin") == 0) && (hasPermission(sender, "activity.admin", false))) {
      if (args.length == 1) {
        sendMessage(sender, ChatColor.RED + "/activity admin <reload|stats|resetstats>");
      } else if (args[1].compareToIgnoreCase("reload") == 0) {
        this.config.reloadAndSave();
        this.config.load();
        stopUpdateTimer();
        startUpdateTimer();
        sendMessage(sender, ChatColor.GREEN + "Reloaded");
      } else if (args[1].compareToIgnoreCase("stats") == 0) {
        sendMessage(sender, ChatColor.YELLOW + "Statistic " + ChatColor.DARK_GRAY + "--------------------");
        sendMessage(sender, "Chat: " + ChatColor.WHITE + 
          this.chatPointsGiven.intValue());
        sendMessage(sender, "Chat char: " + ChatColor.WHITE + 
          this.chatCharPointsGiven.intValue());
        sendMessage(sender, "Command: " + ChatColor.WHITE + 
          this.commandPointsGiven.intValue());
        sendMessage(sender, "Command char: " + ChatColor.WHITE + 
          this.commandCharPointsGiven.intValue());
        sendMessage(sender, "Move: " + ChatColor.WHITE + 
          this.movePointsGiven.intValue());
        sendMessage(sender, "Block place: " + ChatColor.WHITE + 
          this.blockPlacePointsGiven.intValue());
        sendMessage(sender, "Block break: " + ChatColor.WHITE + 
          this.blockBreakPointsGiven.intValue());
      } else if (args[1].compareToIgnoreCase("resetstats") == 0) {
        this.chatPointsGiven = Double.valueOf(0.0D);
        this.chatCharPointsGiven = Double.valueOf(0.0D);
        this.commandPointsGiven = Double.valueOf(0.0D);
        this.commandCharPointsGiven = Double.valueOf(0.0D);
        this.movePointsGiven = Double.valueOf(0.0D);
        this.blockPlacePointsGiven = Double.valueOf(0.0D);
        this.blockBreakPointsGiven = Double.valueOf(0.0D);
        sendMessage(sender, ChatColor.RED + "Stats reseted");
      }
    } else if (args.length == 1) {
      if (hasPermission(sender, "activity.view.other", true)) {
        String playerName = matchSinglePlayer(sender, args[0]).getName();
        int activity = getActivity(playerName);
        sendMessage(sender, "Current activity of " + playerName + ": " + activityColor(activity) + activity + "%");
      } else {
        sendMessage(sender, ChatColor.RED + "You have no permission to see other's activity.");
      }
    }

    return true;
  }

  public boolean hasPermission(CommandSender sender, String node, boolean noPermissionsReturn)
  {
    return hasPermission((Player)sender, node, noPermissionsReturn);
  }

  public boolean hasPermission(Player player, String node, boolean noPermissionsReturn)
  {
    if (player.isOp()) {
      return true;
    }
    if (Permissions != null) {
      return Permissions.has(player, node);
    }
    return noPermissionsReturn;
  }

  public double getMultiplier(Player player, String which)
  {
    if (Permissions == null) {
      return 1.0D;
    }
    double multiplier = Permissions.getPermissionDouble(
      player.getWorld().getName(), player.getName(), "activity.multiplier." + which);
    if (multiplier == -1.0D) {
      return 1.0D;
    }
    return multiplier;
  }

  public void addActivity(String playerName, Double activity)
  {
    activity = Double.valueOf(this.config.pointMultiplier.doubleValue() * activity.doubleValue());
    playerName = playerName.toLowerCase();
    if (((Map)this.playersActivities.get(Integer.valueOf(this.currentSequence))).containsKey(playerName)) {
      activity = Double.valueOf(activity.doubleValue() + ((Double)((Map)this.playersActivities.get(Integer.valueOf(this.currentSequence))).get(playerName)).doubleValue());
    }

    ((Map)this.playersActivities.get(Integer.valueOf(this.currentSequence))).put(playerName, activity);
  }

  public int getActivity(Player player)
  {
    return getActivity(player.getName());
  }

  public int getActivity(String playerName)
  {
    playerName = playerName.toLowerCase();

    Iterator iterator = this.playersActivities.values().iterator();

    Double rawActivity = Double.valueOf(0.0D);
    while (iterator.hasNext()) {
      Map playersActivity = (Map)iterator.next();
      if (playersActivity.containsKey(playerName)) {
        rawActivity = Double.valueOf(rawActivity.doubleValue() + ((Double)playersActivity.get(playerName)).doubleValue());
      }
    }

    int activity = (int)(rawActivity.doubleValue() / this.playersActivities.size());
    if (activity > 100) activity = 100;

    return activity;
  }

  public void sendMessage(CommandSender sender, String message)
  {
    sender.sendMessage(ChatColor.DARK_GRAY + "[Activity] " + ChatColor.GRAY + message);
  }

  public void sendMessage(Player player, String message)
  {
    player.sendMessage(ChatColor.DARK_GRAY + "[Activity] " + ChatColor.GRAY + message);
  }

  public Player matchSinglePlayer(CommandSender sender, String filter)
  {
    filter = filter.toLowerCase();
    for (Player player : getServer().getOnlinePlayers()) {
      if (player.getName().toLowerCase().contains(filter)) {
        return player;
      }
    }

    sender.sendMessage(ChatColor.RED + "No matching player found, matching yourself.");
    return (Player)sender;
  }

  protected void startUpdateTimer()
  {
    updateTimer = new Timer();
    updateTimer.scheduleAtFixedRate(new TimerTask()
    {
      public void run()
      {
        if (HeavenActivity.this.currentSequence % HeavenActivity.this.config.notificationSequence == 0) {
          for (Player player : HeavenActivity.this.getServer().getOnlinePlayers()) {
            int activity = HeavenActivity.this.getActivity(player.getName());
            HeavenActivity.this.sendMessage(player, "Your current activity is: " + 
              HeavenActivity.this.activityColor(activity) + activity + "%");
          }

        }

        if ((HeavenActivity.this.currentSequence % HeavenActivity.this.config.incomeSequence == 0) && (HeavenActivity.this.config.incomeEnabled))
          HeavenActivity.this.handleOnlineIncome();
        int nextSequence;
        int nextSequence;
        if (HeavenActivity.this.currentSequence == HeavenActivity.this.config.maxSequences)
          nextSequence = 1;
        else {
          nextSequence = HeavenActivity.this.currentSequence + 1;
        }

        HeavenActivity.this.playersActivities.put(Integer.valueOf(nextSequence), new HashMap());
        HeavenActivity.this.currentSequence = nextSequence;
      }
    }
    , 0L, this.config.sequenceInterval * 1000L);

    logger.info("[HeavenActivity] Update timer started");
  }

  protected void stopUpdateTimer()
  {
    updateTimer.cancel();
    logger.info("[HeavenActivity] Update timer stopped");
  }

  protected void handleOnlineIncome()
  {
    if (this.playersActivities.size() == 0) {
      return;
    }
    if (iConomy == null) {
      logger.warning("[HeavenActivity] Want to give income, but iConomy isn't active! Skipping...");
      return;
    }

    for (Player player : getServer().getOnlinePlayers()) {
      int activity = getActivity(player);
      if (activity >= this.config.incomeMinActivity) {
        Holdings balance = iConomy.getAccount(player.getName()).getHoldings();

        Double amount = Double.valueOf(this.config.incomeBaseValue.doubleValue() + 
          (activity - this.config.incomeTargetActivity) / this.config.incomeActivityModifier * this.config.incomeBaseValue.doubleValue() + 
          balance.balance() * this.config.incomeBalanceMultiplier.doubleValue());

        if ((amount.doubleValue() > 0.0D) || (this.config.incomeAllowNegative)) {
          balance.add(amount.doubleValue());

          sendMessage(player, "You got " + activityColor(activity) + iConomy.format(amount.doubleValue()) + 
            ChatColor.GRAY + " income for being " + 
            activityColor(activity) + activity + "% " + ChatColor.GRAY + "active.");
          sendMessage(player, "Your Balance is now: " + ChatColor.WHITE + 
            iConomy.format(balance.balance()));

          continue;
        }
      }

      sendMessage(player, ChatColor.RED + "You were too lazy, no income for you this time!");
    }
  }

  protected ChatColor activityColor(int activity)
  {
    if (activity > 75)
      return ChatColor.GREEN;
    if (activity < 25) {
      return ChatColor.RED;
    }
    return ChatColor.YELLOW;
  }
}