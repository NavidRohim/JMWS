package me.brynview.navidrohim.jmws.client.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ShareScreen extends Screen {

    private final Screen parent;

    public ShareScreen(Screen parent, Component title) {
        super(title);
        this.parent = parent;
    }


    @Override
    protected void init()
    {
        // Button that is used to quit game. Placed in the middle of the screen, lower half vertically if I am remembering correctly.
        this.addRenderableWidget(Button.builder(Component.literal("Close"), (button) -> this.minecraft.setScreen(parent)).bounds((this.width / 10) - 30, 15, 60, 20).build());
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return true;
    }
}
