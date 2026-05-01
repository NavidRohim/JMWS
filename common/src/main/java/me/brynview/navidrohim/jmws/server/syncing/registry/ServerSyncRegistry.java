package me.brynview.navidrohim.jmws.server.syncing.registry;

import me.brynview.navidrohim.jmws.server.objects.ServerGroup;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.objects.ServerWaypoint;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ServerSyncRegistry extends HashMap<String, ServerSyncRegistryEntry<?>>
{
    public static ServerSyncRegistryEntry<ServerWaypoint> WAYPOINT;
    public static ServerSyncRegistryEntry<ServerGroup> GROUP;
    public static ServerSyncRegistryEntry<ServerObject> SHARED;
    public static ServerSyncRegistryEntry<ServerObject> GENERIC;

    public ServerSyncRegistry()
    {
        super();
        WAYPOINT = this.register("WAYPOINT", ServerWaypoint.class);
        GROUP = this.register("GROUP", ServerGroup.class);
        SHARED = this.register("SHARED", null);
        GENERIC = this.register("GENERIC", null, true);
    }

    public <E extends ServerObject> ServerSyncRegistryEntry<E> register(String id, @Nullable Class<E> registryClass, boolean internal)
    {
        if (containsKey(id))
        {
            //noinspection unchecked
            return (ServerSyncRegistryEntry<E>) get(id);
        }
        ServerSyncRegistryEntry<E> type = new ServerSyncRegistryEntry<>(id, registryClass, internal);
        put(id, type);
        return type;
    }

    public <E extends ServerObject> ServerSyncRegistryEntry<E> register(String id, @Nullable Class<E> registryClass)
    {
        return register(id, registryClass, false);
    }

    public Optional<ServerSyncRegistryEntry<?>> getOptional(String id)
    {
        return Optional.ofNullable(this.get(id));
    }

    public ServerSyncRegistryEntry<?> getStrict(String id)
    {
        if (!this.containsKey(id))
        {
            this.put(id, this.register(id, null));
        }

        return this.get(id);
    }

    public List<ServerSyncRegistryEntry<?>> getRegistryValues()
    {
        return List.copyOf(this.values());
    }
}
