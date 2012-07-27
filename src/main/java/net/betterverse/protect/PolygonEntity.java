package net.betterverse.protect;

import java.util.List;
import net.betterverse.protect.utils.ProtectedPolygon;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PolygonEntity implements Listener {
	
	PolygonManager pm = PolygonManager.getInstance();
	
	public PolygonEntity(PluginManager pm, JavaPlugin plugin) {
		pm.registerEvents(this,plugin);
	}
	
	public void eventCheck(Event event, List<ProtectedPolygon> poly) {
		if(poly.isEmpty())
			return;
		Cancellable cancel ;
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
	public void onCreatureSpawn(CreatureSpawnEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getEntity().getLocation());
				eventCheck(event, poly);

	}

	@EventHandler
	public void onCreeperPower(CreeperPowerEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getEntity().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onEndermanPickup(EntityChangeBlockEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getBlock().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onEntityCombust(EntityCombustEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getEntity().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
				if(event.getEntity() instanceof Player) {
				List<ProtectedPolygon> poly = pm.getList(event.getEntity().getLocation());
				if(event instanceof EntityDamageByEntityEvent) {
					EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
					if(ev.getDamager() instanceof Player) {
						if(poly.size() == 0)
							return;
						String flag = "PLAYER_DAMAGE_PVP";
						boolean cancelled = false;
						for(ProtectedPolygon p : poly) {
							boolean fl = p.getFlag(flag);
							if(fl) {
								cancelled = true;
							}
						}
						if(cancelled) {
							event.setCancelled(true);
						((Player)ev.getDamager()).sendMessage(ChatColor.RED+flag+" is disabled");
						}
						return;
					}
				} else {
				if(poly.size() == 0)
					return;
				String flag = "PLAYER_DAMAGE";
				boolean cancelled = false;
				for(ProtectedPolygon p : poly) {
					boolean fl = p.getFlag(flag);
					if(fl) {
						cancelled = true;
					}
				}
				if(cancelled)
					event.setCancelled(true);
				return;
				}
				}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getEntity().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onExplosionPrime(ExplosionPrimeEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getEntity().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getEntity().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onPaintingPlace(PaintingPlaceEvent event) {
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
		event.setCancelled(!build);	
	}

	@EventHandler
	public void onPaintingBreak(PaintingBreakEvent ev) {
		if(ev instanceof PaintingBreakByEntityEvent) {
		PaintingBreakByEntityEvent event = (PaintingBreakByEntityEvent) ev;
		if(!(event.getRemover() instanceof Player))
			return;
		
		Player player = (Player) event.getRemover();
		boolean build = true;
		List<ProtectedPolygon> poly = pm.getList(event.getPainting().getLocation());
		
		if(poly.size() == 0)
			return;
		
		for(ProtectedPolygon p : poly) {
			if(p.canOverride(player))
				return;
			if(!p.canBuild(player))
				build = false;
		}
		event.setCancelled(!build);	
		}
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getEntity().getLocation());
				eventCheck(event, poly);
	}

	@EventHandler
	public void onSlimeSplit(SlimeSplitEvent event) {
				List<ProtectedPolygon> poly = pm.getList(event.getEntity().getLocation());
				eventCheck(event, poly);
	}

}
