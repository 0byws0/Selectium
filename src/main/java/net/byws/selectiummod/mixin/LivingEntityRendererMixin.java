//package net.byws.selectiummod.mixin;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import net.byws.selectiummod.config.ConfigManger;
//
//import net.byws.selectiummod.render.OutlineRender;
//import net.minecraft.client.model.EntityModel;
//import net.minecraft.client.renderer.LightTexture;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.entity.LivingEntityRenderer;
//import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
//import net.minecraft.client.renderer.texture.OverlayTexture;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.player.Player;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.util.UUID;
//
//@Mixin(LivingEntityRenderer.class)
//public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
//{
//    private UUID playerUUID;
//
//    @Inject(method = "extractRenderState", at = @At("HEAD"))
//    private void getUUID(T livingEntity, S livingEntityRenderState, float f)
//    {
//        if (!(livingEntity instanceof Player player)) return;
//
//        playerUUID = player.getGameProfile().getId();
//    }
//
//    @Inject(
//            method = "render",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
//                    shift = At.Shift.AFTER
//            )
//    )
//    private void renderDepthOutline(
//            S livingEntityRenderState,
//            PoseStack matrices,
//            MultiBufferSource vertexConsumers,
//            int light,
//            CallbackInfo ci)
//    {
//        if (!ConfigManger.getEnableOutline()) return;
//        //if (!(entity instanceof Player player)) return;
//
//        Integer playerOutline = ConfigManger.getPlayerColorRGBA(playerUUID);
//
//        if (playerOutline != null)
//        {
//            matrices.pushPose();
//            matrices.scale(1.03f, 1.01f, 1.03f);
//
//            LivingEntityRenderer playerRender = ((LivingEntityRenderer) (Object) this);
//
//            RenderType outlineLayer = OutlineRender.getFrontCullOutline(playerRender.getTextureLocation(livingEntityRenderState));
//            VertexConsumer consumer = vertexConsumers.getBuffer(outlineLayer);
//
//            playerRender.getModel().renderToBuffer(matrices, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, playerOutline);
//
//            matrices.popPose();
//        }
//    }
//}