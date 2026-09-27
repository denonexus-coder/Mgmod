package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.astc.AstcAtlasManager;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class AstcAtlasMixin {

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
    private void mgshaders_blockPngAnimationUpload(
            CallbackInfo ci
    ) {
        TextureAtlas atlas =
                (TextureAtlas) (Object) this;

        if (AstcAtlasManager.isAstcManaged(atlas)) {
            ci.cancel();
        }
    }
}
