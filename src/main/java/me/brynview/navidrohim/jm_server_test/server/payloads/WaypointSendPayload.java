package me.brynview.navidrohim.jm_server_test.server.payloads;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brynview.navidrohim.jm_server_test.JMServerTest;
import me.brynview.navidrohim.jm_server_test.client.payloads.WaypointPayloadOutbound;
import me.brynview.navidrohim.jm_server_test.common.SavedWaypoint;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record WaypointSendPayload(String waypointJsonPayload) implements CustomPayload {

    public static final Identifier packetIdentifier = Identifier.of(JMServerTest.MODID, "waypoint_outbound");
    public static final Id<WaypointSendPayload> ID = new Id<>(packetIdentifier);
    public static final PacketCodec<RegistryByteBuf, WaypointSendPayload> CODEC = PacketCodec.ofStatic(
            (buf, waypoint) -> {
                buf.writeString(waypoint.waypointJsonPayload);
            },
            buf -> new WaypointSendPayload(buf.readString(32767))
    );
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public Map<String, String> getJsonData() {
        ObjectMapper jsonMap = new ObjectMapper();
        Map<String, String> waypointData;

        try {
            waypointData = jsonMap.readValue(this.waypointJsonPayload, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return waypointData;
    }
    public List<SavedWaypoint> getSavedWaypoints() {
        List<SavedWaypoint> waypoints = new ArrayList<>();
        for (Map.Entry<String, String> wpEntry : this.getJsonData().entrySet()) {
            JsonObject waypointJson = JsonParser.parseString(getJsonData().get(wpEntry.getKey())).getAsJsonObject();

            JMServerTest.LOGGER.info("LOKKKO > > " + waypointJson.get("x").getAsString());

            SavedWaypoint wp = new SavedWaypoint(waypointJson);
            waypoints.add(wp);
        }
        return waypoints;
        
    }
}
