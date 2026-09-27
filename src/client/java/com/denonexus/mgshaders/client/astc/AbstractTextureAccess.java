package com.denonexus.mgshaders.client.astc;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;

public interface AbstractTextureAccess {

    void mg$setTexture(GpuTexture texture);

    void mg$setTextureView(GpuTextureView textureView);
}
