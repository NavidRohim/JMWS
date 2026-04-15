package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.client.ui.scroll.ObjectSharePanel;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;

import java.awt.*;

import static me.brynview.navidrohim.jmws.common.JMWSCommon.minecraftClientInstance;

public class ShareScreen <T extends ClientObjectWrapper<?>> extends Screen {

    private final Screen parent;
    private final ClientObjectWrapper<?> object;

    private @Nullable LinearLayout layout;
    private ObjectSharePanel<ClientObjectWrapper<?>> sharePanel;

    public static final int DONE_BUTTON_HEIGHT = 20;
    public static final int DONE_BUTTON_WIDTH = 50;
    public static final int ELEMENT_SPACING = 8;

    public static final int PLAYER_LIST_HEIGHT = 175;
    public static final int PLAYER_LIST_WIDTH = 200;

    private static final int ALERT_DURATION_MS = 4000;
    private static final int ALERT_FADE_DURATION_MS = 800;

    private long alertStartTime = 0;
    private @Nullable Component alertText = Component.empty();

    public ShareScreen(Screen parent, ClientWaypointWrapper object) {
        super(Component.empty());
        this.parent = parent;
        this.object = object;
    }

    private int getCornerXWithSpacing(int width, int spacing)
    {
        return this.width - (width + spacing);
    }

    private int getCornerYWithSpacing(int height, int spacing, int row)
    {
        return this.height - (height + spacing) * row;
    }

    public void sendAlert(Component text)
    {
        this.alertText = text;
        this.alertStartTime = Util.getMillis();
    }

    @Override
    protected void init()
    {
        // Define the sharing panel and add all shared objects on this client to panel
        this.sharePanel = new ObjectSharePanel<>(minecraftClientInstance,  PLAYER_LIST_WIDTH, PLAYER_LIST_HEIGHT,this.width / 10, (height / 2) - (PLAYER_LIST_HEIGHT / 2), 50, object);
        this.sharePanel.addWidgets();

        // Add layout


        // Add close button and share panel
        this.addRenderableWidget(this.sharePanel);
        this.addRenderableWidget(Button.builder(Component.translatable("button.jmws.update_button"), (bnt) -> JMWSPlugin.sync(false)).bounds(getCornerXWithSpacing(DONE_BUTTON_WIDTH, ELEMENT_SPACING), getCornerYWithSpacing(DONE_BUTTON_HEIGHT, ELEMENT_SPACING, 3), DONE_BUTTON_WIDTH, DONE_BUTTON_HEIGHT).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (bnt) -> this.onClose()).bounds(getCornerXWithSpacing(DONE_BUTTON_WIDTH, ELEMENT_SPACING), getCornerYWithSpacing(DONE_BUTTON_HEIGHT, ELEMENT_SPACING, 1), DONE_BUTTON_WIDTH, DONE_BUTTON_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.translatable("jmws.ui.sharing.reload"), (bnt) -> this.sharePanel.refresh()).bounds(getCornerXWithSpacing(DONE_BUTTON_WIDTH, ELEMENT_SPACING), getCornerYWithSpacing(DONE_BUTTON_HEIGHT, ELEMENT_SPACING, 2), DONE_BUTTON_WIDTH, DONE_BUTTON_HEIGHT).build());

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

                int alertX = this.sharePanel.getX();
                int sharePanelBottom = this.sharePanel.getY() + PLAYER_LIST_HEIGHT;
                int alertY = sharePanelBottom + ((this.height - sharePanelBottom) / 2) - (this.font.lineHeight / 2);

                float alpha = 1.0F;
                long fadeStart = ALERT_DURATION_MS - ALERT_FADE_DURATION_MS;

                if (totalDuration > fadeStart)
                {
                    float fadeProgress = (float) (totalDuration - fadeStart) / ALERT_FADE_DURATION_MS;
                    alpha = 1.0F - Mth.clamp(fadeProgress, 0.0F, 1.0F);
                }

                Vector4f baseColor = ARGB.vector4fFromARGB32(MessageType.NEUTRAL.getNumericalColour());
                int argbColor = ARGB.colorFromFloat(alpha, baseColor.x, baseColor.y, baseColor.z);

                graphics.text(this.font, this.alertText, alertX, alertY, argbColor);
            } else {
                this.alertText = null;
            }
        }
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        int x = (this.width / 10) + PLAYER_LIST_WIDTH + 20;

        graphics.text(this.font, Component.translatable("jmws.ui.sharing.share_object", this.object.getType().getReadableName(), this.object.getName()), x, (height / 2) - (PLAYER_LIST_HEIGHT / 2), -1);
        this.extractAlertText(graphics, mouseX, mouseY, a);
    }

    public static void openShare(ClientWaypointWrapper waypoint)
    {
        JMWSClientCommon.currentShareScreen = new ShareScreen<ClientWaypointWrapper>(minecraftClientInstance.screen, waypoint);
        minecraftClientInstance.setScreen(JMWSClientCommon.currentShareScreen);
    }
}
