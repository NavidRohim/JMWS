package me.brynview.navidrohim.jmws.server.registry;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.api.registry.RegistryEntry;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerSyncRegistryEntry <T extends ServerObject> implements RegistryEntry {

    private final String id;
    private final String displayName;

    private final boolean internal;
    private final Class<T> registryClass;
    private final String registryPath;

    ServerSyncRegistryEntry(@NotNull String id, @Nullable final Class<T> savedClass, boolean internal) {
        this.id = id.toUpperCase();
        this.displayName = id.toLowerCase();
        this.registryClass = savedClass;

        this.registryPath = "./%s/%s/".formatted(Constants.MODID, this.displayName);
        Constants.getLogger().info(registryPath);
        this.internal = internal;
    }

    public boolean isInternal()
    {
        return internal;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getId()
    {
        return id;
    }

    public Class<T> getRegistryClass()
    {
        return this.registryClass;
    }

    public @NotNull String getRegistryPath()
    {
        if (this.registryPath != null)
        {
            return this.registryPath;
        } else {
            throw new RuntimeException("Registry path is null! Do not use on registry type %s".formatted(this.id));
        }
    }
}