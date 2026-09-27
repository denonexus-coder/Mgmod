package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.astc.GlTextureAccess;
import com.mojang.blaze3d.opengl.GlTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GlTexture.class)
public abstract class GlTextureAccessMixin
        implements GlTextureAccess {

    @Shadow
    @Final
    @Mutable
    private int id;

    @Unique
    @Override
    public int mg$getId() {
        return this.id;
    }

    @Unique
    @Override
    public void mg$setId(int id) {
        this.id = id;
    }
}
