package me.brynview.navidrohim.jmws.server.objects;

import com.google.gson.*;
import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.api.PossessesIdentifier;
import me.brynview.navidrohim.jmws.common.api.Synchronizable;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.enums.ShareRequestDirection;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.syncing.SyncUtils;
import me.brynview.navidrohim.jmws.common.syncing.Syncing;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.io.UserSharingFile;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerObject extends LegacyObject implements Synchronizable, PossessesIdentifier {

    String name;
    String groupIdentifier; // TODO: remove?

    public UserSharingFile accessorSharing;
    public Syncing syncing;

    public boolean dataclass;
    public static ObjectType objectType = ObjectType.GENERIC;

    @Nullable
    private Path currentObjectPath;

    @Nullable
    public final Path globalObjectPath;

    @Nullable
    private final Path normalObjectPath;

    protected final UUID ownerUUID;

    // Constructors. Both are private access, but you aren't supposed to instantiate this class anyway.
    // Use ServerWaypoint or ServerGroup.

    private ServerObject(JsonObject payload, UUID playerUUID, boolean dataclass) {
        super(payload);

        this.dataclass = dataclass;

        this.ownerUUID = playerUUID; // Note; if you set ownerUUID before this.syncing is defined, it enables some sort of compatibility for legacy clients. But I've left it as-is to avoid chaos.
        this.syncing = SyncUtils.getSyncingInfo(this);

        this.name = payload.get("name").getAsString();
        this.accessorSharing = !dataclass ? new UserSharingFile(playerUUID) : null;

        this.globalObjectPath = !dataclass ? Path.of(ObjectType.getPathLocationPrefix(this.getObjectType()) + JMWSServerIO.PathUtils.makeFilename(this.syncing.objectIdentifier, this.ownerUUID, true)) : null;
        this.normalObjectPath = !dataclass ? JMWSServerIO.PathUtils.getObjectFilename(this.syncing.getOwner(), this.syncing.objectIdentifier, getObjectType(), false) : null;
        this.groupIdentifier = payload.get("guid").getAsString();

        if (!dataclass) {
            this.currentObjectPath = !syncing.isGlobal() ? normalObjectPath : globalObjectPath;
            if (this.didTransitionToNewData) // If true, means object was using old customData.
            {
                this.update();
                Constants.getLogger().info("Transitioned old customData for object '%s' field to new customDataMap Hashmap (ID: %s). You can ignore this.".formatted(this.name, this.syncing.objectIdentifier));
            }
        }
    }

    protected ServerObject(JsonObject payload, UUID playerUUID) {
        this(payload, playerUUID, false);
    }

    // Global

    @Override // From syncable
    public void makeGlobal() {
        File oldNameFile = new File(this.getCurrentObjectPath().toString());
        File newFileName = new File(this.getGlobalObjectPath().toString());
        oldNameFile.renameTo(newFileName);

        this.currentObjectPath = getGlobalObjectPath();
        this.syncing.setGlobal(true);
    }

    @Override // From syncable
    public void removeGlobal() {
        File oldNameFile = new File(this.getCurrentObjectPath().toString());
        File newFileName = new File(this.getNormalObjectPath().toString());
        this.currentObjectPath = getNormalObjectPath();

        oldNameFile.renameTo(newFileName);
        this.syncing.setGlobal(false);
    }

    // Sharing

    @Override
    public void shareWith(UUID toUser) {

        Dispatcher.sendToClient(new JMWSActionPayload(CommandFactory.makeObjectShareRequestForUser(this.rawPacketData, this.ownerUUID, toUser, ShareRequestDirection.FOR_CLIENT, getObjectType())), CommonClass.minecraftServerInstance.getPlayerList().getPlayer(toUser)); // Send shareWith request to player
        // Send information of the shareWith to the sender. This is needed because this command is server-side only and the client will have no knowledge of the shared obj.
        Dispatcher.sendToClient(new JMWSActionPayload(CommandFactory.makeObjectShareRequestForUser(this.rawPacketData, toUser, this.ownerUUID, ShareRequestDirection.FOR_HOST, getObjectType())), CommonClass.minecraftServerInstance.getPlayerList().getPlayer(this.ownerUUID));
    }

    @Override
    public void stopSharingWith(UUID user) {
        this.syncing.removeUserFromShare(user);
        this.accessorSharing.removeFromShared(this.syncing.objectIdentifier, getObjectType());
    }

    @Override
    public void stopSharingWithAll() {
        for (String userUUID : this.syncing.sharedTo) {
            JMWSServerIO.removeObjectFromUser(this, UUID.fromString(userUUID), this.syncing.objectIdentifier, this.getObjectType());
        }
        this.syncing.removeAllFromShare();
    }

    // General server operations

    public boolean delete(boolean stopSharing) {
        if (this.currentObjectPath != null && !dataclass) {
            if (stopSharing) {
                this.stopSharingWithAll();
            }
            return CommonHelper.deleteFile(this.getCurrentObjectPath());
        }
        return false;
    }

    public static boolean deleteAll(UUID user, ObjectType deletionType) {
        List<Boolean> deletionStatusList = new ArrayList<>();

        for (Path waypointPath : JMWSServerIO.getObjectPathsForUser(user, deletionType)) {
            deletionStatusList.add(JMWSServerIO.getObjectFromFile(waypointPath, user, deletionType).delete(true));
        }

        return deletionStatusList.isEmpty() || deletionStatusList.stream().allMatch(deletionStatusList.getFirst()::equals);
    }

    public void update(String data, boolean updateSyncInfo) {
        if (!updateSyncInfo) {
            ServerObject newChange = new ServerObject(JsonParser.parseString(data).getAsJsonObject(), this.ownerUUID, true);
            newChange.setSyncedCustomData(this.getOldCustomData());
            data = newChange.toString();
        }

        if (this.hasFile() && !dataclass) {
            try (FileWriter objWriter = new FileWriter(this.getCurrentObjectPath().toFile())) {
                objWriter.write(data);
            } catch (IOException ioException) {
                Constants.getLogger().error("Error on server when trying to process %s from %s ERROR: %s".formatted(getObjectType(), this.ownerUUID, ioException.toString()));
            }
        }
    }

    public void update() {
        update(this.getRawJson().toString(), true);
    }

    public boolean create() {

        if (!this.hasFile() && !dataclass) {
            try {
                Path waypointFilePath = this.getCurrentObjectPath();
                if (waypointFilePath != null) {
                    Files.createFile(waypointFilePath);
                    FileWriter waypointFileWriter = new FileWriter(waypointFilePath.toFile());
                    waypointFileWriter.write(this.getRawString());
                    waypointFileWriter.close();

                    return true;
                } else {
                    PlayerNetworkingHelper.sendUserMessage(this.ownerUUID, "error.jmws.invalid_name", false, MessageType.FAILURE);
                    return false;
                }

            } catch (NoSuchFileException noSuchFileException) {
                CommonClass.createServerResources();
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

    // Getters

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    // From PossessesIdentifier
    @Override
    public String getName() {
        return this.name;
    }

    // From PossessesIdentifier
    @Override
    public String getSyncedCustomData() {
        return this.customData;
    }

    // From PossessesIdentifier
    @Override
    public String getGroupIdentifier() {
        return this.groupIdentifier;
    } // No usages but may be used elsewhere like with generics not sure

    // From PossessesIdentifier
    @Override
    public ObjectType getObjectType() {
        return objectType;
    }

    public String getRawString() {
        return this.payload.toString();
    }

    public JsonObject getRawJson() {
        return this.payload;
    }

    @Override
    public Syncing getSyncingHandler() {
        return syncing;
    }

    @Nullable
    public Path getCurrentObjectPath() {
        return this.currentObjectPath;
    }

    @Nullable
    public Path getGlobalObjectPath() {
        return globalObjectPath;
    }

    @Nullable
    public Path getNormalObjectPath() {
        return normalObjectPath;
    }

    @Nullable
    public String getDifferentiator() {
        return "Object";
    }

    public String getObjectNonDuplicateIdentifier()
    {
        return "%s (%s)".formatted(this.getName(), this.getDifferentiator());
    }

    @Override
    public String toString() {
        return payload.toString();
    }

    public Boolean hasFile() {
        return this.getCurrentObjectPath() != null && CommonHelper.fileExists(this.getCurrentObjectPath());
    }
}
