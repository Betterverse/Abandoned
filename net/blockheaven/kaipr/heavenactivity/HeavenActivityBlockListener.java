package net.blockheaven.kaipr.heavenactivity;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class HeavenActivityBlockListener extends BlockListener
{
  protected HeavenActivity plugin;
  protected Map<String, Long> lastAction = new HashMap();

  public HeavenActivityBlockListener(HeavenActivity plugin)
  {
    this.plugin = plugin;
  }

  public void onBlockPlace(BlockPlaceEvent event)
  {
    if (event.isCancelled()) {
      return;
    }
    long time = System.currentTimeMillis();
    String playerName = event.getPlayer().getName();

    if ((!this.lastAction.containsKey(playerName)) || (time > ((Long)this.lastAction.get(playerName)).longValue() + this.plugin.config.blockDelay.intValue())) {
      Double points = Double.valueOf(this.plugin.getMultiplier(event.getPlayer(), "block_place") * this.plugin.config.blockPlacePoints.doubleValue());

      this.plugin.addActivity(playerName, points);

      this.lastAction.put(playerName, Long.valueOf(time));
      HeavenActivity tmp137_134 = this.plugin; tmp137_134.blockPlacePointsGiven = Double.valueOf(tmp137_134.blockPlacePointsGiven.doubleValue() + tmp137_134.doubleValue());
    }
  }

  public void onBlockBreak(BlockBreakEvent event)
  {
    if (event.isCancelled()) {
      return;
    }
    long time = System.currentTimeMillis();
    String playerName = event.getPlayer().getName();

    if ((!this.lastAction.containsKey(playerName)) || (time > ((Long)this.lastAction.get(playerName)).longValue() + this.plugin.config.blockDelay.intValue())) {
      Double points = Double.valueOf(this.plugin.getMultiplier(event.getPlayer(), "block_break") * this.plugin.config.blockBreakPoints.doubleValue());

      this.plugin.addActivity(playerName, points);

      this.lastAction.put(playerName, Long.valueOf(time));
      HeavenActivity tmp137_134 = this.plugin; tmp137_134.blockBreakPointsGiven = Double.valueOf(tmp137_134.blockBreakPointsGiven.doubleValue() + tmp137_134.doubleValue());
    }
  }
}