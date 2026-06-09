package net.byws.selectiummod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
        String playerName = player.getGameProfile().getName();
        MutableComponent mutableText = (MutableComponent)original;

        var playerData = configManager.getPlayerData(player.getGameProfile().getId());
        if (playerData.getRight() == null) playerData = configManager.getPlayerData(playerName);

        if (playerData.getRight() != null)
        {
            if (!playerName.equals(playerData.getRight().nickname) && !playerData.getRight().nickname.isEmpty())
            {
                mutableText.append(" (" + playerData.getRight().nickname + ")");
            }
            mutableText.withStyle((playerData.getLeft()) ? playerData.getRight().type.getClientColorFormatting()
                    : playerData.getRight().type.getServerColorFormatting());
        }
        else
        {
            mutableText.withStyle(ChatFormatting.WHITE);
        }

        return mutableText;
    }
}
