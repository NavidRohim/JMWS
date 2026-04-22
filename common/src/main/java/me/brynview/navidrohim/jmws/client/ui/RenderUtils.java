package me.brynview.navidrohim.jmws.client.ui;

import me.brynview.navidrohim.jmws.client.ui.generic.selection_list.CheckableSelectionList;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public class RenderUtils
{
    public static void renderBorderForList(@NotNull GuiGraphicsExtractor graphics, CheckableSelectionList<?> list)
    {
        graphics.outline(list.getX() - 2, list.getY() - 2, list.getWidth() + 4, list.getHeight() + 4, MessageType.GREY.getNumericalColour());
    }

    public static String shortenObjectName(String name, int maxLength)
    {
        return name.length() > maxLength ? name.substring(0, maxLength) + "..." : name;
    }

    public static void setDimensionsForList(ObjectSelectionList<?> list, int screenWidth, int screenHeight)
    {
        int panelWidth = (int) (screenWidth * 0.70);
        int panelHeight = (int) (screenHeight * 0.65);
        int panelX = screenWidth / 10;
        int panelY = (screenHeight - panelHeight) / 2;

        list.setX(panelX);
        list.setY(panelY);
        list.setWidth(panelWidth);
        list.setHeight(panelHeight);
    }

    public static Vector2i getPositionRelativeToList(ObjectSelectionList<?> list)
    {
        int y = list.getY() + list.getHeight() + 15;
        return new Vector2i(list.getX(), y);
    }

}
