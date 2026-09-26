package com.denonexus.mgshaders.region.mixin;

import com.denonexus.mgshaders.region.storage.RegionLz4Cache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RegionFileStorage.class)
public abstract class RegionFileStorageMixin {

    @Inject(
            method =
                    "read(Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/nbt/CompoundTag;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mg$read(
            ChunkPos pos,
            CallbackInfoReturnable<CompoundTag> cir
    ) {

        CompoundTag cached =
                RegionLz4Cache.get(
                        (RegionFileStorage)(Object)this,
                        pos
                );

        if (cached != null) {
            cir.setReturnValue(cached);
        }
    }

    @Inject(
            method =
                    "read(Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/nbt/CompoundTag;",
            at = @At("RETURN")
    )
    private void mg$store(
            ChunkPos pos,
            CallbackInfoReturnable<CompoundTag> cir
    ) {

        RegionLz4Cache.put(
                (RegionFileStorage)(Object)this,
                pos,
                cir.getReturnValue()
        );
    }

    @Inject(
            method =
                    "write(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("HEAD")
    )
    private void mg$invalidate(
            ChunkPos pos,
            CompoundTag tag,
            CallbackInfo ci
    ) {

        RegionLz4Cache.invalidate(
                (RegionFileStorage)(Object)this,
                pos
        );
    }
}
