package me.brynview.navidrohim.jmws.client.ui.generic.list.entry;

import me.brynview.navidrohim.jmws.client.ui.generic.list.CheckableSelectionList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public abstract class SelectableLabelEntry<E extends SelectableLabelEntry<E>> extends ObjectSelectionList.Entry<E> {

    protected final @NotNull CheckableSelectionList<E> listOwner;
    public boolean isSelected = false;

    public SelectableLabelEntry(@NotNull CheckableSelectionList<E> listOwner) {
        super();
        this.listOwner = listOwner;
    }

    public void extractSelectedState(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
    {

    }

    public void extractUnselectedState(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
    {

    }

    @SuppressWarnings("unchecked")
    public void setSelected(boolean selected) {
        if (selected) {
            if (canSelect()) {
                this.isSelected = true;
                this.listOwner.getSelectedEntries().add((E) this);
                this.listOwner.entryChanged((E) this);
            }
        } else if (canSelect()){
            this.isSelected = false;
            this.listOwner.getSelectedEntries().remove((E) this);
            this.listOwner.entryChanged((E) this);
        }
    }

    public boolean canSelect() {
        return true;
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        super.mouseClicked(event, doubleClick);

        if (!doubleClick) {
            this.setSelected(!isSelected);
        }

        return isSelected;
    }

    @Override
    public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
        if (this.isSelected)
        {
            this.extractSelectedState(guiGraphicsExtractor, i, i1, b, v);
        } else {
            this.extractUnselectedState(guiGraphicsExtractor, i, i1, b, v);
        }
    }


}
