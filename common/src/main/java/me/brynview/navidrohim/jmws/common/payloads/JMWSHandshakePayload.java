package me.brynview.navidrohim.jmws.common.payloads;

import com.google.gson.*;

import me.brynview.navidrohim.jmws.Constants;

import me.brynview.navidrohim.jmws.client.config.ClientSideServerConfigObject;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.platform.Services;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class JMWSHandshakePayload
{
    public static final Identifier CHANNEL = Identifier.fromNamespaceAndPath(Constants.MODID, "jmws_handshake");
    public static final StreamCodec<@NotNull FriendlyByteBuf, @NotNull JMWSHandshakePayload> STREAM_CODEC = StreamCodec.ofMember(JMWSHandshakePayload::encode, JMWSHandshakePayload::new);
    public String serverConfigDataJson;
    public ClientSideServerConfigObject serverConfigData;

    /**
     * Client-side constructor
     * @param friendlyByteBuf Packet data from server (byte buffer, can read json string from it)
     */
    public JMWSHandshakePayload(FriendlyByteBuf friendlyByteBuf)
    {
        if ((Services.PLATFORM.side().equals("CLIENT") || !JMWSCommon.isInternalServer()) && friendlyByteBuf.readableBytes() != 0)
        {
            try
            {
                serverConfigDataJson = friendlyByteBuf.readUtf(512);
                serverConfigData = JMWSCommon.gson.fromJson(serverConfigDataJson, ClientSideServerConfigObject.class);
            }
            catch (IndexOutOfBoundsException | JsonSyntaxException malformed) {
                Constants.getLogger().error("Missing or corrupted server data! Usually means a server version mismatch.");
                throw malformed;
            }
        }
    }

    /**
     * Server-side constructor
     */
    public JMWSHandshakePayload()
    {
        serverConfigData = null;

        JsonObject jsonObject = JsonParser.parseString(ServerConfig.rawServerConfigData).getAsJsonObject();
        jsonObject.addProperty("serverVersion", Constants.SERVER_VERSION);

        serverConfigDataJson = jsonObject.toString();
    }

    public static CustomPacketPayload.Type<CustomPacketPayload> type()
    {
        return new CustomPacketPayload.Type<>(CHANNEL);
    }

    /**
     * Encodes data ready to send to client
     * @param buf Buffer to add data to for the client
     */
    public void encode(FriendlyByteBuf buf)
    {
        if (Services.PLATFORM.side().equals("SERVER") || JMWSCommon.isInternalServer())
        {
            buf.writeUtf(serverConfigDataJson);
        }
    }

}


