package net.blockheaven.kaipr.heavenactivity;

import java.io.File;
import java.util.List;
import org.bukkit.util.config.Configuration;

public class HeavenActivityConfig
{
  protected HeavenActivity plugin;
  protected Configuration config;
  public int maxSequences;
  public int sequenceInterval;
  public int notificationSequence;
  public int incomeSequence;
  public Double pointMultiplier;
  public boolean chatTracking;
  public Double chatPoints;
  public Double chatCharPoints;
  public boolean commandTracking;
  public Double commandPoints;
  public Double commandCharPoints;
  public boolean moveTracking;
  public Integer moveDelay;
  public Double movePoints;
  public boolean blockTracking;
  public Integer blockDelay;
  public Double blockPlacePoints;
  public Double blockBreakPoints;
  public boolean incomeEnabled;
  public int incomeMinActivity;
  public boolean incomeAllowNegative;
  public Double incomeBaseValue;
  public int incomeTargetActivity;
  public int incomeActivityModifier;
  public Double incomeBalanceMultiplier;
  public boolean logCommands;

  public HeavenActivityConfig(HeavenActivity plugin)
  {
    this.plugin = plugin;

    plugin.getDataFolder().mkdirs();
    this.config = plugin.getConfiguration();

    load();
  }

  public void load() {
    this.config.load();

    this.maxSequences = this.config.getInt("general.max_sequences", 15);
    this.sequenceInterval = this.config.getInt("general.sequence_interval", 60);
    this.notificationSequence = this.config.getInt("general.notification_sequence", 6);
    this.incomeSequence = this.config.getInt("general.income_sequence", 15);
    this.pointMultiplier = Double.valueOf(this.config.getDouble("general.point_multiplier", 1.0D));

    this.incomeEnabled = this.config.getBoolean("income.enabled", true);
    this.incomeMinActivity = this.config.getInt("income.min_activity", 1);
    this.incomeAllowNegative = this.config.getBoolean("income.allow_negative", true);
    this.incomeBaseValue = Double.valueOf(this.config.getDouble("income.base_value", 8.0D));
    this.incomeTargetActivity = this.config.getInt("income.target_activity", 50);
    this.incomeActivityModifier = this.config.getInt("income.activity_modifier", 75);
    this.incomeBalanceMultiplier = Double.valueOf(this.config.getDouble("income.balance_multiplier", 0.0D));

    this.chatTracking = this.config.getBoolean("chat.tracking", true);
    this.chatPoints = Double.valueOf(this.config.getDouble("chat.points", 1.0D));
    this.chatCharPoints = Double.valueOf(this.config.getDouble("chat.char_points", 0.49D));
    this.commandTracking = this.config.getBoolean("command.tracking", true);
    this.commandPoints = Double.valueOf(this.config.getDouble("command.points", 1.0D));
    this.commandCharPoints = Double.valueOf(this.config.getDouble("command.char_points", 0.53D));
    this.moveTracking = this.config.getBoolean("move.tracking", true);
    this.moveDelay = Integer.valueOf(this.config.getInt("move.delay", 1100));
    this.movePoints = Double.valueOf(this.config.getDouble("move.points", 0.58D));
    this.blockTracking = this.config.getBoolean("block.tracking", true);
    this.blockDelay = Integer.valueOf(this.config.getInt("block.delay", 950));
    this.blockPlacePoints = Double.valueOf(this.config.getDouble("block.place_points", 3.75D));
    this.blockBreakPoints = Double.valueOf(this.config.getDouble("block.break_points", 1.95D));

    this.logCommands = this.config.getBoolean("general.log_commands", false);

    this.plugin.chatPointsGiven = Double.valueOf(this.config.getDouble("stats.chat_points", 0.0D));
    this.plugin.chatCharPointsGiven = Double.valueOf(this.config.getDouble("stats.chat_char_points", 0.0D));
    this.plugin.commandPointsGiven = Double.valueOf(this.config.getDouble("stats.command_points", 0.0D));
    this.plugin.commandCharPointsGiven = Double.valueOf(this.config.getDouble("stats.command_char_points", 0.0D));
    this.plugin.movePointsGiven = Double.valueOf(this.config.getDouble("stats.move_points", 0.0D));
    this.plugin.blockPlacePointsGiven = Double.valueOf(this.config.getDouble("stats.block_place_points", 0.0D));
    this.plugin.blockBreakPointsGiven = Double.valueOf(this.config.getDouble("stats.block_break_points", 0.0D));
  }

  public void reloadAndSave() {
    this.config.load();

    List configNodes = this.config.getKeys("general");
    if (!configNodes.contains("max_sequences"))
      this.config.setProperty("general.max_sequences", Integer.valueOf(this.maxSequences));
    if (!configNodes.contains("sequence_interval"))
      this.config.setProperty("general.sequence_interval", Integer.valueOf(this.sequenceInterval));
    if (!configNodes.contains("notification_sequence"))
      this.config.setProperty("general.notification_sequence", Integer.valueOf(this.notificationSequence));
    if (!configNodes.contains("income_sequence"))
      this.config.setProperty("general.income_sequence", Integer.valueOf(this.incomeSequence));
    if (!configNodes.contains("point_multiplier")) {
      this.config.setProperty("general.point_multiplier", this.pointMultiplier);
    }
    configNodes = this.config.getKeys("income");
    if (!configNodes.contains("enabled"))
      this.config.setProperty("income.enabled", Boolean.valueOf(this.incomeEnabled));
    if (!configNodes.contains("base_value"))
      this.config.setProperty("income.base_value", this.incomeBaseValue);
    if (!configNodes.contains("target_activity"))
      this.config.setProperty("income.target_activity", Integer.valueOf(this.incomeTargetActivity));
    if (!configNodes.contains("activity_modifier"))
      this.config.setProperty("income.activity_modifier", Integer.valueOf(this.incomeActivityModifier));
    if (!configNodes.contains("balance_multiplier")) {
      this.config.setProperty("income.balance_multiplier", this.incomeBalanceMultiplier);
    }
    this.config.setProperty("stats.chat_points", this.plugin.chatPointsGiven);
    this.config.setProperty("stats.chat_char_points", this.plugin.chatCharPointsGiven);
    this.config.setProperty("stats.command_points", this.plugin.commandPointsGiven);
    this.config.setProperty("stats.command_char_points", this.plugin.commandCharPointsGiven);
    this.config.setProperty("stats.move_points", this.plugin.movePointsGiven);
    this.config.setProperty("stats.block_place_points", this.plugin.blockPlacePointsGiven);
    this.config.setProperty("stats.block_break_points", this.plugin.blockBreakPointsGiven);

    this.config.save();
  }
}