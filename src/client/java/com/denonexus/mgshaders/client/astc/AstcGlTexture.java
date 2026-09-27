package com.denonexus.mgshaders.client.astc;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.TextureFormat;

/**
 * Minecraft 1.21.11 Mojang-mappings GlTexture wrapper
 * around an OpenGL texture name containing ASTC data.
 */
public final class AstcGlTexture extends GlTexture {

    public AstcGlTexture(
            int usage,
            String label,
            TextureFormat format,
            int width,
            int height,
            int depthOrLayers,
            int mipLevels,
            int glId
    ) {
        super(
                usage,
                label,
                format,
                width,
                height,
                depthOrLayers,
                mipLevels,
                glId
        );
    }
}
