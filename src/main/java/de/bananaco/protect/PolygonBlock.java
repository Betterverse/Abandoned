package de.bananaco.protect;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.bananaco.protect.util.ProtectedPolygon;

public class PolygonBlock implements Listener {
	
	public PolygonBlock(PluginManager pm, JavaPlugin plugin) {
		pm.registerEvents(this, plugin);
	}
	
	PolygonManager pm = PolygonManager.getInstance();
	
	public void eventCheck(Event event, List<ProtectedPolygon> poly) {
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
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		boolean build = true;
		List<ProtectedPolygon> poly = pm.getList(event.getBlock().getLocation());
		
		if(poly.size() == 0)
			return;
		
		for(ProtectedPolygon p : poly) {
			if(p.canOverride(player))
				return;
			if(!p.canBuild(player))
				build = false;
		}
		if(!build)
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		boolean build = true;
		List<ProtectedPolygon> poly = pm.getList(event.getBlock().getLocation());
		
		if(poly.size() == 0)
			return;
		
		for(ProtectedPolygon p : poly) {
			if(p.canOverride(player))
				return;
			if(!p.canBuild(player))
				build = false;
		}
		if(!build)
		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockBurn(BlockBurnEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getBlock().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getBlock().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onBlockFade(BlockFadeEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getBlock().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onBlockForm(BlockFormEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getBlock().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getBlock().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getBlock().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getBlock().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getBlock().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getBlock().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onBlockSpread(BlockSpreadEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getBlock().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getBlock().getLocation());
				eventCheck(event, poly);
	}

}
