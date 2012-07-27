package net.betterverse.protect;

import java.util.List;
import net.betterverse.protect.utils.ProtectedPolygon;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInventoryEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PolygonPlayer implements Listener{
	
	PolygonManager pm = PolygonManager.getInstance();
	
	public PolygonPlayer(PluginManager pm, JavaPlugin plugin) {
		pm.registerEvents(this,plugin);
	}

	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		Player player = event.getPlayer();
		boolean build = true;
		List<ProtectedPolygon> poly = pm.getList(event.getBlockClicked().getLocation());
		
		if(poly.isEmpty())
			return;
		
		for(ProtectedPolygon p : poly) {
			if(p.canOverride(player))
				return;
			if(!p.canBuild(player))
				build = false;
		}
		event.setCancelled(!build);	
		PolygonDebug.log(event, (Cancellable) event);
	}
	
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();
		boolean build = true;
		List<ProtectedPolygon> poly = pm.getList(event.getBlockClicked().getLocation());
		
		if(poly.size() == 0)
			return;
		
		for(ProtectedPolygon p : poly) {
			if(p.canOverride(player))
				return;
			if(!p.canBuild(player))
				build = false;
		}
		event.setCancelled(!build);	
		PolygonDebug.log(event, (Cancellable) event);
	}
	
	public void eventCheck(Player player, Event event, List<ProtectedPolygon> poly) {
		if(poly.size() == 0)
			return;
		Cancellable cancel = null;
		if(event instanceof Cancellable)
			cancel = (Cancellable) event;
		else
			return;
		String flag = event.getEventName();
		boolean cancelled = false;
		for(ProtectedPolygon p : poly) {
			if(p.canOverride(player))
				return;
			boolean fl = p.getFlag(flag);
			if(fl) {
				cancelled = true;
			}
		}
		if(cancelled)
			cancel.setCancelled(true);

		PolygonDebug.log(event, (Cancellable) event);
	}

	@EventHandler
	public void onInventoryOpen(PlayerInventoryEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getPlayer().getLocation());
				eventCheck(event.getPlayer(), event, poly);
	}

	@EventHandler
	public void onItemHeldChange(PlayerItemHeldEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getPlayer().getLocation());
				eventCheck(event.getPlayer(), event, poly);
	}

	@EventHandler
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getBed().getLocation());
				eventCheck(event.getPlayer(), event, poly);
	}

	@EventHandler
	public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getBed().getLocation());
				eventCheck(event.getPlayer(), event, poly);
	}

	@EventHandler
	public void onPlayerChat(PlayerChatEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getPlayer().getLocation());
				eventCheck(event.getPlayer(), event, poly);
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getPlayer().getLocation());
				eventCheck(event.getPlayer(), event, poly);
	}

	@EventHandler
	public void onPlayerEggThrow(PlayerEggThrowEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getPlayer().getLocation());
				eventCheck(event.getPlayer(), event, poly);
	}

	@EventHandler
	public void onPlayerFish(PlayerFishEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getPlayer().getLocation());
				eventCheck(event.getPlayer(), event, poly);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getClickedBlock() == null)
			return;
		if(event.getClickedBlock().getLocation() == null)
			return;
	
		
		Player player = event.getPlayer();
		boolean cancel = false;
		
		List<ProtectedPolygon> poly = pm.getList(event.getClickedBlock().getLocation());
		
		if(poly.size() == 0)
			return;
		
		String flag = event.getEventName()+"_"+event.getClickedBlock().getType().name();
		String gFlag = event.getEventName();
		
		for(ProtectedPolygon p : poly) {
			if(p.canOverride(player))
				return;
			
			if((p.getFlag(flag) || p.getFlag(gFlag)))
				cancel = true;
		}
		if(cancel)
		event.setCancelled(true);
		PolygonDebug.log(event, (Cancellable) event);
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getRightClicked().getLocation());
				eventCheck(event.getPlayer(), event, poly);
	}

	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getPlayer().getLocation());
				eventCheck(event.getPlayer(), event, poly);
	}

	@EventHandler
	public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getPlayer().getLocation());
				eventCheck(event.getPlayer(), event, poly);
	}

}
