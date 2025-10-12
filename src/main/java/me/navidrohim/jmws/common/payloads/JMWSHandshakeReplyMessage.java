package me.navidrohim.jmws.common.payloads;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import me.navidrohim.jmws.client.config.ClientSideServerConfigObject;
import me.navidrohim.jmws.client.network.PacketHandler;
import me.navidrohim.jmws.common.Constants;
import me.navidrohim.jmws.server.config.ServerConfig;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class JMWSHandshakeReplyMessage implements IMessage {

    public String serverConfigDataJson;
    public ClientSideServerConfigObject serverConfigData;

    public static class JMWSHandshakeReplyMessageHandler implements IMessageHandler<JMWSHandshakeReplyMessage, IMessage>
    {

        @Override
        public IMessage onMessage(JMWSHandshakeReplyMessage message, MessageContext ctx) {
            if (ctx.side.equals(Side.CLIENT))
            {
                PacketHandler.HandshakeHandler(message);
            }
            return null;
        }
    }

    public JMWSHandshakeReplyMessage()
    {

    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readableBytes() != 0)
        {
            try
            {
                serverConfigDataJson = ByteBufUtils.readUTF8String(buf);
                Gson configJsonObj = new Gson();
                serverConfigData = configJsonObj.fromJson(serverConfigDataJson, ClientSideServerConfigObject.class);
            }
            catch (IndexOutOfBoundsException | JsonSyntaxException malformed) {
                Constants.getLogger().error("Missing or corrupted server data! Usually means a server version mismatch.");
                throw malformed;
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        serverConfigData = null;

        Constants.getLogger().info(ServerConfig.rawServerConfigData);
        JsonObject jsonObject = new JsonParser().parse(ServerConfig.rawServerConfigData).getAsJsonObject();
        jsonObject.addProperty("serverVersion", Constants.SERVER_VERSION);

        serverConfigDataJson = jsonObject.toString();

        ByteBufUtils.writeUTF8String(buf, serverConfigDataJson);
    }
}
