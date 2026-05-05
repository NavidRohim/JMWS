package me.brynview.navidrohim.jmws.server.objects;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.api.PossessesIdentifier;
import me.brynview.navidrohim.jmws.common.api.Synchronizable;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.utils.CommonUtils;
import me.brynview.navidrohim.jmws.server.JMWSServerCommon;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.io.UserSharingFile;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.syncing.ServerSyncInformation;
import me.brynview.navidrohim.jmws.server.syncing.ServerSyncingInformationWrapper;
import me.brynview.navidrohim.jmws.server.syncing.registry.ServerSyncRegistry;
import me.brynview.navidrohim.jmws.server.syncing.registry.ServerSyncRegistryEntry;
import me.brynview.navidrohim.jmws.server.syncing.rules.api.ShareRule;
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
    
    protected static ServerSyncRegistryEntry<?> serverSyncRegistry = ServerSyncRegistry.GENERIC;
    protected final UserSharingFile accessorSharing;
    protected ServerSyncingInformationWrapper serverSyncingHandlerNative;
    protected ImmutableList<ShareRule> rules;

    private final String name;
    @Nullable private Path currentObjectPath;
    @Nullable private final Path globalObjectPath;
    @Nullable private final Path normalObjectPath;

    protected final UUID ownerUUID;
    public boolean dataclass;

    private ServerObject(JsonObject payload, UUID playerUUID, boolean dataclass) {
        super(payload);

        this.dataclass = dataclass;

        this.serverSyncingHandlerNative = ServerSyncingInformationWrapper.getSyncingHandlerFromServerObject(this, this.customData);
        this.rules = this.getRuleset(this);

        this.ownerUUID = getServerSyncingHandler().info.owner;
        this.name = payload.get("name").getAsString();
        this.accessorSharing = !dataclass ? new UserSharingFile(playerUUID) : null;

        this.globalObjectPath = !dataclass ? Path.of(this.getObjectType().getRegistryPath() + JMWSServerIO.PathUtils.makeFilename(this.getServerSyncingHandler().info.objectIdentifier, this.ownerUUID, true)) : null;
        this.normalObjectPath = !dataclass ? JMWSServerIO.PathUtils.getObjectFilename(this.getServerSyncingHandler().getOwner(), this.getServerSyncingHandler().info.objectIdentifier, getObjectType(), false) : null;

        if (!dataclass) {
            this.currentObjectPath = !getServerSyncingHandler().isGlobal() ? normalObjectPath : globalObjectPath;
            Constants.LoggerHolder.debug("Current object path: %s".formatted(this.currentObjectPath), "Current object path");
            if (this.didTransitionToNewData) // If true, means object was using old customData.
            {
                this.update();
                Constants.getLogger().info("Transitioned old customData for object '%s' field to new customDataMap Hashmap (ID: %s). You can ignore this.".formatted(this.name, this.getServerSyncingHandler().info.objectIdentifier));
            }
        }
    }

    protected ServerObject(JsonObject payload, UUID playerUUID) {
        this(payload, playerUUID, false);
    }

    // Global


    @Override // From Synchronizable
    public void makeGlobal() {
        File oldNameFile = new File(this.getCurrentObjectPath().toString());
        File newFileName = new File(this.getGlobalObjectPath().toString());
        oldNameFile.renameTo(newFileName);

        this.currentObjectPath = getGlobalObjectPath();
        this.getServerSyncingHandler().setGlobal(true);
    }

    @Override // From Synchronizable
    public void removeGlobal() {
        File oldNameFile = new File(this.getCurrentObjectPath().toString());
        File newFileName = new File(this.getNormalObjectPath().toString());
        this.currentObjectPath = getNormalObjectPath();

        oldNameFile.renameTo(newFileName);
        this.getServerSyncingHandler().setGlobal(false);
    }

    @Override // From Synchronizable
    public boolean isGlobal()
    {
        return this.getServerSyncingHandler().isGlobal();
    }

    // Sharing

    @Override // From Synchronizable
    public void stopSharingWith(UUID user)
    {
        this.getServerSyncingHandler().removeUserFromShare(user);
        this.accessorSharing.removeFromShared(this.getServerSyncingHandler().info.objectIdentifier, getObjectType());
    }

    @Override // From Synchronizable
    public void stopSharingWithAll()
    {
        for (UUID userUUID : this.getServerSyncingHandler().info.sharedTo)
        {
            JMWSServerIO.removeObjectFromUser(this, userUUID, this.getServerSyncingHandler().info.objectIdentifier, this.getObjectType());
        }
        this.getServerSyncingHandler().removeAllFromShare();
    }

    // General server-only operations
    
    public boolean delete(boolean stopSharing) {
        if (this.currentObjectPath != null && !dataclass)
        {
            if (stopSharing) {this.stopSharingWithAll();}
            return CommonUtils.deleteFile(this.getCurrentObjectPath());
        }
        return false;
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
                    Constants.LoggerHolder.debug(waypointFilePath, "PATH");
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
                JMWSCommon.createServerResources();
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
        Constants.getLogger().warn("Could not create server object. hasFile {} (should be false) dataclass {} (should be false)", hasFile(), dataclass);
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

    @Override
    public ServerSyncInformation getInfo()
    {
        return this.getServerSyncingHandler().info;
    }

    public ServerSyncingInformationWrapper getServerSyncingHandler()
    {
        return serverSyncingHandlerNative;
    }

    public ImmutableList<ShareRule> getRules()
    {
        // Return a copy
        return ImmutableList.copyOf(this.rules);
    }

    public UserSharingFile getAccessorSharing()
    {
        return accessorSharing;
    }

    // From PossessesIdentifier
    public ServerSyncRegistryEntry<?> getObjectType() {
        return serverSyncRegistry;
    }

    @Override
    public String getRegistryTypeName()
    {
        return this.getObjectType().toString();
    }

    public String getRawString() {
        return this.payload.toString();
    }

    public JsonObject getRawJson() {
        return this.payload;
    }

    @Nullable
    public JsonElement getRulesetData()
    {
        return this.customDataJmwsFieldObject.get(Constants.RULESET_ID);
    }

    private ImmutableList<ShareRule> getRuleset(ServerObject serverObject)
    {
        JsonElement rulesetData = serverObject.getRulesetData();
        List<ShareRule> rules = new ArrayList<>();

        // iterate through all rules and return list of instantiated rules from registry
        if (rulesetData != null)
        {
            for (JsonElement rule : JsonParser.parseString(rulesetData.getAsString()).getAsJsonArray()) {
                ShareRule ruleInstance = JMWSServerCommon.SHARE_RULES.get(rule.getAsString());
                if (ruleInstance == null) {
                    Constants.getLogger().warn("Share rule with id '{}' does not exist, skipping.", rule.getAsString());
                    continue;
                }
                rules.add(ruleInstance);
            }
        }

        return ImmutableList.copyOf(rules);
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

    public Boolean hasFile()
    {
        return this.getCurrentObjectPath() != null && CommonUtils.fileExists(this.getCurrentObjectPath());
    }
    
    @Override
    public String toString() {
        return payload.toString();
    }
}
