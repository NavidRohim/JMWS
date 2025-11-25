package me.brynview.navidrohim.jmws.server.objects;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
import me.brynview.navidrohim.jmws.common.syncing.SyncingInformation;
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

    public String getCustomData() { return this.customData; }

    public void setCustomData(String data)
    {
        this.customData = data;
        this.payload.add("customData", new JsonPrimitive(data));
    }

    @Nullable
    public static <T extends ServerObject> T transitionIfNeed(Path path, UUID owner, ObjectType newType)
    {
        try {
            JsonObject payload = JMWSServerIO.getObjectDataFromDisk(path, false);
            if (payload != null)
            {
                LegacyObject oldObj = new LegacyObject(payload);
                if (isLegacyDataField(oldObj.getCustomData()))
                {
                    oldObj.setCustomData(SyncingInformation.getEmptySyncingInfoString(oldObj.getCustomData(), owner, false));
                    Constructor<? extends ServerObject> constructor = newType.getObjectClass().getConstructor(JsonObject.class, UUID.class);
                    T newObj = (T) constructor.newInstance(payload, owner);
                    newObj.create();

                    CommonHelper.deleteFile(path);
                    return newObj;
                }
                return null;
            } else {
                Constants.getLogger().warn("Could not transition user object.");
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
