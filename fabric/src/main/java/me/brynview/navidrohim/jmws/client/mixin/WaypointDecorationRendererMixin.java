package me.brynview.navidrohim.jmws.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.kinds.Const;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.ingame.WaypointDecorationRenderer;
import journeymap.client.waypoint.ClientWaypointImpl;
import me.brynview.navidrohim.jmws.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WaypointDecorationRenderer.class)
public class WaypointDecorationRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lcom/mojang/blaze3d/vertex/PoseStack;Ljourneymap/client/render/draw/DrawStep$Pass;Lnet/minecraft/client/renderer/MultiBufferSource;Ljourneymap/client/waypoint/ClientWaypointImpl;FJ[FFDDDLnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;DDD)V", at = @At("TAIL"))
    private void callWaypointIconRenderEvent(GuiGraphics graphics, PoseStack poseStack, DrawStep.Pass pass, MultiBufferSource buffers, ClientWaypointImpl waypoint, float partialTicks, long gameTime, float[] rgba, float fadeAlpha, double shiftX, double shiftY, double shiftZ, Vec3 playerVec, Vec3 waypointVec, double viewDistance, double actualDistance, double scale, CallbackInfo ci)
    {
        Constants.getLogger().info("test");
    }
}
