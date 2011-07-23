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
            
            if (args[0].equalsIgnoreCase("undisguise")) {
                plugin.pu.undisguiseToAll(s);
                plugin.disList.remove(s);
                plugin.playerMobId.put(s, null);
                plugin.playerEntIds.remove(Integer.valueOf(s.getEntityId()));
                return true;
            }

            
            if (args[0].equalsIgnoreCase("types")) {
                for (String key : MobIdEnum.map.keySet()) {
                    s.sendMessage("[MobDisguise] " + key);
                }
            }

            if (args[0].equalsIgnoreCase("disguise")) {
                String mobtype = args[1].toLowerCase();
                if (!MobIdEnum.map.containsKey(mobtype)) {
                    s.sendMessage("[MobDisguise] Invalid mob type!");
                    return true;
                }
                plugin.disList.add(s);
                plugin.playerMobId.put(s, (byte) MobIdEnum.map.get(mobtype).intValue());
                plugin.playerEntIds.add(Integer.valueOf(s.getEntityId()));
                plugin.pu.disguiseToAll(s);
                return true;
            }

        }
        return false;
    }
}
