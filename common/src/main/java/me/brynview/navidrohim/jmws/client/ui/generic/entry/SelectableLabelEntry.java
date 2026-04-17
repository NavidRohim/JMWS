package me.brynview.navidrohim.jmws.client.ui.generic.entry;

import me.brynview.navidrohim.jmws.client.ui.share_panel.ObjectSharePanel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public abstract class SelectableLabelEntry extends ObjectSelectionList.Entry<SelectableLabelEntry> {

    protected final @NotNull ObjectSharePanel<?> listOwner;
    public boolean selectedEntry = false;

    public SelectableLabelEntry(@NotNull ObjectSharePanel<?> listOwner, @NotNull Component title, @NotNull ObjectSharePanel.Subtitle subtitle) {
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
    public @NonNull Component getNarration() {
        return Component.empty();
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        boolean clicked = super.mouseClicked(event, doubleClick);

        if (doubleClick) {
            this.setSelected(false);
        } else {
            this.setSelected(!selectedEntry);
        }
        return clicked;
    }
}
