package me.brynview.navidrohim.jmws.client.shared;

import me.brynview.navidrohim.jmws.Constants;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class OutgoingShareRequest extends ShareRequest {

    public static final boolean isOutgoing = true; // kinda useless

    public OutgoingShareRequest(@Nullable UUID uuid, @Nullable UUID meantForPlayerUUID, Object waypointOrGroup) {
        super(uuid, meantForPlayerUUID, waypointOrGroup);
    }
}
