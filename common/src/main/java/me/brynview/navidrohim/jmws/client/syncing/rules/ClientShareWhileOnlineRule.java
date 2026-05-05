package me.brynview.navidrohim.jmws.client.syncing.rules;

import me.brynview.navidrohim.jmws.client.syncing.rules.registry.ClientShareRule;
import me.brynview.navidrohim.jmws.client.ui.elements.Checkbox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClientShareWhileOnlineRule implements ClientShareRule
{
    Component FAILURE = Component.literal("Object cannot be shared while owner is offline.");
    Component DESCRIPTION = Component.literal("Object will only share while you are online.");
    Component NAME = Component.literal("Only share when your online");

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
    public @NotNull List<RuleElement<?>> getDisplayableElements()
    {
        Checkbox c = Checkbox.buildCheckbox(NAME, (cb) -> {}, null, null, 13);
        return List.of(
                new RuleElement<Checkbox>(c, "enabled", InputTypes.BOOLEAN, (cb) -> cb.isChecked)
        );
    }
}
