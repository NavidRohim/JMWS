package me.brynview.navidrohim.jmws.server.objects;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
import me.brynview.navidrohim.jmws.common.syncing.Syncing;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.UUID;

public class LegacyObject
{
    protected final JsonObject payload;
    protected final String rawPacketData;
    protected String customData;

    public LegacyObject(JsonObject payload) {
        this.payload = payload;
        this.rawPacketData = payload.toString();
        this.customData = payload.get("customData").getAsString();
    }

    public String getOldCustomData() { return this.customData; }

    public void setSyncedCustomData(String data)
    {
        this.customData = data;
        this.payload.add("customData", new JsonPrimitive(data));
    }

    @Nullable
    public static <T extends ServerObject> T transitionIfNeed(Path path, UUID owner, ObjectType newType)
    {
        try {
            JsonObject payload = JMWSServerIO.getObjectDataFromDisk(path, true);
            if (payload != null)
            {
                LegacyObject oldObj = new LegacyObject(payload);
                if (isLegacyDataField(oldObj.getOldCustomData()))
                {
                    oldObj.setSyncedCustomData(Syncing.getEmptySyncingInfoString(oldObj.getOldCustomData(), owner, false));
                    Constructor<? extends ServerObject> constructor = newType.getObjectClass().getConstructor(JsonObject.class, UUID.class);
                    T newObj = (T) constructor.newInstance(payload, owner);
                    newObj.create();

                    CommonHelper.deleteFile(path);
                    return newObj;
                }
                return null;
            } else {
                Constants.getLogger().debug("Possible issue translating server object. If issue arises please report.");
                Constants.getLogger().debug("Diagnostic \nObject Path: %s\nOwner UUID: %s\nObjectType: %s\nInternal Server: %s\n\nIf in an internal server, you can likely ignore this message.\n\n".formatted(path, owner, newType, CommonClass.isInternalServer()));
                return null;
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException initExc)
        {
            Constants.getLogger().error("Could not transition pre-1.2.0 object to new. Error: %s".formatted(initExc));
            throw new RuntimeException(initExc);
        }
    }

    public static boolean isLegacyDataField(@Nullable String field) {
        if (field != null && field.length() == 64)
        {
            for (int i = 0; i < field.length(); i++) {
                char c = field.charAt(i);
                if (!Character.isLetterOrDigit(c))
                    return false;
            }

            return true;
        }
        return false;
    }

    public String getDifferentiator()
    {
        return ObjectType.GENERIC.toString();
    }
}
