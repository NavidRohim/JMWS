package me.brynview.navidrohim.jmws.client.ui.generic.list;

import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.generic.list.entry.SelectableLabelEntry;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;

public abstract class CheckableSelectionList<E extends SelectableLabelEntry<E>> extends ObjectSelectionList<E> {

    protected final Set<E> selectedEntries = new HashSet<>();
    public boolean isSelectingAll = false;

    public CheckableSelectionList(Minecraft minecraft, int width, int height, int x, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
        this.setX(x);
    }

    public void unselectAll()
    {
        this.selectedEntries.forEach(entry -> entry.isSelected = false);
        this.selectedEntries.clear();
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

    public Set<E> getSelectedEntries()
    {
        return this.selectedEntries;
    }

    public void entryChanged(E entry)
    {

    }

    public boolean isEmpty()
    {
        return this.children().isEmpty();
    }

    public int getAmount()
    {
        return this.children().size();
    }

    public Component getEmptyStateText()
    {
        return UIConstants.EMPTY_TEXT;
    }

    @Override
    public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        if (this.children().isEmpty())
        {
            graphics.text(JMWSCommon.minecraftClientInstance.font, getEmptyStateText(), this.getX(), this.getY() - 15, -1);
        }

    }

    @Override
    protected void extractListSeparators(@NotNull GuiGraphicsExtractor graphics) {
        RenderUtils.renderBorderForList(graphics, this);
    }

    @Override
    protected void extractSelection(@NonNull GuiGraphicsExtractor graphics, @NonNull E entry, int outlineColor) {
        outlineColor = this.isFocused() ? 0xFFCCCCCC : -8355712;

        int outlineX0 = entry.getX();
        int outlineY0 = entry.getY();
        //int outlineX1 = outlineX0 + entry.getWidth();
        //int outlineY1 = outlineY0 + entry.getHeight();
        graphics.outline(outlineX0, outlineY0, entry.getWidth(), entry.getHeight(), outlineColor);
        //graphics.fill(outlineX0 + 1, outlineY0 + 1, outlineX1 - 1, outlineY1 - 1, 0xF0000000);
    }
}
