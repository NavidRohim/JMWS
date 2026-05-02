package me.brynview.navidrohim.jmws.server.syncing.rules;

import me.brynview.navidrohim.jmws.Constants;
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
        ServerPlayer ownerPlayer = JMWSCommon.minecraftServerInstance.getPlayerList().getPlayer(object.getOwnerUUID());
        Constants.getLogger().info("Owner player: " + ownerPlayer);
        return ownerPlayer != null;
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
