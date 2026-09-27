package com.denonexus.mgshaders.client.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Expõe as listas de seções visíveis de LevelRenderer.
 *
 * Mojang 1.21.11 mappings:
 *   visibleSections        → hoh.u  (ObjectArrayList<RenderSection>)
 *   nearbyVisibleSections  → hoh.v  (ObjectArrayList<RenderSection>)
 *
 * Usado por MGChunkBatch para obter a lista exata que o vanilla processou
 * sem duplicar o algoritmo de culling/occlusion.
 */
@Mixin(LevelRenderer.class)
public interface MGVisibleSectionsMixin {

    @Accessor("visibleSections")
    ObjectArrayList<SectionRenderDispatcher.RenderSection> mg$getVisibleSections();

    @Accessor("nearbyVisibleSections")
    ObjectArrayList<SectionRenderDispatcher.RenderSection> mg$getNearbyVisibleSections();
}
