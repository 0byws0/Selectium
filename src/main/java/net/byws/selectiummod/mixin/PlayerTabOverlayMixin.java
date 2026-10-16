package net.byws.selectiummod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.byws.selectiummod.config.ConfigJsonObj;
import net.byws.selectiummod.config.ConfigManager;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin
{
    @ModifyReturnValue(method = "getNameForDisplay", at = @At("RETURN"))
    public Component overrideNameColor(Component original, PlayerInfo entry)
    {
        ConfigManager configManager = ConfigManager.getInstance();
        if (!configManager.getEnableInfo()) return original;

        String playerName = entry.getProfile().name();
        ConfigJsonObj.PlayerData playerData = configManager.getPlayerData(playerName);
        MutableComponent mutableText = original.copy().withStyle(ChatFormatting.WHITE);
        if (playerData == null) return mutableText;

        mutableText.withStyle(playerData.getDominantColor());
        if (configManager.getEnableGroupInfo() && !playerData.group.isEmpty())
        {
            mutableText = Component.literal("[" + playerData.group + "] ").withStyle(playerData.getDominantColor()).append(mutableText);
        }
        if (configManager.getEnableNicknameInfo() && !playerName.equals(playerData.nickname) && !playerData.nickname.isEmpty())
        {
            mutableText.append(" (" + playerData.nickname + ")");
        }

        return mutableText;
    }
}
