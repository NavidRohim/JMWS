package me.brynview.navidrohim.jmws.common.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncInformation;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncUtils;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncRegistry;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.server.registry.ServerSyncRegistryEntry;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import me.brynview.navidrohim.jmws.server.syncing.ServerSyncingInformationWrapper;
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
    public static ClientSyncInformation getIdentifier(Waypoint waypoint)
    {
        return ClientSyncUtils.syncInformationFromString(waypoint.getCustomData(Constants.MODID), ClientSyncRegistry.WAYPOINT);
    }

    public static String getEmptySyncingInfoString(String objectIdentifier, UUID owner, boolean isGlobal, ServerSyncRegistryEntry registryType) {
        return JMWSCommon.gson.toJson(new ServerSyncingInformationWrapper(objectIdentifier, owner, Set.of(), isGlobal, registryType));
    }

    public static JsonElement serialize(SyncInformation src)
    {
        // cursed. like most of this code.

        JsonObject jsonObject = JMWSCommon.gson.toJsonTree(src).getAsJsonObject();
        jsonObject.remove("syncRegistryType");
        jsonObject.add("syncRegistryType", new JsonPrimitive(src.syncRegistryType().getId()));

        return jsonObject;
    }
}
