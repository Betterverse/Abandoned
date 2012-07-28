package net.betterverse.signshop;

import com.sk89q.config.Configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class SSMain extends JavaPlugin {
	public static Economy econ = null;
	static Map shops = new HashMap();
	static Set valid = new HashSet();
	static Map names = new HashMap();
	static Map revnames = new HashMap();
	static Configuration saveshops;


	public void onDisable() {
		this.save();
		System.out.println("[SignShop] Disabled.");
	}

	public void onEnable() {
		setupEconomy();
		if(!(new File(this.getDataFolder(), "names.yml")).exists()) {
			this.getDataFolder().mkdirs();
			File names = new File(this.getDataFolder(), "names.yml");
			InputStream s = SSMain.class.getResourceAsStream("/me/br_/minecraft/bukkit/signshop/names.yml");
			if(s != null) {
				FileOutputStream output = null;

				try {
					output = new FileOutputStream(names);
					byte[] shop = new byte[8192];
					boolean length = false;

					int var29;
					while((var29 = s.read(shop)) > 0) {
						output.write(shop, 0, var29);
					}
				} catch (IOException var20) {
					var20.printStackTrace();
				} finally {
					try {
						if(s != null) {
							s.close();
						}
					} catch (IOException var18) {
						;
					}

					try {
						if(output != null) {
							output.close();
						}
					} catch (IOException var17) {
						;
					}

				}
			}
		}

		Configuration var22 = new Configuration(new File(this.getDataFolder(), "names.yml"));
		var22.load();
		Iterator var25 = var22.getAll().keySet().iterator();

		String var23;
		while(var25.hasNext()) {
			var23 = (String)var25.next();
			names.put(var22.getString(var23), var23);
			try{ 
			revnames.put(var23, Integer.valueOf(Integer.parseInt(var22.getString(var23))));
			} catch(Exception ex) {
				System.out.println("Got exception! "+var23);
				ex.printStackTrace();
			}
		}

		saveshops = new Configuration(new File(this.getDataFolder(), "shops.yml"));
		saveshops.load();
		Material[] var30;
		int var27 = (var30 = Material.values()).length;

		for(int var26 = 0; var26 < var27; ++var26) {
			Material var24 = var30[var26];
			valid.add(Integer.valueOf(var24.getId()));
		}

		var25 = saveshops.getStringList("shops", (List)null).iterator();

		while(var25.hasNext()) {
			var23 = (String)var25.next();

			try {
				SSShop var28 = new SSShop(var23);
				shops.put(var28.loc, var28);
			} catch (Exception var19) {
				;
			}
		}

		this.getServer().getPluginManager().registerEvents(new SSBListener(), this);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new SSSaver(this), 1200L, 1200L);
		System.out.println("[SignShop] Enabled.");
	}
	
	private boolean setupEconomy() {
		 if (getServer().getPluginManager().getPlugin("Vault") == null) {
			  return false;
		 }
		 RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		 if (rsp == null) {
			  return false;
		 }
		 econ = rsp.getProvider();
		 return econ != null;
	}

	public boolean onCommand(CommandSender sender, Command command, String com, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("[SignShop] " + ChatColor.DARK_RED + "Sorry, you can\'t do that!");
			return true;
		} else {
			Player player = (Player)sender;
			if(args.length == 0) {
				player.sendMessage("[SignShop] " + ChatColor.DARK_RED + "What do you want to do?");
				return true;
			} else {
				try {
					Integer.parseInt(args[1]);
				} catch (Exception var12) {
					return true;
				}
				
				Block targetBlock = player.getTargetBlock(null, 10);
				Location target = targetBlock.getLocation();

				if("buy".equalsIgnoreCase(args[0]) && Integer.parseInt(args[1]) > 0) {
					if(targetBlock != null && shops.containsKey(target) && targetBlock.getState() instanceof Sign) {
						
						Sign sign = (Sign)targetBlock.getState();
						double cost = (double)((args.length >= 2?Integer.parseInt(args[1]):1) * Integer.parseInt(sign.getLine(1).substring(2)));
						
						if(Integer.parseInt(sign.getLine(3).substring(2)) == 0) {
							player.sendMessage("[SignShop] " + ChatColor.DARK_RED + "That shop is out of stock!");
						} else if(Integer.parseInt(sign.getLine(3).substring(2)) < (args.length >= 2?Integer.parseInt(args[1]):1)) {
							player.sendMessage("[SignShop] " + ChatColor.DARK_RED + "That shop only has " + Integer.parseInt(sign.getLine(3).substring(2)) + " things in stock!");
						} else if(!player.getName().equalsIgnoreCase(((SSShop)shops.get(target)).player) && econ.getBalance(player.getName()) < cost) {
							player.sendMessage("[SignShop] " + ChatColor.DARK_RED + "You don't have enough money!");
						} else {
							
							econ.bankWithdraw(player.getName(), cost);
							econ.bankDeposit(((SSShop)shops.get(target)).player, cost);
							
							ItemStack var13 = new ItemStack(((Integer)revnames.get(sign.getLine(0).substring(2))).intValue());
							var13.setAmount(args.length >= 2?Integer.parseInt(args[1]):1);
							player.getInventory().addItem(new ItemStack[]{var13});
							sign.setLine(3, "" + ChatColor.GRAY + (Integer.parseInt(sign.getLine(3).substring(2)) - (args.length >= 2?Integer.parseInt(args[1]):1)));
							sign.update(true);
							player.sendMessage("[SignShop] " + ChatColor.GREEN + "Bought " + (args.length >= 2?Integer.parseInt(args[1]):1) + " " + sign.getLine(0).substring(2) + " for " + econ.format(cost) + "!");
						}
					} else {
						player.sendMessage("[SignShop] " + ChatColor.DARK_RED + "You have to look at a shop!");
					}
				} else if("sell".equalsIgnoreCase(args[0]) && Integer.parseInt(args[1]) > 0) {
					if(targetBlock != null && shops.containsKey(target) && targetBlock.getState() instanceof Sign) {
						
						Sign sign = (Sign)targetBlock.getState();
						double cost = (double)((args.length >= 2?Integer.parseInt(args[1]):1) * Integer.parseInt(sign.getLine(2).substring(2)));
						
						int amt = 0;
						int materialId = ((Integer)revnames.get(sign.getLine(0).substring(2))).intValue();
						ItemStack[] var11;
						int var10 = (var11 = player.getInventory().getContents()).length;

						ItemStack is;
						for(int var9 = 0; var9 < var10; ++var9) {
							is = var11[var9];
							if(is != null && is.getTypeId() == materialId && is.getData().getData() == 0 && is.getDurability() == 0) {
								amt += is.getAmount();
							}
						}

						if(amt < (args.length >= 2?Integer.parseInt(args[1]):1)) {
							player.sendMessage("[SignShop] " + ChatColor.DARK_RED + "You don\'t have enough!");
						} else if(("" + (Integer.parseInt(sign.getLine(3).substring(2)) + (args.length >= 2?Integer.parseInt(args[1]):1))).length() > 13) {
							player.sendMessage("[SignShop] " + ChatColor.DARK_RED + "The sign can\'t hold any more!");
						} else if(!player.getName().equalsIgnoreCase(((SSShop)shops.get(target)).player) && (econ.getBalance(((SSShop)shops.get(target)).player) < cost)) {
							player.sendMessage("[SignShop] " + ChatColor.DARK_RED + "The shop doesn\'t have enough money!");
						} else {
							
							econ.bankDeposit(player.getName(), cost);
							econ.bankWithdraw(((SSShop)shops.get(target)).player, cost);
							
							is = new ItemStack(((Integer)revnames.get(sign.getLine(0).substring(2))).intValue());
							is.setAmount(args.length >= 2?Integer.parseInt(args[1]):1);
							player.getInventory().removeItem(new ItemStack[]{is});
							sign.setLine(3, "" + ChatColor.GRAY + (Integer.parseInt(sign.getLine(3).substring(2)) + (args.length >= 2?Integer.parseInt(args[1]):1)));
							sign.update(true);
							player.sendMessage("[SignShop] " + ChatColor.GREEN + "Sold " + (args.length >= 2?Integer.parseInt(args[1]):1) + " " + sign.getLine(0).substring(2) + " for " + econ.format(cost) + "!");
						}
					} else {
						player.sendMessage("[SignShop] " + ChatColor.DARK_RED + "You have to look at a shop!");
					}
				} else if(Integer.parseInt(args[1]) <= 0) {
					player.sendMessage("[SignShop] " + ChatColor.DARK_RED + "You have to buy/sell at least one item!");
				} else {
					player.sendMessage("[SignShop] Syntax: /ss [buy|sell] [amount]");
				}

				return true;
			}
		}
	}

	void save() {
		saveshops.removeProperty("shops");
		ArrayList shopstring = new ArrayList();
		Iterator var3 = shops.values().iterator();

		while(var3.hasNext()) {
			SSShop shop = (SSShop)var3.next();
			if(shop.loc.getWorld() != null && shop.loc.getBlock().getState() instanceof Sign) {
				shopstring.add(shop.toString());
			}
		}

		saveshops.setProperty("shops", shopstring);
		saveshops.save();
	}
}
