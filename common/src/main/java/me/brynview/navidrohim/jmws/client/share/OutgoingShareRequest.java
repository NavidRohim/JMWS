package me.brynview.navidrohim.jmws.client.share;

import me.brynview.navidrohim.jmws.common.enums.FetchType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class OutgoingShareRequest extends ShareRequest {

    public static final boolean isOutgoing = true; // kinda useless

    public OutgoingShareRequest(@Nullable UUID uuid, @Nullable UUID meantForPlayerUUID, Object waypointOrGroup, FetchType sharedObjectType, String requestIdentifier) {
        super(uuid, meantForPlayerUUID, waypointOrGroup, sharedObjectType, requestIdentifier);
    }
}
