package me.brynview.navidrohim.jmws.client.ui.scroll;

import me.brynview.navidrohim.jmws.client.ClientCommonClass;
import me.brynview.navidrohim.jmws.common.CommonClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class WaypointSharePanel extends ObjectSelectionList<WaypointSharePanel.Entry> {
    public WaypointSharePanel(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    public abstract static class Entry extends ObjectSelectionList.Entry<Entry>
    {
        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            //guiGraphicsExtractor.text(CommonClass.minecraftClientInstance.font, "wdawd", this.getX() / 2, this.getY() / 2, 999999);
        }
    }
}
