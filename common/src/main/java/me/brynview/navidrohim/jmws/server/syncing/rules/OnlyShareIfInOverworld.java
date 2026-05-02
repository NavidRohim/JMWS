package me.brynview.navidrohim.jmws.server.syncing.rules;

import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.syncing.rules.api.ShareRule;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class OnlyShareIfInOverworld implements ShareRule
{

    @Override
    public boolean passed(ServerObject object, ServerPlayer player) {
        return player.level().dimension() == Level.OVERWORLD;
    }

    @Override
    public Component getFailureMessage() {
        return Component.literal("You can only share objects in the overworld.");
    }

    @Override
    public String getRegistryKey()
    {
        return "only_share_while_in_overworld";
    }
}
