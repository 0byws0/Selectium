//package net.byws.selectiummod.mixin;
//
//import com.llamalad7.mixinextras.injector.ModifyReturnValue;
//import net.byws.selectiummod.config.ConfigManger;
//import net.minecraft.ChatFormatting;
//import net.minecraft.client.gui.components.PlayerTabOverlay;
//import net.minecraft.client.multiplayer.PlayerInfo;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.MutableComponent;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//
//@Mixin(PlayerTabOverlay.class)
//public class PlayerListHudMixin
//{
//    @ModifyReturnValue(method = "getNameForDisplay", at = @At("RETURN"))
//    private Component overrideNameColor(Component original, PlayerInfo entry)
//    {
//        if (ConfigManger.getEnableNameColor())
//        {
//            ChatFormatting playerName = ConfigManger.getPlayerColorFormatting(entry.getProfile().id());
//            MutableComponent mutableText = (MutableComponent) original;
//
//            if (playerName != null) mutableText.withStyle(playerName);
//
//            return mutableText;
//        }
//        else
//        {
//            return original;
//        }
//    }
//}
