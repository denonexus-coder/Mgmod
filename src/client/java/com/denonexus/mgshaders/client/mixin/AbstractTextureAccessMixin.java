package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.astc.AbstractTextureAccess;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractTexture.class)
public abstract class AbstractTextureAccessMixin
        implements AbstractTextureAccess {

    @Shadow
    protected GpuTexture texture;

    @Shadow
    protected GpuTextureView textureView;

    @Override
    public void mg$setTexture(GpuTexture texture) {
        this.texture = texture;
    }

    @Override
    public void mg$setTextureView(GpuTextureView textureView) {
        this.textureView = textureView;
    }
}
