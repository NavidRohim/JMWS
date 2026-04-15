package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;

import static me.brynview.navidrohim.jmws.common.JMWSCommon.minecraftClientInstance;

public class NotificationAlertScreen extends Screen {

    private static final int ALERT_DURATION_MS = 4000;
    private static final int ALERT_FADE_DURATION_MS = 800;
    private long alertStartTime = 0;

    private final Screen parent;
    private @Nullable Component alertText = Component.empty();

    public NotificationAlertScreen(Screen parent)
    {
        super(Component.empty());
        this.parent = parent;
    }

    public void sendAlert(Component text)
    {
        this.alertText = text;
        this.alertStartTime = Util.getMillis();
    }

    protected Vector2i getDrawLocation()
    {
        assert this.alertText != null;
        int alertX = this.width / 2 - this.font.width(this.alertText) / 2;
        int alertY = this.height / 2;

        return new Vector2i(alertX, alertY);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        this.extractAlertText(graphics, mouseX, mouseY, a);
    }

    @Override
    public void onClose()
    {
        minecraftClientInstance.setScreen(parent);
        JMWSClientCommon.currentShareScreen = null;
    }

    public void extractAlertText(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        if (this.alertText != null)
        {
            long totalDuration = Util.getMillis() - this.alertStartTime;
            if (totalDuration < ALERT_DURATION_MS)
            {
                float alpha = 1.0F;
                long fadeStart = ALERT_DURATION_MS - ALERT_FADE_DURATION_MS;

                if (totalDuration > fadeStart)
                {
                    float fadeProgress = (float) (totalDuration - fadeStart) / ALERT_FADE_DURATION_MS;
                    alpha = 1.0F - Mth.clamp(fadeProgress, 0.0F, 1.0F);
                }

                Vector4f baseColor = ARGB.vector4fFromARGB32(MessageType.NEUTRAL.getNumericalColour());
                int argbColor = ARGB.colorFromFloat(alpha, baseColor.x, baseColor.y, baseColor.z);
                Vector2i drawLocation = getDrawLocation();

                graphics.text(this.font, this.alertText, drawLocation.x, drawLocation.y, argbColor);
            } else {
                this.alertText = null;
            }
        }
    }
}
