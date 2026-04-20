package me.brynview.navidrohim.jmws.server.syncing;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.api.ServerSyncInformationImpl;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class ServerSyncingInformation extends ServerSyncInformationImpl {

    @Nullable
    protected ServerObject parentObject = null;

    public ServerSyncingInformation(String identifier, UUID owner, Set<UUID> sharedTo, boolean isGlobal, ObjectType syncRegistryType) {
        super(identifier, owner, sharedTo, isGlobal, syncRegistryType);
    }

    public static ServerSyncingInformation getSyncingHandlerFromServerObject(ServerObject object) {
        try {
            Constants.getLogger().info("Syncing handler: " + object.getSyncedCustomData());
            ServerSyncingInformation serverSyncingHandler = JMWSCommon.gson.fromJson(object.getSyncedCustomData(), ServerSyncingInformation.class);
            serverSyncingHandler.parentObject = object;

            return serverSyncingHandler;
        } catch (IllegalStateException | JsonSyntaxException reader) {
            PlayerNetworkingHelper.sendUserMessage(object.getOwnerUUID(), "FATAL: You are on the wrong JMWS version! Update to JMWS v%s as soon as possible or you may suffer data loss!".formatted(Constants.SERVER_VERSION), false, MessageType.FAILURE);
            object.dataclass = true;
            return null;
        }
    }

    // Sharing

    @Override
    public void addUserToShare(UUID playerUUID) {
        super.addUserToShare(playerUUID);
        this.update();
    }

    @Override
    public void removeUserFromShare(UUID playerUUID) {
        super.removeUserFromShare(playerUUID);
        this.update();
    }

    @Override
    public void removeAllFromShare() {
        super.removeAllFromShare();
        this.update();
    }

    // Global handling

    @Override
    public void setGlobal(boolean global)
    {
        super.setGlobal(global);
        this.update();
    }

    @Override
    public void setRegistry(@Nullable ObjectType registry) {
        super.setRegistry(registry);
        this.update();
    }

    // IO (SERVER ONLY)

    private void update() {
        if (this.parentObject != null) {
            String jsonString = JMWSCommon.gsonExcludeNoExpose.toJson(this, ServerSyncingInformation.class);

            this.parentObject.getRawJson().get("customDataMap").getAsJsonObject().add(Constants.MODID, new JsonPrimitive(jsonString));
            this.parentObject.update(this.parentObject.getRawJson().getAsJsonObject().toString(), true); // TODO: bug test more. This seems very janky and not done right. Will test more
        } else {
            throw new RuntimeException("Cannot update object from dataclass instance of SyncingInformation. Get instance of SyncingInformation from child of SavedObject. (SavedObject.syncing.update())");
        }
    }

    // Syncing

    public void syncToUsers() {
        for (UUID playerUUID : this.sharedTo) {
            ServerPlayer sharedUser = JMWSCommon.minecraftServerInstance.getPlayerList().getPlayer(playerUUID);

            if (sharedUser != null) {
                ServerPacketHandler.sendUserSync(sharedUser, false, false, true);
            }
        }
    }
}
