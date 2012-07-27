package me.kalmanolah.os;

import java.util.Calendar;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class OKPlayerListener implements Listener {

	private static OKmain plugin;

	public OKPlayerListener(OKmain instance) {
		plugin = instance;
	}

	@EventHandler
	public void onPlayerChat(PlayerChatEvent event) {
		final Player plr = event.getPlayer();
		if (!OKmain.CheckPermission(plr, "okspamsecurity.ignore")) {
			Calendar cal;
			long time;
			if (!OKmain.manuallypunishedplayers.contains(plr)) {
				final String name = plr.getName();
				cal = Calendar.getInstance();
				time = cal.getTimeInMillis();
				int taskid;
				if (!OKmain.punishedplayers.contains(plr)) {
					if (!OKmain.players.containsKey(plr)) {
						OKmain.players.put(plr, Integer.valueOf(1));
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

							public void run() {
								if (OKmain.players.containsKey(plr)) {
									OKmain.players.remove(plr);
								}
							}
						}, 1200L);
					} else {
						Integer amount = (Integer) OKmain.players.get(plr);
						OKmain.players.put(plr, Integer.valueOf(amount.intValue() + 1));
						if (amount == OKmain.maxmsgs) {
							OKmain.punishedplayers.add(plr);
							OKmain.times.put(plr, Long.valueOf(time));
							taskid = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

								public void run() {
									Calendar cal = Calendar.getInstance();
									long currenttime = cal.getTimeInMillis();
									if (currenttime - ((Long) OKmain.times.get(plr)).longValue() > OKmain.duration.intValue() * 1000) {
										OKmain.punishedplayers.remove(plr);
										if (OKmain.players.containsKey(plr)) {
											OKmain.players.remove(plr);
										}
										OKmain.finishedtasks.add(plr);
										if (OKmain.mode.intValue() == 1) {
											OKLogger.info("Player " + name + " was unmuted.");
											if ((plr.isOnline()) && (OKmain.enablemessages.booleanValue())) {
												plr.sendMessage(ChatColor.GOLD + "Notice: " + ChatColor.GRAY + "You have been unmuted and can chat again.");
											}
										} else if (OKmain.mode.intValue() == 2) {
											OKLogger.info("Player " + name + " left mirror mode.");
										} else if (OKmain.mode.intValue() == 3) {
											OKLogger.info("Player " + name + " left damage mode.");
											if ((plr.isOnline()) && (OKmain.enablemessages.booleanValue())) {
												plr.sendMessage(ChatColor.GOLD + "Notice: " + ChatColor.GRAY + "You can now chat freely once again.");
											}
										} else if (OKmain.mode.intValue() == 4) {
											OKLogger.info("Player " + name + " left command mode.");
										}
									}
								}
							}, 100L, 200L);
							OKmain.tasks.put(plr, Integer.valueOf(taskid));
							if (OKmain.mode.intValue() == 1) {
								OKLogger.info("Player " + name + " was muted.");
								if (OKmain.enablemessages.booleanValue()) {
									plr.sendMessage(ChatColor.RED + "Notice: " + ChatColor.GRAY + "You have been muted.");
								}
							} else if (OKmain.mode.intValue() == 2) {
								OKLogger.info("Player " + name + " entered mirror mode.");
							} else if (OKmain.mode.intValue() == 3) {
								OKLogger.info("Player " + name + " entered damage mode.");
								if (OKmain.enablemessages.booleanValue()) {
									plr.sendMessage(ChatColor.RED + "Notice: " + ChatColor.GRAY + "You will lose " + ChatColor.WHITE + OKmain.damagepermessage.intValue() / 2 + ChatColor.GRAY + " heart per chat message.");
								}
							} else if (OKmain.mode.intValue() == 4) {
								OKLogger.info("Player " + name + " will be punished with custom commands.");
							}
						}
					}
				} else {
					OKmain.times.put(plr, Long.valueOf(time));
					if (OKmain.players.containsKey(plr)) {
						OKmain.players.remove(plr);
					}
					if (OKmain.mode.intValue() == 1) {
						event.setCancelled(true);
					} else if (OKmain.mode.intValue() == 2) {
						String fakemsg = event.getFormat();
						fakemsg = fakemsg.replace("%2$s", event.getMessage());
						fakemsg = fakemsg.replace("%1$s", plr.getDisplayName());
						plr.sendMessage(fakemsg);
						event.setCancelled(true);
					} else if (OKmain.mode.intValue() == 3) {
						plr.damage(OKmain.damagepermessage.intValue());
					} else if (OKmain.mode.intValue() == 4) {
						for (String cmd : OKmain.commands) {
							plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd.replace("%name%", plr.getName()).replace("%world%", plr.getWorld().getName()));
						}
					}
				}
			} else if (OKmain.mode.intValue() == 1) {
				event.setCancelled(true);
			} else if (OKmain.mode.intValue() == 2) {
				String fakemsg = event.getFormat();
				fakemsg = fakemsg.replace("%2$s", event.getMessage());
				fakemsg = fakemsg.replace("%1$s", plr.getDisplayName());
				plr.sendMessage(fakemsg);
				event.setCancelled(true);
			} else if (OKmain.mode.intValue() == 3) {
				plr.damage(OKmain.damagepermessage.intValue());
			} else if (OKmain.mode.intValue() == 4) {
				String[] arrayOfString1;
				time = (arrayOfString1 = OKmain.commands).length;
				for (int ccal = 0; ccal < time; ccal++) {
					String cmd = arrayOfString1[ccal];
					plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd.replace("%name%", plr.getName()).replace("%world%", plr.getWorld().getName()));
				}
			}
		}
	}
}