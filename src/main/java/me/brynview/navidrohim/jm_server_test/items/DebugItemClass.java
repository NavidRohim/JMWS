package me.brynview.navidrohim.jm_server_test.items;

import me.brynview.navidrohim.jm_server_test.JMServerTest;
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
        JMServerTest.LOGGER.info("TODO: test and implement waypoint fetching");
        return ActionResult.SUCCESS;
    }
}

