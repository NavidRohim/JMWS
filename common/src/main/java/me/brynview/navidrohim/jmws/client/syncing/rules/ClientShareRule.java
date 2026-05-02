package me.brynview.navidrohim.jmws.client.syncing.rules;

import me.brynview.navidrohim.jmws.common.syncing.rules.CommonRule;
import net.minecraft.network.chat.Component;

public interface ClientShareRule extends CommonRule
{
    Component getDescription();
}
