package net.betterverse.kiwiadmin;

import java.util.Date;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class KiwiAdminPlayerListener implements Listener {

	private final KiwiAdmin plugin;

	public KiwiAdminPlayerListener(KiwiAdmin instance) {
		this.plugin = instance;
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		if (KiwiAdmin.bannedPlayers.contains(player.getName().toLowerCase())) {
			if (KiwiAdmin.tempBans.get(player.getName().toLowerCase()) != null) {
				long tempTime = ((Long) KiwiAdmin.tempBans.get(player.getName().toLowerCase())).longValue();
				long now = System.currentTimeMillis();
				long diff = tempTime - now;
				if (diff <= 0L) {
					if (Database.removeFromBanlist(player.getName().toLowerCase())) {
						KiwiAdmin.bannedPlayers.remove(player.getName().toLowerCase());
						if (KiwiAdmin.tempBans.containsKey(player.getName().toLowerCase())) {
							KiwiAdmin.tempBans.remove(player.getName().toLowerCase());
						}
					}
					return;
				}
				Date date = new Date();
				date.setTime(tempTime);
				String kickerMsg = this.plugin.formatMessage(this.plugin.getConfig().getString("messages.LoginTempban"));
				kickerMsg = kickerMsg.replaceAll("%time%", date.toString());
				kickerMsg = kickerMsg.replaceAll("%reason%", Database.getReason(player.getName().toLowerCase()));
				event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickerMsg);
				return;
			}
			String kickerMsg = this.plugin.formatMessage(this.plugin.getConfig().getString("messages.LoginBan"));
			kickerMsg = kickerMsg.replaceAll("%reason%", Database.getReason(player.getName().toLowerCase()));
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickerMsg);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		System.out.println("connect from ip " + player.getAddress().getAddress().getHostAddress());
		if (KiwiAdmin.bannedIPs.contains(player.getAddress().getAddress().getHostAddress())) {
			System.out.println("ip is banned");
			event.setJoinMessage(null);
			String kickerMsg = this.plugin.formatMessage(this.plugin.getConfig().getString("messages.LoginIPBan"));
			player.kickPlayer(kickerMsg);
		}
	}
}