package me.brynview.navidrohim.jmws.common;

import com.google.gson.JsonObject;
import me.brynview.navidrohim.jmws.JMServer;

public class SavedGroup {

    String rawPacketData;
    String groupName;
    String universalIdentifier;
    String groupIdentifier;

    public SavedGroup(JsonObject payload) {

        JsonObject iconSettings = payload.get("icon").getAsJsonObject();

        this.rawPacketData = payload.toString();
        this.universalIdentifier = payload.get("customData").getAsString();
        this.groupIdentifier = payload.get("guid").getAsString();
        this.groupName = payload.get("name").getAsString();
    }

    public String getGroupName()
    {
        return this.groupName;
    }

    public String getRawPacketData()
    {
        return this.rawPacketData;
    }

    public String getUniversalIdentifier()
    {
        return this.universalIdentifier;
    }

    public String getGroupIdentifier()
    {
        return this.groupIdentifier;
    }
}
