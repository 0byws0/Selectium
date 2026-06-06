package net.byws.selectiummod.mixin;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.byws.selectiummod.config.ConfigJsonObj;
import net.byws.selectiummod.config.ConfigManager;

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin
{
    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void overrideNameColor(CallbackInfoReturnable<Component> cir)
    {
        if (ConfigManager.getEnableNameInfo())
        {
            PlayerInfo playerInfo = (PlayerInfo)(Object)this;

            String playerName = playerInfo.getProfile().getName();
            ConfigJsonObj.PlayerData playerData = ConfigManager.getPlayerData(playerName, playerInfo.getProfile().getId());
            MutableComponent mutableText = Component.empty().append(playerName);

            if (playerData != null)
            {
                if (!playerName.equals(playerData.nickname)) mutableText.append(" (" + playerData.nickname + ")");
                mutableText.withStyle(ConfigManager.getPlayerColorFormatting(playerData.type));
            }

            cir.setReturnValue(mutableText);
        }
    }
}