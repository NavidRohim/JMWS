package me.brynview.navidrohim.jmws.payloads;

import me.brynview.navidrohim.jmws.Constants;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;



public class JMWSHandshakePayload
{
    public static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "jmws_handshake");
    public static final StreamCodec<FriendlyByteBuf, JMWSHandshakePayload> STREAM_CODEC = StreamCodec.ofMember(JMWSHandshakePayload::encode, JMWSHandshakePayload::new);

    public JMWSHandshakePayload(FriendlyByteBuf friendlyByteBuf)
    {

    }

    public JMWSHandshakePayload()
    {

    }

    public static CustomPacketPayload.Type<CustomPacketPayload> type()
    {
        return new CustomPacketPayload.Type<>(CHANNEL);
    }

    public void encode(FriendlyByteBuf buf)
    {

    }

}


