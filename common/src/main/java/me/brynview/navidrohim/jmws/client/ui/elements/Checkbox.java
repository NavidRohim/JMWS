package me.brynview.navidrohim.jmws.client.ui.elements;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.generic.elements.AbstractJMWSButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;

public class Checkbox extends AbstractJMWSButton {

    public boolean isChecked = false;
    protected OnCheckboxPress onPress;
    private final Tooltip checkedTooltip;
    private final Tooltip uncheckedTooltip;

    private static final MutableComponent CHECKMARK = Component.literal("✔");

    protected Checkbox(int x, int y, int width, int height, Component message, OnCheckboxPress onPress, CreateNarration createNarration, Tooltip checkedTooltip, Tooltip uncheckedTooltip) {
        super(x, y, width, height, message, (_) -> {}, createNarration);
        this.onPress = onPress;
        this.checkedTooltip = checkedTooltip;
        this.uncheckedTooltip = uncheckedTooltip;
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
        super.extractContents(guiGraphicsExtractor, i, i1, v);

        if (isChecked)
        {
            guiGraphicsExtractor.centeredText(Minecraft.getInstance().font, CHECKMARK, this.getX() + (width / 2), this.getY() + (height / 2) - 4, 0xFFFFFFFF);
        }

        guiGraphicsExtractor.text(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width + 3, this.getY() + 2, 0xFFFFFFFF);
    }

    public void check()
    {
        isChecked = true;
        this.setTooltip(this.checkedTooltip);
    }

    public void uncheck()
    {
        isChecked = false;
        this.setTooltip(this.uncheckedTooltip);
    }

    @Override
    public void onPress(@NonNull InputWithModifiers input) {
        super.onPress(input);

        if (isChecked)
        {
            this.uncheck();
        } else {
            this.check();
        }

        this.onPress.onPress(this);
    }

    public static Checkbox buildCheckbox(Component label, OnCheckboxPress onPress, Tooltip checkedTooltip, Tooltip uncheckedTooltip, int width) {
        Checkbox box = new Checkbox(0, 0, 12, 12, label, onPress, Button.DEFAULT_NARRATION, checkedTooltip, uncheckedTooltip);
        box.setTooltip(uncheckedTooltip);

        return box;
    }

    public static Checkbox buildSelectAllCheckbox(OnCheckboxPress onPress)
    {
        return buildCheckbox(Component.empty(), onPress, UIConstants.DESELECT_ALL, UIConstants.SELECT_ALL, 12);
    }

    public interface OnCheckboxPress
    {
        void onPress(Checkbox checkboxPressed);
    }
}
