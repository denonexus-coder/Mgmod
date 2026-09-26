package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.ServerBottleneckProfiler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerBottleneckLevelMixin {

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void mgshaders_world_start(
            java.util.function.BooleanSupplier haveTime,
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.beginWorldTick();
    }

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void mgshaders_world_end(
            java.util.function.BooleanSupplier haveTime,
            CallbackInfo ci
    ) {
        ServerLevel level =
                (ServerLevel) (Object) this;

        String world =
                level.dimension()
                        .location()
                        .toString();

        ServerBottleneckProfiler.endWorldTick(world);
    }

    @Inject(
            method = "tickChunk",
            at = @At("HEAD")
    )
    private void mgshaders_chunk_start(
            LevelChunk chunk,
            int randomTickSpeed,
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.beginChunkTick();
    }

    @Inject(
            method = "tickChunk",
            at = @At("RETURN")
    )
    private void mgshaders_chunk_end(
            LevelChunk chunk,
            int randomTickSpeed,
            CallbackInfo ci
    ) {
        ServerLevel level =
                (ServerLevel) (Object) this;

        String key =
                level.dimension()
                        .location()
                        .toString()
                + ":"
                + chunk.getPos().x
                + ":"
                + chunk.getPos().z;

        ServerBottleneckProfiler.endChunkTick(key);
    }

    @Inject(
            method = "tickCustomSpawners",
            at = @At("HEAD")
    )
    private void mgshaders_spawner_start(
            boolean spawnEnemies,
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.beginSpawners();
    }

    @Inject(
            method = "tickCustomSpawners",
            at = @At("RETURN")
    )
    private void mgshaders_spawner_end(
            boolean spawnEnemies,
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.endSpawners();
    }

    @Inject(
            method = "runBlockEvents",
            at = @At("HEAD")
    )
    private void mgshaders_block_events_start(
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.beginBlockEvents();
    }

    @Inject(
            method = "runBlockEvents",
            at = @At("RETURN")
    )
    private void mgshaders_block_events_end(
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.endBlockEvents();
    }

    @Inject(
            method = "tickNonPassenger",
            at = @At("HEAD")
    )
    private void mgshaders_entity_start(
            net.minecraft.world.entity.Entity entity,
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.beginEntity();
    }

    @Inject(
            method = "tickNonPassenger",
            at = @At("RETURN")
    )
    private void mgshaders_entity_end(
            net.minecraft.world.entity.Entity entity,
            CallbackInfo ci
    ) {
        String type;

        try {
            type = entity.getType()
                    .toString();
        } catch (Throwable ignored) {
            type = entity.getClass()
                    .getName();
        }

        ServerBottleneckProfiler.endEntity(type);
    }
}
