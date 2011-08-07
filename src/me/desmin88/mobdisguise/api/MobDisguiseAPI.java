package me.desmin88.mobdisguise.api;

import me.desmin88.mobdisguise.MobDisguise;
import me.desmin88.mobdisguise.utils.MobIdEnum;

import org.bukkit.entity.Player;

public class MobDisguiseAPI { //Basic api to allow for other plugins to disguise/undisguise players
    private MobDisguiseAPI() {}
   
    
    public static boolean disguisePlayerAsPlayer(Player p, String name) {
        if(isDisguised(p)) {
            return false;
        }
        if(name.length() > 16) {
            System.out.println(MobDisguise.pref + "Error, some other plugin is setting a name over 16 characters, truncating.");
            String tmp = name.substring(0, 16);
            name=tmp;
        }
        MobDisguise.apiList.add(p.getName());
        MobDisguise.disList.add(p.getName());
        MobDisguise.playerdislist.add(p.getName());
        MobDisguise.pu.disguisep2pToAll(p, name);
        MobDisguise.p2p.put(p.getName(), name);
        return true;
    }
    
    public static boolean undisguisePlayerAsPlayer(Player p, String name) {
        if(!isDisguised(p)) {
            return false;
        }
        MobDisguise.apiList.remove(p.getName());
        MobDisguise.disList.remove(p.getName());
        MobDisguise.playerdislist.remove(p.getName());
        MobDisguise.pu.undisguisep2pToAll(p);
        MobDisguise.p2p.put(p.getName(), null);
        return true;
    }
    
    public static boolean disguisePlayer(Player p, String mobtype) {
        if (!MobIdEnum.map.containsKey(mobtype)) {
            return false;
        }
        if(isDisguised(p)) {
            return false;
        }
        MobDisguise.apiList.add(p.getName());
        MobDisguise.disList.add(p.getName());
        MobDisguise.playerMobId.put(p.getName(), (byte) MobIdEnum.map.get(mobtype).intValue());
        MobDisguise.playerEntIds.add(Integer.valueOf(p.getEntityId()));
        MobDisguise.pu.disguiseToAll(p);
        return true;
    }
    public static boolean undisguisePlayer(Player p, String mobtype) {
        if(isDisguised(p)) {
            return false;
        }
        MobDisguise.pu.undisguiseToAll(p);
        MobDisguise.disList.remove(p);
        MobDisguise.apiList.remove(p.getName());
        MobDisguise.playerMobId.put(p.getName(), null);
        MobDisguise.playerEntIds.remove(Integer.valueOf(p.getEntityId()));
        return true;
        
    }

    public static boolean isDisguised(Player p) {
        return MobDisguise.disList.contains(p.getName());
    }


}
