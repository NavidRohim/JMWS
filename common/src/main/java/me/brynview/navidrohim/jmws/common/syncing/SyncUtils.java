package me.brynview.navidrohim.jmws.common.syncing;

import com.google.gson.JsonSyntaxException;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.server.syncing.ServerSyncingHandler;

import java.util.List;
import java.util.UUID;

public class SyncUtils {

    public static ServerSyncingHandler getSyncingInfo(String customDataField, boolean returnNullIfError) {
        try {
            return CommonClass.gson.fromJson(customDataField, ServerSyncingHandler.class);
        } catch (JsonSyntaxException syntaxException) // will throw if object hasn't been ported.
        {
            if (!returnNullIfError) {
                return getSyncingInfo(getEmptySyncingInfoString(customDataField, PlayerUtils.ourUUID(), false));
            }
            return null;
        }
    }

    public static ServerSyncingHandler getSyncingInfo(String customDataField) {
        return getSyncingInfo(customDataField, false);
    }

    public static String getEmptySyncingInfoString(String objectIdentifier, UUID owner, boolean isGlobal) {
        return CommonClass.gson.toJson(new ServerSyncingHandler(List.of(), objectIdentifier, owner, isGlobal));
    }
}
