package me.brynview.navidrohim.jmws.server.syncing;

import com.google.gson.*;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.exceptions.NoInfoException;
import me.brynview.navidrohim.jmws.common.api.ServerSyncInformation;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.server.registry.ServerSyncRegistryEntry;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

import static me.brynview.navidrohim.jmws.client.syncing.ClientSyncUtils.syncInformationFromString;

public class ServerSyncingInformationWrapper {

    // TODO: Extend from SyncInformation? And no need for expose I don't believe.

    @Nullable
    ServerObject parentObject = null;
    public ServerSyncInformation info;

    public ServerSyncingInformationWrapper(String identifier, UUID owner, Set<UUID> sharedTo, boolean isGlobal, ServerSyncRegistryEntry syncRegistryType) {
        this.info = new ServerSyncInformation(identifier, owner, sharedTo, isGlobal, syncRegistryType);

    }

    private static ServerSyncingInformationWrapper getFromImpl(ServerSyncInformation impl, ServerObject obj)
    {
        ServerSyncingInformationWrapper info = new ServerSyncingInformationWrapper(impl.objectIdentifier, impl.owner, impl.sharedTo, impl.isGlobal, impl.syncRegistryType);
        info.parentObject = obj;

        return info;
    }

    public static ServerSyncingInformationWrapper getSyncingHandlerFromServerObject(ServerObject object, String syncData) {
        try {
            Constants.LoggerHolder.debug(syncData, "DATA BEFORE HANBDLER");
            ServerSyncInformation serverSyncingHandlerImpl = ServerSyncInformation.SYNC_DECODER.fromJson(syncData, ServerSyncInformation.class);

            return getFromImpl(serverSyncingHandlerImpl, object);
        } catch (NoInfoException e) {
            PlayerNetworkingHelper.sendUserMessage(object.getOwnerUUID(), "FATAL: You are on the wrong JMWS version! Update to JMWS v%s as soon as possible or you may suffer data loss!".formatted(Constants.SERVER_VERSION), false, MessageType.FAILURE);
            object.dataclass = true;
            return null;
        }
    }

    // Sharing

    public void addUserToShare(UUID playerUUID) {
        this.info.addUserToShare(playerUUID);
        this.update();
    }

    public void removeUserFromShare(UUID playerUUID) {
        this.info.removeUserFromShare(playerUUID);
        this.update();
    }

    public void removeAllFromShare() {
        this.info.removeAllFromShare();
        this.update();
    }

    // Global handling

    public void setGlobal(boolean global)
    {
        this.info.setGlobal(global);
        this.update();
    }


    public void setRegistry(@Nullable ServerSyncRegistryEntry registry) {
        this.info.setRegistry(registry);
        this.update();
    }

    public boolean isGlobal()
    {
        return this.info.isGlobal;
    }

    public boolean isOwner(UUID playerUUID)
    {
        return this.info.owner.equals(playerUUID);
    }

    public UUID getOwner()
    {
        return this.info.owner;
    }

    // IO (SERVER ONLY)

    private void update() {
        if (this.parentObject != null) {
            //System.out.println(this.info.serialize() + "SYNCING INFO");
            String jsonString = ServerSyncInformation.SYNC_DECODER.toJson(this.info, ServerSyncInformation.class);

            this.parentObject.getRawJson().get("customDataMap").getAsJsonObject().add(Constants.MODID, new JsonPrimitive(jsonString));
            this.parentObject.update(this.parentObject.getRawJson().getAsJsonObject().toString(), true); // TODO: bug test more. This seems very janky and not done right. Will test more
        } else {
            throw new RuntimeException("Cannot update object from dataclass instance of SyncingInformation. Get instance of SyncingInformation from child of SavedObject. (SavedObject.syncing.update())");
        }
    }

    // Syncing

    public void syncToUsers() {
        for (UUID playerUUID : this.info.sharedTo) {
            ServerPlayer sharedUser = JMWSCommon.minecraftServerInstance.getPlayerList().getPlayer(playerUUID);

            if (sharedUser != null) {
                ServerPacketHandler.sendUserSync(sharedUser, false, false, true);
            }
        }
    }

}
