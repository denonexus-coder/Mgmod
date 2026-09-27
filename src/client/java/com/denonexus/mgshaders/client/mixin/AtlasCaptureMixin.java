package com.denonexus.mgshaders.client.mixin;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

@Mixin(AtlasManager.class)
public abstract class AtlasCaptureMixin {

    private static final Path ROOT =
            FabricLoader.getInstance()
                    .getGameDir()
                    .resolve("mg_atlas_cache");

    @Inject(
            method = "reload",
            at = @At("RETURN")
    )
    private void mgshaders_captureAtlases(
            PreparableReloadListener.SharedState sharedState,
            Executor preparationExecutor,
            PreparableReloadListener.PreparationBarrier preparationBarrier,
            Executor reloadExecutor,
            CallbackInfoReturnable<CompletableFuture<?>> cir
    ) {
        CompletableFuture<?> reloadFuture = cir.getReturnValue();

        reloadFuture.thenRun(() -> {
            try {
                capture((AtlasManager) (Object) this);
            } catch (Throwable t) {
                System.err.println(
                        "[MGShaders] Atlas capture failed: " + t
                );
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void capture(AtlasManager manager) throws IOException {

        Files.createDirectories(ROOT);

        Path atlasDir = ROOT.resolve("atlases");
        Files.createDirectories(atlasDir);

        Path index = ROOT.resolve("atlas-index.txt");

        StringBuilder indexText = new StringBuilder();

        indexText.append("MGShaders Atlas Cache\n");
        indexText.append("=====================\n\n");

        manager.forEach(
                (BiConsumer) (idObject, entryObject) -> {

                    Identifier id = (Identifier) idObject;

                    try {
                        TextureAtlas atlas =
                                manager.getAtlasOrThrow(id);

                        String namespace =
                                id.getNamespace();

                        String path =
                                id.getPath()
                                        .replace('/', '_')
                                        .replace('\\', '_');

                        Path namespaceDir =
                                atlasDir.resolve(namespace);

                        Files.createDirectories(namespaceDir);

                        /*
                         * TextureAtlas.dumpContents() recebe o
                         * DIRETÓRIO de saída, não o caminho final
                         * do PNG.
                         *
                         * O método gera o PNG do atlas montado e
                         * também o arquivo de nomes dos sprites.
                         */
                        atlas.dumpContents(
                                id,
                                namespaceDir
                        );

                        indexText.append(
                                id
                        ).append('\n');

                        System.out.println(
                                "[MGShaders] Atlas captured: "
                                        + id
                                        + " -> "
                                        + namespaceDir
                        );

                    } catch (Throwable t) {

                        System.err.println(
                                "[MGShaders] Failed atlas "
                                        + id
                                        + ": "
                                        + t
                        );
                    }
                }
        );

        Files.writeString(
                index,
                indexText.toString()
        );

        System.out.println(
                "[MGShaders] Atlas capture complete: "
                        + ROOT
        );
    }
}
