package me.desmin88.mobdisguise.commands;

import me.desmin88.mobdisguise.MobDisguise;
import me.desmin88.mobdisguise.utils.MobIdEnum;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MDCommand implements CommandExecutor {
    @SuppressWarnings("unused")
    private final MobDisguise plugin;

    public MDCommand(MobDisguise instance) {
        this.plugin = instance;
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player s = (Player) sender;
            
            if (args.length == 0) { // Undisguising, player types /md
                if(!MobDisguise.disList.contains(s.getName())) {
                    s.sendMessage(MobDisguise.pref + "You are not disguised, so you can't undisguise!");
                    return true;
                }
                if(MobDisguise.playerdislist.contains(s.getName())) {
                    MobDisguise.disList.remove(s.getName());
                    MobDisguise.playerdislist.remove(s.getName());
                    MobDisguise.pu.undisguisep2pToAll(s);
                    MobDisguise.p2p.put(s.getName(), null);
                    s.sendMessage(MobDisguise.pref + "You have undisguised as a different player and returned back to normal!");
                    return true;
                }
                MobDisguise.pu.undisguiseToAll(s);
                MobDisguise.disList.remove(s.getName());
                MobDisguise.playerMobId.put(s.getName(), null);
                MobDisguise.playerEntIds.remove(Integer.valueOf(s.getEntityId()));
                s.sendMessage(MobDisguise.pref + "You have been changed back to human form!");
                return true;
            }
            
            if (args[0].equalsIgnoreCase("types")) { // They want to know valid types of mobs
                String available = new String("");
                for(String key: MobIdEnum.map.keySet()) {
                    if(s.hasPermission("mobdisguise." + key)) {
                        available = available + ", " + key;
                    }
                }
                s.sendMessage(MobDisguise.pref + available);
                return true;
            }
            if (args[0].equalsIgnoreCase("stats")) { // They want to know they're current disguing status
                
                if(!MobDisguise.disList.contains(s.getName())){
                    s.sendMessage(MobDisguise.pref + "You are currently NOT disguised!");
                    return true;
                }
                else if(MobDisguise.playerdislist.contains(s.getName())) {
                    s.sendMessage(MobDisguise.pref + "You are currently disguised as player " + MobDisguise.p2p.get(s.getName()));
                    return true;
                }
                else {
                    
                    Integer inte = (Integer)  MobDisguise.playerMobId.get(s.getName()).intValue();
                    String mobtype = MobIdEnum.getTypeFromByte(inte);
                    s.sendMessage(MobDisguise.pref + "You are currently disguised as a " + mobtype);
                    return true;
                }
                
            }
             
            if(args[0].equalsIgnoreCase("p") && args.length == 2) {
                if(MobDisguise.disList.contains(s.getName())) {
                    s.sendMessage(MobDisguise.pref + "You are already disguised!");
                    return true;
                }
                if(MobDisguise.perm && !s.isOp()){
                    if(!s.hasPermission("mobdisguise.player") ) {
                        s.sendMessage(MobDisguise.pref + "You don't have permission to change into other players!");
                        return true;
                    }
                }
                if(!MobDisguise.perm && !s.isOp()) {
                    s.sendMessage(MobDisguise.pref + "You are not an OP and perms are disabled!");
                    return true;
                }
                MobDisguise.disList.add(s.getName());
                MobDisguise.playerdislist.add(s.getName());
                MobDisguise.pu.disguisep2pToAll(s, args[1]);
                MobDisguise.p2p.put(s.getName(), args[1]);
                s.sendMessage(MobDisguise.pref + "You have disguised as player " + args[1]);
                return true;
            }
            
            if(args.length == 1) { // Means they're trying to disguise
                String mobtype = args[0].toLowerCase();
                if(MobDisguise.disList.contains(s.getName())) {
                    s.sendMessage(MobDisguise.pref + "You are already disguised!");
                    return true;
                }
                if (!MobIdEnum.map.containsKey(mobtype)) {
                    s.sendMessage(MobDisguise.pref + "Invalid mob type!");
                    return true;
                }
                if(MobDisguise.perm && !s.isOp()){
                    if(!s.hasPermission("mobdisguise." + mobtype) ) {
                        s.sendMessage(MobDisguise.pref + "You don't have permission for this mob!");
                        return true;
                    }
                }
                if(!MobDisguise.perm && !s.isOp()) {
                    s.sendMessage(MobDisguise.pref + "You are not an OP and perms are disabled!");
                    return true;
                }
                if (!MobDisguise.cfg.getBoolean("BlackList." + mobtype, true)) {
                    s.sendMessage(MobDisguise.pref + "This mob type has been restricted!");
                    return true;
                }
                MobDisguise.disList.add(s.getName());
                MobDisguise.playerMobId.put(s.getName(), (byte) MobIdEnum.map.get(mobtype).intValue());
                MobDisguise.playerEntIds.add(Integer.valueOf(s.getEntityId()));
                MobDisguise.pu.disguiseToAll(s);
                s.sendMessage(MobDisguise.pref + "You have been disguised as a " + args[0].toLowerCase() + "!");
                return true;
            
            }
        }
        return false;
    }
}
