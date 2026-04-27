package me.brynview.navidrohim.jmws.client.ui.elements;

import me.brynview.navidrohim.jmws.client.ui.generic.elements.AbstractJMWSButton;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.NotificationAlertScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;

public class CloseButton extends AbstractJMWSButton {

    private static final MutableComponent CLOSE_BUTTON_TEXT = Component.literal("x");
    private static final Tooltip CLOSE_BUTTON_TOOLTIP = Tooltip.create(Component.translatable("mco.selectServer.close"));

    protected CloseButton(int x, int y, int width, int height, CreateNarration createNarration, NotificationAlertScreen parentScreen) {
        super(x, y, width, height, Component.empty(), (bnt) -> parentScreen.onClose(), createNarration);
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
        super.extractContents(guiGraphicsExtractor, i, i1, v);
        guiGraphicsExtractor.centeredText(Minecraft.getInstance().font, CLOSE_BUTTON_TEXT, this.getX() + (width / 2) + 1, this.getY() + (height / 2) - 4, 0xFFFFFFFF);
    }

    public static CloseButton buildButton(int width, int height, NotificationAlertScreen parentScreen) {
        CloseButton button = new CloseButton(0, 0, width, height, Button.DEFAULT_NARRATION, parentScreen);
        button.setTooltip(CLOSE_BUTTON_TOOLTIP);
        return button;
    }
}
