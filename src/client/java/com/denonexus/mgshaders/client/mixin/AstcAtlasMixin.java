package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.astc.AstcAtlasManager;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class AstcAtlasMixin
        implements AstcAtlasMixinAccess {

    @Shadow
    @Mutable
    private int maxMipLevel;

    @Shadow
    @Mutable
    private int mipLevelCount;

    @Shadow
    @Mutable
    private GpuTextureView[] mipViews;

    @Inject(
            method = "uploadInitialContents",
            at = @At("RETURN")
    )
    private void mgshaders_installAstcAtlas(
            CallbackInfo ci
    ) {
        AstcAtlasManager.install(
                (TextureAtlas) (Object) this
        );
    }

    @Inject(
            method = "uploadAnimationFrames",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mgshaders_blockAstcAnimationUpload(
            CallbackInfo ci
    ) {
        TextureAtlas atlas =
                (TextureAtlas) (Object) this;

        if (AstcAtlasManager.isAstcManaged(atlas)) {
            ci.cancel();
        }
    }

    @Override
    public void mg$setMipViews(
            GpuTextureView[] views
    ) {
        this.mipViews = views;
    }

    @Override
    public GpuTextureView[] mg$getMipViews() {
        return this.mipViews;
    }

    @Override
    public void mg$setMipLevelCount(
            int count
    ) {
        this.mipLevelCount = count;
    }

    @Override
    public void mg$setMaxMipLevel(
            int level
    ) {
        this.maxMipLevel = level;
    }
}
