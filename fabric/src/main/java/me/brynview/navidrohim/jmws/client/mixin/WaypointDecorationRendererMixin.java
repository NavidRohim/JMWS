package me.brynview.navidrohim.jmws.client.mixin;


import journeymap.client.render.ingame.WaypointDecorationRenderer;
import journeymap.client.waypoint.ClientWaypointImpl;

import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.common.CommonClass;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(WaypointDecorationRenderer.class)
public abstract class WaypointDecorationRendererMixin {

    @Unique
    private static int teleportCooldown = 0;

    @Shadow
    protected abstract double angleToBeacon(Vec3 waypointVec);

    @Inject(
            method = "renderNameTag",
            at = @At(value = "TAIL")
    )
    private void callWaypointIconRenderEvent(GuiGraphics graphics, ClientWaypointImpl waypoint, double labelX, double labelY, float alpha, double actualDistance, int size, CallbackInfo ci) {
        double angle = this.angleToBeacon(waypoint.getPosition());
        teleportCooldown -= 1;

        if (angle < 2 && CommonClass.isHoldingTeleportKey && teleportCooldown <= 0) {
            JMWSPlugin.getInstance().teleportPlayer(waypoint.getPosition());
            teleportCooldown = 120;
        }
    }
}
