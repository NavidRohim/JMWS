package me.brynview.navidrohim.jmws.client.objects;
import com.google.gson.JsonObject;

public class SavedGroup extends SavedObject {
    public SavedGroup(JsonObject payload) {
        this.rawPacketData = payload.toString();
        this.universalIdentifier = payload.get("customData").getAsString();
        this.groupIdentifier = payload.get("guid").getAsString();
        this.name = payload.get("name").getAsString();
    }
}
