package net.bettercraft.traps;

import net.betterverse.mychunks.api.MyChunks;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.getspout.spoutapi.SpoutManager;



public class Main extends JavaPlugin{
	
	private static MyChunks mchunks;
	@Override
	public void onDisable() {
	}

	@Override
	public void onEnable() {
		BlockListener bl = new BlockListener(this);
		this.getServer().getPluginManager().registerEvents(bl, this);
		this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		SpoutManager.getPacketManager().addListener(51, bl);
		mchunks = (MyChunks) Bukkit.getServer().getPluginManager().getPlugin("MyChunks");
	}
	
	public static MyChunks getMyChunks() {
		if(mchunks==null) {
			mchunks =(MyChunks) Bukkit.getServer().getPluginManager().getPlugin("MyChunks");
		}
		return mchunks;
	}

}
