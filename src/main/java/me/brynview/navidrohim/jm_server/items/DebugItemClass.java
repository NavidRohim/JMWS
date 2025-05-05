package me.brynview.navidrohim.jm_server.items;

import me.brynview.navidrohim.jm_server.client.plugin.IClientPluginJMTest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class DebugItemClass extends Item {
    public DebugItemClass(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        IClientPluginJMTest.updateWaypoints(MinecraftClient.getInstance());
        return ActionResult.SUCCESS;
    }
}

