package me.brynview.navidrohim.jmws.server.syncing.rules.api;

import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public interface ShareRule
{
    boolean passed(ServerObject object, ServerPlayer player);
    Component getFailureMessage();

    default String getRegistryKey() {
        return getClass().getSimpleName();
    }
}
