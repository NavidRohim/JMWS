package me.brynview.navidrohim.jmws.common.payloads;

import io.netty.handler.codec.DecoderException;
import me.brynview.navidrohim.jmws.Constants;

import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.platform.Services;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.common.config.ServerConfigObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;


public class JMWSHandshakePayload
{
    public static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "jmws_handshake");
    public static final StreamCodec<FriendlyByteBuf, JMWSHandshakePayload> STREAM_CODEC = StreamCodec.ofMember(JMWSHandshakePayload::encode, JMWSHandshakePayload::new);
    public String serverConfigDataJson;
    public ServerConfigObject serverConfigData;

    public JMWSHandshakePayload(FriendlyByteBuf friendlyByteBuf)
    {
        if (Services.PLATFORM.side().equals("CLIENT") || !CommonClass.isInternalServer())
        {
            @Nullable Double version = null;
            if (friendlyByteBuf.readableBytes() != 0) {
                try
                {
                    serverConfigDataJson = friendlyByteBuf.readUtf(512);
                    version = friendlyByteBuf.readDouble();
                }
                catch (IndexOutOfBoundsException ignored)
                {
                    Constants.getLogger().error("Server does not have JMWS server version! Things are likely to break!");
                }
                finally
                {
                    serverConfigData = ServerConfig.getConfig(serverConfigDataJson, version);
                }
            }
        }
    }

    public JMWSHandshakePayload()
    {
        serverConfigData = null;
        serverConfigDataJson = ServerConfig.rawServerConfigData;
    }

    public static CustomPacketPayload.Type<CustomPacketPayload> type()
    {
        return new CustomPacketPayload.Type<>(CHANNEL);
    }

    public void encode(FriendlyByteBuf buf)
    {
        if (Services.PLATFORM.side().equals("SERVER") || CommonClass.isInternalServer())
        {
            buf.writeUtf(serverConfigDataJson);
            buf.writeDouble(Constants.SERVER_VERSION);
        }
    }

}


