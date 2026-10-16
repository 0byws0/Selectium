package net.byws.selectiummod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.byws.selectiummod.config.ConfigJsonObj;
import net.byws.selectiummod.config.ConfigManager;

@Mixin(Player.class)
public abstract class PlayerMixin
{
    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private Component overrideNameColor(Component original)
    {
        ConfigManager configManager = ConfigManager.getInstance();
        if (!configManager.getEnableInfo()) return original;

        String playerName = ((Player)(Object)this).getGameProfile().name();
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
