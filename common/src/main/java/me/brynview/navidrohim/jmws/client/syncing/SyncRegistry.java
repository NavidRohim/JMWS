package me.brynview.navidrohim.jmws.client.syncing;

import me.brynview.navidrohim.jmws.client.syncing.api.decoder.BaseDecoder;
import me.brynview.navidrohim.jmws.client.syncing.impl.GroupDecoder;
import me.brynview.navidrohim.jmws.client.syncing.impl.WaypointDecoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SyncRegistry {

    private static final Map<String, SyncRegistry> REGISTRY = new HashMap<>();

    public static final SyncRegistry WAYPOINT = register("WAYPOINT", new WaypointDecoder());
    public static final SyncRegistry GROUP = register("GROUP", new GroupDecoder());
    public static final SyncRegistry UNKNOWN = register("UNKNOWN", null);

    private final String id;
    private final String displayName;
    private final BaseDecoder<?, ?> stringDecoder;

    private SyncRegistry(String id, BaseDecoder<?, ?> stringDecoder)
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

    public static SyncRegistry register(String id, BaseDecoder<?, ?> stringDecoder)
    {
        if (REGISTRY.containsKey(id))
        {
            return REGISTRY.get(id);
        }
        SyncRegistry type = new SyncRegistry(id, stringDecoder);
        REGISTRY.put(id, type);
        return type;
    }

    public static Optional<SyncRegistry> of(String id)
    {
        return Optional.ofNullable(REGISTRY.get(id));
    }
}
