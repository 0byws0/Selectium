package net.byws.selectiummod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.byws.selectiummod.config.ConfigManger;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerEntityMixin
{
    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private Component overrideNameColor(Component original)
    {
        if (ConfigManger.getEnableNameColor())
        {
            Player player = (Player) (Object) this;
            String playerName = player.getGameProfile().getName();
            ConfigManger.PlayerData playerData = ConfigManger.getPlayerData(playerName, player.getGameProfile().getId());
            MutableComponent mutableText = (MutableComponent) original;

            if (playerData != null)
            {
                if (!playerName.equals(playerData.nickname)) mutableText.append("(" + playerData.nickname + ")");
                mutableText.withStyle(ConfigManger.getPlayerColorFormatting(playerData.type));
            }
            else
            {
                mutableText.withStyle(ChatFormatting.WHITE);
            }

            return mutableText;
        }
        return original;
    }
}
