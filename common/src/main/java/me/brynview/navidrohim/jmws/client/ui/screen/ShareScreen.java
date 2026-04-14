package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.client.ui.scroll.ObjectSharePanel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import static me.brynview.navidrohim.jmws.common.JMWSCommon.minecraftClientInstance;

public class ShareScreen <T extends ClientObjectWrapper<?>> extends Screen {

    private final Screen parent;
    private final ClientObjectWrapper<?> object;

    private @Nullable LinearLayout layout;
    private ObjectSharePanel<ClientObjectWrapper<?>> sharePanel;

    public static final int DONE_BUTTON_HEIGHT = 20;
    public static final int DONE_BUTTON_WIDTH = 50;
    public static final int ELEMENT_SPACING = 20;

    public static final int PLAYER_LIST_HEIGHT = 175;
    public static final int PLAYER_LIST_WIDTH = 200;

    public ShareScreen(Screen parent, ClientWaypointWrapper object, Component title) {
        super(title);
        this.parent = parent;
        this.object = object;
    }

    private int getCornerXWithSpacing(int width, int spacing)
    {
        return this.width - (width + spacing);
    }

    private int getCornerYWithSpacing(int height, int spacing, int row)
    {
        return this.height - (height + spacing) * row;
    }

    @Override
    protected void init()
    {
        // Define the sharing panel and add all shared objects on this client to panel
        this.sharePanel = new ObjectSharePanel<>(minecraftClientInstance,  PLAYER_LIST_WIDTH, PLAYER_LIST_HEIGHT,this.width / 10, (height / 2) - (PLAYER_LIST_HEIGHT / 2), 50, object);
        this.sharePanel.addWidgets();

        // Add layout


        // Add close button and share panel
        this.addRenderableWidget(this.sharePanel);
        this.addRenderableWidget(Button.builder(Component.literal("Sync"), (bnt) -> JMWSPlugin.sync(false)).bounds(getCornerXWithSpacing(DONE_BUTTON_WIDTH, ELEMENT_SPACING), getCornerYWithSpacing(DONE_BUTTON_HEIGHT, ELEMENT_SPACING, 3), DONE_BUTTON_WIDTH, DONE_BUTTON_HEIGHT).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (bnt) -> minecraftClientInstance.setScreen(parent)).bounds(getCornerXWithSpacing(DONE_BUTTON_WIDTH, ELEMENT_SPACING), getCornerYWithSpacing(DONE_BUTTON_HEIGHT, ELEMENT_SPACING, 1), DONE_BUTTON_WIDTH, DONE_BUTTON_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.translatable("Reload"), (bnt) -> this.sharePanel.refresh()).bounds(getCornerXWithSpacing(DONE_BUTTON_WIDTH, ELEMENT_SPACING), getCornerYWithSpacing(DONE_BUTTON_HEIGHT, ELEMENT_SPACING, 2), DONE_BUTTON_WIDTH, DONE_BUTTON_HEIGHT).build());

    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        int x = (this.width / 10) + PLAYER_LIST_WIDTH + 20;
        graphics.text(this.font, Component.literal("Share %s \"%s\"".formatted(this.object.getType().getReadableName(), this.object.getName())), x, (height / 2) - (PLAYER_LIST_HEIGHT / 2), -1);
    }

    public static void openShare(ClientWaypointWrapper waypoint)
    {
        minecraftClientInstance.setScreen(new ShareScreen<ClientWaypointWrapper>(minecraftClientInstance.screen, waypoint, Component.empty()));
    }
}
