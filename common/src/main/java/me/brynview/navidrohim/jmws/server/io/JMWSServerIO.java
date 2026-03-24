package me.brynview.navidrohim.jmws.server.io;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.server.exceptions.ObjectError;
import me.brynview.navidrohim.jmws.server.objects.LegacyObject;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
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

    public static <T extends ServerObject> List<T> getAllInitialisedGlobalObjects(ObjectType globalObjectType)
    {
        List<T> wp = new ArrayList<>();
        for (Path path : getAllObjects(globalObjectType).toList())
        {
            if (path.toString().contains(globalObjPrefix))
            {
                UUID playerUUID = PathUtils.getUUIDFromPath(path, globalObjectType);
                wp.add(getObjectFromFile(path, playerUUID, globalObjectType));
            }
        }
        return wp;
    }

    public static class PathUtils
    {
        public static String makeFilename(String objectID, UUID playerOwner, boolean isGlobal)
        {
            return "%s%s#%s.json".formatted(isGlobal ? JMWSServerIO.globalObjPrefix : "", objectID, playerOwner);
        }

        @Nullable
        public static Path getObjectFilename(@Nullable UUID playerOwner, String objectID, ObjectType objectType, boolean isGlobal) {
            try
            {
                return Path.of(ObjectType.getPathLocationPrefix(objectType) + makeFilename(objectID, playerOwner, isGlobal));
            } catch (InvalidPathException oldVersion)
            {
                return null;
            }
        }

        public static UUID getUUIDFromPath(Path path, ObjectType transitionType)
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

    public static Stream<Path> getAllObjects(ObjectType objectType)
    {
        String pathSearch = ObjectType.getPathLocationPrefix(objectType);
        try {
            return Files.list(Path.of(pathSearch));
        } catch (SecurityException e)
        {
            Constants.getLogger().error("FATAL: Missing permissions! cannot read from {}", pathSearch);
        } catch (IOException ignored) {}
        return Stream.of();
    }

    private static List<Path> getObjectPathsForUser(UUID uuid, ObjectType objectType, boolean global) {

        List<Path> waypointFileList = new ArrayList<>();
        String pathSearch = ObjectType.getPathLocationPrefix(objectType);
        String globalPrefix = global ? globalObjPrefix : "";

        try (Stream<Path> files = Files.list(Path.of(pathSearch))) {
            files.filter(Files::isRegularFile).forEach(path -> {
                if (path.toString().contains(uuid.toString()) && path.toString().contains(globalPrefix)) {
                    waypointFileList.add(path);
                }
            });
        } catch (NoSuchFileException exc) {
            CommonClass.createServerResources();
            return getObjectPathsForUser(uuid, objectType, global);
        } catch (IOException err) {
            Constants.getLogger().error("Got error trying to get user objects: {}", err.getMessage());
            return List.of();
            }
        return waypointFileList;
    }

    public static void validateUserObjects(UUID userUUID)
    {
        try (Stream<Path> wpFiles = Files.list(Path.of(ObjectType.WAYPOINT.getObjectPathPrefix())) ; Stream<Path> gpFiles = Files.list(Path.of(ObjectType.GROUP.getObjectPathPrefix()))) {
            wpFiles.filter(Files::isRegularFile).forEach(path -> {
                if (path.toString().contains(userUUID.toString())) {
                    getObjectFromFile(path, userUUID, ObjectType.WAYPOINT);
                }
            });
            gpFiles.filter(Files::isRegularFile).forEach(path -> {
                if (path.toString().contains(userUUID.toString())) {
                    getObjectFromFile(path, userUUID, ObjectType.GROUP);
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static List<Path> getObjectPathsForUser(UUID uuid, ObjectType objectType) {
        return getObjectPathsForUser(uuid, objectType, false);
    }

    public static <T extends ServerObject> List<T> getObjectsForUser(UUID user, ObjectType objectType, boolean global)
    {
        List<T> list = new ArrayList<>();

        for (Path objPath : getObjectPathsForUser(user, objectType, global))
        {
            list.add((T) getObjectFromFile(objPath, user, objectType));
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

    public static <T extends ServerObject> T getObjectFromFile(Path objPath, UUID user, ObjectType objectType, boolean silentFail)
    {
        try {
            @Nullable JsonObject data = getObjectDataFromDisk(objPath, silentFail);
            if (data != null && objPath != null)
            {
                Constructor<? extends ServerObject> constructor = objectType.getObjectClass().getConstructor(JsonObject.class, UUID.class);
                return (T) constructor.newInstance(data, PathUtils.getUUIDFromPath(objPath, objectType));
            } else {
                return null;
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException initExc)
        {
            throw new RuntimeException("Cannot pass %s to getObjectFromDisk TODO ERROR: %s".formatted("PLACEHOLDER", initExc));
        }
    }

    public static <T extends ServerObject> T getObjectFromFile(Path objPath, UUID user, ObjectType objectType)
    {
        return getObjectFromFile(objPath, user, objectType, false);
    }

    public static HashMap<String, Path> getNameHashmapLookup(UUID user, ObjectType objectType)
    {
        HashMap<String, Path> map = new HashMap<>();
        for (ServerObject obj : getObjectsForUser(user, objectType, false))
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
        return data != null ? JsonParser.parseString(data).getAsJsonObject() : null;
    }

    @Nullable
    public static <T extends ServerObject> T getObjectFromDisk(String objectIdentifier, UUID ownerUUID, ObjectType objectType, boolean silentFail, boolean global) {
        Path objPath = PathUtils.getObjectFilename(ownerUUID, objectIdentifier, objectType, global);
        if (objPath != null)
        {
            return getObjectFromFile(objPath, ownerUUID, objectType, silentFail);
        }
        return null;
    }

    @Nullable
    public static <T extends ServerObject> T getObjectFromDisk(String objectIdentifier, UUID ownerUUID, ObjectType objectType) {
        return getObjectFromDisk(objectIdentifier, ownerUUID, objectType, false, false);
    }

    @Nullable
    public static Path getObjectPathFromUniqueIdentifier(String identifier, ObjectType objectType)
    {
        for (Path objectPath : getAllObjects(objectType).toList())
        {
            if (objectPath.toString().contains(identifier))
            {
                return objectPath;
            }
        }
        return null;
    }

    @Nullable
    public static <T extends ServerObject> T getObjectFromUniqueIdentifier(String identifier, UUID playerUUID, ObjectType objectType)
    {
        Path objectPath = getObjectPathFromUniqueIdentifier(identifier, objectType);
        if (objectPath != null)
        {
            return getObjectFromFile(objectPath, playerUUID, objectType, false);
        }
        return null;
    }
}

