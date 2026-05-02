package me.brynview.navidrohim.jmws.server.syncing.rules.api;

import me.brynview.navidrohim.jmws.common.syncing.rules.CommonRule;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public interface ShareRule extends CommonRule
{
    boolean passed(ServerObject object, ServerPlayer player);
}
