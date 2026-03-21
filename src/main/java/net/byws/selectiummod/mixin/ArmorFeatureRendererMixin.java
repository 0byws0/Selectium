package net.byws.selectiummod.mixin;

import net.byws.selectiummod.render.OutlineRender;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>>
{
    @Inject(method = "renderArmorParts", at = @At("HEAD"))
    public void renderDepthOutline(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            A model,
            int i,
            Identifier identifier,
            CallbackInfo ci)
    {
        matrices.push();
        matrices.scale(1.0f, 1.0f, 1.0f);

        RenderLayer outlineLayer = OutlineRender.getFrontCullOutline(identifier);
        VertexConsumer consumer = vertexConsumers.getBuffer(outlineLayer);

        model.render(matrices, consumer, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, 0xFF00FF00);

        matrices.pop();
    }
}
