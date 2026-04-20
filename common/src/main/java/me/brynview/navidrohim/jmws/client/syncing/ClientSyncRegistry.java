package me.brynview.navidrohim.jmws.client.syncing;

import me.brynview.navidrohim.jmws.client.syncing.api.decoder.BaseDecoder;
import me.brynview.navidrohim.jmws.client.syncing.impl.GroupDecoder;
import me.brynview.navidrohim.jmws.client.syncing.impl.WaypointDecoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClientSyncRegistry {

    private static final Map<String, ClientSyncRegistry> REGISTRY = new HashMap<>();

    public static final ClientSyncRegistry WAYPOINT = register("WAYPOINT", new WaypointDecoder());
    public static final ClientSyncRegistry GROUP = register("GROUP", new GroupDecoder());
    public static final ClientSyncRegistry UNKNOWN = register("UNKNOWN", null);

    private final BaseDecoder<?, ?> stringDecoder;
    private final String id;
    private final String displayName;

    private ClientSyncRegistry(String id, BaseDecoder<?, ?> stringDecoder)
    {
        this.id = id.toUpperCase();
        this.displayName = id.toLowerCase();
        this.stringDecoder = stringDecoder;
    }

    public BaseDecoder<?, ?> getDecoder()
    {
        return this.stringDecoder;
    }

    public String getId()
    {
        return this.id;
    }

    public String toString()
    {
        return this.id;
    }

    public String getReadableName()
    {
        return this.displayName;
    }

    public static ClientSyncRegistry register(String id, BaseDecoder<?, ?> stringDecoder)
    {
        if (REGISTRY.containsKey(id))
        {
            return REGISTRY.get(id);
        }
        ClientSyncRegistry type = new ClientSyncRegistry(id, stringDecoder);
        REGISTRY.put(id, type);
        return type;
    }

    public static Optional<ClientSyncRegistry> of(String id)
    {
        return Optional.ofNullable(REGISTRY.get(id));
    }
}
