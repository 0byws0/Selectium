package net.byws.selectiummod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.byws.selectiummod.config.ConfigManger;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin
{
    @ModifyReturnValue(method = "getPlayerName", at = @At("RETURN"))
    private Text overrideNameColor(Text original, PlayerListEntry entry)
    {
        if (ConfigManger.getEnableNameColor())
        {
            Formatting playerName = ConfigManger.getPlayerColorFormatting(entry.getProfile().getId());
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
