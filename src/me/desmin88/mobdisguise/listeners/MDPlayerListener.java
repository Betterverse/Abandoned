package me.desmin88.mobdisguise.listeners;

import me.desmin88.mobdisguise.MobDisguise;
import me.desmin88.mobdisguise.utils.DisguiseTask;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class MDPlayerListener extends PlayerListener {
    @SuppressWarnings("unused")
    private final MobDisguise plugin;

    public MDPlayerListener(MobDisguise instance) {
        this.plugin = instance;
    }

    public void onPlayerQuit(final PlayerQuitEvent event) {
        System.out.println("quits");
        if(MobDisguise.disList.contains(event.getPlayer())) {
            //Should fix the "carcass" mob when disguised
           Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
               public void run() {
                   System.out.println("killcarcass");
                   MobDisguise.pu.killCarcass(event.getPlayer());
                }
            }, 5);
            
        }
    }
    
    //Waiting for my stinking pull.
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (MobDisguise.disList.contains(event.getPlayer())) {
            // event.setCancelled(true);
            return;
        }
    }

    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!MobDisguise.disList.contains(event.getPlayer())) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DisguiseTask(plugin), 8);
        }
        if (MobDisguise.disList.contains(event.getPlayer())) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DisguiseTask(plugin), 8);
            if(!MobDisguise.apiList.contains(event.getPlayer())) {
                event.getPlayer().sendMessage(MobDisguise.pref + "You have been disguised because you teleported");
            }
        }
    }
    
    
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!MobDisguise.disList.contains(event.getPlayer())) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DisguiseTask(plugin), 8 );
        }
        if (MobDisguise.disList.contains(event.getPlayer())) {
            if(!MobDisguise.apiList.contains(event.getPlayer())) {
                event.getPlayer().sendMessage(MobDisguise.pref + "You have been disguised because you died");
            }
            MobDisguise.pu.disguiseToAll(event.getPlayer());
        }
    }

    public void onPlayerJoin(PlayerJoinEvent event) {
        if(!MobDisguise.disList.contains(event.getPlayer())) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DisguiseTask(plugin), 8 );
        }
        if (MobDisguise.disList.contains(event.getPlayer())) {
            event.getPlayer().sendMessage(MobDisguise.pref + "You have been disguised because you relogged");
            if(!MobDisguise.apiList.contains(event.getPlayer())) {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DisguiseTask(plugin), 8 );
            }
        }
    }
}
