package me.brynview.navidrohim.jmws.client.syncing.rules;

import net.minecraft.network.chat.Component;

public class OnlyShareWhileInOverworld implements ClientShareRule
{
    @Override
    public Component getDescription() {
        return null;
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
