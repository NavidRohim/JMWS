package me.brynview.navidrohim.jmws.common.payloads;

import me.brynview.navidrohim.jmws.JMWS;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record HandshakePayload() implements CustomPayload {

    public static final Identifier packetIdentifier = Identifier.of(JMWS.MODID, "jmws_handshake");
    public static final CustomPayload.Id<HandshakePayload> ID = new CustomPayload.Id<>(packetIdentifier);
    public static final PacketCodec<RegistryByteBuf, HandshakePayload> CODEC = PacketCodec.ofStatic(
            (buf, waypoint) -> {},
            buf -> new HandshakePayload()
    );
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}


