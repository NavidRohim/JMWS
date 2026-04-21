package me.brynview.navidrohim.jmws.server.registry;

import me.brynview.navidrohim.jmws.server.objects.ServerGroup;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.objects.ServerWaypoint;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ServerSyncRegistry extends HashMap<String, ServerSyncRegistryEntry>
{
    public static ServerSyncRegistryEntry WAYPOINT;
    public static ServerSyncRegistryEntry GROUP;
    public static ServerSyncRegistryEntry SHARED;
    public static ServerSyncRegistryEntry GENERIC;

    public ServerSyncRegistry()
    {
        super();
        WAYPOINT = this.register("WAYPOINT", ServerWaypoint.class);
        GROUP = this.register("GROUP", ServerGroup.class);
        SHARED = this.register("SHARED", null);
        GENERIC = this.register("GENERIC", null, true);
    }

    public ServerSyncRegistryEntry register(String id, Class<? extends ServerObject> registryClass, boolean internal)
    {
        if (containsKey(id))
        {
            return get(id);
        }
        ServerSyncRegistryEntry type = new ServerSyncRegistryEntry(id, registryClass, internal);
        put(id, type);
        return type;
    }

    public ServerSyncRegistryEntry register(String id, Class<? extends ServerObject> registryClass)
    {
        return register(id, registryClass, false);
    }

    public Optional<ServerSyncRegistryEntry> getOptional(String id)
    {
        return Optional.ofNullable(this.get(id));
    }

    public ServerSyncRegistryEntry getStrict(String id)
    {
        if (!this.containsKey(id))
        {
            this.put(id, this.register(id, null));
        }

        return this.get(id);
    }

    public List<ServerSyncRegistryEntry> getRegistryValues()
    {
        return List.copyOf(this.values());
    }
}
