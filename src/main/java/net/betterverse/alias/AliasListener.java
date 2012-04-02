package net.betterverse.alias;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AliasListener implements Listener {
    private final Alias plugin;

    public AliasListener(Alias instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.plugin.aliases == null)
            return;
        
        Player player = event.getPlayer();
        String alias = this.plugin.aliases.get(player.getName());

        if (alias != null) {
            if ((this.plugin.isPlayerName(alias)) || (this.plugin.isBanned(alias))) {
                this.plugin.aliases.remove(player.getName());
                this.plugin.saveAliases();
                player.sendMessage("You are no longer allowed allowed to use " + alias + " as an alias.");
                Alias.print(player.getName() + " has had their alias " + ChatColor.DARK_RED + alias + ChatColor.WHITE +
                            " cleared.");
                player.setDisplayName(player.getName());
            }
            
            if (player.hasPermission("alias.use")) 
                player.setDisplayName(alias);
        }
    }
}