package me.brynview.navidrohim.jmws.common.syncing;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class Syncing {

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

    public static Syncing getSyncingInfo(ServerObject object) {
        try {
            Syncing syncing = CommonClass.gson.fromJson(object.getSyncedCustomData(), Syncing.class);
            syncing.parentObject = object;

            return syncing;
        } catch (IllegalStateException | JsonSyntaxException reader) {
            PlayerNetworkingHelper.sendUserMessage(object.getOwnerUUID(), "FATAL: You are on the wrong JMWS version! Update to JMWS v%s as soon as possible or you may suffer data loss!".formatted(Constants.SERVER_VERSION), false, MessageType.FAILURE);
            object.dataclass = true;

            return null;
        }
    }

    public static Syncing getSyncingInfo(String customDataField, @Nullable UUID owner, boolean returnNullIfError) {
        try {
            return CommonClass.gson.fromJson(customDataField, Syncing.class);
        } catch (JsonSyntaxException syntaxException) {
            if (!returnNullIfError) {
                if (owner == null) {
                    throw syntaxException;
                }
                return getSyncingInfo(getEmptySyncingInfoString(customDataField, owner, false));
            }
            return null;
        }
    }

    public static Syncing getSyncingInfo(String customDataField, boolean returnNullIfError) {
        return getSyncingInfo(customDataField, null, returnNullIfError);
    }

    public static Syncing getSyncingInfo(String customDataField, UUID owner) {
        return getSyncingInfo(customDataField, owner, false);
    }

    public static Syncing getSyncingInfo(String customDataField) {
        return getSyncingInfo(customDataField, false);
    }

    public static String getEmptySyncingInfoString(String objectIdentifier, UUID owner, boolean isGlobal) {
        return CommonClass.gson.toJson(new Syncing(List.of(), objectIdentifier, owner, isGlobal));
    }

    public void addUserToShare(UUID playerUUID) {
        this.sharedTo.add(playerUUID.toString());
        this.update();
    }

    public void removeUserFromShare(String playerUUID) {
        this.sharedTo.remove(playerUUID);
        this.update();
    }

    public void removeAllFromShare() {
        this.sharedTo.clear();
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
            String jsonString = CommonClass.gsonExcludeNoExposeNotPretty.toJson(this, Syncing.class);

            this.parentObject.getRawJson().get("customDataMap").getAsJsonObject().add(Constants.MODID, new JsonPrimitive(jsonString));
            this.parentObject.update(this.parentObject.getRawJson().getAsJsonObject().toString(), true);
        } else {
            throw new RuntimeException("Cannot update object from dataclass instance of SyncingInformation. Get instance of SyncingInformation from child of SavedObject. (SavedObject.syncing.update())");
        }
    }

    public void syncToUsers() {
        for (String playerUUID : this.sharedTo) {
            ServerPacketHandler.sendUserSync(UUID.fromString(playerUUID), false, false, true);
        }
    }
}
