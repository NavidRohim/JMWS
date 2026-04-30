package me.brynview.navidrohim.jmws.client.ui.generic.elements;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public abstract class AbstractJMWSButton extends Button implements AbstractJMWSElement
{

    private static final Identifier BUTTON_BG = Identifier.fromNamespaceAndPath(Constants.MODID, "button_bg");
    protected boolean isHeld = false;
    private boolean isEnabled = true;

    protected AbstractJMWSButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, onPress, createNarration);
    }

    public boolean isEnabled()
    {
        return isEnabled;
    }

    public void setEnabled(boolean enabled)
    {
        isEnabled = enabled;
    }

    @Override
    public void refresh()
    {
        this.setEnabled(true);
        this.isHeld = false;
    }

    @Override
    public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (isEnabled)
        {
            isHeld = true;
            super.onClick(event, doubleClick);
        }
    }

    @Override
    public void onRelease(@NonNull MouseButtonEvent event) {
        isHeld = false;
        super.onRelease(event);
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {

        renderBackground(guiGraphicsExtractor, i, i1, v);
        renderBorder(guiGraphicsExtractor);

        if (isHeld)
        {
            RenderUtils.renderFillForElement(guiGraphicsExtractor, this, 0x7F404040);
        } else if (this.isHovered())
        {
            RenderUtils.renderFillForElement(guiGraphicsExtractor, this, 0x40909090);
        }

        if (!isEnabled)
        {
            RenderUtils.renderFillForElement(guiGraphicsExtractor, this, 0x40929292);
        }
    }

    @Override
    public void renderBorder(@NonNull GuiGraphicsExtractor graphics) {
        RenderUtils.renderBorderForList(graphics, this);
    }

    @Override
    public void renderBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BUTTON_BG, this.getX(), this.getY(), width, height);
    }


}
