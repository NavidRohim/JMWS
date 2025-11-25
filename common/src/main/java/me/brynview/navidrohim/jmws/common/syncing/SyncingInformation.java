package me.brynview.navidrohim.jmws.common.syncing;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class SyncingInformation {

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

    public SyncingInformation(List<String> sharedTo, String identifier, UUID owner, boolean isGlobal) {
        this.objectIdentifier = identifier;
        this.sharedTo = sharedTo;
        this.owner = owner;
        this.isGlobal = isGlobal;
    }

    public static SyncingInformation getSyncingInfo(ServerObject object) {
        try {
            SyncingInformation syncingInformation = CommonClass.gson.fromJson(object.getCustomData(), SyncingInformation.class);
            syncingInformation.parentObject = object;

            return syncingInformation;
        } catch (IllegalStateException | JsonSyntaxException reader) {
            return transition(object); // if old waypoint is present
            //PlayerNetworkingHelper.sendUserMessage(object.ownerUUID, "fatal.jmws.server_mismatch", false, true);
        }
    }

    public static SyncingInformation getSyncingInfo(String customDataField, boolean returnNullIfError) {
        try {
            return CommonClass.gson.fromJson(customDataField, SyncingInformation.class);
        } catch (JsonSyntaxException syntaxException) // will throw if object hasn't been ported.
        {
            if (!returnNullIfError) {
                return getSyncingInfo(getEmptySyncingInfoString(customDataField, PlayerHelper.ourUUID(), false));
            }
            return null;
        }
    }

    public static SyncingInformation getSyncingInfo(String customDataField) {
        return getSyncingInfo(customDataField, false);
    }

    private static SyncingInformation transition(ServerObject object) {
        // TODO transition to new customDataField
        object.setCustomData(SyncingInformation.getEmptySyncingInfoString(object.getCustomData(), object.getOwnerUUID(), false));
        return getSyncingInfo(object);
    }

    public static String getEmptySyncingInfoString(String objectIdentifier, UUID owner, boolean isGlobal) {
        return CommonClass.gson.toJson(new SyncingInformation(List.of(), objectIdentifier, owner, isGlobal));
    }

    public void addUserToShare(UUID playerUUID) {
        this.sharedTo.add(playerUUID.toString());
        this.update();
    }

    public void removeUserFromShare(String playerUUID) {
        this.sharedTo.remove(playerUUID);
        this.update();
    }

    public boolean isOwner(UUID supposedOwner) {
        return this.owner.equals(supposedOwner);
    }

    public UUID getOwner() {
        return this.owner;
    }

    public boolean isGlobal() {
        return this.isGlobal;
    }

    public void setGlobal(boolean global) {
        this.isGlobal = global;
        this.update();
    }

    private void update() {
        if (this.parentObject != null) {
            String jsonString = CommonClass.gsonExcludeNoExpose.toJson(this, SyncingInformation.class);
            this.parentObject.getRawJson().add("customData", new JsonPrimitive(jsonString));

            this.parentObject.update(this.parentObject.getRawJson().getAsJsonObject().toString(), true); // TODO: bug test more. This seems very janky and not done right. Will test more
        } else {
            throw new RuntimeException("Cannot update object from dataclass instance of SyncingInformation. Get instance of SyncingInformation from child of SavedObject. (SavedObject.syncing.update())");
        }
    }

    public void syncToUsers() {
        for (String playerUUID : this.sharedTo) {
            ServerPlayer sharedUser = CommonClass.getMinecraftServerInstance().getPlayerList().getPlayer(UUID.fromString(playerUUID));

            if (sharedUser != null) {
                ServerPacketHandler.sendUserSync(sharedUser, false, false, true);
            }
        }
    }
}
