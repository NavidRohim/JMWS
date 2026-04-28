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

    private static final int NO_TINT = 0xFFFFFFFF;

    private final int buttonIconWH;
    private final int buttonIconWHHalfed;
    private final Identifier image;
    private final int tintColor;

    protected IconButton(int x, int y, int width, int height, Identifier image, int imageWH, int tintColor, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, Component.empty(), onPress, createNarration);
        this.image = image;
        this.buttonIconWH = imageWH;
        this.buttonIconWHHalfed = imageWH / 2;
        this.tintColor = tintColor;
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
        super.extractContents(guiGraphicsExtractor, i, i1, v);
        guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, image, this.getX() + (width / 2) - buttonIconWHHalfed, this.getY() + (height / 2) - buttonIconWHHalfed, buttonIconWH, buttonIconWH, tintColor);
    }

    public static IconButton buildButton(Identifier image, OnPress onPress, int width, int height, int iconWH, int tintColor, @Nullable Tooltip tooltip) {
        if (width < iconWH || height < iconWH)
        {
            throw new IllegalArgumentException("Icon button size must be bigger than the icon itself!");
        }
        IconButton icnButton = new IconButton(0, 0, width, height, image, iconWH, tintColor, onPress, Button.DEFAULT_NARRATION);
        if (tooltip != null)
        {
            icnButton.setTooltip(tooltip);
        }
        return icnButton;
    }

    public static IconButton buildButton(Identifier image, OnPress onPress, int width, int height, int iconWH, @Nullable Tooltip tooltip) {
        return buildButton(image, onPress, width, height, iconWH, NO_TINT, tooltip);
    }

    public static IconButton buildGenericButton(Identifier image, OnPress onPress, Tooltip tooltip)
    {
        return buildButton(image, onPress, 20, 20, 20, NO_TINT, tooltip);
    }

    public static IconButton buildGenericButton(Identifier image, OnPress onPress, int tintColor, Tooltip tooltip)
    {
        return buildButton(image, onPress, 20, 20, 20, tintColor, tooltip);
    }
}
