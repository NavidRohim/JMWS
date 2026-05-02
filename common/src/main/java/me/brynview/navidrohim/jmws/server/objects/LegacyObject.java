package me.brynview.navidrohim.jmws.server.objects;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.server.syncing.registry.ServerSyncRegistry;
import me.brynview.navidrohim.jmws.server.syncing.registry.ServerSyncRegistryEntry;

import me.brynview.navidrohim.jmws.common.utils.CommonUtils;
import me.brynview.navidrohim.jmws.common.syncing.SyncUtils;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.UUID;

public class LegacyObject
{
    protected final JsonObject payload;
    protected final String rawPacketData;
    protected JsonObject customDataJmwsFieldObject;
    protected String customData;

    protected boolean didTransitionToNewData = false;

    public LegacyObject(JsonObject payload) throws IllegalStateException {
        this.payload = payload;
        this.rawPacketData = payload.toString();
        //try
        //{
        Constants.LoggerHolder.debug(payload, "Legacy Object Payload");
            this.customData = payload.get("customDataMap").getAsJsonObject().get(Constants.MODID).getAsString();
            this.customDataJmwsFieldObject = payload.get("customDataMap").getAsJsonObject();
        /*} catch (IllegalStateException | NullPointerException e) // catch old customData field.
        {
            if (payload.has("customData"))
            {
                String customDataForJMWSLegacy = payload.get("customData").getAsString();

                payload.add("customDataMap", new JsonObject());
                payload.get("customDataMap").getAsJsonObject().add(Constants.MODID, new JsonPrimitive(customDataForJMWSLegacy));

                this.customData = payload.get("customDataMap").getAsJsonObject().get(Constants.MODID).getAsString();
                this.customDataJmwsFieldObject = payload.get("customDataMap").getAsJsonObject();
                payload.remove("customData");

                this.didTransitionToNewData = true;
            } else {
                throw new IllegalStateException("Unable to parse legacy object. customData doesn't exist which likely means the object has been tampered with.");
            }
        }*/
    }

    public String getOldCustomData() { return this.customData; }

    public void setSyncedCustomData(String data)
    {
        this.customData = data;
        this.customDataJmwsFieldObject.add(Constants.MODID, new JsonPrimitive(data));
    }

    public static <T extends ServerObject> void transitionIfNeed(Path path, UUID owner, ServerSyncRegistryEntry<T> newType)
    {
        try {
            JsonObject payload = JMWSServerIO.getObjectDataFromDisk(path, true);
            if (payload != null)
            {
                LegacyObject oldObj = new LegacyObject(payload);
                if (SyncUtils.isLegacySyncField(oldObj.getOldCustomData()))
                {
                    oldObj.setSyncedCustomData(SyncUtils.getEmptySyncingInfoString(oldObj.getOldCustomData(), owner, false, newType));
                    Constructor<T> constructor = newType.getRegistryClass().getConstructor(JsonObject.class, UUID.class);
                    T newObj = constructor.newInstance(payload, owner);
                    newObj.create();

                    CommonUtils.deleteFile(path);
                }
            } else {
                Constants.getLogger().debug("Possible issue translating server object. If issue arises please report.");
                Constants.getLogger().debug("Diagnostic \nObject Path: {}\nOwner UUID: {}\nObjectType: {}\nInternal Server: {}\n\nIf in an internal server, you can likely ignore this message.\n\n", path, owner, newType, JMWSCommon.isInternalServer());
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException | IllegalStateException initExc)
        {
            Constants.getLogger().error("Could not transition pre-1.2.0 object to new. Error: {}", initExc);
            throw new RuntimeException(initExc);
        }
    }
}
