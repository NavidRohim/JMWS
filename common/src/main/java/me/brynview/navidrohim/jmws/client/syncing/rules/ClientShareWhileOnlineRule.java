package me.brynview.navidrohim.jmws.client.syncing.rules;

import net.minecraft.network.chat.Component;

public class ClientShareWhileOnlineRule implements ClientShareRule
{
    Component FAILURE = Component.literal("Object cannot be shared while owner is offline.");
    Component DESCRIPTION = Component.literal("You can only share objects while you are online.");
    Component NAME = Component.literal("Only share when your online");

    @Override
    public Component getFailureMessage() {
        return FAILURE;
    }

    @Override
    public Component getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Component getDisplayName()
    {
        return NAME;
    }

    @Override
    public String getRegistryKey()
    {
        return "share_while_online";
    }
}
