package me.brynview.navidrohim.jmws.server.objects;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class JMServerObject extends ServerObject
{
    @Nullable protected final ResourceKey<Level> dimension;

    protected JMServerObject(JsonObject payload, UUID playerUUID)
    {
        super(payload, playerUUID);

        @Nullable ResourceKey<Level> dim = null;
        try {
            String rawDim = payload.get("dimensions").getAsJsonArray().get(0).getAsString();
            dim = ResourceKey.create(Registries.DIMENSION, Identifier.parse(rawDim));
        } catch (Exception _)
        {

        }
        this.dimension = dim;
    }

    @Nullable
    public ResourceKey<Level> getDimension()
    {
        return dimension;
    }
}
