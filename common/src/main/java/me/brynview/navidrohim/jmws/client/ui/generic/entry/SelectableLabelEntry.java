package me.brynview.navidrohim.jmws.client.ui.generic.entry;

import me.brynview.navidrohim.jmws.client.ui.generic.selection_list.CheckableSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public abstract class SelectableLabelEntry<L extends CheckableSelectionList> extends ObjectSelectionList.Entry<SelectableLabelEntry<?>> {

    protected final @NotNull L listOwner;
    public boolean selectedEntry = false;

    public SelectableLabelEntry(@NotNull L listOwner) {
        super();
        this.listOwner = listOwner;
    }

    public void setSelected(boolean selected) {
        if (selected) {
            if (canSelect()) {
                this.selectedEntry = true;
                this.listOwner.getSelectedEntries().add(this);
            }
        } else {
            this.selectedEntry = false;
            this.listOwner.getSelectedEntries().remove(this);
        }
    }

    public boolean canSelect() {
        return true;
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        super.mouseClicked(event, doubleClick);

        if (!doubleClick) {
            this.setSelected(!selectedEntry);
        }

        return selectedEntry;
    }
}
