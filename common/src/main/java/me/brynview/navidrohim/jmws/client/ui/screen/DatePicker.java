package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.time.*;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatePicker extends Screen
{
    private final @NotNull Screen parent;
    private LinearLayout mainLayout;
    private GridLayout gridLayout;
    private Component title = Component.empty();
    private final DatePickerCallback callback;

    private final int openAtX;
    private final int openAtY;

    private final static int widthHeightPicker = 130;
    private final static int dayWidthHeight = 16;
    private final static int daysPerRow = 7;
    private static final int spacing = (widthHeightPicker - (dayWidthHeight * daysPerRow)) / daysPerRow / 2;

    private static final Tooltip NAV_MONTH = Tooltip.create(Component.translatable("jmws.ui.calendar.nav_month"));
    private static final Tooltip NAV_YEAR = Tooltip.create(Component.translatable("jmws.ui.calendar.nav_year"));
    private static final Tooltip SET_DATE = Tooltip.create(Component.translatable("jmws.ui.calendar.set_timeout"));

    private static final Component CLOSE = Component.literal("X");

    private Month month;
    private int selectedYear;
    private int selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
    private int selectedDay = 0;
    private int selectedHour = 0;
    private int selectedMinute = 0;
    private int selectedSecond = 0;

    private final Instant now = Instant.now();
    private final Month currentMonth = Month.of(selectedMonth);
    private final Year currentYear = Year.now();

    private static final List<Integer> STUPID_BRITISH_ENGLISH_ORDINAL_INDICATOR_EXCEPTIONS = List.of(11, 12, 13);

    protected DatePicker(@NotNull Screen parentScreen, @Nullable LayoutElement element, DatePickerCallback callback)
    {
        super(Component.literal("Select Date"));
        this.parent = parentScreen;

        if (element != null)
        {
            this.openAtX = element.getX() + element.getWidth();
            this.openAtY = element.getY() - widthHeightPicker;
        } else {
            this.openAtX = parent.width / 2 - widthHeightPicker / 2;
            this.openAtY = parent.height / 2 - widthHeightPicker / 2;
        }
        this.selectedYear = Year.now().getValue();
        this.month = getMonthFromInt(this.selectedMonth);
        this.callback = callback;

    }

    @Override
    protected void init()
    {
        super.init();
        render();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
    {
        // render screen behind, and the blurring colour
        this.parent.extractRenderState(graphics, 0, 0, 0);

        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680); // Src can be found at net.minecraft.client.gui.screens::Screen line 449
        graphics.fill(gridLayout.getX(), gridLayout.getY(), gridLayout.getX() + gridLayout.getWidth(), gridLayout.getY() + gridLayout.getHeight(), 0x80303030);

        // render the main layout
        super.extractRenderState(graphics, mouseX, mouseY, a);

        // render everything else (borders, text, background etc.)
        graphics.outline(gridLayout.getX(), gridLayout.getY(), gridLayout.getWidth(), gridLayout.getHeight(), 0x40FFFFFF);
        graphics.centeredText(this.font, title, mainLayout.getX() + (widthHeightPicker / 2), mainLayout.getY() - minecraft.font.lineHeight - 2, -1);
    }

    private void render()
    {
        this.clearWidgets();

        mainLayout = LinearLayout.vertical().spacing(2);

        LinearLayout actionLayout = LinearLayout.horizontal().spacing(2);
        EditBox box = actionLayout.addChild(new EditBox(minecraft.font, 0, 0, widthHeightPicker + 20, 15, Component.empty()));

        box.setValue("HH:MM:SS");
        box.setMaxLength(8);
        box.setResponder(this::onEdit);

        mainLayout.addChild(actionLayout);
        mainLayout.addChild(setMonth(this.selectedMonth, this.selectedYear));
        mainLayout.arrangeElements();
        FrameLayout.centerInRectangle(mainLayout, 0, 0, width, height);
        mainLayout.visitWidgets(this::addRenderableWidget);

        box.setWidth(gridLayout.getWidth());

        setTitleWithDate();
    }

    private int getTimeFriendlyNumber(String number) throws NumberFormatException, IndexOutOfBoundsException
    {
        // Use first to characters. If it is too short, just pad it with 0s.
        if (!number.isEmpty())
        {
            return Integer.parseInt(number.substring(0, 2));
        }

        return 0;
    }

    private void onEdit(String s)
    {

        if (s.isEmpty())
        {
            selectedHour = selectedMinute = selectedSecond = 0;
        } else {
            try
            {
                String[] split = s.split(":");
                selectedHour = Math.min(getTimeFriendlyNumber(split[0]), 23);
                selectedMinute = Math.min(getTimeFriendlyNumber(split[1]), 59);
                selectedSecond = Math.min(getTimeFriendlyNumber(split[2]), 59);

                setTitleWithDate();
            } catch (NumberFormatException e)
            {
                selectedHour = selectedMinute = selectedSecond = 0;
            } catch (IndexOutOfBoundsException _) {}
        }
        setTitleWithDate();
    }

    public boolean lastYearIsValid()
    {
        return (this.selectedYear == this.currentYear.getValue() + 1 && this.selectedMonth >= currentMonth.getValue() || this.selectedYear > currentYear.getValue() + 1);
    }

    public boolean lastMonthIsValid()
    {
        return (this.selectedMonth > currentMonth.getValue() || this.selectedYear > currentYear.getValue());
    }

    public void scrollLeft()
    {

        if (lastMonthIsValid())
        {
            if (this.selectedMonth - 1 < 1)
            {
                this.selectedMonth = 12;
                this.selectedYear--;
            } else {
                this.selectedMonth--;
            }

            render();
        }
    }

    public void scrollLeftByYear()
    {
        if (lastYearIsValid()) {
            this.selectedYear--;
            render();
        }
    }

    public void scrollRight()
    {
        if (this.selectedMonth + 1 > 12)
        {
            this.selectedMonth = 1;
            this.selectedYear++;
        } else {
            this.selectedMonth++;

        }

        render();
    }

    public void scrollRightByYear()
    {
        this.selectedYear++;
        render();
    }

    public Month getMonthFromInt(int monthIndex)
    {
        return Month.of(monthIndex);
    }

    public LayoutElement setMonth(int monthIndex, int year)
    {
        month = getMonthFromInt(monthIndex);
        gridLayout = new GridLayout(openAtX, openAtY); // Could use helper but no.

        Button leftButton = gridLayout.addChild(Button.builder(Component.literal("<"), button -> scrollLeft()).bounds(0, 0, dayWidthHeight, dayWidthHeight).tooltip(NAV_MONTH).build(), 0, 1, layoutSettings -> layoutSettings.padding(spacing).paddingVertical(1));
        Button leftButtonByYear = gridLayout.addChild(Button.builder(Component.literal("<<"), button -> scrollLeftByYear()).bounds(0, 0, dayWidthHeight, dayWidthHeight).tooltip(NAV_YEAR).build(), 0, 2, layoutSettings -> layoutSettings.padding(spacing).paddingVertical(1));
        gridLayout.addChild(Button.builder(Component.literal("OK"), button -> {calculateSelectionToMillis();}).bounds(0, 0, dayWidthHeight, dayWidthHeight).tooltip(SET_DATE).build(), 0, 4, layoutSettings -> layoutSettings.padding(spacing).paddingVertical(1));
        gridLayout.addChild(Button.builder(Component.literal(">>"), button -> scrollRightByYear()).bounds(0, 0, dayWidthHeight, dayWidthHeight).tooltip(NAV_YEAR).build(), 0, 6, layoutSettings -> layoutSettings.padding(spacing).paddingVertical(1));
        gridLayout.addChild(Button.builder(Component.literal(">"), button -> scrollRight()).bounds(0, 0, dayWidthHeight, dayWidthHeight).tooltip(NAV_MONTH).build(), 0, 7, layoutSettings -> layoutSettings.padding(spacing).paddingVertical(1));

        leftButton.visible = lastMonthIsValid();
        leftButtonByYear.visible = lastYearIsValid();

        // 6 spacing according to (100 / 15 * 5 = 33 / 5 = 6px spacing)
        // 6 px whole, 3 px each side. (OUTDATED)
        int daysInMonth = month.length(Year.isLeap(year)) + 1;
        for (int i = 1; i < daysInMonth; i++)
        {
            int ogR = i % daysPerRow;
            int r = ogR == 0 ? daysPerRow : ogR;
            int c = Math.ceilDiv(i, daysPerRow) + 1;

            gridLayout.addChild(Button.builder(Component.literal(String.valueOf(i)), this::setCurrentDayFromButton).bounds(0, 0, dayWidthHeight, dayWidthHeight).build(), c,r, layoutSettings -> layoutSettings.paddingHorizontal(spacing).paddingVertical(1));
        }
        Button closeButton = gridLayout.addChild(Button.builder(CLOSE, (b)->{this.onClose();}).bounds(0, 0, dayWidthHeight, dayWidthHeight).build(), 6, 7, layoutSettings -> layoutSettings.paddingHorizontal(spacing).paddingVertical(1)); // Stops the screen from jerking around when rendering small months
        closeButton.setTooltip(UIConstants.CLOSE_BUTTON_TOOLTIP);

        return gridLayout;
    }

    private void calculateSelectionToMillis()
    {
        if (selectedDay > 0)
        {
            ZonedDateTime inputtedDate = LocalDate.of(selectedYear, selectedMonth, selectedDay).atTime(12, 0).atZone(ZoneId.systemDefault());
            Instant future = inputtedDate.toInstant();
            callback.accept(inputtedDate, now, Duration.between(now, future).toMillis(), title);
            this.onClose();
        } else {
            this.onClose();
        }
    }

    public void setCurrentDayFromButton(Button button)
    {
        this.selectedDay = Integer.parseInt(button.getMessage().getString());
        this.setTitleWithDate();
    }

    public void setTitleWithDate()
    {
        String day = "";
        String yearMonth = month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + selectedYear;
        String time;

        if (selectedDay != 0)
        {
            String suffix = switch (String.valueOf(selectedDay).charAt(String.valueOf(selectedDay).length() - 1))
            {
                case '1' -> "st";
                case '2' -> "nd";
                case '3' -> "rd";
                default -> "th";
            };
            if (STUPID_BRITISH_ENGLISH_ORDINAL_INDICATOR_EXCEPTIONS.contains(selectedDay)) suffix = "th";
            day = selectedDay + suffix + " of ";
        }

        String displayHourPadded = String.format("%02d", selectedHour);
        String displayMinutePadded = String.format("%02d", selectedMinute);
        String displaySecondPadded = String.format("%02d", selectedSecond);

        time = " at " + displayHourPadded + ":" + displayMinutePadded + ":" + displaySecondPadded;

        this.title = Component.literal(day + yearMonth + time);
    }

    @Override
    public void onClose()
    {
        this.minecraft.setScreen(parent);
    }

    public interface DatePickerCallback
    {
        void accept(ZonedDateTime chosenDate, Instant now, Long millisDifference, Component displayableTime);
    }
}
