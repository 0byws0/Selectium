package net.byws.selectiummod.mixin;

import net.byws.selectiummod.config.PlayerListConfig;
import net.byws.selectiummod.render.OutlineRender;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>>
{
    @Inject(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderDepthOutline(
            T entity,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci)
    {
        if (!(entity instanceof PlayerEntity player)) return;

        Integer playerOutline = PlayerListConfig.getPlayerColorRGBA(player.getGameProfile().getId());

        if (playerOutline != null) {

            matrices.push();
            matrices.scale(1.03f, 1.01f, 1.03f);

            LivingEntityRenderer playerRender = ((LivingEntityRenderer) (Object) this);

            RenderLayer outlineLayer = OutlineRender.getFrontCullOutline(playerRender.getTexture(entity));
            VertexConsumer consumer = vertexConsumers.getBuffer(outlineLayer);

            playerRender.getModel().render(matrices, consumer, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, playerOutline);

            matrices.pop();
        }
    }
}