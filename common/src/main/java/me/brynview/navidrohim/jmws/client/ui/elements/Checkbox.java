package me.brynview.navidrohim.jmws.client.ui.elements;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.generic.elements.AbstractJMWSButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;

public class Checkbox extends AbstractJMWSButton {

    public boolean isChecked = false;
    protected OnCheckboxPress onPress;
    private static final MutableComponent CHECKMARK = Component.literal("✔");

    protected Checkbox(int x, int y, int width, int height, Component message, OnCheckboxPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, (_) -> {}, createNarration);
        this.onPress = onPress;
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
        super.extractContents(guiGraphicsExtractor, i, i1, v);
        if (isChecked)
        {
            guiGraphicsExtractor.centeredText(Minecraft.getInstance().font, CHECKMARK, this.getX() + (width / 2), this.getY() + (height / 2) - 3, 0xFFFFFFFF);
        }
    }

    @Override
    public void onPress(@NonNull InputWithModifiers input) {
        super.onPress(input);
        isChecked = !isChecked;

        if (isChecked)
        {
            this.setTooltip(UIConstants.DESELECT_ALL);
        } else {
            this.setTooltip(UIConstants.SELECT_ALL);
        }
        this.onPress.onPress(this);
    }

    public static Checkbox buildCheckbox(OnCheckboxPress onPress) {
        Checkbox box = new Checkbox(0, 0, 12, 12, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        box.setTooltip(UIConstants.SELECT_ALL);

        return box;
    }

    public interface OnCheckboxPress
    {
        void onPress(Checkbox checkboxPressed);
    }
}
