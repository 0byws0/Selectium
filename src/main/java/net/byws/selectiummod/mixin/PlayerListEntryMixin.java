package net.byws.selectiummod.mixin;

import net.byws.selectiummod.config.ConfigManger;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin
{
    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void overrideNameColor(CallbackInfoReturnable<Text> cir)
    {
        PlayerListEntry playerEntry = (PlayerListEntry)(Object)this;
        Formatting playerName = ConfigManger.getPlayerColorFormatting(playerEntry.getProfile().getId());

        if (playerName != null)
        {
            MutableText mutableText = Text.empty().append(playerEntry.getProfile().getName());

            cir.setReturnValue(mutableText.formatted(playerName));
        }
    }
}
