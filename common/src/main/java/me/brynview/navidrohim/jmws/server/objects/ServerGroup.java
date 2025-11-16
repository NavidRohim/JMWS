package me.brynview.navidrohim.jmws.server.objects;
import com.google.gson.JsonObject;
import me.brynview.navidrohim.jmws.common.enums.FetchType;

import java.util.UUID;


/**
 * Dataclass that holds a synced group from the server. Contains data to make a local group.
 */
public class ServerGroup extends ServerObject {

    public static FetchType objectType = FetchType.GROUP;

    public ServerGroup(JsonObject payload, UUID playerUUID) {
        super(payload, playerUUID);

        this.rawPacketData = payload.toString();
        this.groupIdentifier = payload.get("guid").getAsString();
    }

    @Override
    public FetchType getObjectType()
    {
        return objectType;
    }
}
