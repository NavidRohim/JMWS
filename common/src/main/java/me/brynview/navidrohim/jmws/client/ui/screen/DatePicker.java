package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.NotificationAlertScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Locale;

public class DatePicker extends NotificationAlertScreen
{
    private final @NotNull Screen parent;
    private final @NotNull LayoutElement element;
    private LinearLayout mainLayout;

    private final int openAtX;
    private final int openAtY;

    private final static int widthHeightPicker = 100;
    private final static int dayWidthHeight = 16;
    private final static int daysPerRow = 5;
    private int rows = 0;
    private final Month month;

    private static final int spacing = (widthHeightPicker - (dayWidthHeight * daysPerRow)) / daysPerRow / 2;

    protected DatePicker(@NotNull Screen parentScreen, @NonNull LayoutElement element)
    {
        super(parentScreen, true);
        this.parent = parentScreen;
        this.element = element;

        this.openAtX = element.getX() + element.getWidth();
        this.openAtY = element.getY() - widthHeightPicker;
        this.month = Month.of(Calendar.getInstance().get(Calendar.MONTH) + 1);
    }

    @Override
    protected void init()
    {
        super.init();

        mainLayout = LinearLayout.vertical().spacing(2);
        mainLayout.setPosition(openAtX, openAtY);

        LinearLayout actionLayout = LinearLayout.horizontal().spacing(2);
        GridLayout main = new GridLayout(openAtX, openAtY);

        MultiLineTextWidget monthLabel = new MultiLineTextWidget(0, 0, Component.literal("January"), minecraft.font);
        actionLayout.addChild(monthLabel);

        // 6 spacing according to (100 / 15 * 5 = 33 / 5 = 6px spacing)
        // 6 px whole, 3 px each side. (OUTDATED)
        int daysInMonth = this.month.length(false) + 1;
        for (int i = 1; i < daysInMonth; i++)
        {
            int ogR = i % daysPerRow;
            int r = ogR == 0 ? daysPerRow : ogR;
            int c = Math.ceilDiv(i, daysPerRow);

            main.addChild(Button.builder(Component.literal(String.valueOf(i)), button -> {}).bounds(0, 0, dayWidthHeight, dayWidthHeight).build(), c,r, layoutSettings -> layoutSettings.padding(spacing));
            if (i + 1 == daysInMonth)
            {
                main.addChild(Button.builder(Component.literal("OK"), button -> {}).bounds(0, 0, dayWidthHeight, dayWidthHeight).build(), c + 2, 5, layoutSettings -> layoutSettings.padding(spacing));
                this.rows = c + 1;
            }
        }

        mainLayout.addChild(main);
        mainLayout.addChild(actionLayout);

        mainLayout.arrangeElements();
        FrameLayout.centerInRectangle(mainLayout, mainLayout.getX(), main.getY(), DatePicker.widthHeightPicker, getFinalHeight());
        main.visitWidgets(this::addRenderableWidget);


    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
    {
        // render screen behind, and the blurring colour
        this.parent.extractRenderState(graphics, 0, 0, 0);
        graphics.fill(0, 0, width, height, 0x80404040);
        graphics.fill(mainLayout.getX(), mainLayout.getY(), mainLayout.getX() + DatePicker.widthHeightPicker, mainLayout.getY() + getFinalHeight(), 0x80404040);

        // render the main layout
        super.extractRenderState(graphics, mouseX, mouseY, a);

        // render everything else (borders, text, background etc)
        graphics.outline(mainLayout.getX(), mainLayout.getY(), DatePicker.widthHeightPicker, getFinalHeight(), 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.literal(this.month.getDisplayName(TextStyle.FULL, Locale.getDefault())), mainLayout.getX() + (widthHeightPicker / 2), mainLayout.getY() - minecraft.font.lineHeight - 2, -1);
    }

    public int getFinalHeight()
    {
        return (dayWidthHeight + (spacing * 2)) * (rows);
    }

    public enum Alignment
    {
        LEFT, RIGHT
    }
}
