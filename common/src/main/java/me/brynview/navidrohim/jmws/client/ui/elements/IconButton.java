package me.brynview.navidrohim.jmws.client.ui.elements;

import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import me.brynview.navidrohim.jmws.client.ui.generic.elements.AbstractJMWSButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import javax.tools.Tool;

public class IconButton extends AbstractJMWSButton {

    private final int buttonIconWH;
    private final int buttonIconWHHalfed;
    private final Identifier image;

    protected IconButton(int x, int y, int width, int height, Identifier image, int imageWH, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, Component.empty(), onPress, createNarration);
        this.image = image;
        this.buttonIconWH = imageWH;
        this.buttonIconWHHalfed = imageWH / 2;
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
        super.extractContents(guiGraphicsExtractor, i, i1, v);
        guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, image, this.getX() + (width / 2) - buttonIconWHHalfed, this.getY() + (height / 2) - buttonIconWHHalfed, buttonIconWH, buttonIconWH);
    }

    public static IconButton buildButton(Identifier image, OnPress onPress, int width, int height, int iconWH, @Nullable Tooltip tooltip) {
        if (width < iconWH || height < iconWH)
        {
            throw new IllegalArgumentException("Icon button size must be bigger than the icon itself!");
        }
        IconButton icnButton = new IconButton(0, 0, width, height, image, iconWH, onPress, Button.DEFAULT_NARRATION);
        if (tooltip != null)
        {
            icnButton.setTooltip(tooltip);
        }
        return icnButton;
    }

    public static IconButton buildGenericButton(Identifier image, OnPress onPress, Tooltip tooltip)
    {
        return buildButton(image, onPress, 20, 20, 20, tooltip);
    }
}
