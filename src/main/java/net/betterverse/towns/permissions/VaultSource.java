package net.betterverse.towns.permissions;

import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.Resident;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultSource extends TownsPermissionSource {
	private Chat chat = null;
	private Permission permissions = null;

	public VaultSource(Towns towns, Plugin test) {
		this.plugin = towns;

		RegisteredServiceProvider<Chat> chatProvider = plugin.getServer().getServicesManager().getRegistration(Chat.class);
		if(chatProvider != null) chat = chatProvider.getProvider();

		RegisteredServiceProvider<Permission> permProvider = plugin.getServer().getServicesManager().getRegistration(Permission.class);
		if(permProvider != null) permissions = permProvider.getProvider();
	}

	@Override
	public String getPrefixSuffix(Resident resident, String node) {
		String group = "", user = "";
		Player player = plugin.getServer().getPlayer(resident.getName());

		if (node == "prefix") {
			group = chat.getGroupPrefix(player.getWorld(), getPlayerGroup(player));
			user = chat.getPlayerPrefix(player);
		} else if (node == "suffix") {
			group = chat.getGroupSuffix(player.getWorld(), getPlayerGroup(player));
			user = chat.getPlayerSuffix(player);
		}

		if (group == null) group = "";
		if (user == null) user = "";

		if (!group.equals(user)) {
			user = group + user;
		}

		user = TownsSettings.parseSingleLineString(user);

		return user;
	}

	@Override
	public int getGroupPermissionIntNode(String playerName, String node) {
		/*
		 *  Vault doesn't support non boolean nodes
		 */

		Player player = plugin.getServer().getPlayer(playerName);

		for (PermissionAttachmentInfo test : player.getEffectivePermissions()) {
			if (test.getPermission().startsWith(node + ".")) {
				String[] split = test.getPermission().split("\\.");
				try {
					return Integer.parseInt(split[split.length - 1]);
				} catch (NumberFormatException e) {
				}
			}
		}

		return -1;
	}

	@Override
	public boolean hasPermission(Player player, String node) {
		if(permissions != null) {
			return permissions.has(player, node);
		} else {
			return false;
		}
	}

	@Override
	public String getPlayerGroup(Player player) {
		if(permissions != null) {
			return permissions.getPrimaryGroup(player);
		} else {
			return "";
		}
	}

}
