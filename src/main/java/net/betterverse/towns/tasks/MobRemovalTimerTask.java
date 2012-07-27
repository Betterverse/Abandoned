package net.betterverse.towns.tasks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.Coord;
import net.betterverse.towns.object.TownBlock;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;
import net.betterverse.towns.util.JavaUtil;

public class MobRemovalTimerTask extends TownsTimerTask {
	private Server server;
	@SuppressWarnings("rawtypes")
	public static List<Class> worldMobsToRemove = new ArrayList<Class>();
	@SuppressWarnings("rawtypes")
	public static List<Class> townMobsToRemove = new ArrayList<Class>();

	@SuppressWarnings("rawtypes")
	public MobRemovalTimerTask(TownsUniverse universe, Server server) {
		super(universe);
		this.server = server;

		worldMobsToRemove.clear();
		for (String mob : TownsSettings.getWorldMobRemovalEntities()) {
			try {
				Class c = Class.forName("org.bukkit.entity." + mob);
				if (JavaUtil.isSubInterface(LivingEntity.class, c)) {
					worldMobsToRemove.add(c);
				} else {
					throw new Exception();
				}
			} catch (ClassNotFoundException e) {
				TownsMessaging.sendErrorMsg("WorldMob: " + mob + " is not an acceptable class.");
			} catch (Exception e) {
				TownsMessaging.sendErrorMsg("WorldMob: " + mob + " is not an acceptable living entity.");
			}
		}

		townMobsToRemove.clear();
		for (String mob : TownsSettings.getTownMobRemovalEntities()) {
			try {
				Class c = Class.forName("org.bukkit.entity." + mob);
				if (JavaUtil.isSubInterface(LivingEntity.class, c)) {
					townMobsToRemove.add(c);
				} else {
					throw new Exception();
				}
			} catch (ClassNotFoundException e) {
				TownsMessaging.sendErrorMsg("TownMob: " + mob + " is not an acceptable class.");
			} catch (Exception e) {
				TownsMessaging.sendErrorMsg("TownMob: " + mob + " is not an acceptable living entity.");
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static boolean isRemovingWorldEntity(LivingEntity livingEntity) {
		for (Class c : worldMobsToRemove) {
			if (c.isInstance(livingEntity)) {
				return true;
			} else if (c.getName().contains(livingEntity.toString())) {
				System.out.print(livingEntity.toString());
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	public static boolean isRemovingTownEntity(LivingEntity livingEntity) {
		for (Class c : townMobsToRemove) {
			if (c.isInstance(livingEntity)) {
				return true;
			} else if (c.getName().contains(livingEntity.toString())) {
				System.out.print(livingEntity.toString());
			}
		}
		return false;
	}

	@Override
	public void run() {
		//int numRemoved = 0;
		//int livingEntities = 0;

		/* OLD METHOD
		for (World world : server.getWorlds()) {
			List<LivingEntity> worldLivingEntities = new ArrayList<LivingEntity>(world.getLivingEntities());
			livingEntities += worldLivingEntities.size();
			for (LivingEntity livingEntity : worldLivingEntities)
				if (isRemovingEntity(livingEntity)) {
					Location loc = livingEntity.getLocation();
					Coord coord = Coord.parseCoord(loc);
					try {
						TownsWorld townsWorld = universe.getWorld(world.getName());
						TownBlock townBlock = townsWorld.getTownBlock(coord);
						if (!townBlock.getTown().hasMobs()) {
							//universe.getPlugin().sendDebugMsg("MobRemoval Removed: " + livingEntity.toString());
							livingEntity.teleportTo(new Location(world, loc.getX(), -50, loc.getZ()));
							numRemoved++;
						}
					} catch (TownsException x) {
					}
				}
			//universe.getPlugin().sendDebugMsg(world.getName() + ": " + StringMgmt.join(worldLivingEntities));
		}
		//universe.getPlugin().sendDebugMsg("MobRemoval (Removed: "+numRemoved+") (Total Living: "+livingEntities+")");
		*/

		//System.out.println("[Towns] MobRemovalTimerTask - run()");

		//boolean isRemovingWorldMobs = TownsSettings.isRemovingWorldMobs();
		//boolean isRemovingTownMobs = TownsSettings.isRemovingTownMobs();

		// Build a list of mobs to be removed
		//if (isRemovingTownMobs || isRemovingWorldMobs)
		for (World world : server.getWorlds()) {
			List<LivingEntity> livingEntitiesToRemove = new ArrayList<LivingEntity>();

			for (LivingEntity livingEntity : world.getLivingEntities()) {
				Coord coord = Coord.parseCoord(livingEntity.getLocation());
				TownsWorld townsWorld = null;
				try {
					townsWorld = TownsUniverse.getWorld(world.getName());
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					TownBlock townBlock = townsWorld.getTownBlock(coord);
					if ((! townBlock.getTown().hasMobs() && ! townBlock.getPermissions().mobs && isRemovingTownEntity(livingEntity))) {
						//System.out.println("[Towns] Town MobRemovalTimerTask - added: " + livingEntity.toString());
						livingEntitiesToRemove.add(livingEntity);
					}
				} catch (TownsException x) {
					// it will fall through here if the mob has no townblock.
					if ((! townsWorld.hasWorldMobs() && isRemovingWorldEntity(livingEntity))) {
						//System.out.println("[Towns] World MobRemovalTimerTask - added: " + livingEntity.toString());
						livingEntitiesToRemove.add(livingEntity);
					}
				}
			}

			for (LivingEntity livingEntity : livingEntitiesToRemove) {
				TownsMessaging.sendDebugMsg("MobRemoval Removed: " + livingEntity.toString());
				//livingEntity.teleportTo(new Location(world, livingEntity.getLocation().getX(), -50, livingEntity.getLocation().getZ()));
				livingEntity.remove();
				//numRemoved++;
			}

			//universe.getPlugin().sendDebugMsg(world.getName() + ": " + StringMgmt.join(worldLivingEntities));

		}
	}
}
