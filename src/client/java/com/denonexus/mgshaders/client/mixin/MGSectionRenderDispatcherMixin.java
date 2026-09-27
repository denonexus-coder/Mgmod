package com.denonexus.mgshaders.client.mixin;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Expõe campos internos de SectionRenderDispatcher.
 *
 * Mojang 1.21.11 mapping:
 *   SectionRenderDispatcher → hts
 *   sectionCompiler         → hts.q (SectionCompiler)
 *
 * Permite ao pipeline MG acessar o compilador de seções para,
 * se necessário, enfileirar recompilações fora do ciclo padrão.
 */
@Mixin(SectionRenderDispatcher.class)
public interface MGSectionRenderDispatcherMixin {

    @Accessor("sectionCompiler")
    SectionCompiler mg$getSectionCompiler();
}
