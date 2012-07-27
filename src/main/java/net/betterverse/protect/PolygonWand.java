package net.betterverse.protect;

import java.util.List;
import net.betterverse.protect.utils.ProtectedPolygon;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PolygonWand implements Listener {

	private final PolygonProtect parent;
	
	public PolygonWand(PolygonProtect parent) {
		this.parent = parent;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.isCancelled()) return;
		// Are we clicking with a gold axe?
		if(event.getPlayer().getItemInHand().getType() == Material.GOLD_AXE &&
				event.getAction() == Action.LEFT_CLICK_BLOCK) {
			// Are we in the list in the first place?
			if(parent.players.contains(event.getPlayer().getName())) {
				Location clicked = event.getClickedBlock().getLocation();
				parent.data.get(event.getPlayer().getName()).add(clicked);
				String locs = clicked.getBlockX()+","+clicked.getBlockY()+","+clicked.getBlockZ();
				// And finally send the message
				event.getPlayer().sendMessage(ChatColor.GOLD+locs+" added to polygon buffer");
				event.setCancelled(true);
			}
		}
		if(event.getPlayer().getItemInHand().getType() == Material.GOLD_AXE &&
				event.getAction() == Action.RIGHT_CLICK_BLOCK) { 
			if(event.getPlayer().hasPermission("polygon.check")) {
				Location loc = event.getClickedBlock().getLocation();
				List<ProtectedPolygon> polys = PolygonManager.getInstance().getList(loc);
				event.getPlayer().sendMessage(ChatColor.GOLD+"This area contains "+polys.size()+" polygons");
				for(ProtectedPolygon pol : polys)
					event.getPlayer().sendMessage(ChatColor.GOLD+pol.getName());
				event.setCancelled(true);
			}
		}
	}	
}
