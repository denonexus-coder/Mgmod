package com.denonexus.mgshaders.client.astc;

import com.mojang.blaze3d.textures.GpuTextureView;

/**
 * Accessor interface for AstcAtlasMixin.
 * Must live OUTSIDE the registered mixin package so that non-mixin
 * code (AstcAtlasManager) can reference it directly.
 */
public interface AstcAtlasMixinAccess {

    void mg$setMipViews(
            GpuTextureView[] views
    );

    GpuTextureView[] mg$getMipViews();

    void mg$setMipLevelCount(
            int count
    );

    void mg$setMaxMipLevel(
            int level
    );
}
