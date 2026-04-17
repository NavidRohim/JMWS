package me.brynview.navidrohim.jmws.client.syncing;

import me.brynview.navidrohim.jmws.client.syncing.api.decoder.BaseDecoder;
import me.brynview.navidrohim.jmws.client.syncing.impl.GroupDecoder;
import me.brynview.navidrohim.jmws.client.syncing.impl.WaypointDecoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SyncObjectType {

    private static final Map<String, SyncObjectType> REGISTRY = new HashMap<>();

    public static final SyncObjectType WAYPOINT = register("WAYPOINT", new WaypointDecoder());
    public static final SyncObjectType GROUP = register("GROUP", new GroupDecoder());

    private final String id;
    private final String displayName;
    private final BaseDecoder<?, ?> stringDecoder;

    private SyncObjectType(String id, BaseDecoder<?, ?> stringDecoder)
    {
        this.id = id;
        this.displayName = id.toLowerCase();
        this.stringDecoder = stringDecoder;
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

    public BaseDecoder<?, ?> getDecoder()
    {
        return this.stringDecoder;
    }

    public static SyncObjectType register(String id, BaseDecoder<?, ?> stringDecoder)
    {
        if (REGISTRY.containsKey(id))
        {
            return REGISTRY.get(id);
        }
        SyncObjectType type = new SyncObjectType(id, stringDecoder);
        REGISTRY.put(id, type);
        return type;
    }

    public static Optional<SyncObjectType> of(String id)
    {
        return Optional.ofNullable(REGISTRY.get(id));
    }
}
