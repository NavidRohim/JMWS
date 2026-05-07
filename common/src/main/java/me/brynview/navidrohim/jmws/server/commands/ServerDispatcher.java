package me.brynview.navidrohim.jmws.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ServerDispatcher {


    public static void addCommandsToDispatcher(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("share_waypoint")
                .requires(ServerDispatcher::isValidCommandUser)
                .then(Commands.argument("username", EntityArgument.player()).then(Commands.argument("waypointName", StringArgumentType.greedyString()).suggests(ServerDispatcher::suggestWaypoints).executes(ServerDispatcher::doShareWaypoint)))
        );
        dispatcher.register(Commands.literal("share_group")
                .requires(ServerDispatcher::isValidCommandUser)
                .then(Commands.argument("username", EntityArgument.player()).then(Commands.argument("groupName", StringArgumentType.greedyString()).suggests(ServerDispatcher::suggestGroups).executes(ServerDispatcher::doShareGroup)))
        );
        dispatcher.register(Commands.literal("stop_sharing_group")
                .requires(ServerDispatcher::isValidCommandUser)
                .then(Commands.argument("groupName", StringArgumentType.greedyString()).suggests(ServerDispatcher::suggestSharedGroups).executes(ServerDispatcher::doRemoveShareGroup))
        );
        dispatcher.register(Commands.literal("stop_sharing_waypoint")
                .requires(ServerDispatcher::isValidCommandUser)
                .then(Commands.argument("waypointName", StringArgumentType.greedyString()).suggests(ServerDispatcher::suggestSharedWaypoints).executes(ServerDispatcher::doRemoveShareWaypoint))
        );

        dispatcher.register(Commands.literal("jmws_handshake")
                .requires(ServerDispatcher::isValidCommandUser)
                        .executes(ServerDispatcher::sendHandshake)
        );

        dispatcher.register(Commands.literal("jmws_admin")
                .requires(Commands.hasPermission(Commands.LEVEL_MODERATORS))
                .then(Commands.literal("create_global_waypoint")
                        .then(Commands.argument("waypointName", StringArgumentType.greedyString())
                                .suggests(ServerDispatcher::suggestWaypoints)
                                .executes(ServerDispatcher::createServerWp)))
                .then(Commands.literal("create_global_group")
                        .then(Commands.argument("groupName", StringArgumentType.greedyString())
                                .suggests(ServerDispatcher::suggestGroups)
                                .executes(ServerDispatcher::createServerGp)))
                .then(Commands.literal("remove_global_group")
                        .then(Commands.argument("groupName", StringArgumentType.greedyString())
                                .suggests(ServerDispatcher::suggestGlobalGroups)
                                .executes(ServerDispatcher::removeServerGp)))
                .then(Commands.literal("remove_global_waypoint")
                        .then(Commands.argument("waypointName", StringArgumentType.greedyString())
                                .suggests(ServerDispatcher::suggestGlobalWaypoints)
                                .executes(ServerDispatcher::removeServerWp)))
                .then(Commands.literal("remove_global_no_op")
                        .then(Commands.literal("waypoint")
                            .then(Commands.argument("waypointName", StringArgumentType.greedyString())
                            .suggests(ServerDispatcher::suggestInactiveOpWp)
                            .executes(ServerDispatcher::removeServerWpFromBadOp)))
                        .then(Commands.literal("group")
                            .then(Commands.argument("groupName", StringArgumentType.greedyString())
                            .suggests(ServerDispatcher::suggestInactiveOpGp)
                            .executes(ServerDispatcher::removeServerGpFromBadOp)))
                )
        );
    }

    private static int sendHandshake(CommandContext<CommandSourceStack> commandSourceStackCommandContext)
    {
        ServerPlayer senderPlayer = commandSourceStackCommandContext.getSource().getPlayer();
        if (senderPlayer != null)
        {
            PlayerNetworkingHelper.sendHandshakeAndValidate(senderPlayer.getUUID());
            return 1;
        }
        return 0;
    }

    private static int removeServerGpFromBadOp(CommandContext<CommandSourceStack> commandSourceStackCommandContext)
    {
        String groupID = StringArgumentType.getString(commandSourceStackCommandContext, "groupName");
        return ServerCommands.removeGlobalFromBadOp(groupID, ObjectType.GROUP, commandSourceStackCommandContext.getSource().getPlayer().getUUID());
    }

    private static int removeServerWpFromBadOp(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String waypointID = StringArgumentType.getString(commandSourceStackCommandContext, "waypointName");
        return ServerCommands.removeGlobalFromBadOp(waypointID, ObjectType.WAYPOINT, commandSourceStackCommandContext.getSource().getPlayer().getUUID());
    }

    private static int doRemoveShareWaypoint(CommandContext<CommandSourceStack> context1) throws CommandSyntaxException {
        String waypointID = StringArgumentType.getString(context1, "waypointName");

        return ServerCommands.removeShare(context1.getSource().getPlayer().getUUID(), waypointID, ObjectType.WAYPOINT);
    }

    private static int doRemoveShareGroup(CommandContext<CommandSourceStack> context1) throws CommandSyntaxException {
        String groupID = StringArgumentType.getString(context1, "groupName");

        return ServerCommands.removeShare(context1.getSource().getPlayer().getUUID(), groupID, ObjectType.GROUP);
    }

    private static int doShareWaypoint(CommandContext<CommandSourceStack> context1) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context1, "username");
        String waypointID = StringArgumentType.getString(context1, "waypointName");

        return ServerCommands.share(context1.getSource().getPlayer().getUUID(), player.getUUID(), waypointID, ObjectType.WAYPOINT);
    }

    private static int doShareGroup(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(commandSourceStackCommandContext, "username");
        String groupName = StringArgumentType.getString(commandSourceStackCommandContext, "groupName");

        return ServerCommands.share(commandSourceStackCommandContext.getSource().getPlayer().getUUID(), player.getUUID(), groupName, ObjectType.GROUP);
    }

    private static int createServerWp(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String waypointName = StringArgumentType.getString(commandSourceStackCommandContext, "waypointName");
        return ServerCommands.globalShare(waypointName, commandSourceStackCommandContext.getSource().getPlayer().getUUID(), ObjectType.WAYPOINT, true);
    }

    private static int createServerGp(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String groupName = StringArgumentType.getString(commandSourceStackCommandContext, "groupName");
        return ServerCommands.globalShare(groupName, commandSourceStackCommandContext.getSource().getPlayer().getUUID(), ObjectType.GROUP, true);
    }

    private static int removeServerGp(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String groupName = StringArgumentType.getString(commandSourceStackCommandContext, "groupName");
        return ServerCommands.globalShare(groupName, commandSourceStackCommandContext.getSource().getPlayer().getUUID(), ObjectType.GROUP, false);
    }

    private static int removeServerWp(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String waypointName = StringArgumentType.getString(commandSourceStackCommandContext, "waypointName");
        return ServerCommands.globalShare(waypointName, commandSourceStackCommandContext.getSource().getPlayer().getUUID(), ObjectType.WAYPOINT, false);
    }

    public static HashMap<String, ServerObject> getUserObjectsAsNameHashmap(UUID playerUUID, ObjectType objectType, boolean global, boolean onlyShared)
    {
        HashMap<String, ServerObject> stringServerObjectHashMap = new HashMap<>();
        for (ServerObject object : JMWSServerIO.getObjectsForUser(playerUUID, objectType, global))
        {
            String nonDupeIdentifier = object.getObjectNonDuplicateIdentifier();
            if ((!object.syncing.isGlobal() && !onlyShared) || global || (onlyShared && !object.syncing.sharedTo.isEmpty()))
            {
                stringServerObjectHashMap.put(nonDupeIdentifier, object);
            }
        }

        return stringServerObjectHashMap;
    }

    public static HashMap<String, ServerObject> getInactiveOpUserGlobalObjects(ObjectType objectType)
    {
        return ServerCommands.getInactiveOpUserGlobalObjects(objectType);
    }

    private static CompletableFuture<Suggestions> suggestInactiveOpWp(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder)
    {
        Set<String> objNames = getInactiveOpUserGlobalObjects(ObjectType.WAYPOINT).keySet();
        return SharedSuggestionProvider.suggest(objNames, suggestionsBuilder);
    }

    private static CompletableFuture<Suggestions> suggestInactiveOpGp(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder)
    {
        Set<String> objNames = getInactiveOpUserGlobalObjects(ObjectType.GROUP).keySet();
        return SharedSuggestionProvider.suggest(objNames, suggestionsBuilder);
    }

    private static CompletableFuture<Suggestions> suggestObject(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder, ObjectType objectType)
    {
        Set<String> names = getUserObjectsAsNameHashmap(commandSourceStackCommandContext.getSource().getPlayer().getUUID(), objectType, false, false).keySet();
        return SharedSuggestionProvider.suggest(names, suggestionsBuilder);
    }

    private static CompletableFuture<Suggestions> suggestGlobalObject(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder, ObjectType objectType)
    {
        Set<String> names = getUserObjectsAsNameHashmap(commandSourceStackCommandContext.getSource().getPlayer().getUUID(), objectType, true, false).keySet();
        return SharedSuggestionProvider.suggest(names, suggestionsBuilder);
    }

    private static CompletableFuture<Suggestions> suggestSharedObject(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder, ObjectType objectType)
    {
        Set<String> names = getUserObjectsAsNameHashmap(commandSourceStackCommandContext.getSource().getPlayer().getUUID(), objectType, false, true).keySet();
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

    public static CompletableFuture<Suggestions> suggestSharedGroups(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        return suggestSharedObject(commandSourceStackCommandContext, suggestionsBuilder, ObjectType.GROUP);
    }

    public static CompletableFuture<Suggestions> suggestSharedWaypoints(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        return suggestSharedObject(commandSourceStackCommandContext, suggestionsBuilder, ObjectType.WAYPOINT);
    }

    private static boolean isValidCommandUser(CommandSourceStack commandSourceStack)
    {
        return !CommonClass.isInternalServer() || (!CommonClass.minecraftServerInstance.isSingleplayerOwner(commandSourceStack.getPlayer().nameAndId())); // No host user
    }
}
