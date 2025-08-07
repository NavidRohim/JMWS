package me.navidrohim.jmws.payloads;

import io.netty.buffer.ByteBuf;
import me.navidrohim.jmws.server.config.ServerConfig;
import me.navidrohim.jmws.server.config.ServerConfigObject;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class JMWSHandshakeMessage implements IMessage {

    public static class JMWSHandshakeMessageHandler implements IMessageHandler<JMWSHandshakeMessage, IMessage>
    {

        @Override
        public IMessage onMessage(JMWSHandshakeMessage message, MessageContext ctx) {
            if (ctx.side.equals(Side.SERVER))
            {
                return new JMWSHandshakeReplyMessage(ServerConfig.getConfigJson());
            }
            return null;
        }
    }

    public JMWSHandshakeMessage() {}

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {}
}
