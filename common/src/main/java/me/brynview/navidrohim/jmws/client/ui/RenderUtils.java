package me.brynview.navidrohim.jmws.client.ui;

import me.brynview.navidrohim.jmws.client.ui.generic.selection_list.CheckableSelectionList;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;

public class RenderUtils
{
    public static void renderBorderForList(@NotNull GuiGraphicsExtractor graphics, CheckableSelectionList<?> list)
    {
        graphics.outline(list.getX() - 2, list.getY() - 2, list.getWidth() + 4, list.getHeight() + 4, MessageType.GREY.getNumericalColour());
    }
}
