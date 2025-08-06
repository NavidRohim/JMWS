package me.brynview.navidrohim.jmws.payloads;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.enums.WaypointPayloadCommand;
import me.brynview.navidrohim.jmws.helper.CommandHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.BitSet;
import java.util.List;


public class  JMWSActionPayload
{
    public static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "action_command");
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
    }
}
