package me.desmin88.mobdisguise.listeners;

import me.desmin88.mobdisguise.MobDisguise;

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

    public void onPlayerQuit(PlayerQuitEvent event) {
        if(MobDisguise.disList.contains(event.getPlayer()) && !MobDisguise.apiList.contains(event.getPlayer())) {
            //Should fix the "carcass" mob when disguised
            MobDisguise.pu.undisguiseToAll(event.getPlayer());
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
        if (MobDisguise.disList.contains(event.getPlayer()) && !MobDisguise.apiList.contains(event.getPlayer())) {
            event.getPlayer().sendMessage(MobDisguise.pref + "You have been disguised because you teleported");
            MobDisguise.pu.disguiseToAll(event.getPlayer());
        }
        if (!MobDisguise.disList.contains(event.getPlayer())) {
            for (Player p : MobDisguise.disList) {
                MobDisguise.pu.disguiseToAll(p);
            }
        }
    
    }
    
    
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (MobDisguise.disList.contains(event.getPlayer()) && !MobDisguise.apiList.contains(event.getPlayer())) {
            event.getPlayer().sendMessage(MobDisguise.pref + "You have been disguised because you died");
            MobDisguise.pu.disguiseToAll(event.getPlayer());
        }
    }

    public void onPlayerJoin(PlayerJoinEvent event) {
        if (MobDisguise.disList.contains(event.getPlayer()) && !MobDisguise.apiList.contains(event.getPlayer())) {
            event.getPlayer().sendMessage(MobDisguise.pref + "You have been disguised because you relogged");
            MobDisguise.pu.disguiseToAll(event.getPlayer());
        }
    }
}
