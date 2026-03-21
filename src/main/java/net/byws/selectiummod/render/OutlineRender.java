package net.byws.selectiummod.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

public class OutlineRender
{
    private static final RenderLayer CUSTOM_OUTLINE = RenderLayer.of(
            "entity_outline_front_cull",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            1536,
            true,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.ENTITY_CUTOUT_PROGRAM)
                    .texture(new RenderPhase.Texture(Identifier.of("selectium-mod", "textures/glow.png"), false, false))
                    .transparency(RenderPhase.NO_TRANSPARENCY)
                    .cull(new RenderPhase.Cull(true) {
                        @Override
                        public void startDrawing() {
                            RenderSystem.enableCull();
                            GL11.glCullFace(GL11.GL_FRONT);
                        }

                        @Override
                        public void endDrawing() {
                            GL11.glCullFace(GL11.GL_BACK);
                        }
                    })
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .build(false)
    );

    public static RenderLayer getFrontCullOutline(Identifier texture) { return CUSTOM_OUTLINE; }
}