package me.brynview.navidrohim.jmws.client.syncing.rules;

import me.brynview.navidrohim.jmws.client.syncing.rules.registry.ClientShareRule;
import me.brynview.navidrohim.jmws.client.ui.elements.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ClientShareWhileOnlineRule extends ClientShareRule
{
    Component FAILURE = Component.literal("Object cannot be shared while owner is offline.");
    Component DESCRIPTION = Component.literal("Object will only share while you are online.");
    Component NAME = Component.literal("Only share when your online");

    public ClientShareWhileOnlineRule()
    {
        super();
    }

    @Override
    public Component getFailureMessage() {
        return FAILURE;
    }

    @Override
    public Component getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Component getDisplayName()
    {
        return NAME;
    }

    @Override
    public String getRegistryKey()
    {
        return "share_while_online";
    }

    @Override
    protected List<RuleSetting<?>> getSettingsForRule()
    {
        return List.of(new RuleSetting<>(this, "active", InputTypes.BOOLEAN));
    }

    @Override
    @NotNull
    public List<RuleSetting<?>.RuleSettingWrapper<?>> getDisplayableElements()
    {
        List<RuleSetting<?>.RuleSettingWrapper<?>> displayableElements = new ArrayList<>();
        Checkbox cb = Checkbox.buildCheckbox(this.getDisplayName(), (_) -> {}, Tooltip.create(getDescription()), null);

        for (RuleSetting<?> setting : this.settings)
        {
            RuleSetting<?>.RuleSettingWrapper<?> wrapper = setting.getWrapper(cb, c -> c.isChecked);
            displayableElements.add(wrapper);
        }
        return displayableElements;
    }
}
