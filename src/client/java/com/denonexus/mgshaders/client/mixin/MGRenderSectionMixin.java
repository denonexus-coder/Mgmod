package com.denonexus.mgshaders.client.mixin;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Expõe os campos essenciais de SectionRenderDispatcher.RenderSection.
 *
 * Mojang 1.21.11 mapping (hts$a):
 *   sectionMesh    → hts$a.c  (AtomicReference<SectionMesh>)
 *   renderOrigin   → hts$a.j  (BlockPos.MutableBlockPos)
 *   sectionNode    → hts$a.i  (long)
 *   dirty          → hts$a.h  (boolean)
 *   wasPreviouslyEmpty → hts$a.n (boolean)
 *   index          → hts$a.b  (int)
 *
 * sectionMesh é AtomicReference porque pode ser substituído concorrentemente
 * pela thread de upload após uma recompilação.
 *
 * Nota: getSectionNode() já existe como método público em RenderSection
 * (ver MGUploadMixin), mas o Accessor aqui garante acesso direto ao campo
 * para leitura sem sincronização adicional.
 */
@Mixin(SectionRenderDispatcher.RenderSection.class)
public interface MGRenderSectionMixin {

    @Accessor("sectionMesh")
    AtomicReference<SectionMesh> mg$getSectionMeshRef();

    @Accessor("renderOrigin")
    BlockPos.MutableBlockPos mg$getRenderOrigin();

    @Accessor("sectionNode")
    long mg$getSectionNodeField();

    @Accessor("dirty")
    boolean mg$isDirty();

    @Accessor("wasPreviouslyEmpty")
    boolean mg$wasPreviouslyEmpty();

    @Accessor("index")
    int mg$getIndex();
}
