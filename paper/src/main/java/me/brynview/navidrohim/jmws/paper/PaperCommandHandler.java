package me.brynview.navidrohim.jmws.paper;

import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.server.commands.ServerCommands;
import me.brynview.navidrohim.jmws.server.exceptions.ServerConfigurationException;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PaperCommandHandler implements CommandExecutor, TabCompleter {
    private static final List<String> ROOT_SUBCOMMANDS = List.of("reload");
    private static final List<String> ADMIN_SUBCOMMANDS = List.of(
            "create_global_waypoint",
            "create_global_group",
            "remove_global_waypoint",
            "remove_global_group",
            "remove_global_no_op"
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase(Locale.ROOT);
        return switch (commandName) {
            case "share_waypoint" -> share(sender, args, ObjectType.WAYPOINT);
            case "share_group" -> share(sender, args, ObjectType.GROUP);
            case "stop_sharing_waypoint" -> stopSharing(sender, args, ObjectType.WAYPOINT);
            case "stop_sharing_group" -> stopSharing(sender, args, ObjectType.GROUP);
            case "jmws_handshake" -> handshake(sender);
            case "jmws_admin" -> admin(sender, args);
            case "jmws" -> root(sender, args);
            default -> false;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player player = sender instanceof Player p ? p : null;
        String commandName = command.getName().toLowerCase(Locale.ROOT);

        if (commandName.equals("share_waypoint") || commandName.equals("share_group")) {
            if (args.length == 1) {
                return filterPrefix(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[0]);
            }
            if (player != null) {
                ObjectType type = commandName.equals("share_waypoint") ? ObjectType.WAYPOINT : ObjectType.GROUP;
                return filterPrefix(getUserObjectsAsNameHashmap(player.getUniqueId(), type, false, false).keySet(), currentGreedyValue(args, 1));
            }
        }

        if ((commandName.equals("stop_sharing_waypoint") || commandName.equals("stop_sharing_group")) && player != null) {
            ObjectType type = commandName.equals("stop_sharing_waypoint") ? ObjectType.WAYPOINT : ObjectType.GROUP;
            return filterPrefix(getUserObjectsAsNameHashmap(player.getUniqueId(), type, false, true).keySet(), currentGreedyValue(args, 0));
        }

        if (commandName.equals("jmws")) {
            if (args.length == 1 && sender.hasPermission("jmws.reload")) {
                return filterPrefix(ROOT_SUBCOMMANDS, args[0]);
            }
            return List.of();
        }

        if (commandName.equals("jmws_admin") && player != null) {
            return completeAdmin(player.getUniqueId(), args);
        }

        return List.of();
    }

    private boolean share(CommandSender sender, String[] args, ObjectType objectType) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }
        if (args.length < 2) {
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("That player is not online.");
            return true;
        }

        String objectName = joinArgs(args, 1);
        ServerCommands.share(player.getUniqueId(), target.getUniqueId(), objectName, objectType);
        return true;
    }

    private boolean stopSharing(CommandSender sender, String[] args, ObjectType objectType) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }
        if (args.length < 1) {
            return false;
        }

        ServerCommands.removeShare(player.getUniqueId(), joinArgs(args, 0), objectType);
        return true;
    }

    private boolean handshake(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }

        PlayerNetworkingHelper.sendHandshakeAndValidate(player.getUniqueId());
        return true;
    }

    private boolean root(CommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            return reload(sender);
        }
        return false;
    }

    private boolean reload(CommandSender sender) {
        if (!sender.hasPermission("jmws.reload")) {
            sender.sendMessage("You do not have permission to reload JMWS.");
            return true;
        }

        JmwsPaperPlugin plugin = JmwsPaperPlugin.getInstance();
        if (plugin == null) {
            sender.sendMessage("JMWS is not ready to reload yet.");
            return true;
        }

        try {
            int refreshedClients = plugin.reloadPaperConfig();
            sender.sendMessage("JMWS config reloaded from " + PaperServerOptions.getConfigPath() + ". Refreshed " + refreshedClients + " online client handshake(s).");
        } catch (ServerConfigurationException | IllegalArgumentException error) {
            sender.sendMessage("Could not reload JMWS config: " + error.getMessage());
        }
        return true;
    }

    private boolean admin(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }
        if (!sender.hasPermission("jmws.admin")) {
            sender.sendMessage("You do not have permission to use JMWS admin commands.");
            return true;
        }
        if (args.length < 2) {
            return false;
        }

        UUID playerUuid = player.getUniqueId();
        String subcommand = args[0].toLowerCase(Locale.ROOT);
        return switch (subcommand) {
            case "create_global_waypoint" -> runGlobal(playerUuid, args, ObjectType.WAYPOINT, true);
            case "create_global_group" -> runGlobal(playerUuid, args, ObjectType.GROUP, true);
            case "remove_global_waypoint" -> runGlobal(playerUuid, args, ObjectType.WAYPOINT, false);
            case "remove_global_group" -> runGlobal(playerUuid, args, ObjectType.GROUP, false);
            case "remove_global_no_op" -> runRemoveGlobalNoOp(playerUuid, args);
            default -> false;
        };
    }

    private boolean runGlobal(UUID playerUuid, String[] args, ObjectType objectType, boolean makeGlobal) {
        ServerCommands.globalShare(joinArgs(args, 1), playerUuid, objectType, makeGlobal);
        return true;
    }

    private boolean runRemoveGlobalNoOp(UUID playerUuid, String[] args) {
        if (args.length < 3) {
            return false;
        }

        ObjectType objectType = switch (args[1].toLowerCase(Locale.ROOT)) {
            case "waypoint" -> ObjectType.WAYPOINT;
            case "group" -> ObjectType.GROUP;
            default -> null;
        };
        if (objectType == null) {
            return false;
        }

        ServerCommands.removeGlobalFromBadOp(joinArgs(args, 2), objectType, playerUuid);
        return true;
    }

    private List<String> completeAdmin(UUID playerUuid, String[] args) {
        if (args.length == 1) {
            return filterPrefix(ADMIN_SUBCOMMANDS, args[0]);
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        if (subcommand.equals("remove_global_no_op")) {
            if (args.length == 2) {
                return filterPrefix(List.of("waypoint", "group"), args[1]);
            }

            ObjectType type = switch (args[1].toLowerCase(Locale.ROOT)) {
                case "waypoint" -> ObjectType.WAYPOINT;
                case "group" -> ObjectType.GROUP;
                default -> null;
            };
            if (type != null) {
                return filterPrefix(ServerCommands.getInactiveOpUserGlobalObjects(type).keySet(), currentGreedyValue(args, 2));
            }
        }

        return switch (subcommand) {
            case "create_global_waypoint" ->
                    filterPrefix(getUserObjectsAsNameHashmap(playerUuid, ObjectType.WAYPOINT, false, false).keySet(), currentGreedyValue(args, 1));
            case "create_global_group" ->
                    filterPrefix(getUserObjectsAsNameHashmap(playerUuid, ObjectType.GROUP, false, false).keySet(), currentGreedyValue(args, 1));
            case "remove_global_waypoint" ->
                    filterPrefix(getUserObjectsAsNameHashmap(playerUuid, ObjectType.WAYPOINT, true, false).keySet(), currentGreedyValue(args, 1));
            case "remove_global_group" ->
                    filterPrefix(getUserObjectsAsNameHashmap(playerUuid, ObjectType.GROUP, true, false).keySet(), currentGreedyValue(args, 1));
            default -> List.of();
        };
    }

    private static HashMap<String, ServerObject> getUserObjectsAsNameHashmap(UUID playerUuid, ObjectType objectType, boolean global, boolean onlyShared) {
        HashMap<String, ServerObject> objectMap = new HashMap<>();
        for (ServerObject object : JMWSServerIO.getObjectsForUser(playerUuid, objectType, global)) {
            String nonDuplicateIdentifier = object.getObjectNonDuplicateIdentifier();
            if ((!object.syncing.isGlobal() && !onlyShared) || global || (onlyShared && !object.syncing.sharedTo.isEmpty())) {
                objectMap.put(nonDuplicateIdentifier, object);
            }
        }
        return objectMap;
    }

    private static Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }
        sender.sendMessage("This JMWS command can only be used by a player.");
        return null;
    }

    private static String joinArgs(String[] args, int start) {
        return String.join(" ", List.of(args).subList(start, args.length));
    }

    private static String currentGreedyValue(String[] args, int start) {
        if (args.length <= start) {
            return "";
        }
        return joinArgs(args, start);
    }

    private static List<String> filterPrefix(Iterable<String> values, String prefix) {
        String normalizedPrefix = prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(normalizedPrefix)) {
                matches.add(value);
            }
        }
        return matches;
    }
}
