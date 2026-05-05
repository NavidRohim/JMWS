package me.brynview.navidrohim.jmws.client.ui.elements;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.generic.elements.AbstractJMWSButton;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.NotificationAlertScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class CloseButton extends AbstractJMWSButton {

    private static final Identifier CLOSE_BUTTON_ICON = Identifier.fromNamespaceAndPath(Constants.MODID, "close");

    protected CloseButton(int x, int y, int width, int height, CreateNarration createNarration, NotificationAlertScreen parentScreen) {
        super(x, y, width, height, Component.empty(), (bnt) -> parentScreen.onClose(), createNarration);
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
        super.extractContents(guiGraphicsExtractor, i, i1, v);
        guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, CLOSE_BUTTON_ICON, this.getX(), this.getY(), width, height);
    }

    public static CloseButton buildButton(int width, int height, NotificationAlertScreen parentScreen) {
        CloseButton button = new CloseButton(parentScreen.width - (width + 4), 4, width, height, Button.DEFAULT_NARRATION, parentScreen);
        button.setTooltip(UIConstants.CLOSE_BUTTON_TOOLTIP);
        return button;
    }

    public static CloseButton buildAsSystemButton(NotificationAlertScreen parentScreen)
    {
        return buildButton(16, 16, parentScreen);
    }
}
