package me.brynview.navidrohim.jmws;


import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.brynview.navidrohim.jmws.client.ClientVariables;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.server.Server;
import me.brynview.navidrohim.jmws.server.ServerCommands;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Jmws implements ModInitializer {

    @Override
    public void onInitialize() {

        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        FabricLoader fabricLoader = FabricLoader.getInstance();
        boolean isJMLoaded = fabricLoader.isModLoaded("journeymap");

        // Check if JourneyMap is installed, and what version (I hate this solution by the way, will change eventually)
        // This has to be here because if it is not the client will crash when connecting to a server when JM is not installed.
        // Why cant I just specify JM needs to be installed in fabric.mod.json? Well because fabric is lacking a feature to specify if a dependency is on
        // client, server or both sides. (on server, only commonnetworking is needed. On the client, CommonNetworking and JourneyMap is required but CommonNetworking
        // is bundled with JM)
        // TLDR; Version checking is required because server has to have CommonNetworking and client doesn't implicitly need it but JMWS needs JourneyMap.

        try {

            if (fabricLoader.getEnvironmentType() == EnvType.CLIENT) {
                if (isJMLoaded) {
                    Optional<ModContainer> jmModContainer = fabricLoader.getModContainer("journeymap");
                    if (jmModContainer.isPresent()) {
                        String versionString = jmModContainer.get().getMetadata().getVersion().getFriendlyString();

                        SemanticVersion minAllowedVersion = SemanticVersion.parse(Constants.JourneyMapVersionString);
                        SemanticVersion betaVersion = SemanticVersion.parse(versionString);

                        int mcVersionMinor = betaVersion.getVersionComponent(1);
                        int mcVersionPatch = betaVersion.getVersionComponent(2);

                        int minMcVersionMinor = minAllowedVersion.getVersionComponent(1);
                        int minMcVersionPatch = minAllowedVersion.getVersionComponent(2);

                        Matcher regexBetaVersionPatternMinMatcher = Pattern.compile("beta\\.([0-9]+)").matcher(minAllowedVersion.toString());
                        Matcher regexBetaVersionPatternJarMatcher = Pattern.compile("beta\\.([0-9]+)").matcher(betaVersion.toString());

                        regexBetaVersionPatternMinMatcher.find();
                        regexBetaVersionPatternJarMatcher.find();

                        int jarVersionString = Integer.parseInt(regexBetaVersionPatternJarMatcher.group(1));
                        int minVersionString = Integer.parseInt(regexBetaVersionPatternMinMatcher.group(1));

                        ClientVariables.clientJMVersion = versionString;

                        if ((mcVersionMinor >= minMcVersionMinor && mcVersionPatch == minMcVersionPatch && jarVersionString >= minVersionString)) {
                            Constants.getLogger().info("Good to go. JMWS Version %s with JourneyMap Version %s on client-side.".formatted(Constants.VERSION, versionString));
                            ClientVariables.clientHasJM = true;
                            CommonClass.init();
                        }
                    }
                }

            } else {
                Constants.getLogger().info("JourneyMap is optional on the server. If you get a warning about it, you can safely ignore it.");
                CommonClass.init();
            }
        } catch (NoSuchElementException | VersionParsingException | IllegalStateException ignored) {

        }

        CommandRegistrationCallback.EVENT.register((dispatcher, context, commandSelection) -> {

            dispatcher.register(Commands.literal("share_waypoint")
                    .then(Commands.argument("username", EntityArgument.player()).then(Commands.argument("waypointName", StringArgumentType.greedyString()).suggests(Server::suggestWaypoints).executes(Jmws::doShareWaypoint)))
            );
            dispatcher.register(Commands.literal("share_group")
                    .then(Commands.argument("username", EntityArgument.player()).then(Commands.argument("groupName", StringArgumentType.greedyString()).suggests(Server::suggestGroups).executes(Jmws::doShareGroup)))
            );
            dispatcher.register(Commands.literal("share_group_stop")
                    .then(Commands.argument("username", EntityArgument.player()).then(Commands.argument("groupName", StringArgumentType.greedyString()).suggests(Server::suggestGroups).executes(Jmws::doRemoveShareGroup)))
            );
            dispatcher.register(Commands.literal("share_waypoint_stop")
                    .then(Commands.argument("username", EntityArgument.player()).then(Commands.argument("waypointName", StringArgumentType.greedyString()).suggests(Server::suggestGroups).executes(Jmws::doRemoveShareWaypoint)))
            );

            dispatcher.register(Commands.literal("jmws_admin")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.literal("create_global_waypoint")
                            .then(Commands.argument("waypointName", StringArgumentType.greedyString())
                                    .suggests(Server::suggestWaypoints)
                                    .executes(Jmws::createServerWp)))
                    .then(Commands.literal("create_global_group")
                            .then(Commands.argument("groupName", StringArgumentType.greedyString())
                                    .suggests(Server::suggestGroups)
                                    .executes(Jmws::createServerGp)))
                    .then(Commands.literal("remove_global_group")
                            .then(Commands.argument("groupName", StringArgumentType.greedyString())
                                    .suggests(Server::suggestGlobalGroups)
                                    .executes(Jmws::removeServerGp)))
                    .then(Commands.literal("remove_global_waypoint")
                            .then(Commands.argument("waypointName", StringArgumentType.greedyString())
                                    .suggests(Server::suggestGlobalWaypoints)
                                    .executes(Jmws::removeServerWp)))
            );
        });
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
