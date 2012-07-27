package net.bettercraft.traps;

import java.util.HashSet;
import java.util.Set;
import net.betterverse.mychunks.api.chunk.ChunkManager;
import net.betterverse.mychunks.api.chunk.OwnedChunk;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;



public class PlayerListener implements Listener{
	
	private static Set<Location> inactives = new HashSet<Location>();
	private Main plugin;
	public PlayerListener(Main aThis) {
		plugin=aThis;
	}
	@EventHandler
	public void move(PlayerMoveEvent evt) {
		Location loc=evt.getPlayer().getLocation();
		Block b=loc.getBlock();
		if(b.getType()==Material.STONE_PLATE) {
			if(b.getRelative(BlockFace.DOWN).getType()==Material.IRON_BLOCK)
			{
				
				final Location iron=b.getRelative(BlockFace.DOWN).getLocation();
				if(!(inactives.contains(iron))) {
				evt.getPlayer().damage(4);
				evt.getPlayer().sendMessage(ChatColor.DARK_RED+"You were hit by a "+ChatColor.DARK_BLUE +" spike trap"+ChatColor.DARK_BLUE+"!");
				
					if(isOwner(evt.getPlayer())) return;
					inactives.add(iron);
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

						public void run() {
							inactives.remove(iron);
						}
					}, 20L*30);
				}
			}
		}
	}

	private boolean isOwner(Player player) {
		ChunkManager cm=Main.getMyChunks().getChunkManager();
		Chunk c = player.getLocation().getChunk();
		OwnedChunk oc=cm.getOwnedChunk(player.getName(),c.getX() , c.getZ());
		if(oc.hasMember(player.getName()))
			return true;
		return false;
	}

}
