package me.navidrohim.jmws.payloads;

import io.netty.buffer.ByteBuf;
import me.navidrohim.jmws.plugin.PacketHandler;
import me.navidrohim.jmws.server.config.ServerConfig;
import me.navidrohim.jmws.server.config.ServerConfigObject;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class JMWSHandshakeReplyMessage implements IMessage {

    public String serverConfigDataJson;
    public ServerConfigObject serverConfigData;

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

    public JMWSHandshakeReplyMessage(String jsonDataStr)
    {
        serverConfigDataJson = jsonDataStr;
        serverConfigData = null;
    }

    public JMWSHandshakeReplyMessage() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        serverConfigDataJson = String.valueOf(buf.readBytes(buf.readableBytes()));
        serverConfigData = ServerConfig.getConfig(serverConfigDataJson);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(serverConfigDataJson.getBytes());
    }
}
