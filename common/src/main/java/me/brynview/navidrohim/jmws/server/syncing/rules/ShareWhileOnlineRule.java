package me.brynview.navidrohim.jmws.server.syncing.rules;

import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.server.JMWSServerCommon;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.syncing.rules.api.ShareRule;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ShareWhileOnlineRule implements ShareRule
{
    @Override
    public boolean passed(ServerObject object, ServerPlayer player) {
        // Check if object owner is online by using the object's getOwnerUUID() method and checking the server player list
        return JMWSCommon.minecraftServerInstance.getPlayerList().getPlayer(object.getOwnerUUID()) != null;
    }

    @Override
    public Component getFailureMessage()
    {
        return Component.literal("Object owner is offline.");
    }

    @Override
    public String getRegistryKey() {
        return "share_while_online";
    }
}
