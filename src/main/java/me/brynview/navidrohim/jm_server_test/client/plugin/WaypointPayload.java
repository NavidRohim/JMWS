package me.brynview.navidrohim.jm_server_test.client.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.brynview.navidrohim.jm_server_test.JMServerTest;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record WaypointPayload(String jsonData) implements CustomPayload {

    public static final Identifier packetIdentifier = Identifier.of(JMServerTest.MODID, "waypoint_send");
    public static final CustomPayload.Id<WaypointPayload> ID = new CustomPayload.Id<>(packetIdentifier);
    public static final PacketCodec<RegistryByteBuf, WaypointPayload> CODEC = PacketCodec.ofStatic(
            (buf, waypoint) -> {
                buf.writeString(waypoint.jsonData);
            },
            buf -> {
                return new WaypointPayload(buf.readString(32767));
            }
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public Map<String, String> getJsonData() {
        ObjectMapper jsonMap = new ObjectMapper();
        Map<String, String> waypointData = new HashMap<>();

        try {
            waypointData = jsonMap.readValue(jsonData, Map.class);
            waypointData.replace("data", jsonMap.readValue((String) waypointData.get("data"), Map.class).toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return waypointData;
    }

    public SavedWaypoint getSavedWaypoint() {
        return new SavedWaypoint(this);
    }

}

