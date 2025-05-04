package me.brynview.navidrohim.jm_server_test.common.payloads;

import com.google.gson.JsonObject;
import me.brynview.navidrohim.jm_server_test.JMServerTest;
import me.brynview.navidrohim.jm_server_test.common.utils.JsonStaticHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WaypointActionPayload(String jsonAction) implements CustomPayload {

    public static final Identifier packetIdentifier = Identifier.of(JMServerTest.MODID, "waypoint_action");
    public static final Id<WaypointActionPayload> ID = new Id<>(packetIdentifier);
    public static final PacketCodec<RegistryByteBuf, WaypointActionPayload> CODEC = PacketCodec.of(WaypointActionPayload::write, WaypointActionPayload::new);

    public WaypointActionPayload(PacketByteBuf buf) {
        this(buf.readString());
    }

    public void write(RegistryByteBuf registryByteBuf) {
        registryByteBuf.writeString(jsonAction);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public JsonObject getJsonObject() {
        return JsonStaticHelper.getJsonObjectFromJsonString(jsonAction);
    }
}
