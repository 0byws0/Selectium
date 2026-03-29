package net.byws.selectiummod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.byws.selectiummod.config.ConfigManger;

import net.byws.selectiummod.render.OutlineRender;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>>
{
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderDepthOutline(
            T entity,
            float yaw,
            float tickDelta,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            CallbackInfo ci)
    {
        if (!ConfigManger.getEnableOutline()) return;
        if (!(entity instanceof Player player)) return;

        Integer playerOutline = ConfigManger.getPlayerColorRGBA(player.getGameProfile().getId());

        if (playerOutline != null)
        {
            matrices.pushPose();
            matrices.scale(1.03f, 1.01f, 1.03f);

            LivingEntityRenderer playerRender = ((LivingEntityRenderer) (Object) this);

            RenderType outlineLayer = OutlineRender.getFrontCullOutline(playerRender.getTextureLocation(entity));
            VertexConsumer consumer = vertexConsumers.getBuffer(outlineLayer);

            playerRender.getModel().renderToBuffer(matrices, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, playerOutline);

            matrices.popPose();
        }
    }
}