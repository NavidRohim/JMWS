package me.brynview.navidrohim.jmws.client.syncing.rules;

import me.brynview.navidrohim.jmws.common.syncing.rules.CommonRule;
import net.minecraft.network.chat.Component;

public class ClientShareWhileOnlineRule implements ClientShareRule
{
    Component FAILURE = Component.literal("Object cannot be shared while owner is offline.");
    Component DESCRIPTION = Component.literal("You can only share objects while you are online.");

    @Override
    public Component getFailureMessage() {
        return FAILURE;
    }

    @Override
    public Component getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getRegistryKey()
    {
        return "share_while_online";
    }
}
