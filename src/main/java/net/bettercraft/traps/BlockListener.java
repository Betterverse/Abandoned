package net.bettercraft.traps;

import net.betterverse.mychunks.api.chunk.ChunkManager;
import net.betterverse.mychunks.api.chunk.OwnedChunk;
import net.minecraft.server.Packet51MapChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.getspout.spoutapi.packet.listener.PacketListener;
import org.getspout.spoutapi.packet.standard.MCPacket;

public class BlockListener implements PacketListener, Listener {

	private Main plugin;

	public BlockListener(Main aThis) {
		plugin = aThis;
	}

	public boolean checkPacket(final Player player, MCPacket mcp) {
		if (mcp instanceof Packet51MapChunk) {
			Packet51MapChunk mp = (Packet51MapChunk) mcp;
			int ChunkX = mp.a >> 4;
			int ChunkZ = mp.c >> 4;
			System.out.println("Sending chunk " + ChunkX + " and " + ChunkZ + " to player " + player.getName());
			Chunk c = player.getWorld().getChunkAt(ChunkZ, ChunkZ);
			for (int x = 0; x < 16; x++) {
				for (int y = 0; y <= 128; y++) {
					for (int z = 0; z <= 16; z++) {
						if (isValidFakeChest(c.getBlock(x, y, z))) {
							ChunkManager cm = Main.getMyChunks().getChunkManager();
							OwnedChunk oc = cm.getOwnedChunk(player.getWorld().getName(), ChunkX, ChunkZ);
							if (!oc.getMembers().contains(player.getName())) {
								final Block b = c.getBlock(x, y, z);
								Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

									public void run() {
										player.sendBlockChange(b.getLocation(), Material.CHEST, (byte) 0);
										player.sendBlockChange(b.getRelative(BlockFace.UP).getLocation(), Material.AIR, (byte) 0);
										//TODO actually modify the byte data! <= genius :D
									}
								}, 10L);
							}
						}
					}
				}
			}

		}
		return true;
	}

	private boolean isValidFakeChest(Block block) {
		return block.getType() == Material.DIAMOND_BLOCK && block.getRelative(BlockFace.UP).getType() == Material.TNT;
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent evt) {
		Block b = evt.getClickedBlock();
		Player player = evt.getPlayer();
		if (b == null) {
			return;
		}
		if (isValidFakeChest(b)) {
			ChunkManager cm = Main.getMyChunks().getChunkManager();
			OwnedChunk oc = cm.getOwnedChunk(player.getWorld().getName(), b.getChunk().getX(), b.getChunk().getZ());
			if (!oc.getMembers().contains(player.getName())) {
				player.damage(10);
				player.sendMessage("You activated a trap chest!");
			}
		}
	}
}
