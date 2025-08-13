package me.navidrohim.jmws.common.payloads;

import io.netty.buffer.ByteBuf;
import me.navidrohim.jmws.common.Constants;
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
                return new JMWSHandshakeReplyMessage(ServerConfigSendable.getServerConfigSendable());
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
