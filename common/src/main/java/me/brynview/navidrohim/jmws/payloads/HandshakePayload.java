package me.brynview.navidrohim.jmws.payloads;


import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HandshakePayload() implements CustomPacketPayload {
    public static final Type<HandshakePayload> TYPE = new Type<>(ResourceLocation.parse("handshake_payload"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}


