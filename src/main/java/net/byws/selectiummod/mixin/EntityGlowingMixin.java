//package net.byws.selectiummod.mixin;
//
//import net.byws.selectiummod.config.PlayerListConfig;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.player.PlayerEntity;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//import java.util.UUID;
//
//@Mixin(Entity.class)
//public abstract class EntityGlowingMixin
//{
//
//    @Inject(method = "getTeamColorValue", at = @At("RETURN"), cancellable = true)
//    private void overrideOutlineColor(CallbackInfoReturnable<Integer> cir)
//    {
//        if (!(((Entity)(Object)this) instanceof PlayerEntity currentPlayer)) return;
//
//        UUID playerUUID = currentPlayer.getGameProfile().getId();
//        if (PlayerListConfig.isAlly(playerUUID))
//        {
//            cir.setReturnValue(PlayerListConfig.getAllyColorRGBA() & 0x00FFFFFF);
//        }
//        else if (PlayerListConfig.isTarget(playerUUID))
//        {
//            cir.setReturnValue(PlayerListConfig.getTargetColorRGBA() & 0x00FFFFFF);
//        }
//    }
//
//    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
//    private void overrideGlow(CallbackInfoReturnable<Boolean> cir)
//    {
//        if (!(((Entity)(Object)this) instanceof PlayerEntity currentPlayer)) return;
//        if (currentPlayer.isInvisible()) return;
//
//        UUID playerUUID = currentPlayer.getGameProfile().getId();
//        if (PlayerListConfig.isAlly(playerUUID) || PlayerListConfig.isTarget(playerUUID))
//        {
//            cir.setReturnValue(true);
//        }
//    }
//}