package net.blockheaven.kaipr.heavenactivity;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

public class HeavenActivityPlayerListener extends PlayerListener
{
  protected HeavenActivity plugin;
  protected Map<String, Long> lastAction = new HashMap();

  public HeavenActivityPlayerListener(HeavenActivity plugin)
  {
    this.plugin = plugin;
  }

  public void onPlayerMove(PlayerMoveEvent event)
  {
    if (event.isCancelled()) {
      return;
    }

    if ((event.getPlayer().isInsideVehicle()) || 
      (event.getTo().getX() == event.getFrom().getX()) || 
      (event.getTo().getZ() == event.getFrom().getZ())) {
      return;
    }
    long time = System.currentTimeMillis();
    String playerName = event.getPlayer().getName();

    if ((!this.lastAction.containsKey(playerName)) || (time > ((Long)this.lastAction.get(playerName)).longValue() + this.plugin.config.moveDelay.intValue())) {
      Double points = Double.valueOf(this.plugin.getMultiplier(event.getPlayer(), "move") * this.plugin.config.movePoints.doubleValue());

      this.plugin.addActivity(playerName, points);

      this.lastAction.put(playerName, Long.valueOf(time));
      HeavenActivity tmp186_183 = this.plugin; tmp186_183.movePointsGiven = Double.valueOf(tmp186_183.movePointsGiven.doubleValue() + tmp186_183.doubleValue());
    }
  }

  public void onPlayerChat(PlayerChatEvent event)
  {
    if (event.isCancelled()) {
      return;
    }
    String playerName = event.getPlayer().getName();

    Double points = Double.valueOf(this.plugin.getMultiplier(event.getPlayer(), "chat_char") * 
      event.getMessage().length() * this.plugin.config.chatCharPoints.doubleValue());

    this.plugin.addActivity(playerName, Double.valueOf(points.doubleValue() + this.plugin.config.chatPoints.doubleValue()));
    HeavenActivity tmp91_88 = this.plugin; tmp91_88.chatCharPointsGiven = Double.valueOf(tmp91_88.chatCharPointsGiven.doubleValue() + points.doubleValue());
    HeavenActivity tmp113_110 = this.plugin; tmp113_110.chatPointsGiven = Double.valueOf(tmp113_110.chatPointsGiven.doubleValue() + this.plugin.config.chatPoints.doubleValue());
  }

  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
  {
    String playerName = event.getPlayer().getName();

    if (this.plugin.config.commandTracking) {
      Double points = Double.valueOf(this.plugin.getMultiplier(event.getPlayer(), "command_char") * 
        event.getMessage().length() * this.plugin.config.commandCharPoints.doubleValue());

      this.plugin.addActivity(playerName, Double.valueOf(points.doubleValue() + this.plugin.config.commandPoints.doubleValue()));
      HeavenActivity tmp96_93 = this.plugin; tmp96_93.commandCharPointsGiven = Double.valueOf(tmp96_93.commandCharPointsGiven.doubleValue() + points.doubleValue());
      HeavenActivity tmp118_115 = this.plugin; tmp118_115.commandPointsGiven = Double.valueOf(tmp118_115.commandPointsGiven.doubleValue() + this.plugin.config.commandPoints.doubleValue());
    }

    if (this.plugin.config.logCommands)
      HeavenActivity.logger.info("[cmd] " + playerName + ": " + event.getMessage());
  }
}