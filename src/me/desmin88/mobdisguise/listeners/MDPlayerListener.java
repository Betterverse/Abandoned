package me.desmin88.mobdisguise.listeners;

import me.desmin88.mobdisguise.MobDisguise;
import me.desmin88.mobdisguise.utils.DisguiseTask;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class MDPlayerListener implements Listener {

    private final MobDisguise plugin;

    public MDPlayerListener(MobDisguise instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (MobDisguise.disList.contains(event.getPlayer().getName()) && MobDisguise.cfg.getBoolean("DisableItemPickup.enabled", true) && !MobDisguise.playerdislist.contains(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (MobDisguise.disList.contains(event.getPlayer().getName()) && event.getAction() == Action.RIGHT_CLICK_AIR && event.getPlayer().getItemInHand().getType() == Material.FISHING_ROD) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (MobDisguise.disList.contains(event.getPlayer().getName())) {
            MobDisguise.playerEntIds.remove(event.getPlayer().getEntityId());
            // Should fix the "carcass" mob when disguised
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    MobDisguise.pu.killCarcass(event.getPlayer());
                }
            }, 5);

        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (MobDisguise.disList.contains(event.getPlayer().getName())) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (MobDisguise.disList.contains(event.getPlayer().getName())) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (MobDisguise.telelist.contains(event.getPlayer().getName())) {
            MobDisguise.telelist.remove(event.getPlayer().getName());
            return;
        }
        if (!MobDisguise.disList.contains(event.getPlayer().getName())) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DisguiseTask(plugin), 8);
        }
        if (MobDisguise.disList.contains(event.getPlayer().getName())) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DisguiseTask(plugin), 8);
        }
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {

        if (!MobDisguise.disList.contains(event.getPlayer().getName())) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DisguiseTask(plugin), 8);
        }
        if (MobDisguise.disList.contains(event.getPlayer().getName())) {
            if (!MobDisguise.apiList.contains(event.getPlayer().getName())) {
                event.getPlayer().sendMessage(MobDisguise.pref + "You have been disguised because you died");
            }
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DisguiseTask(plugin), 8);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (MobDisguise.disList.contains(event.getPlayer().getName())) {
            MobDisguise.telelist.add(event.getPlayer().getName());
            MobDisguise.playerEntIds.add(Integer.valueOf(event.getPlayer().getEntityId()));
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DisguiseTask(plugin), 20);
            if (!MobDisguise.apiList.contains(event.getPlayer().getName())) {
                event.getPlayer().sendMessage(MobDisguise.pref + "You have been disguised because you relogged");
            }
        }
        if (!MobDisguise.disList.contains(event.getPlayer().getName())) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DisguiseTask(plugin), 20);
        }

    }
}
