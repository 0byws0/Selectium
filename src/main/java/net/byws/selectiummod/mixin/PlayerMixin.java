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
        if (!configManager.getEnableNameInfo()) return original;

        Player player = (Player)(Object)this;
        String playerName = player.getGameProfile().getName();
        MutableComponent mutableText = original.copy();

        ConfigJsonObj.PlayerData playerData = configManager.getPlayerData(player.getGameProfile().getId());
        if (playerData == null) playerData = configManager.getPlayerData(playerName);

        mutableText.withStyle(ChatFormatting.WHITE);

        if (playerData != null)
        {
            if (!playerName.equals(playerData.nickname) && !playerData.nickname.isEmpty()) mutableText.append(" (" + playerData.nickname + ")");
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

        return mutableText;
    }
}
