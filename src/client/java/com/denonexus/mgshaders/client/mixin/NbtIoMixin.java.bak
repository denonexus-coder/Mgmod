package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.config.RegionCacheConfig;
import com.denonexus.mgshaders.ram.RegionCacheManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.*;

@Mixin(NbtIo.class)
public class NbtIoMixin {

    private static long totalReadTimeDisabledNs = 0;
    private static long totalReadTimeEnabledNs = 0;
    private static int readCountDisabled = 0;
    private static int readCountEnabled = 0;

    @Inject(method = "readCompressed(Ljava/io/File;)Lnet/minecraft/nbt/NbtCompound;", at = @At("HEAD"), cancellable = true)
    private static void onReadCompressed(File file, CallbackInfoReturnable<NbtCompound> cir) {
        RegionCacheConfig cfg = RegionCacheConfig.get();
        long start = System.nanoTime();

        if (!cfg.regionCacheEnabled) {
            long elapsed = System.nanoTime() - start;
            totalReadTimeDisabledNs += elapsed;
            readCountDisabled++;
            logStats("DESATIVADO (Disco)", readCountDisabled, totalReadTimeDisabledNs);
            return;
        }

        try {
            // Extrai coordenadas do nome do arquivo ou fluxo
            byte[] data = RegionCacheManager.getOrFetchChunkData(file, 0, 0);
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(data)));
            NbtCompound compound = NbtIo.readCompressed(dis);

            long elapsed = System.nanoTime() - start;
            totalReadTimeEnabledNs += elapsed;
            readCountEnabled++;
            logStats("ATIVADO (LZ4 RAM)", readCountEnabled, totalReadTimeEnabledNs);

            cir.setReturnValue(compound);
        } catch (Exception ignored) {
            // Se falhar, deixa o Minecraft seguir o caminho padrao sem crashar
        }
    }

    private static void logStats(String mode, int count, long totalNs) {
        if (count % 50 != 0) return;
        double avgMs = (totalNs / 1_000_000.0) / count;
        try (PrintWriter writer = new PrintWriter(new FileWriter("logs/mgshaders_regioncache.log", true))) {
            writer.printf("[%s] Chunks lidos: %d | Tempo Medio: %.4f ms%n", mode, count, avgMs);
        } catch (Exception ignored) {}
    }
}
