package me.brynview.navidrohim.jmws.common.enums;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.server.objects.ServerGroup;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.objects.ServerWaypoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServerSyncRegistry {

    private static final Map<String, ServerSyncRegistry> REGISTRY = new HashMap<>();

    public static final ServerSyncRegistry WAYPOINT = register("WAYPOINT", ServerWaypoint.class);
    public static final ServerSyncRegistry GROUP = register("GROUP", ServerGroup.class);
    public static final ServerSyncRegistry SHARED = register("SHARED", null);
    public static final ServerSyncRegistry GENERIC = register("GENERIC", null);

    private final String id;
    private final String displayName;

    private final Class<? extends ServerObject> registryClass;
    private final Path registryPath;

    public static ServerSyncRegistry register(String id, Class<? extends ServerObject> registryClass)
    {
        if (REGISTRY.containsKey(id))
        {
            return REGISTRY.get(id);
        }
        ServerSyncRegistry type = new ServerSyncRegistry(id, registryClass);
        REGISTRY.put(id, type);
        return type;
    }

    public static Optional<ServerSyncRegistry> get(String id)
    {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    public static ServerSyncRegistry getStrict(String id)
    {
        if (!REGISTRY.containsKey(id))
        {
            REGISTRY.put(id, register(id, null));
        }

        return REGISTRY.get(id);
    }

    public static List<ServerSyncRegistry> getRegistryValues()
    {
        return List.copyOf(REGISTRY.values());
    }

    ServerSyncRegistry(@NotNull String id, @Nullable final Class<? extends ServerObject> savedClass) {
        this.id = id.toUpperCase();
        this.displayName = id.toLowerCase();
        this.registryClass = savedClass;
        this.registryPath = Path.of("./%s/%s/".formatted(Constants.MODID, this.id));
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getId()
    {
        return id;
    }

    public Class<? extends ServerObject> getRegistryClass()
    {
        return this.registryClass;
    }

    public @NotNull Path getRegistryPath()
    {
        if (this.registryPath != null)
        {
            return this.registryPath;
        } else {
            throw new RuntimeException("Registry path is null! Do not use on registry type %s".formatted(this.id));
        }
    }
}