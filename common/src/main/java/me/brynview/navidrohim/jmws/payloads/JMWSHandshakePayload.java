package me.brynview.navidrohim.jmws.payloads;

import me.brynview.navidrohim.jmws.Constants;

import me.brynview.navidrohim.jmws.platform.Services;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.server.config.ServerConfigObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;



public class JMWSHandshakePayload
{
    public static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "jmws_handshake");
    public static final StreamCodec<FriendlyByteBuf, JMWSHandshakePayload> STREAM_CODEC = StreamCodec.ofMember(JMWSHandshakePayload::encode, JMWSHandshakePayload::new);
    public String serverConfigDataJson;
    public ServerConfigObject serverConfigData;

    public JMWSHandshakePayload(FriendlyByteBuf friendlyByteBuf)
    {
        if (Services.PLATFORM.side().equals("CLIENT"))
        {
            if (friendlyByteBuf.readableBytes() != 0) {
                serverConfigDataJson = friendlyByteBuf.readUtf(512);
                serverConfigData = ServerConfig.getConfig(serverConfigDataJson);
            }
        }
    }

    public JMWSHandshakePayload(String jsonDataStr)
    {
        serverConfigData = null;
        serverConfigDataJson = jsonDataStr;
    }

    public JMWSHandshakePayload() {}

    public static CustomPacketPayload.Type<CustomPacketPayload> type()
    {
        return new CustomPacketPayload.Type<>(CHANNEL);
    }

    public void encode(FriendlyByteBuf buf)
    {
        if (Services.PLATFORM.side().equals("SERVER"))
        {
            buf.writeUtf(serverConfigDataJson);
        }
    }

}


