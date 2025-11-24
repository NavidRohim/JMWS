package me.brynview.navidrohim.jmws.common.payloads;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;


public class  JMWSActionPayload
{
    public static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "action_command");
    public static final StreamCodec<FriendlyByteBuf, JMWSActionPayload> STREAM_CODEC = StreamCodec.ofMember(JMWSActionPayload::encode, JMWSActionPayload::new);

    public static final int PACKET_SIZE = 2_097_000; // 2MB

    public String rawData = null;
    public CommandFactory.Commands command = null;
    public List<JsonElement> argumentList = null;

    public JMWSActionPayload(FriendlyByteBuf friendlyByteBuf)
    {
        rawData = friendlyByteBuf.readUtf(PACKET_SIZE);
    }

    public JMWSActionPayload(String jsonData)
    {
        if (PACKET_SIZE >= jsonData.getBytes().length)
            rawData = jsonData;
        else {
            Constants.getLogger().error("Packet too big! User may have too many waypoints and or groups!");
            rawData = CommandFactory.makeClientAlertRequestJson("error.jmws.error_packet_size", true, JMWSMessageType.FAILURE);
        }
    }

    public static CustomPacketPayload.Type<CustomPacketPayload> type()
    {
        return new CustomPacketPayload.Type<>(CHANNEL);
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeUtf(rawData, PACKET_SIZE);
    }

    private void _setCommandAndArguments()
    {
        JsonObject jsonifyied = CommandFactory.getJsonObjectFromJsonString(rawData);

        command = CommandFactory.Commands.valueOf(jsonifyied.asMap().get("command").getAsString());
        argumentList = jsonifyied.asMap().get("arguments").getAsJsonArray().asList();
    }

    public CommandFactory.Commands command() {
        _setCommandAndArguments();
        return command;
    }

    public List<JsonElement> arguments() {
        _setCommandAndArguments();
        return argumentList;
    }
}
