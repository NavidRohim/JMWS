package me.brynview.navidrohim.jmws.client.syncing.rules;

import net.minecraft.network.chat.Component;

public class OnlyShareWhileInOverworld implements ClientShareRule
{

    private static final Component NAME = Component.literal("Only share in overworld");
    private static final Component DESCRIPTION = Component.literal("Object will only be shared when in the overworld.");

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
    public Component getFailureMessage() {
        return null;
    }

    @Override
    public String getRegistryKey()
    {
        return "only_share_while_in_overworld";
    }


}
