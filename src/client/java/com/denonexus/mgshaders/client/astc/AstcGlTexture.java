package com.denonexus.mgshaders.client.astc;

import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.texture.GlTexture;

/**
 * Real Minecraft 1.21.11 GlTexture wrapper around an already-created
 * OpenGL texture name containing compressed ASTC data.
 *
 * The GlTexture glId is final in Minecraft 1.21.11, therefore the ASTC
 * texture MUST be represented by a new GlTexture instance instead of
 * modifying the existing one.
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
