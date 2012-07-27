package net.betterverse.betteripcheck;

import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;



class CExecutor implements CommandExecutor {

	private BetterIPCheck plugin;
	public CExecutor(BetterIPCheck aThis) {
		plugin=aThis;
	}

	public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
		if(!(cs.hasPermission("betteripcheck.staff"))) {
			return false;
		}
		if(strings.length==0) {
			return false;
		}
		String desiredName = strings[0];
		if(Bukkit.getServer().getPlayer(desiredName)==null) {
			cs.sendMessage(ChatColor.DARK_RED+"That player is not online!");
			return true;
		}
		Set<String> st=plugin.getAllNames(Bukkit.getServer().getPlayer(desiredName));
		String toSend = ChatColor.GOLD+"Alts: ";
		for(String altNick:st) {
			toSend+=altNick+", ";
		}
		toSend+="that is "+ChatColor.AQUA+st.size()+ChatColor.GOLD+" alts!";
		cs.sendMessage(toSend);
		return true;
	}

}
