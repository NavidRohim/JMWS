package me.brynview.navidrohim.jmws.common.payloads;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;
import me.brynview.navidrohim.jmws.common.utils.CommonUtils;

import java.util.List;

public class JMWSActionPayload {
    public static final String CHANNEL = Constants.MODID + ":action_command";
    public static final int PACKET_SIZE = 2_097_000;

    public String rawData = null;
    public CommandFactory.Commands command = null;
    public List<JsonElement> argumentList = null;

    public JMWSActionPayload(byte[] message) {
        this(PaperPayloadCodec.readUtf(message, PACKET_SIZE));
    }

    public JMWSActionPayload(String jsonData) {
        if (PACKET_SIZE >= jsonData.getBytes().length) {
            rawData = jsonData;
        } else {
            Constants.getLogger().error("Packet too big! User may have too many waypoints and or groups!");
            rawData = CommandFactory.makeClientAlertRequestJson("error.jmws.error_packet_size", true, MessageType.FAILURE);
        }
    }

    public byte[] toByteArray() {
        return PaperPayloadCodec.writeUtf(rawData, PACKET_SIZE);
    }

    private void setCommandAndArguments() {
        JsonObject jsonified = CommonUtils.parseStringToJsonObject(rawData);

        command = CommandFactory.Commands.valueOf(jsonified.asMap().get("command").getAsString());
        argumentList = jsonified.asMap().get("arguments").getAsJsonArray().asList();
    }

    public CommandFactory.Commands command() {
        setCommandAndArguments();
        return command;
    }

    public List<JsonElement> arguments() {
        setCommandAndArguments();
        return argumentList;
    }
}
