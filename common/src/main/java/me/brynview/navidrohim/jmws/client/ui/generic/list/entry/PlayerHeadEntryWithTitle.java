package me.brynview.navidrohim.jmws.client.ui.generic.list.entry;

import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.generic.Subtitle;
import me.brynview.navidrohim.jmws.client.ui.generic.list.CheckableSelectionList;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import static me.brynview.navidrohim.jmws.client.ui.UIConstants.PLAYER_HEAD_SIZE_HALVED;

public abstract class PlayerHeadEntryWithTitle<E extends PlayerHeadEntryWithTitle<E>> extends TitleLabelEntry<E> {

    protected final @NonNull PlayerInfo user;

    public PlayerHeadEntryWithTitle(@NotNull CheckableSelectionList<E> listOwner, PlayerInfo player) {
        super(listOwner, Component.literal(player.getProfile().name()), new Subtitle(Component.empty(), MessageType.PENDING), true);
        this.user = player;
    }

    @Override
    public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
        super.extractContent(guiGraphicsExtractor, i, i1, b, v);
        this.extractThumbnailImage(guiGraphicsExtractor, i, i1, b, v);
    }

    protected int getThumbnailImageX() {
        return this.getContentX() + PLAYER_HEAD_SIZE_HALVED + 4;
    }

    protected int getThumbnailImageY() {
        return this.getContentYMiddle() - PLAYER_HEAD_SIZE_HALVED;
    }

    protected void extractThumbnailImage(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {

        PlayerFaceExtractor.extractRenderState(guiGraphicsExtractor, user.getSkin(), getThumbnailImageX(), getThumbnailImageY(), UIConstants.PLAYER_HEAD_SIZE);
    }
}
