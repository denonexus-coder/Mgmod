package com.denonexus.mgshaders.client.mixin;

import com.mojang.blaze3d.textures.GpuTextureView;

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
