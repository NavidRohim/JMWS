package me.brynview.navidrohim.jmws.common.payloads;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;

public class JMWSHandshakePayload {
    public static final String CHANNEL = Constants.MODID + ":jmws_handshake";

    public String serverConfigDataJson;

    public JMWSHandshakePayload() {
        JsonObject jsonObject = JsonParser.parseString(ServerConfig.rawServerConfigData).getAsJsonObject();
        jsonObject.addProperty("serverVersion", Constants.SERVER_VERSION);

        serverConfigDataJson = jsonObject.toString();
    }

    public byte[] toByteArray() {
        return PaperPayloadCodec.writeUtf(serverConfigDataJson, 512);
    }
}
