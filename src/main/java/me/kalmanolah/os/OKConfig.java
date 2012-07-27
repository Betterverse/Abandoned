package me.kalmanolah.os;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

public class OKConfig
{
  private static OKmain plugin;
  public static String directory = "plugins" + File.separator + OKmain.name;
  static File file = new File(directory + File.separator + "config.yml");

  public OKConfig(OKmain instance)
  {
    plugin = instance;
  }

  public void configCheck()
  {
    new File(directory).mkdir();
    if (!file.exists()) {
      try {
        OKLogger.info("Attempting to create configuration file...");
        file.createNewFile();
        addDefaults();
        OKLogger.info("Configuration file successfully created.");
      } catch (Exception ex) {
        ex.printStackTrace();
        OKLogger.error("Erorr creating configuration file.");
      }
    } else {
      OKLogger.info("Attempting to load configuration file...");
      loadkeys();
      OKLogger.info("Configuration file successfully loaded.");
    }
  }

  private static void write(String root, Object x) {
    YamlConfiguration config = load();
    config.set(root, x);
    try {
      config.save(file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String readString(String root) {
    YamlConfiguration config = load();
    return config.getString(root);
  }

  private static YamlConfiguration load() {
    try {
      YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
      return config;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static void addDefaults() {
    write("punishment-type", "2");
    write("maximum-messages-per-minute", "30");
    write("punishments.quiet-time-requirement", "15");
    write("punishments.enable-messages", Boolean.valueOf(true));
    write("punishments.damage-per-message", "2");
    write("punishments.commands-to-execute", "kick %name%|say Kicking %name% from %world% for spamming!");
    loadkeys();
  }

  public static void loadkeys() {
    OKmain.mode = Integer.valueOf(Integer.parseInt(readString("punishment-type")));
    OKmain.maxmsgs = Integer.valueOf(Integer.parseInt(readString("maximum-messages-per-minute")));
    OKmain.duration = Integer.valueOf(Integer.parseInt(readString("punishments.quiet-time-requirement")));
    OKmain.damagepermessage = Integer.valueOf(Integer.parseInt(readString("punishments.damage-per-message")));
    OKmain.commands = readString("punishments.commands-to-execute").split("\\|");
    OKmain.enablemessages = Boolean.valueOf(readString("punishments.enable-messages"));
    Boolean stats = Boolean.valueOf(readString("enable-anonymous-stat-tracking"));
    if (stats == null)
      OKmain.stats = Boolean.valueOf(true);
    else
      OKmain.stats = stats;
  }
}