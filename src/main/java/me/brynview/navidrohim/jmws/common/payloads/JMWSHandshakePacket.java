package me.brynview.navidrohim.jmws.common.payloads;

import me.brynview.navidrohim.jmws.JMWS;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class JMWSHandshakePacket implements FabricPacket {
    public static final PacketType<JMWSHandshakePacket> TYPE = PacketType.create(
            new Identifier(JMWS.MODID, "jmws_handshake"),
            JMWSHandshakePacket::new
    );

    public JMWSHandshakePacket(PacketByteBuf packetByteBuf) {
    }

    @Override
    public void write(PacketByteBuf packetByteBuf) {
    }
    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
