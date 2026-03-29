package net.byws.selectiummod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.byws.selectiummod.config.ConfigManger;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin
{
    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private Text overrideNameColor(Text original)
    {
        if (ConfigManger.getEnableNameColor())
        {
            PlayerEntity player = (PlayerEntity) (Object) this;

            Formatting playerName = ConfigManger.getPlayerColorFormatting(player.getGameProfile().getId());
            MutableText mutableText = (MutableText) original;

            if (playerName != null) mutableText.formatted(playerName);

            return mutableText;
        }
        else
        {
            return original;
        }
    }
}
