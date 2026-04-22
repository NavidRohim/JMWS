package me.brynview.navidrohim.jmws.client.ui.generic.entry;

import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.generic.Subtitle;
import me.brynview.navidrohim.jmws.client.ui.generic.selection_list.CheckableSelectionList;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class TitleLabelEntry<E extends TitleLabelEntry<E>> extends SelectableLabelEntry<E> {

    private final boolean canSelect;
    protected @NotNull Component title;
    public @NotNull Subtitle subtitle;

    public TitleLabelEntry(@NotNull CheckableSelectionList<E> listOwner, @NotNull Component title, @NotNull Subtitle subtitle, boolean canSelect) {
        super(listOwner);
        this.title = title;
        this.subtitle = subtitle;
        this.canSelect = canSelect;
    }

    @Override
    public @NonNull Component getNarration() {
        return title;
    }

    public void setSubtitle(@NotNull Subtitle subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
        extractMainlineString(guiGraphicsExtractor, i, i1, b, v);
        extractSubtitleText(guiGraphicsExtractor, i, i1, b, v);
    }

    @Override
    public boolean canSelect() {
        return this.canSelect;
    }

    public void extractSubtitleText(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
        guiGraphicsExtractor.text(JMWSCommon.minecraftClientInstance.font, subtitle.getDisplayableComponent(), this.getContentX() + UIConstants.PLAYER_HEAD_SIZE * 2, this.getContentYMiddle() + 3, subtitle.getMessageType().getNumericalColour());
    }

    public void extractMainlineString(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
        guiGraphicsExtractor.text(JMWSCommon.minecraftClientInstance.font, title, this.getContentX() + UIConstants.PLAYER_HEAD_SIZE * 2, this.getContentYMiddle() - 8, -1);
    }
}
