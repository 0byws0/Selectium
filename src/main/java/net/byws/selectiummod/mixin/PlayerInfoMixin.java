package net.byws.selectiummod.mixin;

import net.minecraft.ChatFormatting;
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
        ConfigManager configManager = ConfigManager.getInstance();
        if (!configManager.getEnableInfo()) return;

        String playerName = ((PlayerInfo)(Object)this).getProfile().getName();
        ConfigJsonObj.PlayerData playerData = configManager.getPlayerData(playerName);
        MutableComponent mutableText = Component.empty().append(playerName).withStyle(ChatFormatting.WHITE);
        if (playerData == null)
        {
            cir.setReturnValue(mutableText);
            return;
        }

        if (configManager.getEnableGroupInfo() && !playerData.group.isEmpty())
        {
            mutableText = Component.literal("[" + playerData.group + "] ").append(mutableText);
        }
        if (configManager.getEnableNicknameInfo() && !playerName.equals(playerData.nickname) && !playerData.nickname.isEmpty())
        {
            mutableText.append(" (" + playerData.nickname + ")");
        }
        mutableText.withStyle(playerData.getDominantColor());

        cir.setReturnValue(mutableText);
    }
}