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
public abstract class PlayerEntityMixin
{
    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private Component overrideNameColor(Component original)
    {
        ConfigManager configManager = ConfigManager.getInstance();
        if (!configManager.getEnableNameInfo()) return original;

        Player player = (Player)(Object)this;
        String playerName = player.getGameProfile().name();
        MutableComponent mutableText = original.copy();

        ConfigJsonObj.PlayerData playerData = configManager.getPlayerData(player.getGameProfile().id());
        if (playerData == null) playerData = configManager.getPlayerData(playerName);

        if (playerData != null)
        {
            if (!playerName.equals(playerData.nickname) && !playerData.nickname.isEmpty()) mutableText.append(" (" + playerData.nickname + ")");
            return mutableText.withStyle((playerData.clientSide) ? playerData.type.getClientColorFormatting() : playerData.type.getServerColorFormatting());
        }
        return mutableText.withStyle(ChatFormatting.WHITE);
    }
}
