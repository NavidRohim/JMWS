package me.brynview.navidrohim.jmws.client.ui.generic.list.entry;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.generic.Subtitle;
import me.brynview.navidrohim.jmws.client.ui.generic.list.CheckableSelectionList;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

import static me.brynview.navidrohim.jmws.client.ui.UIConstants.PLAYER_HEAD_SIZE_HALVED;

public abstract class PlayerHeadEntryWithTitle<E extends PlayerHeadEntryWithTitle<E>> extends TitleLabelEntry<E> {

    protected final @Nullable PlayerInfo user;

    private static final Identifier UNKNOWN_USER = Identifier.fromNamespaceAndPath(Constants.MODID, "unknown");

    public PlayerHeadEntryWithTitle(@NotNull CheckableSelectionList<E> listOwner, PlayerInfo player) {
        super(listOwner, Component.literal(player.getProfile().name()), new Subtitle(Component.empty(), MessageType.PENDING), true);
        this.user = player;
    }

    public PlayerHeadEntryWithTitle(@NotNull CheckableSelectionList<E> listOwner, UUID user) {
        super(listOwner, Component.literal("Offline shared user"), new Subtitle(Component.literal(user.toString()), MessageType.PENDING), true);
        this.user = null;
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

        int x = getThumbnailImageX();
        int y = getThumbnailImageY();
        if (user != null)
        {
            PlayerFaceExtractor.extractRenderState(guiGraphicsExtractor, user.getSkin(), x, y, UIConstants.PLAYER_HEAD_SIZE);
        } else {
            guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, UNKNOWN_USER, x, y, 32, 32);
            guiGraphicsExtractor.outline(x, y, 32, 32, 0xFFFFFFFF);
        }
    }
}
