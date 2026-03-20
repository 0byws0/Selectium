package net.byws.selectiummod.mixin;

import net.byws.selectiummod.config.PlayerListConfig;
import net.byws.selectiummod.render.PlayerOutline;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>>
{
//    @Shadow
//    protected M model;
//
//    @Shadow
//    public abstract M getModel();

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
            CallbackInfo ci) {

        if (!((Entity)(Object)entity instanceof PlayerEntity player)) return;

        Integer playerOutline = PlayerListConfig.getPlayerOutlineColorRGBA(player.getGameProfile().getId());

        if (playerOutline != null) {

            matrices.push();
            matrices.scale(1.05f, 1.03f, 1.05f);

            LivingEntityRenderer playerRender = ((LivingEntityRenderer) (Object) this);

            RenderLayer outlineLayer = PlayerOutline.getFrontCullOutline(playerRender.getTexture(entity)); // ((LivingEntityRenderer) (Object) this).getTexture(entity)
            VertexConsumer consumer = vertexConsumers.getBuffer(outlineLayer);

            playerRender.getModel().render(matrices, consumer, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, playerOutline); // model

            matrices.pop();
        }
    }
}