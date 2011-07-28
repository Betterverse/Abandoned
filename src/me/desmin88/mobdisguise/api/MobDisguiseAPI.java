package me.desmin88.mobdisguise.api;

import me.desmin88.mobdisguise.MobDisguise;
import me.desmin88.mobdisguise.utils.MobIdEnum;

import org.bukkit.entity.Player;

public class MobDisguiseAPI {
    private MobDisguiseAPI() {}
   
    public static boolean disguisePlayer(Player p, String mobtype) {
        if (!MobIdEnum.map.containsKey(mobtype)) {
            return false;
        }
        MobDisguise.apiList.add(p);
        MobDisguise.disList.add(p);
        MobDisguise.playerMobId.put(p, (byte) MobIdEnum.map.get(mobtype).intValue());
        MobDisguise.playerEntIds.add(Integer.valueOf(p.getEntityId()));
        MobDisguise.pu.disguiseToAll(p);
        return true;
    }
    public static boolean undisguisePlayer(Player p, String mobtype) {
        MobDisguise.pu.undisguiseToAll(p);
        MobDisguise.disList.remove(p);
        MobDisguise.apiList.add(p);
        MobDisguise.playerMobId.put(p, null);
        MobDisguise.playerEntIds.remove(Integer.valueOf(p.getEntityId()));
        return true;
        
    }

    public static boolean isDisguised(Player p) {
        if(!MobDisguise.disList.contains(p)) {
            return false;
        } 
        else 
            return true;
    }


}
