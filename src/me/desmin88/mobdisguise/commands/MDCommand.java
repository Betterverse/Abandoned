package me.desmin88.mobdisguise.commands;

import me.desmin88.mobdisguise.MobDisguise;
import me.desmin88.mobdisguise.utils.MobIdEnum;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MDCommand implements CommandExecutor {
    private final MobDisguise plugin;

    public MDCommand(MobDisguise instance) {
        this.plugin = instance;
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player s = (Player) sender;
            if(args.length == 0) {
                s.sendMessage(MobDisguise.pref + "You can't have zero arguments!");
                return true;
            }
            
            if (args[0].equalsIgnoreCase("undisguise")) {
                plugin.pu.undisguiseToAll(s);
                plugin.disList.remove(s);
                plugin.playerMobId.put(s, null);
                plugin.playerEntIds.remove(Integer.valueOf(s.getEntityId()));
                s.sendMessage(MobDisguise.pref + "You have been changed back!");
                return true;
            }

            
            if (args[0].equalsIgnoreCase("types")) {
                for (String key : MobIdEnum.map.keySet()) {
                    s.sendMessage(MobDisguise.pref + key);
                }
            }

            if (args[0].equalsIgnoreCase("disguise")) {
                String mobtype = args[1].toLowerCase();
                if (!MobIdEnum.map.containsKey(mobtype)) {
                    s.sendMessage(MobDisguise.pref + "Invalid mob type!");
                    return true;
                }
                plugin.disList.add(s);
                plugin.playerMobId.put(s, (byte) MobIdEnum.map.get(mobtype).intValue());
                plugin.playerEntIds.add(Integer.valueOf(s.getEntityId()));
                plugin.pu.disguiseToAll(s);
                s.sendMessage(MobDisguise.pref + "You have been disguised as a " + args[1].toLowerCase() + "!");
                return true;
            }

        }
        return false;
    }
}
