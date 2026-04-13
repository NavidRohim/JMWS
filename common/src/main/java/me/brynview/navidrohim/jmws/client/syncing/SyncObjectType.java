package me.brynview.navidrohim.jmws.client.syncing;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SyncObjectType {

    private static final Map<String, SyncObjectType> REGISTRY = new HashMap<>();

    public static final SyncObjectType WAYPOINT = register("WAYPOINT");
    public static final SyncObjectType GROUP = register("GROUP");

    private final String id;
    private final String displayName;

    private SyncObjectType(String id)
    {
        this.id = id;
        this.displayName = id.toLowerCase();
    }

    private SyncObjectType(String id, String displayName)
    {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId()
    {
        return id;
    }

    public String getReadableName()
    {
        return displayName;
    }

    public String toString()
    {
        return id;
    }

    public static SyncObjectType register(String id)
    {
        return REGISTRY.computeIfAbsent(id, SyncObjectType::new);
    }

    public static Optional<SyncObjectType> of(String id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }
}
