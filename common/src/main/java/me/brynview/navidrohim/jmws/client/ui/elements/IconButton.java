package me.brynview.navidrohim.jmws.client.ui.elements;

import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.generic.elements.AbstractJMWSButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class IconButton extends AbstractJMWSButton {

    private static final int NO_TINT = 0xFFFFFFFF;

    private final int buttonIconWH;
    private final int buttonIconWHHalfed;
    private final Identifier image;
    private final int tintColor;

    private final Tooltip enabledTooltip;
    private final Tooltip disabledTooltip;

    protected IconButton(int x, int y, int width, int height, Identifier image, int imageWH, int tintColor, OnPress onPress, CreateNarration createNarration, Tooltip enabledTooltip, Tooltip disabledTooltip) {
        super(x, y, width, height, Component.empty(), onPress, createNarration);
        this.image = image;
        this.buttonIconWH = imageWH;
        this.buttonIconWHHalfed = imageWH / 2;
        this.tintColor = tintColor;

        this.enabledTooltip = enabledTooltip;
        this.disabledTooltip = disabledTooltip;
    }


    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled)
        {
            this.setTooltip(this.enabledTooltip);
        } else {
            this.setTooltip(this.disabledTooltip);
        }
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
        super.extractContents(guiGraphicsExtractor, i, i1, v);

        int colour = !this.isEnabled() ? 0x80B0B0B0 : tintColor;
        guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, image, this.getX() + (width / 2) - buttonIconWHHalfed, this.getY() + (height / 2) - buttonIconWHHalfed, buttonIconWH, buttonIconWH, colour);
    }

    @Override
    public void refresh()
    {
        this.setEnabled(true);
    }

    public static IconButton buildButton(Identifier image, OnPress onPress, int width, int height, int iconWH, int tintColor, @Nullable Tooltip tooltip, @Nullable Tooltip disabledTooltip) {
        if (width < iconWH || height < iconWH)
        {
            throw new IllegalArgumentException("Icon button size must be bigger than the icon itself!");
        }
        IconButton btn = new IconButton(0, 0, width, height, image, iconWH, tintColor, onPress, Button.DEFAULT_NARRATION, tooltip, disabledTooltip);
        btn.setTooltip(tooltip);
        return btn;
    }

    public static IconButton buildButton(Identifier image, OnPress onPress, int width, int height, int iconWH, @Nullable Tooltip tooltip) {
        return buildButton(image, onPress, width, height, iconWH, NO_TINT, tooltip, null);
    }

    public static IconButton buildGenericButton(Identifier image, OnPress onPress, @Nullable Tooltip tooltip, @Nullable Tooltip disabledTooltip)
    {
        return buildButton(image, onPress, 20, 20, 20, NO_TINT, tooltip, disabledTooltip);
    }

    public static IconButton buildGenericButton(Identifier image, OnPress onPress, int tintColor, @Nullable Tooltip tooltip, @Nullable Tooltip disabledTooltip)
    {
        if (disabledTooltip == null)
        {
            disabledTooltip = UIConstants.DISABLED_GENERIC;
        }

        return buildButton(image, onPress, 20, 20, 20, tintColor, tooltip, disabledTooltip);
    }
}
