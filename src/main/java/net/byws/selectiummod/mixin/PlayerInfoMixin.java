package net.byws.selectiummod.mixin;

import net.byws.selectiummod.config.ConfigJsonObj;
import net.byws.selectiummod.config.ConfigManger;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
        if (ConfigManger.getEnableNameInfo())
        {
            PlayerInfo playerInfo = (PlayerInfo)(Object)this;

            String playerName = playerInfo.getProfile().name();
            ConfigJsonObj.PlayerData playerData = ConfigManger.getPlayerData(playerName, playerInfo.getProfile().id());
            MutableComponent mutableText = Component.empty().append(playerName);

            if (playerData != null)
            {
                if (!playerName.equals(playerData.nickname)) mutableText.append(" (" + playerData.nickname + ")");
                mutableText.withStyle(ConfigManger.getPlayerColorFormatting(playerData.type));
            }

            cir.setReturnValue(mutableText);
        }
    }
}