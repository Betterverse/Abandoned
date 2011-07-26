package me.desmin88.mobdisguise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.desmin88.mobdisguise.commands.*;
import me.desmin88.mobdisguise.listeners.*;
import me.desmin88.mobdisguise.utils.*;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;

public class MobDisguise extends JavaPlugin {
    public List<Player> disList = new ArrayList<Player>();
    public Map<Player, Byte> playerMobId = new HashMap<Player, Byte>();
    public Set<Integer> playerEntIds = new HashSet<Integer>();
    public PacketUtils pu = new PacketUtils(this);
    public final PacketListener packetlistener = new PacketListener(this);
    public final MDPlayerListener playerlistener = new MDPlayerListener(this);

    @Override
    public void onDisable() {
        System.out.println("[MobDisguise] version 1.1 DEV disabled");

    }

    @Override
    public void onEnable() {
        this.getCommand("md").setExecutor(new MDCommand(this));
        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerlistener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_RESPAWN, playerlistener, Priority.Normal, this);
        // this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ANIMATION,
        // new MDPlayerListener(this), Priority.Normal, this);
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new DisguiseTask(this), 1200, 1200);
        // Register packet listeners
        org.bukkitcontrib.packet.listener.Listeners.addListener(17, packetlistener);
        org.bukkitcontrib.packet.listener.Listeners.addListener(18, packetlistener);
        System.out.println("[MobDisguise] version 1.1 DEV enabled");

    }

}