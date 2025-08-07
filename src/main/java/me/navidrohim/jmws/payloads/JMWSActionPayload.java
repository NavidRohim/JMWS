package me.navidrohim.jmws.payloads;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.navidrohim.jmws.Constants;
import me.navidrohim.jmws.enums.WaypointPayloadCommand;
import me.navidrohim.jmws.helper.CommandHelper;
import net.minecraft.util.ResourceLocation;

import java.util.List;


public class  JMWSActionPayload
{
    /*
    public static final ResourceLocation CHANNEL = new ResourceLocation(Constants.MODID, "action_command");
    public static final StreamCodec<FriendlyByteBuf, JMWSActionPayload> STREAM_CODEC = StreamCodec.ofMember(JMWSActionPayload::encode, JMWSActionPayload::new);

    public static String rawData = null;
    public static WaypointPayloadCommand command = null;
    public static List<JsonElement> argumentList = null;

    public JMWSActionPayload(FriendlyByteBuf friendlyByteBuf)
    {
        rawData = friendlyByteBuf.readUtf();
    }

    public JMWSActionPayload(String jsonData)
    {
        rawData = jsonData;
    }

    public static CustomPacketPayload.Type<CustomPacketPayload> type()
    {
        return new CustomPacketPayload.Type<>(CHANNEL);
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeUtf(rawData);
    }

    private void _setCommandAndArguments()
    {
        JsonObject jsonifyied = CommandHelper.getJsonObjectFromJsonString(rawData);

        command = WaypointPayloadCommand.valueOf(jsonifyied.asMap().get("command").getAsString());
        argumentList = jsonifyied.asMap().get("arguments").getAsJsonArray().asList();
    }

    public WaypointPayloadCommand command() {
        _setCommandAndArguments();
        return command;
    }

    public List<JsonElement> arguments() {
        _setCommandAndArguments();
        return argumentList;
    }*/
}
