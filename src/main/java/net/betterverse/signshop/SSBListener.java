package net.betterverse.signshop;

import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

public class SSBListener implements Listener {

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		int id;
		int var5;
		if(!event.isCancelled() && !event.getPlayer().hasPermission("signshop.break")) {
			if((!(event.getBlock().getState() instanceof Sign) || !SSMain.shops.containsKey(event.getBlock().getLocation()) || ((SSShop)SSMain.shops.get(event.getBlock().getLocation())).player.equalsIgnoreCase(event.getPlayer().getName())) && (event.getBlock().getRelative(BlockFace.UP).getType() != Material.SIGN_POST || !SSMain.shops.containsKey(event.getBlock().getRelative(BlockFace.UP).getLocation()) || ((SSShop)SSMain.shops.get(event.getBlock().getRelative(BlockFace.UP).getLocation())).player.equalsIgnoreCase(event.getPlayer().getName()))) {
				int drops = 2;
				BlockFace[] is;
				var5 = (is = new BlockFace[]{BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH}).length;

				for(id = 0; id < var5; ++id) {
					BlockFace i = is[id];
					if(event.getBlock().getRelative(i).getType() == Material.WALL_SIGN && SSMain.shops.containsKey(event.getBlock().getRelative(i).getLocation()) && !((SSShop)SSMain.shops.get(event.getBlock().getRelative(i).getLocation())).player.equalsIgnoreCase(event.getPlayer().getName()) && event.getBlock().getRelative(i).getData() == drops) {
						event.setCancelled(true);
						event.getPlayer().sendMessage("[SignShop] " + ChatColor.DARK_RED + "I\'m sorry, Dave, but I cannot let you do that.");
						break;
					}

					++drops;
				}
			} else {
				event.setCancelled(true);
				event.getPlayer().sendMessage("[SignShop] " + ChatColor.DARK_RED + "I\'m sorry, Dave, but I cannot let you do that.");
			}
		}

		if(!event.isCancelled()) {
			HashMap var8 = new HashMap();
			if(event.getBlock().getState() instanceof Sign && SSMain.shops.containsKey(event.getBlock().getLocation())) {
				var8.put((Integer)SSMain.revnames.get(((Sign)event.getBlock().getState()).getLine(0).substring(2)), Integer.valueOf((var8.containsKey(SSMain.revnames.get(((Sign)event.getBlock().getState()).getLine(0).substring(2)))?((Integer)var8.get(SSMain.revnames.get(((Sign)event.getBlock().getState()).getLine(0).substring(2)))).intValue():0) + Integer.parseInt(((Sign)event.getBlock().getState()).getLine(3).substring(2))));
				SSMain.shops.remove(event.getBlock().getLocation());
			}

			if(event.getBlock().getRelative(BlockFace.UP).getType() == Material.SIGN_POST && SSMain.shops.containsKey(event.getBlock().getRelative(BlockFace.UP).getLocation())) {
				var8.put(Integer.valueOf(Integer.parseInt(((Sign)event.getBlock().getRelative(BlockFace.UP).getState()).getLine(0).substring(2))), Integer.valueOf((var8.containsKey(SSMain.revnames.get(((Sign)event.getBlock().getRelative(BlockFace.UP).getState()).getLine(0).substring(2)))?((Integer)var8.get(SSMain.revnames.get(((Sign)event.getBlock().getRelative(BlockFace.UP).getState()).getLine(0).substring(2)))).intValue():0) + Integer.parseInt(((Sign)event.getBlock().getRelative(BlockFace.UP).getState()).getLine(3).substring(2))));
				SSMain.shops.remove(event.getBlock().getRelative(BlockFace.UP).getLocation());
			}

			int var9 = 2;
			BlockFace[] var7;
			int var13 = (var7 = new BlockFace[]{BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH}).length;

			for(var5 = 0; var5 < var13; ++var5) {
				BlockFace var10 = var7[var5];
				if(event.getBlock().getRelative(var10).getType() == Material.WALL_SIGN && SSMain.shops.containsKey(event.getBlock().getRelative(var10).getLocation()) && event.getBlock().getRelative(var10).getData() == var9++) {
					var8.put((Integer)SSMain.revnames.get(((Sign)event.getBlock().getRelative(var10).getState()).getLine(0).substring(2)), Integer.valueOf((var8.containsKey(SSMain.revnames.get(((Sign)event.getBlock().getRelative(var10).getState()).getLine(0).substring(2)))?((Integer)var8.get(SSMain.revnames.get(((Sign)event.getBlock().getRelative(var10).getState()).getLine(0).substring(2)))).intValue():0) + Integer.parseInt(((Sign)event.getBlock().getRelative(var10).getState()).getLine(3).substring(2))));
					SSMain.shops.remove(event.getBlock().getRelative(var10).getLocation());
				}

				++var9;
			}

			Iterator var11 = var8.keySet().iterator();

			while(var11.hasNext()) {
				id = ((Integer)var11.next()).intValue();
				if(((Integer)var8.get(Integer.valueOf(id))).intValue() > 0) {
					ItemStack var12 = new ItemStack(id);
					var12.setAmount(((Integer)var8.get(Integer.valueOf(id))).intValue());
					event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), var12);
				}
			}
		}

	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if(!event.isCancelled() && "[SignShop]".equalsIgnoreCase(event.getLine(3))) {
			try {
				Integer.parseInt(event.getLine(0));
				Integer.parseInt(event.getLine(1));
				Integer.parseInt(event.getLine(2));
			} catch (Exception var3) {
				event.getPlayer().sendMessage("[SignShop] " + ChatColor.DARK_RED + "Sorry, your numbers are wrong!");
				event.setCancelled(true);
				return;
			}

			if(!event.getPlayer().hasPermission("signshop.make")) {
				event.getPlayer().sendMessage("[SignShop] " + ChatColor.DARK_RED + "You can\'t do that!");
				event.setCancelled(true);
				return;
			}

			if(!SSMain.valid.contains(Integer.valueOf(Integer.parseInt(event.getLine(0))))) {
				event.getPlayer().sendMessage("[SignShop] " + ChatColor.DARK_RED + "That isn\'t a valid ID!");
				event.setCancelled(true);
				return;
			}

			if(event.getLine(1).length() > 13 || event.getLine(2).length() > 13) {
				event.getPlayer().sendMessage("[SignShop] " + ChatColor.DARK_RED + "Your numbers are too big!");
				event.setCancelled(true);
				return;
			}

			String name = event.getLine(0);
			if(((String)SSMain.names.get(event.getLine(0))).isEmpty()) {
				System.out.println("[SignShop] ERROR!!! The name for " + name + " does not exist!");
			} else if(((String)SSMain.names.get(event.getLine(0))).length() > 13) {
				System.out.println("[SignShop] ERROR!!! The name for " + name + " is too long! It is \"" + (String)SSMain.names.get(event.getLine(0)) + "\" and is " + ((String)SSMain.names.get(event.getLine(0))).length() + " characters long, the maximum is 13.");
			} else {
				name = (String)SSMain.names.get(event.getLine(0));
			}

			event.setLine(0, ChatColor.YELLOW + name);
			event.setLine(1, ChatColor.BLUE + event.getLine(1));
			event.setLine(2, ChatColor.AQUA + event.getLine(2));
			event.setLine(3, ChatColor.GRAY + "0");
			SSMain.shops.put(event.getBlock().getLocation(), new SSShop(event.getBlock().getLocation(), event.getPlayer().getName()));
			event.getPlayer().sendMessage("[SignShop] " + ChatColor.GREEN + "Shop created!");
		}

	}
}
