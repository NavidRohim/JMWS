package me.brynview.navidrohim.jmws.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.enums.ServerSyncRegistry;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.players.NameAndId;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.brynview.navidrohim.jmws.server.io.JMWSServerIO.getAllInitialisedGlobalObjects;

public class ServerDispatcher {


    public static void addCommandsToDispatcher(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stop_sharing_group")
                .requires(ServerDispatcher::isValidCommandUser)
                .then(Commands.argument("groupName", StringArgumentType.greedyString()).suggests(ServerDispatcher::suggestSharedGroups).executes(ServerDispatcher::doRemoveShareGroup))
        );
        dispatcher.register(Commands.literal("stop_sharing_waypoint")
                .requires(ServerDispatcher::isValidCommandUser)
                .then(Commands.argument("waypointName", StringArgumentType.greedyString()).suggests(ServerDispatcher::suggestSharedWaypoints).executes(ServerDispatcher::doRemoveShareWaypoint))
        );
    }

    private static int doRemoveShareWaypoint(CommandContext<CommandSourceStack> context1) throws CommandSyntaxException {
        String waypointID = StringArgumentType.getString(context1, "waypointName");

        return ServerCommands.removeShare(context1.getSource().getPlayer(), waypointID, ServerSyncRegistry.WAYPOINT);
    }

    private static int doRemoveShareGroup(CommandContext<CommandSourceStack> context1) throws CommandSyntaxException {
        String groupID = StringArgumentType.getString(context1, "groupName");

        return ServerCommands.removeShare(context1.getSource().getPlayer(), groupID, ServerSyncRegistry.GROUP);
    }

    public static HashMap<String, ServerObject> getUserObjectsAsNameHashmap(UUID playerUUID, ServerSyncRegistry serverSyncRegistry, boolean global, boolean onlyShared)
    {
        HashMap<String, ServerObject> stringServerObjectHashMap = new HashMap<>();
        for (ServerObject object : JMWSServerIO.getObjectsForUser(playerUUID, serverSyncRegistry, global))
        {
            String nonDupeIdentifier = object.getObjectNonDuplicateIdentifier();
            if ((!object.serverSyncingHandler.isGlobal() && !onlyShared) || global || (onlyShared && !object.serverSyncingHandler.sharedTo.isEmpty()))
            {
                stringServerObjectHashMap.put(nonDupeIdentifier, object);
            }
        }

        return stringServerObjectHashMap;
    }

    public static HashMap<String, ServerObject> getInactiveOpUserGlobalObjects(ServerSyncRegistry serverSyncRegistry)
    {
        List<ServerObject> objs = getAllInitialisedGlobalObjects(serverSyncRegistry);
        HashMap<String, ServerObject> stringServerObjectHashMap = new HashMap<>();

        objs.forEach(obj -> {
            Optional<GameProfile> oldOpPlayerProfile = JMWSCommon.minecraftServerInstance.services().profileResolver().fetchById(obj.getOwnerUUID());
            boolean isOp = oldOpPlayerProfile.isPresent() && JMWSCommon.minecraftServerInstance.getPlayerList().isOp(new NameAndId(oldOpPlayerProfile.get()));

            if (!isOp)
            {
                stringServerObjectHashMap.put(obj.getObjectNonDuplicateIdentifier(), obj);
            }
        });
        return stringServerObjectHashMap;
    }

    private static CompletableFuture<Suggestions> suggestSharedObject(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder, ServerSyncRegistry serverSyncRegistry)
    {
        Set<String> names = getUserObjectsAsNameHashmap(commandSourceStackCommandContext.getSource().getPlayer().getUUID(), serverSyncRegistry, false, true).keySet();
        return SharedSuggestionProvider.suggest(names, suggestionsBuilder);
    }

    public static CompletableFuture<Suggestions> suggestSharedGroups(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        return suggestSharedObject(commandSourceStackCommandContext, suggestionsBuilder, ServerSyncRegistry.GROUP);
    }

    public static CompletableFuture<Suggestions> suggestSharedWaypoints(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        return suggestSharedObject(commandSourceStackCommandContext, suggestionsBuilder, ServerSyncRegistry.WAYPOINT);
    }

    private static boolean isValidCommandUser(CommandSourceStack commandSourceStack)
    {
        return !JMWSCommon.isInternalServer() || (!JMWSCommon.minecraftServerInstance.isSingleplayerOwner(commandSourceStack.getPlayer().nameAndId())); // No host user
    }
}
