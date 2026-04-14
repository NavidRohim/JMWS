package me.brynview.navidrohim.jmws.common.utils;

import com.google.gson.JsonSyntaxException;
import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import me.brynview.navidrohim.jmws.server.syncing.ServerSyncingHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class SyncUtils {

    private static final String[] syncFieldKeys = {
            "objectIdentifier",
            "sharedTo",
            "owner",
            "isGlobal"
    };

    public static boolean isLegacySyncField(@Nullable String field) {
        if (field != null && field.length() == 64)
        {
            for (int i = 0; i < field.length(); i++) {
                char c = field.charAt(i);
                if (!Character.isLetterOrDigit(c))
                    return false;
            }

            return true;
        }
        return false;
    }

    public static boolean isValidSyncField(@Nullable String input) {

        if (input == null) { return true; }
        return Arrays.stream(syncFieldKeys).allMatch(input::contains);
    }

    @Nullable
    public static SyncInformation getIdentifier(Waypoint waypoint)
    {
        return SyncInformation.syncInformationFromString(waypoint.getCustomData(Constants.MODID));
    }

    public static ServerSyncingHandler getSyncingInfo(String customDataField, boolean returnNullIfError) {
        try {
            return JMWSCommon.gson.fromJson(customDataField, ServerSyncingHandler.class);
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
        return JMWSCommon.gson.toJson(new ServerSyncingHandler(Set.of(), objectIdentifier, owner, isGlobal));
    }
}
