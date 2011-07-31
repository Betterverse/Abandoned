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
                MobDisguise.pu.undisguiseToAll(s);
                MobDisguise.disList.remove(s.getName());
                MobDisguise.playerMobId.put(s.getName(), null);
                MobDisguise.playerEntIds.remove(Integer.valueOf(s.getEntityId()));
                s.sendMessage(MobDisguise.pref + "You have been changed back to human form!");
                return true;
            }
            
            if (args[0].equalsIgnoreCase("types")) { // They want to know valid types of mobs
                s.sendMessage(MobDisguise.pref + MobIdEnum.types);
                return true;
            }
            if (args[0].equalsIgnoreCase("stats")) { // They want to know they're current disguing status
                boolean disguised = MobDisguise.disList.contains(s.getName());
                if(!disguised){
                    s.sendMessage(MobDisguise.pref + "You are currently NOT disguised!");
                    return true;
                }
                else {
                    Integer inte = (Integer)  MobDisguise.playerMobId.get(s.getName()).intValue();
                    String mobtype = MobIdEnum.getTypeFromByte(inte);
                    s.sendMessage(MobDisguise.pref + "You are currently disguised as a " + mobtype);
                    return true;
                }
                
            }

            
            if(args.length == 1) { // Means they're trying to disguise
                String mobtype = args[0].toLowerCase();
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
