package net.betterverse.mobdisguise.listeners;

import net.betterverse.mobdisguise.MobDisguise;
import net.betterverse.mobdisguise.utils.RealDropsUtils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class MDEntityListener implements Listener {
    @SuppressWarnings("unused")
    private final MobDisguise plugin;

    public MDEntityListener(MobDisguise instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player p = (Player) event.getTarget();
            if (MobDisguise.disList.contains(p.getName()) && MobDisguise.cfg.getBoolean("MobTarget.enabled", true) && !MobDisguise.playerdislist.contains(p.getName())) {
                event.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player p = (Player) event.getEntity();
            if (MobDisguise.cfg.getBoolean("RealDrops.enabled", false) && MobDisguise.disList.contains(p.getName()) && !MobDisguise.playerdislist.contains(p.getName())) {
                event.getDrops().clear();
                if (RealDropsUtils.getDrop(p) != null)
                    p.getWorld().dropItemNaturally(p.getLocation(), RealDropsUtils.getDrop(p));

            }
        }
    }
}