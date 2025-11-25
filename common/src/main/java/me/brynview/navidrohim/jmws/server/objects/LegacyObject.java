package me.brynview.navidrohim.jmws.server.objects;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.syncing.SyncingInformation;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
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

    public <T extends ServerObject> T transition(UUID owner, ObjectType newType)
    {
        try {
            this.setCustomData(SyncingInformation.getEmptySyncingInfoString(getCustomData(), owner, false));
            Constructor<? extends ServerObject> constructor = newType.getObjectClass().getConstructor(JsonObject.class, UUID.class);
            return (T) constructor.newInstance(this.payload, owner);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException initExc)
        {
            Constants.getLogger().error("Could not transition pre-1.2.0 object to new. Error: %s".formatted(initExc));
            throw new RuntimeException(initExc);
        }
    }
}
