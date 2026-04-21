package me.brynview.navidrohim.jmws.server.io;

import com.google.gson.JsonObject;

import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.api.ServerSyncInformation;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;
import me.brynview.navidrohim.jmws.common.utils.CommonUtils;
import me.brynview.navidrohim.jmws.server.exceptions.ObjectError;
import me.brynview.navidrohim.jmws.server.objects.LegacyObject;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.registry.ServerSyncRegistry;
import me.brynview.navidrohim.jmws.server.registry.ServerSyncRegistryEntry;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class JMWSServerIO {

    public static final String globalObjPrefix = "GLOBAL_";

    public static <T extends ServerObject> List<T> getAllInitialisedGlobalObjects(ServerSyncRegistryEntry globalServerSyncRegistry)
    {
        List<T> wp = new ArrayList<>();
        for (Path path : getAllObjects(globalServerSyncRegistry).toList())
        {
            if (path.toString().contains(globalObjPrefix))
            {
                UUID playerUUID = PathUtils.getUUIDFromPath(path, globalServerSyncRegistry);
                wp.add(getObjectFromFile(path, playerUUID, globalServerSyncRegistry));
            }
        }
        return wp;
    }

    public static List<Path> getGlobalObjects(ServerSyncRegistryEntry globalServerSyncRegistry)
    {
        List<Path> wp = new ArrayList<>();
        for (Path path : getAllObjects(globalServerSyncRegistry).toList())
        {
            if (path.toString().contains(globalObjPrefix))
            {
                wp.add(path);
            }
        }
        return wp;
    }

    public static void removeObjectFromUser(ServerObject serverObject, UUID playerUUID, String objectIdentifier, ServerSyncRegistryEntry serverSyncRegistry) {
        UserSharingFile.removeObjectFromUser(playerUUID, objectIdentifier, serverObject.getObjectType());
        ServerPlayer sharedPlayer = JMWSCommon.minecraftServerInstance.getPlayerList().getPlayer(playerUUID);
        if (sharedPlayer != null) {
            if (serverSyncRegistry == ServerSyncRegistry.WAYPOINT) {
                Dispatcher.sendToClient(new JMWSActionPayload(CommandFactory.deleteWaypoint(objectIdentifier, true, false)), sharedPlayer); // TODO: SERVER
            } else {
                Dispatcher.sendToClient(new JMWSActionPayload(CommandFactory.deleteGroup(objectIdentifier, null, true, true, true, false, false)), sharedPlayer);
            }
        }
    }

    public static class PathUtils
    {
        public static String makeFilename(String objectID, UUID playerOwner, boolean isGlobal)
        {
            return "%s%s#%s.json".formatted(isGlobal ? JMWSServerIO.globalObjPrefix : "", objectID, playerOwner);
        }

        @Nullable
        public static Path getObjectFilename(@Nullable UUID playerOwner, String objectID, ServerSyncRegistryEntry serverSyncRegistry, boolean isGlobal) {
            try
            {
                Constants.LoggerHolder.debug(serverSyncRegistry.getRegistryPath(), "RG PATH");
                return Path.of(serverSyncRegistry.getRegistryPath() + makeFilename(objectID, playerOwner, isGlobal));
            } catch (InvalidPathException oldVersion)
            {
                return null;
            }
        }

        public static UUID getUUIDFromPath(Path path, ServerSyncRegistryEntry transitionType)
        {
            String pathString = path.toString();
            String uuidString = pathString.substring(pathString.indexOf("#") + 1, pathString.length() - 5);

            try
            {
                return UUID.fromString(uuidString);
            } catch (IllegalArgumentException | IndexOutOfBoundsException err)
            {
                try {
                    int uuidEnd = !pathString.contains("group") ? 5 : 11;

                    UUID uuidFromLegacy = UUID.fromString(pathString.substring(pathString.lastIndexOf("_") + 1, pathString.length() - uuidEnd));
                    LegacyObject.transitionIfNeed(path, uuidFromLegacy, transitionType);

                    return uuidFromLegacy;
                } catch (IllegalArgumentException | IndexOutOfBoundsException err2)
                {
                    throw new ObjectError("UUID is malformed. UUID: %s From String: %s".formatted(uuidString, pathString));
                }
            }
        }
    }

    public static Stream<Path> getAllObjects(ServerSyncRegistryEntry serverSyncRegistry)
    {
        Path pathSearch = Path.of(serverSyncRegistry.getRegistryPath());
        try {
            return Files.list(pathSearch);
        } catch (SecurityException e)
        {
            Constants.getLogger().error("FATAL: Missing permissions! cannot read from {}", pathSearch);
        } catch (IOException ignored) {}
        return Stream.of();
    }

    private static List<Path> getObjectPathsForUser(UUID uuid, ServerSyncRegistryEntry serverSyncRegistry, boolean global) {

        List<Path> waypointFileList = new ArrayList<>();
        System.out.println(serverSyncRegistry);
        Path pathSearch = Path.of(serverSyncRegistry.getRegistryPath());
        String globalPrefix = global ? globalObjPrefix : "";

        try (Stream<Path> files = Files.list(pathSearch)) {
            files.filter(Files::isRegularFile).forEach(path -> {
                if (path.toString().contains(uuid.toString()) && path.toString().contains(globalPrefix)) {
                    waypointFileList.add(path);
                }
            });
        } catch (NoSuchFileException exc) {
            JMWSCommon.createServerResources();
            return getObjectPathsForUser(uuid, serverSyncRegistry, global);
        } catch (IOException err) {
            Constants.getLogger().error("Got error trying to of user objects: {}", err.getMessage());
            return List.of();
            }
        return waypointFileList;
    }

    public static void validateUserObjects(UUID userUUID)
    {
        try (Stream<Path> wpFiles = Files.list(Path.of(ServerSyncRegistry.WAYPOINT.getRegistryPath())); Stream<Path> gpFiles = Files.list(Path.of(ServerSyncRegistry.GROUP.getRegistryPath()))) {
            wpFiles.filter(Files::isRegularFile).forEach(path -> {
                if (path.toString().contains(userUUID.toString())) {
                    getObjectFromFile(path, userUUID, ServerSyncRegistry.WAYPOINT);
                }
            });
            gpFiles.filter(Files::isRegularFile).forEach(path -> {
                if (path.toString().contains(userUUID.toString())) {
                    getObjectFromFile(path, userUUID, ServerSyncRegistry.GROUP);
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static List<Path> getObjectPathsForUser(UUID uuid, ServerSyncRegistryEntry serverSyncRegistry) {
        return getObjectPathsForUser(uuid, serverSyncRegistry, false);
    }

    public static <T extends ServerObject> List<T> getObjectsForUser(UUID user, ServerSyncRegistryEntry serverSyncRegistry, boolean global)
    {
        List<T> list = new ArrayList<>();

        for (Path objPath : getObjectPathsForUser(user, serverSyncRegistry, global))
        {
            list.add((T) getObjectFromFile(objPath, user, serverSyncRegistry));
        }

        return list;
    }

    /*
    public static <T extends ServerObject> List<T> getAllGlobals(ObjectType objectType)
    {
        List<T> list = new ArrayList<>();

        try (Stream<Path> wpFiles = Files.list(Path.of(objectType.getObjectPathPrefix())))
        {
            wpFiles.filter(Files::isRegularFile).forEach(path -> {
                if (path.toString().contains(globalObjPrefix)) {

                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    public static <T extends ServerObject> T getObjectFromFile(Path objPath, UUID user, ServerSyncRegistryEntry serverSyncRegistry, boolean silentFail)
    {
        try {
            @Nullable JsonObject data = getObjectDataFromDisk(objPath, silentFail);
            if (data != null && objPath != null)
            {
                Constructor<? extends ServerObject> constructor = serverSyncRegistry.getRegistryClass().getConstructor(JsonObject.class, UUID.class);
                return (T) constructor.newInstance(data, PathUtils.getUUIDFromPath(objPath, serverSyncRegistry));
            } else {
                return null;
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException initExc)
        {
            // This catch field should never ever be used.
            throw new RuntimeException("Error constructing ServerObject: DEBUG (Exception, exception message)-> %s, %s".formatted(initExc, initExc.getMessage()));
        }
    }

    public static <T extends ServerObject> T getObjectFromFile(Path objPath, UUID user, ServerSyncRegistryEntry serverSyncRegistry)
    {
        return getObjectFromFile(objPath, user, serverSyncRegistry, false);
    }

    public static HashMap<String, Path> getNameHashmapLookup(UUID user, ServerSyncRegistryEntry serverSyncRegistry)
    {
        HashMap<String, Path> map = new HashMap<>();
        for (ServerObject obj : getObjectsForUser(user, serverSyncRegistry, false))
        {
            map.put(obj.getObjectNonDuplicateIdentifier(), obj.getCurrentObjectPath());
        }

        return map;
    }

    @Nullable
    public static String readRaw(Path objPath, boolean silentFail)
    {
        try {
            return Files.readString(objPath);
        } catch (IOException | NullPointerException ioException)
        {
            if (!silentFail)
            {
                Constants.getLogger().error("Error retrieving saved object data -> {}", ioException.getMessage());
            }
        }
        return null;
    }

    @Nullable
    public static JsonObject getObjectDataFromDisk(Path objPath, boolean silentFail) {
        String data = readRaw(objPath, silentFail);
        return data != null ? CommonUtils.parseStringToJsonObject(data) : null;
    }

    @Nullable
    public static <T extends ServerObject> T getObjectFromDisk(String objectIdentifier, UUID ownerUUID, ServerSyncRegistryEntry serverSyncRegistry, boolean silentFail, boolean global) {
        Path objPath = PathUtils.getObjectFilename(ownerUUID, objectIdentifier, serverSyncRegistry, global);
        if (objPath != null)
        {
            return getObjectFromFile(objPath, ownerUUID, serverSyncRegistry, silentFail);
        }
        return null;
    }

    @Nullable
    public static <T extends ServerObject> T getObjectFromDisk(String objectIdentifier, UUID ownerUUID, ServerSyncRegistryEntry serverSyncRegistry) {
        return getObjectFromDisk(objectIdentifier, ownerUUID, serverSyncRegistry, false, false);
    }

    @Nullable
    public static Path getObjectPathFromUniqueIdentifier(String identifier, ServerSyncRegistryEntry serverSyncRegistry)
    {
        for (Path objectPath : getAllObjects(serverSyncRegistry).toList())
        {
            if (objectPath.toString().contains(identifier))
            {
                return objectPath;
            }
        }
        return null;
    }

    @Nullable
    public static <T extends ServerObject> T getObjectFromUniqueIdentifier(String identifier, UUID playerUUID, ServerSyncRegistryEntry serverSyncRegistry)
    {
        Path objectPath = getObjectPathFromUniqueIdentifier(identifier, serverSyncRegistry);
        if (objectPath != null)
        {
            return getObjectFromFile(objectPath, playerUUID, serverSyncRegistry, false);
        }
        return null;
    }

    @Nullable
    public static <T extends ServerObject> T getObjectFromSyncInformation(ServerSyncInformation syncInfo)
    {
        @Nullable Path objPath = JMWSServerIO.PathUtils.getObjectFilename(syncInfo.owner, syncInfo.objectIdentifier, syncInfo.syncRegistryType, syncInfo.isGlobal);
        if (objPath != null)
        {
            return getObjectFromFile(objPath, null, syncInfo.syncRegistryType);
        }
        return null;
    }
}

