package me.brynview.navidrohim.jmws.common.syncing;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.server.level.ServerPlayer;
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
            PlayerNetworkingHelper.sendUserMessage(object.getOwnerUUID(), "FATAL: You are on the wrong JMWS version! Update to JMWS v%s as soon as possible or you may suffer data loss!".formatted(Constants.SERVER_VERSION), false, JMWSMessageType.FAILURE);
            object.dataclass = true;

            return null;
        }
    }

    public static Syncing getSyncingInfo(String customDataField, boolean returnNullIfError) {
        try {
            return CommonClass.gson.fromJson(customDataField, Syncing.class);
        } catch (JsonSyntaxException syntaxException) // will throw if object hasn't been ported.
        {
            if (!returnNullIfError) {
                return getSyncingInfo(getEmptySyncingInfoString(customDataField, PlayerHelper.ourUUID(), false));
            }
            return null;
        }
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
            String jsonString = CommonClass.gsonExcludeNoExpose.toJson(this, Syncing.class);
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
