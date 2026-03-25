package me.brynview.navidrohim.jmws.common.syncing;

import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.api.CommonSyncHandler;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class Syncing extends CommonSyncHandler {

    @Expose
    public String objectIdentifier;

    @Expose
    public List<String> sharedTo;

    @Expose
    protected UUID owner;

    @Expose
    protected boolean isGlobal;

    @Nullable
    protected ServerObject parentObject = null;

    public Syncing(List<String> sharedTo, String identifier, UUID owner, boolean isGlobal) {
        this.objectIdentifier = identifier;
        this.sharedTo = sharedTo;
        this.owner = owner;
        this.isGlobal = isGlobal;
    }

    // Sharing

    @Override
    public void addUserToShare(UUID playerUUID) {
        this.sharedTo.add(playerUUID.toString());
        this.update();
    }

    @Override
    public void removeUserFromShare(UUID playerUUID) {
        this.sharedTo.remove(playerUUID.toString());
        this.update();
    }

    @Override
    public void removeAllFromShare() {
        this.sharedTo.clear();
        this.update();
    }

    // Owner stuffs

    public boolean isOwner(UUID supposedOwner) {
        return this.owner.equals(supposedOwner);
    }

    public UUID getOwner() {
        return this.owner;
    }

    // Global handling

    public boolean isGlobal() {
        return this.isGlobal;
    }

    public void setGlobal(boolean global)
    {
        this.isGlobal = global;
        this.update();
    }

    // IO (SERVER ONLY)

    private void update() {
        if (this.parentObject != null) {
            String jsonString = CommonClass.gsonExcludeNoExpose.toJson(this, Syncing.class);

            this.parentObject.getRawJson().get("customDataMap").getAsJsonObject().add(Constants.MODID, new JsonPrimitive(jsonString));
            this.parentObject.update(this.parentObject.getRawJson().getAsJsonObject().toString(), true); // TODO: bug test more. This seems very janky and not done right. Will test more
        } else {
            throw new RuntimeException("Cannot update object from dataclass instance of SyncingInformation. Get instance of SyncingInformation from child of SavedObject. (SavedObject.syncing.update())");
        }
    }

    // Syncing

    public void syncToUsers() {
        for (String playerUUID : this.sharedTo) {
            ServerPlayer sharedUser = CommonClass.getMinecraftServerInstance().getPlayerList().getPlayer(UUID.fromString(playerUUID));

            if (sharedUser != null) {
                ServerPacketHandler.sendUserSync(sharedUser, false, false, true);
            }
        }
    }
}
