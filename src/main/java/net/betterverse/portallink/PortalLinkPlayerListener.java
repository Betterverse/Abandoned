package net.betterverse.portallink;

import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class PortalLinkPlayerListener implements Listener
{
  private final PortalLink plugin;
  Logger log = Logger.getLogger("Minecraft");

  public PortalLinkPlayerListener(PortalLink instance) {
    this.plugin = instance;
  }

	@EventHandler
  public void onPlayerPortal(PlayerPortalEvent event) {
    if (event.isCancelled()) {
      return;
    }
    World fromWorld = null;
    World toWorld = null;
    int dimension = 0;
    boolean useDimension = true;
    Player player = event.getPlayer();
    Map definedLinks = this.plugin.getPortalLinkConfig().getUserDefinedLinks();
    if (definedLinks.containsKey(player.getWorld().getName())) {
      fromWorld = player.getWorld();
      PortalLinkLinkValue linkValue = (PortalLinkLinkValue)definedLinks.get(fromWorld.getName());
      switch (linkValue.getWhichNether()) {
      case 0:
        dimension = -1;
        useDimension = false;
        break;
      case 1:
        dimension = -1;
        break;
      case 2:
        break;
      case 3:
        useDimension = false;
        break;
      }

      World.Environment environment = dimension == -1 ? World.Environment.NORMAL : World.Environment.NETHER;
      if (!linkValue.getString().equals("")) {
				WorldCreator we = new WorldCreator(linkValue.getString());
				we=we.environment(environment);
        toWorld = this.plugin.getServer().createWorld(we);
      } else {
        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "The Nether has been disabled for " + player.getWorld().getName() + ".");
        return;
      }
    } else {
      World.Environment environment = player.getWorld().getEnvironment();
      if (environment.equals(World.Environment.NETHER)) {
        dimension = -1;
      }
      else {
        dimension = 0;
      }
      for (World world1 : this.plugin.getServer().getWorlds()) {
        if (world1.getEnvironment().equals(environment)) {
          if (world1.getName().equals(player.getWorld().getName()))
            fromWorld = world1;
        }
        else
        {
          String worldWorld;
          if (dimension == -1) {
            worldWorld = world1.getName() + "_nether";
          }
          else {
            worldWorld = world1.getName().replaceAll("_nether", "");
          }
          if (worldWorld.equals(player.getWorld().getName())) {
            toWorld = world1;
          }
        }
      }
      if (!this.plugin.getAllowNether(fromWorld)) {
        return;
      }
      if (fromWorld == null) {
        this.log.warning("Unable To Match A World To The Player's World!");
        return;
      }
      if (toWorld == null) {
				WorldCreator wc = new WorldCreator(fromWorld.getName().replaceAll("_nether", ""));
        if (dimension == -1) {
				wc=wc.environment(Environment.NORMAL);
        }
        else {
				wc=wc.environment(Environment.NETHER);
        }
				toWorld = Bukkit.getServer().createWorld(wc);
      }
      if (toWorld.getEnvironment().equals(fromWorld.getEnvironment())) useDimension = false;
    }
    double blockRatio = useDimension ? 0.125D : dimension == -1 ? 8.0D : 1.0D;

    Location fromLocation = new Location(fromWorld, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
    Location toLocation = new Location(toWorld, player.getLocation().getX() * blockRatio, player.getLocation().getY(), player.getLocation().getZ() * blockRatio, player.getLocation().getYaw(), player.getLocation().getPitch());
    event.setTo(toLocation);
    event.setFrom(fromLocation);
  }
}