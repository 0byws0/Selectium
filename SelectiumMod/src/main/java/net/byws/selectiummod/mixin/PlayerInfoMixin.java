package net.byws.selectiummod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.byws.selectiummod.config.ConfigJsonObj;
import net.byws.selectiummod.config.ConfigManager;

@Mixin(value = PlayerInfo.class, priority = 1)
public abstract class PlayerInfoMixin
{
    @Shadow
    public abstract GameProfile getProfile();

    private void resetComponent(Component component, String targetText)
    {
        if (component instanceof MutableComponent mutableComponent && component.getString().contains(targetText))
        {
            mutableComponent.setStyle(mutableComponent.getStyle().withColor(ChatFormatting.RESET));
        }

        for (Component sibling : component.getSiblings()) resetComponent(sibling, targetText);
    }

    @ModifyReturnValue(method = "getTabListDisplayName", at = @At("RETURN"))
    private Component overrideNameColor(Component original)
    {
        ConfigManager configManager = ConfigManager.getInstance();
        if (!configManager.getEnableInfo()) return original;

        String playerName = ((PlayerInfo)(Object)this).getProfile().name();
        ConfigJsonObj.PlayerData playerData = configManager.getPlayerData(playerName);
        MutableComponent modifiedOriginal = (original == null) ? Component.literal(getProfile().name()) : original.copy();
        resetComponent(modifiedOriginal, playerName);
        if (playerData == null) return modifiedOriginal.withStyle(ChatFormatting.WHITE);

        MutableComponent updatedName = Component.empty().withStyle(playerData.getDominantColor());

        if (configManager.getEnableGroupInfo() && !playerData.group.isEmpty())
        {
            updatedName.append("[" + playerData.group + "] ");
        }

        updatedName.append(modifiedOriginal);

        if (configManager.getEnableNicknameInfo() && !playerName.equals(playerData.nickname) && !playerData.nickname.isEmpty())
        {
            updatedName.append(" (" + playerData.nickname + ")");
        }

        return updatedName;
    }
}
