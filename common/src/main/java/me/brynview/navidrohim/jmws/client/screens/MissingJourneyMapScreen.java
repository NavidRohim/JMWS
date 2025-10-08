package me.brynview.navidrohim.jmws.client.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.network.chat.Component;

/**
 * Screen that is displayed if JourneyMap is missing.
 * We extend ErrorScreen because the default ErrorScreen's button is to just close the screen
 * instead of closing the game which we want here.
 */
public class MissingJourneyMapScreen extends ErrorScreen {
    public MissingJourneyMapScreen(Component title, Component message) {
        super(title, message);
    }

    @Override
    protected void init()
    {
        // Button that is used to quit game. Placed in the middle of the screen, lower half vertically if I am remembering correctly.
        this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"), (button) -> this.minecraft.stop()).bounds(this.width / 2 - 100, 140, 200, 20).build());
    }
}
