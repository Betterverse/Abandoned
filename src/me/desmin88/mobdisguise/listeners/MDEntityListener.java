package me.desmin88.mobdisguise.listeners;

import me.desmin88.mobdisguise.MobDisguise;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

public class MDEntityListener extends EntityListener{
    private final MobDisguise plugin;

    public MDEntityListener(MobDisguise instance) {
        this.plugin = instance;
    }
    
    public void onEntityDeath(EntityDeathEvent event) {
        if(event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            if(plugin.disList.contains(p) && !plugin.apiList.contains(p)) {
                p.sendMessage("[MobDisguise] You have been disguised");
                plugin.pu.disguiseToAll(p);
            }
        }
    }
}
