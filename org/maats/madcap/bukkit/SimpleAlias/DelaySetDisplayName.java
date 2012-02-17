package org.maats.madcap.bukkit.SimpleAlias;

import org.bukkit.entity.Player;

public class DelaySetDisplayName
  implements Runnable
{
  private Player player = null;

  protected DelaySetDisplayName(Player p) {
    this.player = p;
  }

  public void run()
  {
    if (this.player.isOnline())
      this.player.setDisplayName(this.player.getName());
  }
}