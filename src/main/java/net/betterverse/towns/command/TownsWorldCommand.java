package net.betterverse.towns.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;
import net.betterverse.towns.util.ChatTools;
import net.betterverse.towns.util.Colors;
import net.betterverse.towns.util.MinecraftTools;
import net.betterverse.towns.util.StringMgmt;

/**
 * Send a list of all general townsworld help commands to player
 * Command: /townsworld
 */
public class TownsWorldCommand implements CommandExecutor {
	private static Towns plugin;
	private static final List<String> townsworld_help = new ArrayList<String>();
	private static final List<String> townsworld_set = new ArrayList<String>();
	private static TownsWorld Globalworld;

	public TownsWorldCommand(Towns instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		townsworld_help.add(ChatTools.formatTitle("/townsworld"));
		townsworld_help.add(ChatTools.formatCommand("", "/townsworld", "", TownsSettings.getLangString("world_help_1")));
		townsworld_help.add(ChatTools.formatCommand("", "/townsworld", TownsSettings.getLangString("world_help_2"), TownsSettings.getLangString("world_help_3")));
		townsworld_help.add(ChatTools.formatCommand("", "/townsworld", "list", TownsSettings.getLangString("world_help_4")));
		townsworld_help.add(ChatTools.formatCommand("", "/townsworld", "toggle", ""));
		townsworld_help.add(ChatTools.formatCommand(TownsSettings.getLangString("admin_sing"), "/townsworld", "set [] .. []", ""));

		townsworld_set.add(ChatTools.formatTitle("/townsworld set"));
		townsworld_set.add(ChatTools.formatCommand("", "/townsworld set", "wildname [name]", ""));
		//townsworld_set.add(ChatTools.formatCommand("", "/townsworld set", "usingtowns [on/off]", ""));

		// if using permissions and it's active disable this command
		if (! plugin.isPermissions()) {
			townsworld_set.add(ChatTools.formatCommand("", "/townsworld set", "usedefault", ""));
			townsworld_set.add(ChatTools.formatCommand("", "/townsworld set", "wildperm [perm] .. [perm]", "build,destroy,switch,useitem"));
			townsworld_set.add(ChatTools.formatCommand("", "/townsworld set", "wildignore [id] [id] [id]", ""));
		}

		if (sender instanceof Player) {
			Player player = (Player) sender;
			System.out.println("[PLAYER_COMMAND] " + player.getName() + ": /" + commandLabel + " " + StringMgmt.join(args));
		}
		parseWorldCommand(sender, args);
		/*
		} else {
			// Console
			for (String line : townsworld_help)
				sender.sendMessage(Colors.strip(line));
		}
		*/

		townsworld_set.clear();
		townsworld_help.clear();
		Globalworld = null;
		return true;
	}

	public void parseWorldCommand(CommandSender sender, String[] split) {
		Player player = null;

		if (sender instanceof Player) {
			player = (Player) sender;
			try {
				Globalworld = TownsUniverse.getWorld(player.getWorld().getName());
			} catch (NotRegisteredException e) {
				TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_area_not_recog"));
				return;
			}
		} else {
			if (split.length == 0) {
				sender.sendMessage(String.format(TownsSettings.getLangString("msg_err_invalid_property"), "world"));
				return;
			}
			if ((! split[0].equalsIgnoreCase("?")) && (! split[0].equalsIgnoreCase("list"))) {
				try {
					if ((split.length >= 1)) {
						Globalworld = TownsUniverse.getWorld(split[split.length - 1].toLowerCase());
						split = StringMgmt.remLastArg(split);
					} else {
						sender.sendMessage(TownsSettings.getLangString("msg_area_not_recog"));
						return;
					}
				} catch (NotRegisteredException e) {
					sender.sendMessage(String.format(TownsSettings.getLangString("msg_err_invalid_property"), "world"));
					return;
				}
			}
		}

		if (split.length == 0) {
			if (player == null) {
				for (String line : plugin.getTownsUniverse().getStatus(Globalworld)) {
					sender.sendMessage(Colors.strip(line));
				}
			} else {
				TownsMessaging.sendMessage(player, plugin.getTownsUniverse().getStatus(Globalworld));
			}
		} else if (split[0].equalsIgnoreCase("?")) {
			if (player == null) {
				for (String line : townsworld_help) {
					sender.sendMessage(line);
				}
			} else {
				for (String line : townsworld_help) {
					player.sendMessage(line);
				}
			}
		} else if (split[0].equalsIgnoreCase("list")) {
			listWorlds(player, sender);
		} else if (split[0].equalsIgnoreCase("set")) {
			worldSet(player, sender, StringMgmt.remFirstArg(split));
		} else if (split[0].equalsIgnoreCase("toggle")) {
			worldToggle(player, sender, StringMgmt.remFirstArg(split));
		} else {
			/*
			try {
				TownsWorld world = plugin.getTownsUniverse().getWorld(split[0]);
				TownsMessaging.sendMessage(player, plugin.getTownsUniverse().getStatus(world));
			} catch (NotRegisteredException x) {
				plugin.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_not_registered_1"), split[0]));
			}
			*/
		}
	}

