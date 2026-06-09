package net.byws.selectiummod.mixin;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.byws.selectiummod.config.ConfigManager;

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin
{
    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void overrideNameColor(CallbackInfoReturnable<Component> cir)
    {
        ConfigManager configManager = ConfigManager.getInstance();
        if (!configManager.getEnableNameInfo()) return;

        PlayerInfo playerInfo = (PlayerInfo)(Object)this;
        String playerName = playerInfo.getProfile().getName();
        MutableComponent mutableText = Component.empty().append(playerName);

        var playerData = configManager.getPlayerData(playerInfo.getProfile().getId());
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

        cir.setReturnValue(mutableText);
    }
}