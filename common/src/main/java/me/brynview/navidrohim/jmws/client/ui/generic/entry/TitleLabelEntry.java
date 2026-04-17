package me.brynview.navidrohim.jmws.client.ui.generic.entry;

import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.share_panel.ObjectSharePanel;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class TitleLabelEntry extends SelectableLabelEntry {

    private final boolean canSelect;
    protected @NotNull Component title;
    public @NotNull ObjectSharePanel.Subtitle subtitle;

    public TitleLabelEntry(@NotNull ObjectSharePanel<?> listOwner, @NotNull Component title, @NotNull ObjectSharePanel.Subtitle subtitle, boolean canSelect) {
        super(listOwner, title, subtitle);
        this.title = title;
        this.subtitle = subtitle;
        this.canSelect = canSelect;
    }

    @Override
    public @NonNull Component getNarration() {
        return title;
    }

    public void setSubtitle(@NotNull ObjectSharePanel.Subtitle subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
        extractMainlineString(guiGraphicsExtractor, i, i1, b, v);
        extractSubtitleText(guiGraphicsExtractor, i, i1, b, v);
        if (this.selectedEntry) {
            this.setSubtitle(new ObjectSharePanel.Subtitle(Component.translatable("jmws.ui.sharing.selected"), MessageType.of(null, this.listOwner.sharedObject.getColour())));
        }
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
