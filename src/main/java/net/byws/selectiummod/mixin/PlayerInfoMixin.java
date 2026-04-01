package net.byws.selectiummod.mixin;

import net.byws.selectiummod.config.ConfigManger;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin
{
    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void overrideNameColor(CallbackInfoReturnable<Component> cir)
    {
        if (ConfigManger.getEnableNameColor())
        {
            PlayerInfo playerInfo = (PlayerInfo)(Object)this;
            ChatFormatting playerName = ConfigManger.getPlayerColorFormatting(playerInfo.getProfile().id());
            MutableComponent mutableText = Component.empty().append(playerInfo.getProfile().name());

            if (playerName != null) mutableText.withStyle(playerName);

            cir.setReturnValue(mutableText);
        }
    }
}