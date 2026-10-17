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

@Mixin(value = Player.class, priority = 1)
public abstract class PlayerMixin
{
    private void resetComponent(Component component, String targetText)
    {
        if (component instanceof MutableComponent mutableComponent && component.getString().contains(targetText))
        {
            mutableComponent.setStyle(mutableComponent.getStyle().withColor(ChatFormatting.RESET));
        }

        for (Component sibling : component.getSiblings()) resetComponent(sibling, targetText);
    }

    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private Component overrideNameColor(Component original)
    {
        ConfigManager configManager = ConfigManager.getInstance();
        if (!configManager.getEnableInfo()) return original;

        String playerName = ((Player)(Object)this).getGameProfile().name();
        ConfigJsonObj.PlayerData playerData = configManager.getPlayerData(playerName);
        MutableComponent modifiedOriginal = original.copy();
        resetComponent(modifiedOriginal, playerName);
        if (playerData == null) return original.copy().withStyle(ChatFormatting.WHITE);

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
