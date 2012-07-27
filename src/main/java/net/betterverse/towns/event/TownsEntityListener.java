package net.betterverse.towns.event;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;

import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.PlayerCache;
import net.betterverse.towns.PlayerCache.TownBlockStatus;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.BlockLocation;
import net.betterverse.towns.object.Coord;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.TownBlock;
import net.betterverse.towns.object.TownBlockType;
import net.betterverse.towns.object.TownsPermission;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;
import net.betterverse.towns.object.WorldCoord;
import net.betterverse.towns.tasks.MobRemovalTimerTask;
import net.betterverse.towns.tasks.ProtectionRegenTask;
import net.betterverse.towns.war.TownsWarConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TownsEntityListener implements Listener {
	private final Towns plugin;

	public TownsEntityListener(Towns instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void entityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) {
			return;
		}

		long start = System.currentTimeMillis();

		Entity attacker = null;
		Entity defender = null;

		if (event instanceof EntityDamageByEntityEvent) {
			//plugin.sendMsg("EntityDamageByEntityEvent");
			EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
			if (entityEvent.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) entityEvent.getDamager();
				attacker = projectile.getShooter();
				defender = entityEvent.getEntity();
			} else {
				attacker = entityEvent.getDamager();
				defender = entityEvent.getEntity();
			}
		}

		if (attacker != null) {
			//plugin.sendMsg("Attacker not null");

			TownsUniverse universe = plugin.getTownsUniverse();
			try {
				TownsWorld world = TownsUniverse.getWorld(defender.getWorld().getName());

				// Wartime
				if (universe.isWarTime()) {
					event.setCancelled(false);
					throw new Exception();
				}

				Player a = null;
				Player b = null;

				if (attacker instanceof Player) {
					a = (Player) attacker;
				}
				if (defender instanceof Player) {
					b = (Player) defender;
				}

				if (preventDamageCall(world, attacker, defender, a, b)) {
					event.setCancelled(true);
				}
			} catch (Exception e) {
			}

			TownsMessaging.sendDebugMsg("onEntityDamagedByEntity took " + (System.currentTimeMillis() - start) + "ms");
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void entityDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof Player) {
			Player player = (Player) entity;
			TownsMessaging.sendDebugMsg("onPlayerDeath: " + player.getName() + "[ID: " + entity.getEntityId() + "]");
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void creatureSpawn(CreatureSpawnEvent event) {
		if (event.getEntity() instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) event.getEntity();
			Location loc = event.getLocation();
			Coord coord = Coord.parseCoord(loc);
			TownsWorld townsWorld = null;

			try {
				townsWorld = TownsUniverse.getWorld(loc.getWorld().getName());
			} catch (NotRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//remove from world if set to remove mobs globally
			if (townsWorld.isUsingTowns()) {
				if (! townsWorld.hasWorldMobs() && MobRemovalTimerTask.isRemovingWorldEntity(livingEntity)) {
					//TownsMessaging.sendDebugMsg("onCreatureSpawn world: Canceled " + event.getCreatureType() + " from spawning within "+coord.toString()+".");
					event.setCancelled(true);
				}
			}

			//remove from towns if in the list and set to remove		
			try {
				TownBlock townBlock = townsWorld.getTownBlock(coord);
				if (townsWorld.isUsingTowns() && ! townsWorld.isForceTownMobs()) {
					if (! townBlock.getTown().hasMobs() && ! townBlock.getPermissions().mobs) {
						if (MobRemovalTimerTask.isRemovingTownEntity(livingEntity)) {
							//TownsMessaging.sendDebugMsg("onCreatureSpawn town: Canceled " + event.getCreatureType() + " from spawning within "+coord.toString()+".");
							event.setCancelled(true);
						}
					}
				}
			} catch (TownsException x) {
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void entityInteract(EntityInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Block block = event.getBlock();
		Entity entity = event.getEntity();
		TownsWorld townsWorld = null;

		try {
			townsWorld = TownsUniverse.getWorld(block.getLocation().getWorld().getName());
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Prevent creatures trampling crops
		if ((townsWorld.isUsingTowns()) && (townsWorld.isDisableCreatureTrample())) {
			if ((block.getType() == Material.SOIL) || (block.getType() == Material.CROPS)) {
				if (entity instanceof Creature) {
					event.setCancelled(true);
				}
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void endermanPickupOrPlace(EntityChangeBlockEvent event) {
		Block block = event.getBlock();

		TownsWorld townsWorld = null;
		TownBlock townBlock;

		if(event.getEntityType() != EntityType.ENDERMAN) return;

		try {
			townsWorld = TownsUniverse.getWorld(block.getLocation().getWorld().getName());
			townBlock = townsWorld.getTownBlock(new Coord(Coord.parseCoord(block)));
			if (! townsWorld.isForceTownMobs() && ! townBlock.getPermissions().mobs && ! townBlock.getTown().hasMobs()) {
				event.setCancelled(true);
			}
		} catch (NotRegisteredException e) {
			// not in a townblock so test config
			if (TownsSettings.getUnclaimedZoneEndermanProtect()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void entityExplode(EntityExplodeEvent event) {
		Location loc;
		Coord coord;
		List<Block> blocks = event.blockList();
		Entity entity = event.getEntity();
		int count = 0;

		for (Block block : blocks) {
			loc = block.getLocation();
			coord = Coord.parseCoord(loc);
			count++;
			TownsWorld townsWorld;

			try {
				townsWorld = TownsUniverse.getWorld(loc.getWorld().getName());
			} catch (NotRegisteredException e) {
				// failed to get world so abort
				return;
			}

			// Warzones
			if (townsWorld.isWarZone(coord)) {
				if (! TownsWarConfig.isAllowingExplosionsInWarZone()) {
					if (event.getEntity() != null) {
						TownsMessaging.sendDebugMsg("onEntityExplode: Canceled " + event.getEntity().getEntityId() + " from exploding within " + coord.toString() + ".");
					}
					event.setCancelled(true);
					break;
				} else {
					if (TownsWarConfig.explosionsBreakBlocksInWarZone()) {
						if (TownsWarConfig.regenBlocksAfterExplosionInWarZone()) {
							// ***********************************
							// TODO

							// On completion, remove TODO from config.yml comments.

							/*
							if (!plugin.getTownsUniverse().hasProtectionRegenTask(new BlockLocation(block.getLocation()))) {
								ProtectionRegenTask task = new ProtectionRegenTask(plugin.getTownsUniverse(), block, false);
								task.setTaskId(plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, ((TownsSettings.getPlotManagementWildRegenDelay() + count)*20)));
								plugin.getTownsUniverse().addProtectionRegenTask(task);
							}
							*/

							// TODO
							// ***********************************
						}

						// Break the block
					} else {
						event.blockList().remove(block);
					}
				}
				return;
			}

			//TODO: expand to protect neutrals during a war
			try {
				TownBlock townBlock = townsWorld.getTownBlock(coord);

				// If explosions are off, or it's wartime and explosions are off and the towns has no nation
				if (townsWorld.isUsingTowns() && ! townsWorld.isForceExpl()) {
					if ((! townBlock.getTown().isBANG() && ! townBlock.getPermissions().explosion) || (plugin.getTownsUniverse().isWarTime() && ! townBlock.getTown().hasNation() && ! townBlock.getTown().isBANG())) {
						if (event.getEntity() != null) {
							TownsMessaging.sendDebugMsg("onEntityExplode: Canceled " + event.getEntity().getEntityId() + " from exploding within " + coord.toString() + ".");
						}
						event.setCancelled(true);
					}
				}
			} catch (TownsException x) {
				// Wilderness explosion regeneration
				if ((townsWorld.isUsingTowns()) && (townsWorld.isUsingPlotManagementWildRevert())) {
					if (entity instanceof Creature) {
						if (! plugin.getTownsUniverse().hasProtectionRegenTask(new BlockLocation(block.getLocation()))) {
							ProtectionRegenTask task = new ProtectionRegenTask(plugin.getTownsUniverse(), block, false);
							task.setTaskId(plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, ((TownsSettings.getPlotManagementWildRegenDelay() + count) * 20)));
							plugin.getTownsUniverse().addProtectionRegenTask(task);
							event.setYield((float) 0.0);
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void paintingBreak(PaintingBreakEvent event) {
		if (event.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		long start = System.currentTimeMillis();

		if (event instanceof PaintingBreakByEntityEvent) {
			PaintingBreakByEntityEvent evt = (PaintingBreakByEntityEvent) event;
			if (evt.getRemover() instanceof Player) {
				Player player = (Player) evt.getRemover();
				Painting painting = evt.getPainting();

				WorldCoord worldCoord;
				try {
					worldCoord = new WorldCoord(TownsUniverse.getWorld(painting.getWorld().getName()), Coord.parseCoord(painting.getLocation()));
				} catch (NotRegisteredException e1) {
					TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_not_configured"));
					event.setCancelled(true);
					return;
				}

				//Get destroy permissions (updates if none exist)
				boolean bDestroy = TownsUniverse.getCachePermissions().getCachePermission(player, painting.getLocation(), TownsPermission.ActionType.DESTROY);

				PlayerCache cache = plugin.getCache(player);
				cache.updateCoord(worldCoord);
				TownBlockStatus status = cache.getStatus();
				if (status == TownBlockStatus.UNCLAIMED_ZONE && plugin.hasWildOverride(worldCoord.getWorld(), player, painting.getEntityId(), TownsPermission.ActionType.DESTROY)) {
					return;
				}
				if (! bDestroy) {
					event.setCancelled(true);
				}
				if (cache.hasBlockErrMsg()) {
					TownsMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
				}
			}
		}

		TownsMessaging.sendDebugMsg("onPaintingBreak took " + (System.currentTimeMillis() - start) + "ms (" + event.getCause().name() + ", " + event.isCancelled() + ")");
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void paintingPlace(PaintingPlaceEvent event) {
		if (event.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		long start = System.currentTimeMillis();

		Player player = event.getPlayer();
		Painting painting = event.getPainting();

		WorldCoord worldCoord;
		try {
			worldCoord = new WorldCoord(TownsUniverse.getWorld(painting.getWorld().getName()), Coord.parseCoord(painting.getLocation()));
		} catch (NotRegisteredException e1) {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_not_configured"));
			event.setCancelled(true);
			return;
		}

		//Get build permissions (updates if none exist)
		boolean bBuild = TownsUniverse.getCachePermissions().getCachePermission(player, painting.getLocation(), TownsPermission.ActionType.BUILD);

		PlayerCache cache = plugin.getCache(player);
		TownBlockStatus status = cache.getStatus();
		if (status == TownBlockStatus.UNCLAIMED_ZONE && plugin.hasWildOverride(worldCoord.getWorld(), player, painting.getEntityId(), TownsPermission.ActionType.BUILD)) {
			return;
		}
		if (! bBuild) {
			event.setCancelled(true);
		}
		if (cache.hasBlockErrMsg()) {
			TownsMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
		}

		TownsMessaging.sendDebugMsg("onPaintingBreak took " + (System.currentTimeMillis() - start) + "ms (" + event.getEventName() + ", " + event.isCancelled() + ")");
	}

	public boolean preventDamageCall(TownsWorld world, Entity a, Entity b, Player ap, Player bp) {
		// World using Towns
		if (! world.isUsingTowns()) {
			return false;
		}

		Coord coord = Coord.parseCoord(b);

		if (ap != null && bp != null) {
			if (world.isWarZone(coord)) {
				return false;
			}

			if (preventDamagePvP(world, ap, bp) || preventFriendlyFire(ap, bp)) {
				return true;
			}
		}

		try {
			// Check Town PvP status
			TownBlock townblock = world.getTownBlock(coord);
			if (! townblock.getTown().isPVP() && ! world.isForcePVP() && ! townblock.getPermissions().pvp) {
				if (bp != null && (ap != null || a instanceof Arrow)) {
					return true;
				}

				if (b instanceof Wolf) {
					Wolf wolf = (Wolf) b;
					if (wolf.isTamed() && ! wolf.getOwner().equals((AnimalTamer) a)) {
						return true;
					}
				}

				if (b instanceof Animals) {
					Resident resident = plugin.getTownsUniverse().getResident(ap.getName());
					if ((! resident.hasTown()) || (resident.hasTown() && (resident.getTown() != townblock.getTown()))) {
						return true;
					}
				}
			}
		} catch (NotRegisteredException e) {
		}

		if (plugin.getTownsUniverse().canAttackEnemy(ap.getName(), bp.getName())) {
			return false;
		}

		return false;
	}

	public boolean preventDamagePvP(TownsWorld world, Player a, Player b) {
		// Universe is only PvP
		if (world.isForcePVP() || world.isPVP()) {
			return false;
		}
		//plugin.sendDebugMsg("is not forcing pvp");
		// World PvP
		if (! world.isPVP()) {
			return true;
		}
		//plugin.sendDebugMsg("world is pvp");
		return false;
	}

	public boolean preventFriendlyFire(Player a, Player b) {
		TownsUniverse universe = plugin.getTownsUniverse();
		if (! TownsSettings.getFriendlyFire() && universe.isAlly(a.getName(), b.getName())) {
			try {
				TownsWorld world = TownsUniverse.getWorld(b.getWorld().getName());
				TownBlock townBlock = new WorldCoord(world, Coord.parseCoord(b)).getTownBlock();
				if (! townBlock.getType().equals(TownBlockType.ARENA)) {
					return true;
				}
			} catch (TownsException x) {
				//world or townblock failure
				return true;
			}
		}

		return false;
	}
}
