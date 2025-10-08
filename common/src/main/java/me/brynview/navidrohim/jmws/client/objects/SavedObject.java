package me.brynview.navidrohim.jmws.client.objects;

/**
 * Dataclass to hold groups and waypoints from server. This is old code, so I wouldn't mess with it.
 */
public class SavedObject {
    String rawPacketData;
    String name;
    String universalIdentifier;
    String groupIdentifier;

    public String getRawPacketData() { return this.rawPacketData; }
    public String getName() { return this.name; }
    public String getUniversalIdentifier() { return this.universalIdentifier; }
    public String getGroupIdentifier() { return this.groupIdentifier; }
}
