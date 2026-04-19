package me.brynview.navidrohim.jmws.client.ui.generic.selection_list;

import com.mojang.datafixers.types.templates.Check;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.generic.entry.SelectableLabelEntry;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;

public abstract class CheckableSelectionList<L extends CheckableSelectionList<L>> extends ObjectSelectionList<SelectableLabelEntry<?>> {

    protected final Set<SelectableLabelEntry<L>> highlightedEntries = new HashSet<>();
    private boolean isSelectingAll = false;

    public CheckableSelectionList(Minecraft minecraft, int width, int height, int x, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
        this.setX(x);
    }

    public void unselectAll()
    {
        this.highlightedEntries.forEach(entry -> entry.selectedEntry = false);
        this.highlightedEntries.clear();
        this.isSelectingAll = false;
    }

    public void selectAll()
    {
        this.children().forEach(entry -> entry.setSelected(true));
        this.isSelectingAll = true;
    }

    public void toggleSelectAll()
    {
        if (this.isSelectingAll)
        {
            this.unselectAll();
            this.isSelectingAll = false;
        }
        else
        {
            this.selectAll();
            this.isSelectingAll = true;
        }
    }
    public Set<SelectableLabelEntry<L>> getSelectedEntries()
    {
        return this.highlightedEntries;
    }

    public Component getEmptyStateText()
    {
        return UIConstants.EMPTY_TEXT;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        if (this.children().isEmpty())
        {
            graphics.text(JMWSCommon.minecraftClientInstance.font, getEmptyStateText(), this.getX(), this.getY() - 15, -1);
        }

    }
}
