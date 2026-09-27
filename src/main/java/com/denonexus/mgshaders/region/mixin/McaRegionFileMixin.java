package com.denonexus.mgshaders.region.mixin;

import com.denonexus.mgshaders.nativebridge.McaNativeLoader;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;

@Mixin(RegionFile.class)
public abstract class McaRegionFileMixin {

    @Shadow
    @Final
    private Path path;

    @Unique
    private long mg$mcaHandle;

    @Unique
    private static final ThreadLocal<ByteBuffer> MG$BUFFER =
            ThreadLocal.withInitial(
                    McaNativeLoader::newBuffer
            );

    @Inject(
            method = "getChunkDataInputStream(Lnet/minecraft/world/level/ChunkPos;)Ljava/io/DataInputStream;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mg$nativeRead(
            ChunkPos pos,
            CallbackInfoReturnable<DataInputStream> cir
    ) {

        if (
                !McaNativeLoader.isLoaded()
                        || path == null
        ) {
            return;
        }

        try {

            if (mg$mcaHandle == 0) {

                String file =
                        path.toAbsolutePath()
                                .toString();

                if (!file.endsWith(".mca")) {
                    return;
                }

                mg$mcaHandle =
                        McaNativeLoader.open(
                                file
                        );

                if (mg$mcaHandle == 0) {
                    return;
                }
            }

            ByteBuffer buffer =
                    MG$BUFFER.get();

            buffer.clear();

            int bytes =
                    McaNativeLoader.read(
                            mg$mcaHandle,
                            pos.x,
                            pos.z,
                            buffer,
                            buffer.capacity()
                    );

            if (bytes == -3) {

                McaNativeLoader.close(
                        mg$mcaHandle
                );

                mg$mcaHandle =
                        McaNativeLoader.open(
                                path.toAbsolutePath()
                                        .toString()
                        );

                if (mg$mcaHandle == 0) {
                    return;
                }

                buffer.clear();

                bytes =
                        McaNativeLoader.read(
                                mg$mcaHandle,
                                pos.x,
                                pos.z,
                                buffer,
                                buffer.capacity()
                        );
            }

            if (bytes > 0) {

                byte[] data =
                        new byte[bytes];

                buffer
                        .position(0)
                        .limit(bytes)
                        .get(data);

                cir.setReturnValue(
                        new DataInputStream(
                                new ByteArrayInputStream(
                                        data
                                )
                        )
                );
            }

        } catch (Throwable ignored) {
        }
    }

    @Inject(
            method = "close()V",
            at = @At("HEAD")
    )
    private void mg$nativeClose(
            CallbackInfo ci
    ) {

        if (mg$mcaHandle != 0) {

            McaNativeLoader.close(
                    mg$mcaHandle
            );

            mg$mcaHandle = 0;
        }
    }
}
