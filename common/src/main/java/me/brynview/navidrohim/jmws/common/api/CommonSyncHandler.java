package me.brynview.navidrohim.jmws.common.api;

import com.google.gson.annotations.Expose;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class CommonSyncHandler
{
    @Expose
    public String objectIdentifier;

    @Expose
    public List<String> sharedTo;

    @Expose
    protected UUID owner;

    @Expose
    protected boolean isGlobal;

    @Nullable
    protected ServerObject parentObject = null;

    void addUserToShare(UUID userUUID);
    void removeUserFromShare(UUID userUUID);
    void removeAllFromShare();
}
