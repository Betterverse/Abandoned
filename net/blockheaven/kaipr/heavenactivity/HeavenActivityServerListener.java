package net.blockheaven.kaipr.heavenactivity;

import com.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

public class HeavenActivityServerListener extends ServerListener
{
  private HeavenActivity plugin;

  public HeavenActivityServerListener(HeavenActivity plugin)
  {
    this.plugin = plugin;
  }

  public void onPluginEnable(PluginEnableEvent event)
  {
    if (HeavenActivity.Permissions == null) {
      Plugin permissions = this.plugin.getServer().getPluginManager().getPlugin("Permissions");

      if ((permissions != null) && 
        (permissions.isEnabled())) {
        HeavenActivity.Permissions = ((Permissions)permissions).getHandler();
        HeavenActivity.logger.info("[HeavenActivity] hooked into Permissions");
      }

    }

    if ((HeavenActivity.iConomy == null) && (this.plugin.config.incomeEnabled)) {
      Plugin iConomy = this.plugin.getServer().getPluginManager().getPlugin("iConomy");

      if ((iConomy != null) && 
        (iConomy.isEnabled())) {
        if (!iConomy.getDescription().getVersion().startsWith("5")) {
          HeavenActivity.logger.warning(
            "[HeavenActivity] This version needs iConomy 5 to work! If you get errors, upgrade iConomy or disable income!");
        }
        HeavenActivity.iConomy = (iConomy)iConomy;
        HeavenActivity.logger.info("[HeavenActivity] hooked into iConomy.");
      }
    }
  }

  public void onPluginDisable(PluginDisableEvent event)
  {
    if ((HeavenActivity.Permissions != null) && 
      (event.getPlugin().getDescription().getName().equals("Permissions"))) {
      HeavenActivity.Permissions = null;
      HeavenActivity.logger.info("[HeavenActivity] un-hooked from Permissions.");
    }

    if ((HeavenActivity.iConomy != null) && 
      (event.getPlugin().getDescription().getName().equals("iConomy"))) {
      HeavenActivity.iConomy = null;
      HeavenActivity.logger.info("[HeavenActivity] un-hooked from iConomy.");
    }
  }
}