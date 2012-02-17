package org.maats.madcap.bukkit.SimpleAlias;

import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class PermissionsListener extends ServerListener
{
  private SimpleAlias mainPlugin;

  public PermissionsListener(SimpleAlias thisPlugin)
  {
    this.mainPlugin = thisPlugin;
  }

  public void onPluginEnable(PluginEnableEvent event)
  {
    String pluginName = event.getPlugin().getDescription().getName();

    if (pluginName.equals("Permissions")) {
      Plugin permPlugin = event.getPlugin();
      if ((permPlugin != null) && (permPlugin.isEnabled())) {
        SimpleAlias.permissions = ((Permissions)permPlugin).getHandler();
        SimpleAlias.print("Permissions plugin is enabled, using permission node SimpleAlias.* for /alias.");
      }
    }
  }

  public void onPluginDisabled(PluginEnableEvent event) {
    String pluginName = event.getPlugin().getDescription().getName();
    if (pluginName.equals("Permissions")) {
      Plugin permPlugin = event.getPlugin();

      if (((permPlugin != null) && (!permPlugin.isEnabled())) || (permPlugin == null)) {
        if (SimpleAlias.permissions != null)
        {
          SimpleAlias.print("Permissions plugin has been disabled, no longer requiring any permissions for /alias.");
        }
        SimpleAlias.permissions = null;
      }
    }
  }
}