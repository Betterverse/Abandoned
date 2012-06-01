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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().sendMessage("Debug!");
		//Checking if a hashmap object is null? Wouldn't a hashmap always return null unless you
		//specify a member of said hashmap?
		if (this.plugin.aliases.get(event.getPlayer().getName) == null) {
			return;
		}
		event.getPlayer().sendMessage("Debug 2!");

		Player player = event.getPlayer();
		String alias = this.plugin.aliases.get(player.getName());
		event.getPlayer().sendMessage("Debug! "+alias);

		if (alias != null) {
		event.getPlayer().sendMessage("Debug! 3");
			if ((this.plugin.isPlayerName(alias)) || (this.plugin.isBanned(alias))) {
				this.plugin.aliases.remove(player.getName());
				this.plugin.saveAliases();
				player.sendMessage("You are no longer allowed allowed to use " + alias + " as an alias.");
				Alias.print(player.getName() + " has had their alias " + ChatColor.DARK_RED + alias + ChatColor.WHITE
								+ " cleared.");
				player.setDisplayName(player.getName());
			}

			if (player.hasPermission("alias.use")) {
				player.setDisplayName(alias);
				player.sendMessage("Setting your alias to " + alias);
			}
		}
	}
}