	public void listWorlds(Player player, CommandSender sender) {
		if (player == null) {
			sender.sendMessage(ChatTools.formatTitle(TownsSettings.getLangString("world_plu")));
		} else {
			player.sendMessage(ChatTools.formatTitle(TownsSettings.getLangString("world_plu")));
		}

		ArrayList<String> formatedList = new ArrayList<String>();
		HashMap<String, Integer> playersPerWorld = MinecraftTools.getPlayersPerWorld(plugin.getServer());
		for (TownsWorld world : plugin.getTownsUniverse().getWorlds()) {
			int numPlayers = playersPerWorld.containsKey(world.getName()) ? playersPerWorld.get(world.getName()) : 0;
			formatedList.add(Colors.LightBlue + world.getName() + Colors.Blue + " [" + numPlayers + "]" + Colors.White);
		}

		if (player == null) {
			for (String line : ChatTools.list(formatedList)) {
				sender.sendMessage(line);
			}
		} else {
			for (String line : ChatTools.list(formatedList)) {
				player.sendMessage(line);
			}
		}
	}

	public void worldToggle(Player player, CommandSender sender, String[] split) {
		if (split.length == 0) {
			player.sendMessage(ChatTools.formatTitle("/TownsWorld toggle"));
			player.sendMessage(ChatTools.formatCommand("", "/TownsWorld toggle", "claimable", ""));
			player.sendMessage(ChatTools.formatCommand("", "/TownsWorld toggle", "usingtowns", ""));
			player.sendMessage(ChatTools.formatCommand("", "/TownsWorld toggle", "pvp", ""));
			player.sendMessage(ChatTools.formatCommand("", "/TownsWorld toggle", "forcepvp", ""));
			player.sendMessage(ChatTools.formatCommand("", "/TownsWorld toggle", "explosion", ""));
			player.sendMessage(ChatTools.formatCommand("", "/TownsWorld toggle", "fire", ""));
			player.sendMessage(ChatTools.formatCommand("", "/TownsWorld toggle", "townmobs/worldmobs", ""));
		} else {
			if ((sender == null) && ! plugin.isTownsAdmin(player)) {
				TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_admin_only"));
				return;
			}

			String msg;

			if (split[0].equalsIgnoreCase("claimable")) {
				Globalworld.setClaimable(! Globalworld.isClaimable());
				msg = String.format(TownsSettings.getLangString("msg_set_claim"), Globalworld.getName(), Globalworld.isClaimable() ? "Enabled" : "Disabled");
				if (player != null) {
					TownsMessaging.sendMsg(player, msg);
				} else {
					TownsMessaging.sendMsg(msg);
				}
			} else if (split[0].equalsIgnoreCase("usingtowns")) {
				Globalworld.setUsingTowns(! Globalworld.isUsingTowns());
				plugin.updateCache();
				msg = String.format(Globalworld.isUsingTowns() ? TownsSettings.getLangString("msg_set_use_towns_on") : TownsSettings.getLangString("msg_set_use_towns_off"));
				if (player != null) {
					TownsMessaging.sendMsg(player, msg);
				} else {
					TownsMessaging.sendMsg(msg);
				}
			} else if (split[0].equalsIgnoreCase("pvp")) {
				Globalworld.setPVP(! Globalworld.isPVP());
				msg = String.format(TownsSettings.getLangString("msg_changed_world_setting"), "Global PVP", Globalworld.getName(), Globalworld.isPVP() ? "Enabled" : "Disabled");
				if (player != null) {
					TownsMessaging.sendMsg(player, msg);
				} else {
					TownsMessaging.sendMsg(msg);
				}
			} else if (split[0].equalsIgnoreCase("forcepvp")) {
				Globalworld.setForcePVP(! Globalworld.isForcePVP());
				msg = String.format(TownsSettings.getLangString("msg_changed_world_setting"), "PVP", Globalworld.getName(), Globalworld.isForcePVP() ? "Forced" : "Adjustable");
				if (player != null) {
					TownsMessaging.sendMsg(player, msg);
				} else {
					TownsMessaging.sendMsg(msg);
				}
			} else if (split[0].equalsIgnoreCase("explosion")) {
				Globalworld.setForceExpl(! Globalworld.isForceExpl());
				msg = String.format(TownsSettings.getLangString("msg_changed_world_setting"), "Explosions", Globalworld.getName(), Globalworld.isForceExpl() ? "Enabled" : "Disabled");
				if (player != null) {
					TownsMessaging.sendMsg(player, msg);
				} else {
					TownsMessaging.sendMsg(msg);
				}
			} else if (split[0].equalsIgnoreCase("fire")) {
				Globalworld.setForceFire(! Globalworld.isForceFire());
				msg = String.format(TownsSettings.getLangString("msg_changed_world_setting"), "Fire Spread", Globalworld.getName(), Globalworld.isForceFire() ? "Forced" : "Adjustable");
				if (player != null) {
					TownsMessaging.sendMsg(player, msg);
				} else {
					TownsMessaging.sendMsg(msg);
				}
			} else if (split[0].equalsIgnoreCase("townmobs")) {
				Globalworld.setForceTownMobs(! Globalworld.isForceTownMobs());
				msg = String.format(TownsSettings.getLangString("msg_changed_world_setting"), "Town Mob spawns", Globalworld.getName(), Globalworld.isForceTownMobs() ? "Forced" : "Adjustable");
				if (player != null) {
					TownsMessaging.sendMsg(player, msg);
				} else {
					TownsMessaging.sendMsg(msg);
				}
			} else if (split[0].equalsIgnoreCase("worldmobs")) {
				Globalworld.setWorldMobs(! Globalworld.hasWorldMobs());
				msg = String.format(TownsSettings.getLangString("msg_changed_world_setting"), "World Mob spawns", Globalworld.getName(), Globalworld.hasWorldMobs() ? "Enabled" : "Disabled");
				if (player != null) {
					TownsMessaging.sendMsg(player, msg);
				} else {
					TownsMessaging.sendMsg(msg);
				}
			} else {
				msg = String.format(TownsSettings.getLangString("msg_err_invalid_property"), "'" + split[0] + "'");
				if (player != null) {
					TownsMessaging.sendErrorMsg(player, msg);
				} else {
					TownsMessaging.sendErrorMsg(msg);
				}
				return;
			}

			TownsUniverse.getDataSource().saveWorld(Globalworld);
		}
	}

