package me.brynview.navidrohim.jmws.common.syncing;

import com.google.gson.*;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.UUID;

public class SyncInformation {

    public String objectIdentifier;
    public UUID owner;
    public Set<UUID> sharedTo;
    public boolean isGlobal;

    public SyncInformation(String objectIdentifier, UUID owner, Set<UUID> sharedTo, boolean isGlobal)
    {
        this.objectIdentifier = objectIdentifier;
        this.owner = owner;
        this.sharedTo = sharedTo;
        this.isGlobal = isGlobal;
    }

    public @NonNull String serialize() {
        return JMWSCommon.gson.toJson(this);
    }

    public boolean isOwner(UUID user)
    {
        return owner.equals(user);
    }

    @Nullable
    public static String getOnlyIdentifier(@Nullable String syncInformation)
    {
        if (syncInformation != null)
        {
            JsonObject obj = JsonParser.parseString(syncInformation).getAsJsonObject();
            return obj.get("objectIdentifier").getAsString();
        }
        return null;
    }
}

