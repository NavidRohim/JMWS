package me.brynview.navidrohim.jmws.server.io;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
import me.brynview.navidrohim.jmws.common.share.io.CommonShareIO;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerShareIO extends CommonShareIO
{

    public ServerShareIO(String sharedObjectFilePath) {
        super(sharedObjectFilePath);
    }

    private boolean didRemove = false;

    public boolean deleteSharedObject()
    {
        this.didRemove = true;
        return CommonHelper.deleteFile(this.objectPath.toString());
    }

    @Override
    public void close()
    {
        if (!didRemove)
        {
            super.close();
        }
    }
}
