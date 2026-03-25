package me.brynview.navidrohim.jmws.common.syncing;

import com.google.gson.JsonSyntaxException;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;

import java.util.List;
import java.util.UUID;

public class SyncUtils {

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
}
