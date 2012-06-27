package net.betterverse.nameeffects.listeners;

import net.betterverse.nameeffects.NameEffects;
import net.betterverse.nameeffects.util.PersistUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    NameEffects plugin;

    public PlayerListener(NameEffects instance) {
        plugin = instance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        PersistUtil.addPlayerToAliases(plugin, event.getPlayer());
    }
}
