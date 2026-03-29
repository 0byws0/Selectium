package net.byws.selectiummod.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class OutlineRender
{
    private static final RenderType CUSTOM_OUTLINE = RenderType.create(
            "entity_outline_front_cull",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536,
            true,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_CUTOUT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(ResourceLocation.fromNamespaceAndPath("selectium-mod", "textures/glow.png"), false, false))
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setCullState(new RenderStateShard.CullStateShard(true) {
                        @Override
                        public void setupRenderState() {
                            RenderSystem.enableCull();
                            GL11.glCullFace(GL11.GL_FRONT);
                        }

                        @Override
                        public void clearRenderState() {
                            GL11.glCullFace(GL11.GL_BACK);
                        }
                    })
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .createCompositeState(false)
    );

    public static RenderType getFrontCullOutline(ResourceLocation texture) { return CUSTOM_OUTLINE; }
}