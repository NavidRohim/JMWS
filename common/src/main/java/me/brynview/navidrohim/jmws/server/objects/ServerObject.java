package me.brynview.navidrohim.jmws.server.objects;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.io.UserSharingFile;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import net.minecraft.server.level.ServerPlayer;
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
public class ServerObject implements PossessesIdentifier {

    public static class SyncingInformation
    {

        @Expose
        public String objectIdentifier;

        @Expose
        protected List<String> sharedTo;

        @Expose
        protected UUID owner;

        @Nullable
        protected ServerObject parentObject = null;

        public SyncingInformation(List<String> sharedTo, String identifier, UUID owner)
        {
            this.objectIdentifier = identifier;
            this.sharedTo = sharedTo;
            this.owner = owner;
        }

        public static ServerObject.SyncingInformation getSyncingInfo(ServerObject object)
        {
            try {
                Gson gson = new Gson();
                SyncingInformation syncingInformation = gson.fromJson(object.getCustomData(), SyncingInformation.class);
                syncingInformation.parentObject = object;

                return syncingInformation;
            } catch (IllegalStateException | JsonSyntaxException reader)
            {
                return transition(object); // if old waypoint is present
                //PlayerNetworkingHelper.sendUserMessage(object.ownerUUID, "fatal.jmws.server_mismatch", false, true);
            }
        }

        private static SyncingInformation transition(ServerObject object)
        {
            // TODO transition to new customDataField
            object.customData = SyncingInformation.getEmptySyncingInfoString(object.getCustomData(), object.ownerUUID);
            return getSyncingInfo(object);
        }

        public static ServerObject.SyncingInformation getSyncingInfo(String customDataField)
        {
            try
            {
                Gson gson = new Gson();
                return gson.fromJson(customDataField, SyncingInformation.class);
            } catch (JsonSyntaxException syntaxException) // will throw if object hasn't been ported.
            {
                UUID owner = CommonClass.minecraftClientInstance.player.getUUID();
                return getSyncingInfo(getEmptySyncingInfoString(customDataField, owner));
            }
        }

        public static String getEmptySyncingInfoString(String objectIdentifier, UUID owner)
        {
            Gson gson = new Gson();
            return gson.toJson(new SyncingInformation(List.of(), objectIdentifier, owner));
        }

        public void addUserToShare(UUID playerUUID)
        {
            this.sharedTo.add(playerUUID.toString());
            this.update();
        }

        public void removeUserFromShare(String playerUUID)
        {
            this.sharedTo.remove(playerUUID);
            this.update();
        }

        public boolean isOwner(UUID supposedOwner)
        {
            return this.owner.equals(supposedOwner);
        }

        private void update()
        {
            if (this.parentObject != null)
            {
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String jsonString = gson.toJson(this, SyncingInformation.class);
                this.parentObject.getRawJson().add("customData", new JsonPrimitive(jsonString));

                this.parentObject.update(this.parentObject.getRawJson().getAsJsonObject().toString(), true); // TODO: bug test more. This seems very janky and not done right. Will test more
            }
            else {
                throw new RuntimeException("Cannot update object from dataclass instance of SyncingInformation. Get instance of SyncingInformation from child of SavedObject. (SavedObject.syncing.update())");
            }
        }

        public void syncToUsers()
        {
            for (String playerUUID : this.sharedTo)
            {
                ServerPlayer sharedUser = CommonClass.minecraftServerInstance.getPlayerList().getPlayer(UUID.fromString(playerUUID));

                if (sharedUser != null)
                {
                    ServerPacketHandler.sendUserSync(sharedUser, false, false, false);
                }
            }
        }
    }

    String rawPacketData;
    String name;
    String customData;
    String groupIdentifier;
    JsonObject payload;
    boolean dataclass;

    public UserSharingFile accessorSharing;
    public SyncingInformation syncing;
    public static FetchType objectType = FetchType.GENERIC;

    @Nullable
    public Path objectPath;

    UUID ownerUUID;

    public ServerObject(JsonObject payload, UUID playerUUID, boolean dataclass)
    {
        this.dataclass = dataclass;
        this.payload = payload;
        this.customData = payload.get("customData").getAsString(); // bug with json formatting
        this.syncing = SyncingInformation.getSyncingInfo(this);
        this.ownerUUID = playerUUID;
        this.name = payload.get("name").getAsString();

        this.accessorSharing = !dataclass ? new UserSharingFile(playerUUID) : null;
        this.objectPath = !dataclass ? JMWSServerIO.Utils.getNewObjectFilename(this.syncing.owner, this.syncing.objectIdentifier, getObjectType()) : null;
    }

    public ServerObject(JsonObject payload, UUID playerUUID)
    {
        this(payload, playerUUID, false);
    }

    public String getName() { return this.name; }
    public String getCustomData() { return this.customData; }

    public void setCustomData(String data)
    {
        this.customData = data;
        this.payload.add("customData", new JsonPrimitive(data));
    }

    public String getGroupIdentifier() { return this.groupIdentifier; }

    public String getRawString() { return this.payload.toString();}
    public JsonObject getRawJson() { return this.payload;}

    public FetchType getObjectType()
    {
        return objectType;
    }

    @Nullable
    public Path getObjectPath()
    {
        return objectPath;
    }

    public Boolean hasFile()
    {
        return this.getObjectPath() != null && CommonHelper.fileExists(this.getObjectPath());
    }

    public void removeWaypointFromUser(UUID playerUUID, String objectIdentifier)
    {
        UserSharingFile.removeObjectFromUser(playerUUID, objectIdentifier);
        ServerPlayer sharedPlayer = CommonClass.minecraftServerInstance.getPlayerList().getPlayer(playerUUID);
        if (sharedPlayer != null)
        {
            Dispatcher.sendToClient(new JMWSActionPayload(CommandFactory.makeDeleteRequestJson(objectIdentifier, true, false)), sharedPlayer);
        }
    }

    public boolean delete()
    {
        if (this.objectPath != null && !dataclass)
        {
            return CommonHelper.deleteFile(this.objectPath);
        }
        return false;
    }

    public void update(String data, boolean updateSyncInfo)
    {
        if (!updateSyncInfo) {
            ServerObject newChange = new ServerObject(JsonParser.parseString(data).getAsJsonObject(), this.ownerUUID, true);
            newChange.setCustomData(this.getCustomData());
            data = newChange.toString();
        }

        if (this.hasFile() && !dataclass)
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

        if (!this.hasFile() && !dataclass)
        {
            try {
                Path waypointFilePath = this.getObjectPath();

                if (waypointFilePath != null)
                {
                    Files.createFile(waypointFilePath);
                    FileWriter waypointFileWriter = new FileWriter(waypointFilePath.toFile());
                    waypointFileWriter.write(this.getRawString());
                    waypointFileWriter.close();

                    return true;
                } else {
                    PlayerNetworkingHelper.sendUserMessage(this.ownerUUID, "error.jmws.invalid_name", false, JMWSMessageType.FAILURE);
                    return false;
                }

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
        return false;
    }

    public void stopSharing(UUID user)
    {
        this.syncing.removeUserFromShare(String.valueOf(user));
        this.accessorSharing.removeFromShared(this.syncing.objectIdentifier);
    }

    public void stopSharing()
    {
        for (String userUUID : this.syncing.sharedTo)
        {
            removeWaypointFromUser(UUID.fromString(userUUID), this.syncing.objectIdentifier);
        }
    }

    @Override
    public String toString()
    {
        return payload.toString();
    }
}
