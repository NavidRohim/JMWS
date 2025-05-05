package me.brynview.navidrohim.jm_server.common.payloads;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.brynview.navidrohim.jm_server.JMServerTest;
import me.brynview.navidrohim.jm_server.common.SavedWaypoint;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record RegisterUserPayload(String jsonData) implements CustomPayload {

    public static final Identifier packetIdentifier = Identifier.of(JMServerTest.MODID, "waypoint_send");
    public static final CustomPayload.Id<RegisterUserPayload> ID = new CustomPayload.Id<>(packetIdentifier);
    public static final PacketCodec<RegistryByteBuf, RegisterUserPayload> CODEC = PacketCodec.ofStatic(
            (buf, waypoint) -> {
                buf.writeString(waypoint.jsonData);
            },
            buf -> {
                return new RegisterUserPayload(buf.readString(32767));
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
            waypointData = jsonMap.readValue(this.jsonData(), Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return waypointData;
    }

    public SavedWaypoint getSavedWaypoint() {
        return new SavedWaypoint(this);
    }
}

