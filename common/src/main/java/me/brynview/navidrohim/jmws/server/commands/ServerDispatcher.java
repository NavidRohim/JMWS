package me.brynview.navidrohim.jmws.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.server.Server;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class ServerDispatcher {
    public static void addCommandsToDispatcher(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("share_waypoint")
                .requires(CommandSourceStack::isPlayer)
                .then(Commands.argument("username", EntityArgument.player()).then(Commands.argument("waypointName", StringArgumentType.greedyString()).suggests(Server::suggestWaypoints).executes(ServerDispatcher::doShareWaypoint)))
        );
        dispatcher.register(Commands.literal("share_group")
                .requires(CommandSourceStack::isPlayer)
                .then(Commands.argument("username", EntityArgument.player()).then(Commands.argument("groupName", StringArgumentType.greedyString()).suggests(Server::suggestGroups).executes(ServerDispatcher::doShareGroup)))
        );
        dispatcher.register(Commands.literal("share_group_stop")
                .requires(CommandSourceStack::isPlayer)
                .then(Commands.argument("username", EntityArgument.player()).then(Commands.argument("groupName", StringArgumentType.greedyString()).suggests(Server::suggestGroups).executes(ServerDispatcher::doRemoveShareGroup)))
        );
        dispatcher.register(Commands.literal("share_waypoint_stop")
                .requires(CommandSourceStack::isPlayer)
                .then(Commands.argument("username", EntityArgument.player()).then(Commands.argument("waypointName", StringArgumentType.greedyString()).suggests(Server::suggestGroups).executes(ServerDispatcher::doRemoveShareWaypoint)))
        );

        dispatcher.register(Commands.literal("jmws_admin")
                .requires(src -> src.hasPermission(2) && src.isPlayer())
                .then(Commands.literal("create_global_waypoint")
                        .then(Commands.argument("waypointName", StringArgumentType.greedyString())
                                .suggests(Server::suggestWaypoints)
                                .executes(ServerDispatcher::createServerWp)))
                .then(Commands.literal("create_global_group")
                        .then(Commands.argument("groupName", StringArgumentType.greedyString())
                                .suggests(Server::suggestGroups)
                                .executes(ServerDispatcher::createServerGp)))
                .then(Commands.literal("remove_global_group")
                        .then(Commands.argument("groupName", StringArgumentType.greedyString())
                                .suggests(Server::suggestGlobalGroups)
                                .executes(ServerDispatcher::removeServerGp)))
                .then(Commands.literal("remove_global_waypoint")
                        .then(Commands.argument("waypointName", StringArgumentType.greedyString())
                                .suggests(Server::suggestGlobalWaypoints)
                                .executes(ServerDispatcher::removeServerWp)))
        );
    }

    private static int doRemoveShareWaypoint(CommandContext<CommandSourceStack> context1) throws CommandSyntaxException {
        String waypointID = StringArgumentType.getString(context1, "waypointName");

        return ServerCommands.removeShare(context1.getSource().getPlayer(), waypointID, ObjectType.WAYPOINT);
    }

    private static int doRemoveShareGroup(CommandContext<CommandSourceStack> context1) throws CommandSyntaxException {
        String groupID = StringArgumentType.getString(context1, "groupName");

        return ServerCommands.removeShare(context1.getSource().getPlayer(), groupID, ObjectType.WAYPOINT);
    }

    private static int doShareWaypoint(CommandContext<CommandSourceStack> context1) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context1, "username");
        String waypointID = StringArgumentType.getString(context1, "waypointName");

        return ServerCommands.share(context1.getSource().getPlayer(), player, waypointID, ObjectType.WAYPOINT);
    }

    private static int doShareGroup(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(commandSourceStackCommandContext, "username");
        String groupName = StringArgumentType.getString(commandSourceStackCommandContext, "groupName");

        return ServerCommands.share(commandSourceStackCommandContext.getSource().getPlayer(), player, groupName, ObjectType.GROUP);
    }

    private static int createServerWp(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String waypointName = StringArgumentType.getString(commandSourceStackCommandContext, "waypointName");
        return ServerCommands.globalShare(waypointName, commandSourceStackCommandContext.getSource().getPlayer(), ObjectType.WAYPOINT, true);
    }

    private static int createServerGp(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String groupName = StringArgumentType.getString(commandSourceStackCommandContext, "groupName");
        return ServerCommands.globalShare(groupName, commandSourceStackCommandContext.getSource().getPlayer(), ObjectType.GROUP, true);
    }

    private static int removeServerGp(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String groupName = StringArgumentType.getString(commandSourceStackCommandContext, "groupName");
        return ServerCommands.globalShare(groupName, commandSourceStackCommandContext.getSource().getPlayer(), ObjectType.GROUP, false);
    }

    private static int removeServerWp(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String waypointName = StringArgumentType.getString(commandSourceStackCommandContext, "waypointName");
        return ServerCommands.globalShare(waypointName, commandSourceStackCommandContext.getSource().getPlayer(), ObjectType.WAYPOINT, false);
    }

}
