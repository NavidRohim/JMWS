package me.brynview.navidrohim.jmws.common.payloads;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.brynview.navidrohim.jmws.JMWS;
import me.brynview.navidrohim.jmws.common.enums.WaypointPayloadCommand;
import me.brynview.navidrohim.jmws.common.helpers.JsonStaticHelper;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.List;

public class JMWSActionPacket implements FabricPacket {
    public static final PacketType<JMWSActionPacket> TYPE = PacketType.create(
            new Identifier(JMWS.MODID, "waypoint_action"),
            JMWSActionPacket::new
    );
    private final String rawByteBuffer;

    public JMWSActionPacket(PacketByteBuf buf) {
        this.rawByteBuffer = buf.readString();
    }

    public JMWSActionPacket(String stringPacketByteBuf) {
        this.rawByteBuffer = stringPacketByteBuf;
    }

    public JsonObject getJsonObject() {
        return JsonStaticHelper.getJsonObjectFromJsonString(this.rawByteBuffer);
    }
    @Override
    public void write(PacketByteBuf packetByteBuf) {
        packetByteBuf.writeString(this.rawByteBuffer);
    }
    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public WaypointPayloadCommand command() {
        return WaypointPayloadCommand.valueOf(this.getJsonObject().asMap().get("command").getAsString());
    }

    public List<JsonElement> arguments() {
        return this.getJsonObject().asMap().get("arguments").getAsJsonArray().asList();
    }
}
