package me.brynview.navidrohim.jmws.client.ui.generic.selection_list;

import me.brynview.navidrohim.jmws.client.ui.generic.entry.SelectableLabelEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;

import java.util.HashSet;
import java.util.Set;

public class CheckableSelectionList extends ObjectSelectionList<SelectableLabelEntry> {

    protected final Set<SelectableLabelEntry> highlightedEntries = new HashSet<>();

    public CheckableSelectionList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    public void unselectAll()
    {
        this.highlightedEntries.forEach(entry -> entry.selectedEntry = false);
        this.highlightedEntries.clear();
    }

    public Set<SelectableLabelEntry> getSelectedEntries()
    {
        return this.highlightedEntries;
    }
}
