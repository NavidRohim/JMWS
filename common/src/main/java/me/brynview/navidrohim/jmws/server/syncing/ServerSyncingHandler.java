package me.brynview.navidrohim.jmws.server.syncing;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.api.CommonSyncHandler;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ServerSyncingHandler extends CommonSyncHandler {

    @Nullable
    protected ServerObject parentObject = null;

    public ServerSyncingHandler(List<String> sharedTo, String identifier, UUID owner, boolean isGlobal) {
        super(sharedTo, identifier, owner, isGlobal);
    }

    public static ServerSyncingHandler getSyncingHandlerFromServerObject(ServerObject object) {
        try {
            ServerSyncingHandler serverSyncingHandler = CommonClass.gson.fromJson(object.getSyncedCustomData(), ServerSyncingHandler.class);
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

    // IO (SERVER ONLY)

    private void update() {
        if (this.parentObject != null) {
            String jsonString = CommonClass.gsonExcludeNoExpose.toJson(this, ServerSyncingHandler.class);

            this.parentObject.getRawJson().get("customDataMap").getAsJsonObject().add(Constants.MODID, new JsonPrimitive(jsonString));
            this.parentObject.update(this.parentObject.getRawJson().getAsJsonObject().toString(), true); // TODO: bug test more. This seems very janky and not done right. Will test more
        } else {
            throw new RuntimeException("Cannot update object from dataclass instance of SyncingInformation. Get instance of SyncingInformation from child of SavedObject. (SavedObject.syncing.update())");
        }
    }

    // Syncing

    public void syncToUsers() {
        for (String playerUUID : this.sharedTo) {
            ServerPlayer sharedUser = CommonClass.minecraftServerInstance.getPlayerList().getPlayer(UUID.fromString(playerUUID));

            if (sharedUser != null) {
                ServerPacketHandler.sendUserSync(sharedUser, false, false, true);
            }
        }
    }
}
