package me.brynview.navidrohim.jmws.server;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Server {

    public static HashMap<String, ServerObject> getUserObjectsAsNameHashmap(UUID playerUUID, ObjectType objectType, boolean global)
    {
        HashMap<String, ServerObject> stringServerObjectHashMap = new HashMap<>();
        for (ServerObject object : JMWSServerIO.getObjectsForUser(playerUUID, objectType, global))
        {
            String nonDupeIdentifier = object.getObjectNonDuplicateIdentifier();
            if (!object.syncing.isGlobal() || global)
            {
                stringServerObjectHashMap.put(nonDupeIdentifier, object);
            }
        }

        return stringServerObjectHashMap;
    }

    private static CompletableFuture<Suggestions> suggestObject(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder, ObjectType objectType)
    {
        List<String> names = getUserObjectsAsNameHashmap(commandSourceStackCommandContext.getSource().getPlayer().getUUID(), objectType, false).keySet().stream().toList();
        return SharedSuggestionProvider.suggest(names, suggestionsBuilder);
    }

    private static CompletableFuture<Suggestions> suggestGlobalObject(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder, ObjectType objectType)
    {
        List<String> names = getUserObjectsAsNameHashmap(commandSourceStackCommandContext.getSource().getPlayer().getUUID(), objectType, true).keySet().stream().toList();
        return SharedSuggestionProvider.suggest(names, suggestionsBuilder);
    }



    public static CompletableFuture<Suggestions> suggestWaypoints(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        return suggestObject(commandSourceStackCommandContext, suggestionsBuilder, ObjectType.WAYPOINT);
    }

    public static CompletableFuture<Suggestions> suggestGroups(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        return suggestObject(commandSourceStackCommandContext, suggestionsBuilder, ObjectType.GROUP);
    }

    public static CompletableFuture<Suggestions> suggestGlobalGroups(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        return suggestGlobalObject(commandSourceStackCommandContext, suggestionsBuilder, ObjectType.GROUP);
    }

    public static CompletableFuture<Suggestions> suggestGlobalWaypoints(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        return suggestGlobalObject(commandSourceStackCommandContext, suggestionsBuilder, ObjectType.WAYPOINT);
    }
}
