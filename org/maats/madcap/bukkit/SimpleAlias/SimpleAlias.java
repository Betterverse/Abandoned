package org.maats.madcap.bukkit.SimpleAlias;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class SimpleAlias extends JavaPlugin
{
  private final SimpleAliasPlayerListener playerListener = new SimpleAliasPlayerListener(this);

  private final HashMap<Player, Boolean> debugees = new HashMap();

  protected File dataDir = null;
  protected final String ALIASES_FILE = "aliases.txt";
  protected List<String> bannedAliases = null;
  protected File playerDir = null;
  protected Properties props = null;
  protected final String CONFIG_FILE = "alias_config.yml";
  protected Configuration config = null;

  protected HashMap<String, String> names = null;

  protected static PermissionHandler permissions = null;
  protected PermissionsListener permListener = new PermissionsListener(this);

  protected static void print(String s) {
    System.out.println("SimpleAlias: " + s);
  }

  public void onEnable()
  {
    getCommand("alias").setExecutor(new AliasCommand(this));

    this.props = new Properties();
    File propFile = new File("server.properties");
    if (propFile.exists()) {
      try {
        this.props.load(new FileInputStream(propFile));
        this.playerDir = new File(this.props.getProperty("level-name") + File.separator + "players");
      }
      catch (Exception e)
      {
        print("server.properties file was not found!");
      }

    }

    PluginManager pm = getServer().getPluginManager();
    pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Monitor, this);
    pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this.playerListener, Event.Priority.Lowest, this);
    pm.registerEvent(Event.Type.PLUGIN_ENABLE, this.permListener, Event.Priority.Monitor, this);

    this.dataDir = getDataFolder();

    if (!this.dataDir.exists())
    {
      print("Plugin data folder is missing, creating " + this.dataDir.getAbsolutePath());
      this.dataDir.mkdir();
    }

    File configFile = new File(this.dataDir.getAbsolutePath() + File.separator + "alias_config.yml");
    if (!configFile.exists()) {
      print("Configuration file alias_config.yml is missing!");
      this.config = null;
      this.bannedAliases = null;
    }
    else
    {
      this.config = new Configuration(configFile);
      this.config.load();
      this.bannedAliases = this.config.getStringList("banned-aliases", null);

      if (this.bannedAliases.isEmpty()) {
        this.bannedAliases = null;
      }

      if (this.bannedAliases == null) {
        this.bannedAliases = this.config.getStringList("banned-aliaes", null);
        if (this.bannedAliases.isEmpty()) {
          this.bannedAliases = null;
        }
      }
    }

    this.names = new HashMap();

    File aliasFile = new File(this.dataDir.getAbsolutePath() + File.separator + "aliases.txt");
    if (!aliasFile.exists())
    {
      try
      {
        aliasFile.createNewFile();
        print("Alias file is missing, creating " + aliasFile.getAbsolutePath());
      }
      catch (IOException ex)
      {
        print("Alias file is missing, failed to create new one.");
      }

    }

    if (loadAliases())
      print("Alias file loaded.");
    else {
      print("Alias file could not be loaded.");
    }

    Plugin permPlugin = getServer().getPluginManager().getPlugin("Permissions");
    if ((permPlugin != null) && (permPlugin.isEnabled())) {
      permissions = ((Permissions)permPlugin).getHandler();
      print("Permissions plugin is enabled, using permission node SimpleAlias.* for access to /alias.");
    }

    PluginDescriptionFile pdfFile = getDescription();
    System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled.");
  }

  public void onDisable()
  {
    permissions = null;

    if (saveAliases())
      print("Aliases written to file.");
    else {
      print("Aliases could not be writtren to file.");
    }
    this.names = null;
    this.bannedAliases = null;
    this.playerDir = null;
    this.dataDir = null;
    this.props = null;
    this.config = null;

    print("Plugin shutting down.");
  }

  public boolean isDebugging(Player player) {
    if (this.debugees.containsKey(player)) {
      return ((Boolean)this.debugees.get(player)).booleanValue();
    }
    return false;
  }

  public void setDebugging(Player player, boolean value)
  {
    this.debugees.put(player, Boolean.valueOf(value));
  }

  protected boolean loadAliases()
  {
    try
    {
      FileReader fr = new FileReader(this.dataDir.getAbsolutePath() + File.separator + "aliases.txt");
      BufferedReader reader = new BufferedReader(fr);
      String line = reader.readLine();
      while (line != null)
      {
        String[] values = line.split("=");
        if (values.length == 2)
        {
          this.names.put(values[0], values[1]);
        }
        line = reader.readLine();
      }
      reader.close();
      fr.close();
      return true;
    }
    catch (Exception ex)
    {
      this.names = null;
    }return false;
  }

  protected boolean saveAliases()
  {
    try
    {
      FileWriter fw = new FileWriter(this.dataDir.getAbsolutePath() + File.separator + "aliases.txt");
      BufferedWriter writer = new BufferedWriter(fw);
      for (Map.Entry entry : this.names.entrySet())
      {
        writer.write((String)entry.getKey() + "=" + (String)entry.getValue());
        writer.newLine();
      }
      writer.close();
      fw.close();
      return true;
    }
    catch (Exception ex)
    {
    }
    return false;
  }

  protected boolean isPlayerName(String alias)
  {
    if ((this.playerDir != null) && (this.playerDir.exists()))
    {
      String[] playerArray = this.playerDir.list();
      for (int i = 0; i < playerArray.length; i++)
      {
        String loginName = playerArray[i];

        loginName = loginName.replaceAll("\\.dat", "");

        if (alias.compareToIgnoreCase(loginName) == 0) {
          return true;
        }
      }
    }
    return false;
  }

  protected boolean isBanned(String alias)
  {
    if (this.bannedAliases != null) {
      Iterator it = this.bannedAliases.iterator();
      while (it.hasNext()) {
        String notAllowed = (String)it.next();
        if (alias.compareToIgnoreCase(notAllowed) == 0) {
          return true;
        }
      }
    }
    return false;
  }
}