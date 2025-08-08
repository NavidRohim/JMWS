package me.navidrohim.jmws.payloads;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import me.navidrohim.jmws.enums.WaypointPayloadCommand;
import me.navidrohim.jmws.helper.CommandHelper;
import me.navidrohim.jmws.server.network.ServerPacketHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import static me.navidrohim.jmws.plugin.PacketHandler.handlePacket;

public class JMWSActionMessage implements IMessage {

    private String jsonData;
    public static WaypointPayloadCommand command = null;
    public static JsonArray argumentList = null;

    public static class JMWSActionMessageHandler implements IMessageHandler<JMWSActionMessage, IMessage>
    {

        @Override
        public IMessage onMessage(JMWSActionMessage message, MessageContext ctx) {
            if (ctx.side.equals(Side.CLIENT))
            {
                handlePacket(message);
            } else {
                ServerPacketHandler.handleIncomingActionCommand(message, ctx.getServerHandler().player);
            }
            return null;
        }
    }

    public JMWSActionMessage() {}

    public JMWSActionMessage(String jsonData)
    {
        this.jsonData = jsonData;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.jsonData = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.jsonData);
    }

    private void _setCommandAndArguments()
    {
        JsonObject jsonifyied = CommandHelper.getJsonObjectFromJsonString(jsonData);

        command = WaypointPayloadCommand.valueOf(jsonifyied.get("command").getAsString());
        argumentList = jsonifyied.get("arguments").getAsJsonArray();
    }

    public WaypointPayloadCommand command() {
        _setCommandAndArguments();
        return command;
    }

    public JsonArray arguments() {
        _setCommandAndArguments();
        return argumentList;
    }
}
