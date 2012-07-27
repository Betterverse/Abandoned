package net.betterverse.towns.war.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.command.TownCommand;
import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownBlock;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;
import net.betterverse.towns.object.WorldCoord;
import net.betterverse.towns.tasks.TownClaim;
import net.betterverse.towns.war.CellUnderAttack;
import net.betterverse.towns.war.event.CellAttackCancelledEvent;
import net.betterverse.towns.war.event.CellAttackEvent;
import net.betterverse.towns.war.event.CellDefendedEvent;
import net.betterverse.towns.war.event.CellWonEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TownsWarCustomListener implements Listener {
	
    private final Towns plugin;

	public TownsWarCustomListener(Towns plugin) {
		this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
	}
    
    @EventHandler
    public void cellDefended(CellDefendedEvent event) {
        CellDefendedEvent cellDefendedEvent = (CellDefendedEvent) event;
		Player player = cellDefendedEvent.getPlayer();
		CellUnderAttack cell = cellDefendedEvent.getCell().getAttackData();
        
		TownsUniverse universe = plugin.getTownsUniverse();
		try {
            TownsWorld world = TownsUniverse.getWorld(cell.getWorldName());
			WorldCoord worldCoord = new WorldCoord(world, cell.getX(), cell.getZ());
			universe.removeWarZone(worldCoord);

			plugin.updateCache(worldCoord);
        } catch (NotRegisteredException e) {
			e.printStackTrace();
		}

		String playerName;
		if (player == null) {
			playerName = "Greater Forces";
		} else {
			playerName = player.getName();
			try {
				playerName = plugin.getTownsUniverse().getResident(player.getName()).getFormattedName();
			} catch (TownsException e) {
			}
		}
        plugin.getServer().broadcastMessage(String.format(TownsSettings.getLangString("msg_enemy_war_area_defended"), playerName, cell.getCellString()));
    }
    
    @EventHandler
    public void cellWon(CellWonEvent event) {
        CellWonEvent cellWonEvent = (CellWonEvent) event;
		CellUnderAttack cell = cellWonEvent.getCellAttackData();

		TownsUniverse universe = plugin.getTownsUniverse();
		try {
			Resident resident = universe.getResident(cell.getNameOfFlagOwner());
			Town town = resident.getTown();
			Nation nation = town.getNation();

			TownsWorld world = TownsUniverse.getWorld(cell.getWorldName());
			WorldCoord worldCoord = new WorldCoord(world, cell.getX(), cell.getZ());
			universe.removeWarZone(worldCoord);

			TownBlock townBlock = worldCoord.getTownBlock();
			universe.removeTownBlock(townBlock);

			try {
				List<WorldCoord> selection = new ArrayList<WorldCoord>();
				selection.add(worldCoord);
				TownCommand.checkIfSelectionIsValid(town, selection, false, 0, false);
				new TownClaim(plugin, null, town, selection, true, false).start();
                
			} catch (TownsException te) {
				// Couldn't claim it.
			}

			plugin.updateCache(worldCoord);

			TownsMessaging.sendGlobalMessage(String.format(TownsSettings.getLangString("msg_enemy_war_area_won"), resident.getFormattedName(), (nation.hasTag() ? nation.getTag() : nation.getFormattedName()), cell.getCellString()));
		} catch (NotRegisteredException e) {
			e.printStackTrace();
		}
    }
    
    @EventHandler
    public void cellAttackCancelled(CellAttackCancelledEvent event) {
        System.out.println("CellAttackCanceled");
		CellAttackCancelledEvent cancelCellAttackerEvent = (CellAttackCancelledEvent) event;
		CellUnderAttack cell = cancelCellAttackerEvent.getCell();

		TownsUniverse universe = plugin.getTownsUniverse();
		try {
			TownsWorld world = TownsUniverse.getWorld(cell.getWorldName());
			WorldCoord worldCoord = new WorldCoord(world, cell.getX(), cell.getZ());
			universe.removeWarZone(worldCoord);
			plugin.updateCache(worldCoord);
		} catch (NotRegisteredException e) {
			e.printStackTrace();
		}
		System.out.println(cell.getCellString());
    }
        

	@SuppressWarnings("unused")
	@EventHandler
	public void cellAttack(CellAttackEvent event) {
        try {
            CellUnderAttack cell = event.getData();
        } catch (Exception e) {
            event.setCancelled(true);
            event.setReason(e.getMessage());
        }
    }
}