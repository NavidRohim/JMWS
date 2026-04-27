package me.brynview.navidrohim.jmws.client.ui.elements;

import me.brynview.navidrohim.jmws.client.ui.generic.elements.AbstractJMWSButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class IconButton extends AbstractJMWSButton {

    private final int buttonIconWH;
    private final Identifier image;

    protected IconButton(int x, int y, int width, int height, Identifier image, int imageWH, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, Component.empty(), onPress, createNarration);
        this.image = image;
        this.buttonIconWH = imageWH;
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
        super.extractContents(guiGraphicsExtractor, i, i1, v);
        guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, image, this.getX() + (width / 2) - buttonIconWH, this.getY() + (height / 2) - buttonIconWH, buttonIconWH, buttonIconWH);
    }

    public static IconButton buildButton(Identifier image, OnPress onPress, int width, int height) {
        return new IconButton(0, 0, width, height, image, 32, onPress, Button.DEFAULT_NARRATION);
    }
}
