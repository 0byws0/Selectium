package net.byws.selectiummod.mixin;

import net.byws.selectiummod.config.PlayerListConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin
{
//    @Shadow
//    protected abstract HoverEvent getHoverEvent();

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void overrideNameColor(CallbackInfoReturnable<Text> cir)
    {
        PlayerEntity player = (PlayerEntity)(Object)this;

        Formatting playerName = PlayerListConfig.getPlayerColorFormatting(player.getGameProfile().getId());

        if (playerName != null)
        {
            MutableText mutableText = Text.empty().append(player.getName());

            cir.setReturnValue(mutableText.formatted(playerName));
        }
    }
}