	public void worldSet(Player player, CommandSender sender, String[] split) {
		if (split.length == 0) {
			if (player == null) {
				for (String line : townsworld_set) {
					sender.sendMessage(line);
				}
			} else {
				for (String line : townsworld_set) {
					player.sendMessage(line);
				}
			}
		} else {
			try {
				if (! plugin.isTownsAdmin(player)) {
					throw new TownsException(TownsSettings.getLangString("msg_err_admin_only"));
				}
				//Globalworld = plugin.getTownsUniverse().getWorld(player.getWorld().getName());
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}

			if (split[0].equalsIgnoreCase("usedefault")) {
				Globalworld.setUsingDefault();
				plugin.updateCache();
				if (player != null) {
					TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_usedefault"), Globalworld.getName()));
				} else {
					sender.sendMessage(String.format(TownsSettings.getLangString("msg_usedefault"), Globalworld.getName()));
				}
			} else if (split[0].equalsIgnoreCase("wildperm")) {
				if (split.length < 2) {
					// set default wildperm settings (/tw set wildperm)
					Globalworld.setUsingDefault();
					if (player != null) {
						TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_usedefault"), Globalworld.getName()));
					} else {
						sender.sendMessage(String.format(TownsSettings.getLangString("msg_usedefault"), Globalworld.getName()));
					}
				} else {
					try {
						List<String> perms = Arrays.asList(StringMgmt.remFirstArg(split));
						Globalworld.setUnclaimedZoneBuild(perms.contains("build"));
						Globalworld.setUnclaimedZoneDestroy(perms.contains("destroy"));
						Globalworld.setUnclaimedZoneSwitch(perms.contains("switch"));
						Globalworld.setUnclaimedZoneItemUse(perms.contains("itemuse"));

						plugin.updateCache();
						if (player != null) {
							TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_set_wild_perms"), Globalworld.getName(), perms.toString()));
						} else {
							sender.sendMessage(String.format(TownsSettings.getLangString("msg_set_wild_perms"), Globalworld.getName(), perms.toString()));
						}
					} catch (Exception e) {
						if (player != null) {
							TownsMessaging.sendErrorMsg(player, "Eg: /townsworld set wildperm build destroy");
						} else {
							sender.sendMessage("Eg: /townsworld set wildperm build destroy <world>");
						}
					}
				}
			} else if (split[0].equalsIgnoreCase("wildignore")) {
				if (split.length < 2) {
					if (player != null) {
						TownsMessaging.sendErrorMsg(player, "Eg: /townsworld set wildignore 11,25,45,67");
					} else {
						sender.sendMessage("Eg: /townsworld set wildignore 11,25,45,67 <world>");
					}
				} else {
					try {
						List<Integer> nums = new ArrayList<Integer>();
						for (String s : StringMgmt.remFirstArg(split)) {
							try {
								nums.add(Integer.parseInt(s.trim()));
							} catch (NumberFormatException e) {
							}
						}
						Globalworld.setUnclaimedZoneIgnore(nums);

						plugin.updateCache();
						if (player != null) {
							TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_set_wild_ignore"), Globalworld.getName(), Arrays.toString(nums.toArray(new Integer[0]))));
						} else {
							sender.sendMessage(String.format(TownsSettings.getLangString("msg_set_wild_ignore"), Globalworld.getName(), Arrays.toString(nums.toArray(new Integer[0]))));
						}
					} catch (Exception e) {
						TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_invalid_input"), " on/off."));
					}
				}
			} else if (split[0].equalsIgnoreCase("wildname")) {
				if (split.length < 2) {
					if (player != null) {
						TownsMessaging.sendErrorMsg(player, "Eg: /townsworld set wildname Wildy");
					}
				} else {
					try {
						Globalworld.setUnclaimedZoneName(split[1]);

						if (player != null) {
							TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_set_wild_name"), Globalworld.getName(), split[1]));
						} else {
							sender.sendMessage(String.format(TownsSettings.getLangString("msg_set_wild_name"), Globalworld.getName(), split[1]));
						}
					} catch (Exception e) {
						TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_invalid_input"), " on/off."));
					}
				}
			} else {
				if (player != null) {
					TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_invalid_property"), "world"));
				}
				return;
			}

			TownsUniverse.getDataSource().saveWorld(Globalworld);
		}
	}
}
