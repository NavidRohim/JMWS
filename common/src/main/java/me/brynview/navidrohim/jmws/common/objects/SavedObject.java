package me.brynview.navidrohim.jmws.common.objects;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.io.UserSharingFile;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Dataclass to hold groups and waypoints from server. This is old code, so I wouldn't mess with it.
 */
public class SavedObject implements PossessesIdentifier {

    public static class SyncingInformation
    {
        public String objectIdentifier;
        protected List<UUID> sharedTo;

        @Nullable
        protected SavedObject parentObject = null;

        public SyncingInformation(List<UUID> sharedTo, String identifier)
        {
            this.objectIdentifier = identifier;
            this.sharedTo = sharedTo;
        }

        public static SavedObject.SyncingInformation getSyncingInfo(SavedObject object)
        {
            try {
                Gson gson = new Gson();
                SyncingInformation syncingInformation = gson.fromJson(object.getCustomData(), SyncingInformation.class);
                syncingInformation.parentObject = object;

                return syncingInformation;
            } catch (IllegalStateException | JsonSyntaxException readerr)
            {
                return transition(object); // if old waypoint is present
                //PlayerNetworkingHelper.sendUserMessage(object.ownerUUID, "fatal.jmws.server_mismatch", false, true);
            }
        }

        private static SyncingInformation transition(SavedObject object)
        {
            // TODO transition to new customDataField
            object.customData = SyncingInformation.getEmptySyncingInfoString(object.getCustomData());
            return getSyncingInfo(object);
        }

        public static SavedObject.SyncingInformation getSyncingInfo(String customDataField)
        {
            try
            {
                Gson gson = new Gson();
                return gson.fromJson(customDataField, SyncingInformation.class);
            } catch (JsonSyntaxException syntaxException) // will throw if object hasn't been ported.
            {
                return getSyncingInfo(getEmptySyncingInfoString(customDataField));
            }
        }

        public static String getEmptySyncingInfoString(String objectIdentifier)
        {
            Gson gson = new Gson();
            return gson.toJson(new SyncingInformation(List.of(), objectIdentifier));
        }

        public void addUserToShare(UUID playerUUID)
        {
            this.sharedTo.add(playerUUID);
            this.update();
        }

        public void removeUserFromShare(UUID playerUUID)
        {
            this.sharedTo.remove(playerUUID);
            this.update();
        }

        private void update()
        {
            if (this.parentObject != null)
            {
                Gson gson = new Gson();
                String jsonString = gson.toJson(this, SyncingInformation.class);
                JsonObject customData = this.parentObject.getRawJson().getAsJsonObject("customData");

                customData.remove("sharedTo");
                customData.add("sharedTo", gson.toJsonTree(sharedTo, JsonArray.class));

                this.parentObject.update(this.parentObject.getRawJson().getAsString());
            }
            else {
                throw new RuntimeException("Cannot update object from dataclass instance of SyncingInformation. Get instance of SyncingInformation from child of SavedObject. (SavedObject.syncing.update())");
            }
        }
    }

    String rawPacketData;
    String name;
    String customData;
    String groupIdentifier;
    JsonObject payload;

    public UserSharingFile ownerSharing;
    public SyncingInformation syncing;
    public static FetchType objectType = FetchType.GENERIC;

    UUID ownerUUID;

    public SavedObject(JsonObject payload, UUID playerUUID)
    {
        this.payload = payload;
        this.customData = payload.get("customData").getAsString();
        this.ownerSharing = new UserSharingFile(playerUUID);
        this.syncing = SyncingInformation.getSyncingInfo(this);
        this.ownerUUID = playerUUID;
    }

    public String getName() { return this.name; }
    public String getCustomData() { return this.customData; }
    public String getGroupIdentifier() { return this.groupIdentifier; }

    public String getRawString() { return this.payload.toString();}
    public JsonObject getRawJson() { return this.payload;}

    public FetchType getObjectType()
    {
        return objectType;
    }

    public Path getObjectPath() { return JMWSServerIO.Utils.getNewObjectFilename(this.ownerUUID, this.syncing.objectIdentifier, getObjectType());}

    public static void removeWaypointFromUser(UUID playerUUID, String objectIdentifier)
    {
        UserSharingFile.removeObjectFromUser(playerUUID, objectIdentifier);
    }

    /**
     * TO NOTE; SERVER SIDE ONLY
     */
    public void removeWaypointFromUsers()
    {
        for (UUID userUUID : this.syncing.sharedTo)
        {
            removeWaypointFromUser(userUUID, this.syncing.objectIdentifier);
        }
    }

    public boolean delete()
    {
        return CommonHelper.deleteFile(JMWSServerIO.Utils.getNewObjectFilename(this.ownerUUID, this.syncing.objectIdentifier, this.getObjectType()));
    }

    public void update(String data)
    {
        if (CommonHelper.fileExists(this.getObjectPath()))
        {
            try (FileWriter objWriter = new FileWriter(this.getObjectPath().toFile()))
            {
                objWriter.write(data);
            } catch (IOException ioException)
            {
                Constants.getLogger().error("Error on server when trying to process %s from %s ERROR: %s".formatted(getObjectType(), this.ownerUUID, ioException.toString()));
            }
        }
    }

    public boolean create()
    {
        Path waypointFilePath = JMWSServerIO.Utils.getNewObjectFilename(ownerUUID, this.syncing.objectIdentifier, this.getObjectType());

        try {
            Files.createFile(waypointFilePath);
            FileWriter waypointFileWriter = new FileWriter(waypointFilePath.toFile());
            waypointFileWriter.write(this.getRawString());
            waypointFileWriter.close();

            return true;

        } catch (NoSuchFileException noSuchFileException) {
            CommonClass._createServerResources();
            Constants.getLogger().warn("`jmws` folder was not found so another was made (%s error)".formatted(getObjectType()));
            return create();

        } catch (FileSystemException missingPerms) {
            Constants.getLogger().error("JMWS is missing write permissions to \"jmws\" folder. (%s error)".formatted(getObjectType()));
            return false;

        } catch (IOException genericIOError) {
            Constants.getLogger().error("Got exception trying to make %s -> ".formatted(getObjectType()) + genericIOError);
            return false;
        }
    }
}
