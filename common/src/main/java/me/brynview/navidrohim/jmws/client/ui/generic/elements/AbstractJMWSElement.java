package me.brynview.navidrohim.jmws.client.ui.generic.elements;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import org.jetbrains.annotations.NotNull;

public interface AbstractJMWSElement extends Renderable, LayoutElement
{
    void renderBorder(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks);
    void renderBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks);
}
