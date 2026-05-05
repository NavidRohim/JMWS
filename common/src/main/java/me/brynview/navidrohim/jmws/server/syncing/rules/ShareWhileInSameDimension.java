package me.brynview.navidrohim.jmws.server.syncing.rules;

import me.brynview.navidrohim.jmws.server.objects.JMServerObject;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.syncing.rules.api.ShareRule;
import net.minecraft.server.level.ServerPlayer;

public class ShareWhileInSameDimension implements ShareRule
{
    @Override
    public boolean passed(ServerObject object, ServerPlayer player)
    {
        // Will only work with JM native synced objects. Check for it (Has to be ServrWaypoint or ServerGroup)
        if (object instanceof JMServerObject && ((JMServerObject) object).getDimension() != null)
        {

            return ((JMServerObject) object).getDimension().equals(player.level().dimension());
        }
        return true;
    }

    @Override
    public String getRegistryKey()
    {
        return "";
    }
}
