package me.brynview.navidrohim.jmws.client.syncing.rules;

import com.google.common.collect.ImmutableSet;
import me.brynview.navidrohim.jmws.client.syncing.rules.registry.ClientShareRule;
import me.brynview.navidrohim.jmws.client.ui.elements.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClientShareWhileOnlineRule implements ClientShareRule
{
    Component FAILURE = Component.literal("Object cannot be shared while owner is offline.");
    Component DESCRIPTION = Component.literal("Object will only share while you are online.");
    Component NAME = Component.literal("Only share when your online");

    private final ImmutableSet<RuleSetting<?>> settings;

    public ClientShareWhileOnlineRule()
    {
        this.settings = ImmutableSet.of();
    }

    private ClientShareWhileOnlineRule(RuleSetting<?>... settings)
    {
        this.settings = ImmutableSet.copyOf(settings);
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
    public @NotNull List<RuleSetting<?>.RuleSettingWrapper<?>> getDisplayableElements()
    {
        Checkbox cb = Checkbox.buildCheckbox(this.getDisplayName(), (_) -> {}, Tooltip.create(getDescription()), null, 13);
        RuleSetting<Boolean>.RuleSettingWrapper<Checkbox> w = new RuleSetting<>(this,"active", InputTypes.BOOLEAN).getWrapper(cb, (c) -> c.isChecked);

        return List.of(w);
    }
}
