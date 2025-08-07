package me.navidrohim.jmws.payloads;


import me.navidrohim.jmws.Constants;
import me.navidrohim.jmws.server.config.ServerConfig;
import me.navidrohim.jmws.server.config.ServerConfigObject;
import net.minecraft.util.ResourceLocation;


public class JMWSHandshakePayload
{
    /*
    public static final ResourceLocation CHANNEL = new ResourceLocation(Constants.MODID, "jmws_handshake");
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
    }*/

}


