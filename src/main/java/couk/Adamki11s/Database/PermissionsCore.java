package couk.Adamki11s.Database;

import couk.Adamki11s.Warzone.Warzone;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PermissionsCore {
	
	private boolean hasPermissions = false;
	
	
	
	public boolean doesHaveNode(Player p, String node){
	return p.hasPermission("node");
	}
	
	public void sendInsufficientPermsMsg(Player p){
		p.sendMessage(ChatColor.RED + "[Warzone] " + Warzone.li.getObj("You do not have permissions to do this!"));
	}
	
	public boolean doesHaveSuperNode(Player p, String node){
		return p.hasPermission(node);
	}
	
	public void setupPermissions() {
	      //TODO remove
	  }

}
