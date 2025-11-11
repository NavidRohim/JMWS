package me.brynview.navidrohim.jmws.common.objects;

import com.google.gson.Gson;

import java.util.List;

/**
 * Dataclass to hold groups and waypoints from server. This is old code, so I wouldn't mess with it.
 */
public class SavedObject implements PossessesIdentifier {

    public static class SyncingInformation
    {
        public String objectIdentifier;
        public List<String> sharedTo;

        public SyncingInformation(List<String> sharedTo, String identifier)
        {
            this.objectIdentifier = identifier;
            this.sharedTo = sharedTo;
        }

        public static SavedObject.SyncingInformation getSyncingInfo(SavedObject object)
        {
            Gson gson = new Gson();
            return gson.fromJson(object.getCustomData(), SyncingInformation.class);
        }
    }

    String rawPacketData;
    String name;
    String customData;
    String groupIdentifier;

    public String getRawPacketData() { return this.rawPacketData; }
    public String getName() { return this.name; }
    public String getCustomData() { return this.customData; }
    public String getGroupIdentifier() { return this.groupIdentifier; }
}
