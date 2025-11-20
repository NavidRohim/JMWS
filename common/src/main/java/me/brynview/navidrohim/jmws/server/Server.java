package me.brynview.navidrohim.jmws.server;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Server {

    private static CompletableFuture<Suggestions> suggestObject(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder, ObjectType objectType)
    {
        List<String> names = JMWSServerIO.getObjectsForUser(commandSourceStackCommandContext.getSource().getPlayer().getUUID(), objectType, false)
                .stream()
                .map(ServerObject::getName)
                .toList();

        return SharedSuggestionProvider.suggest(names, suggestionsBuilder);
    }

    private static CompletableFuture<Suggestions> suggestGlobalObject(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder, ObjectType objectType)
    {
        List<String> names = JMWSServerIO.getObjectsForUser(commandSourceStackCommandContext.getSource().getPlayer().getUUID(), objectType, true)
                .stream()
                .map(ServerObject::getName)
                .toList();

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
