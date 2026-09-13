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

@Mixin(PlayerInfo.class) // value = PlayerInfo.class, priority = 9999
public abstract class PlayerInfoMixin
{
    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void overrideNameColor(CallbackInfoReturnable<Component> cir)
    {
        ConfigManager configManager = ConfigManager.getInstance();
        if (!configManager.getEnableInfo()) return;

        PlayerInfo playerInfo = (PlayerInfo)(Object)this;
        String playerName = playerInfo.getProfile().getName();
        MutableComponent mutableText = Component.empty().append(playerName);

        ConfigJsonObj.PlayerData playerData = configManager.getPlayerData(playerInfo.getProfile().getId());
        if (playerData == null) playerData = configManager.getPlayerData(playerName);

        if (playerData != null)
        {
            if (configManager.getEnableGroupInfo() && !playerData.group.isEmpty())
            {
                mutableText = Component.literal("[" + playerData.group + "] ").append(mutableText);
            }
            if (configManager.getEnableNicknameInfo() && !playerName.equals(playerData.nickname) && !playerData.nickname.isEmpty())
            {
                mutableText.append(" (" + playerData.nickname + ")");
            }
            if (playerData.clientType != null && !playerData.clientType.equals(playerData.serverType))
            {
                mutableText.withStyle(playerData.clientType.CLIENT_COLOR_FORMAT);
            }
            else if (playerData.serverType != null)
            {
                mutableText.withStyle(playerData.serverType.SERVER_COLOR_FORMAT);
            }
            // mutableText.withStyle(ChatFormatting.BOLD);
        }

        cir.setReturnValue(mutableText);
    }
}