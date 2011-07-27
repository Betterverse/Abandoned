package me.desmin88.mobdisguise.listeners;

import me.desmin88.mobdisguise.MobDisguise;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet17;
import net.minecraft.server.Packet18ArmAnimation;

import org.bukkitcontrib.packet.listener.Listener;

public class PacketListener implements Listener {
    private final MobDisguise plugin;

    public PacketListener(MobDisguise instance) {
        this.plugin = instance;
    }

    @Override
    public boolean checkPacket(Packet packet) {
        if (packet instanceof Packet18ArmAnimation) {
            Packet18ArmAnimation p18 = (Packet18ArmAnimation) packet;
            if (p18.b != 2 && plugin.playerEntIds.contains(p18.a)) {
                return false;
            }
        }
        if (packet instanceof Packet17) {
            Packet17 p17 = (Packet17) packet;
            if (plugin.playerEntIds.contains(p17.a)) {
                return false;
            }
        }
        return true;
    }

}
