package com.denonexus.mgshaders.client.astc;

import com.mojang.blaze3d.systems.GpuDevice;
import com.denonexus.mgshaders.client.mixin.AstcAtlasMixinAccess;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL21C;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.KHRTextureCompressionASTCLDR;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Direct ASTC atlas backend for Minecraft 1.21.11 OpenGL.
 *
 * Runtime source:
 *
 *     assets/mgshaders/astc/*.astc
 *
 * The resources are materialized once into:
 *
 *     <gameDir>/mg_astc_cache/gpu/*.astc
 *
 * The GPU upload then uses FileChannel.map() and
 * glCompressedTexImage2D().
 *
 * There is deliberately no PNG fallback.
 */
public final class AstcAtlasManager {

    private static final String MOD_ID = "mgshaders";

    private static final Path CACHE_ROOT =
            FabricLoader.getInstance()
                    .getGameDir()
                    .resolve("mg_astc_cache")
                    .resolve("gpu");

    private static final String[] ASTC_FILES = {
            "minecraft_textures_atlas_armor_trims.astc",
            "minecraft_textures_atlas_banner_patterns.astc",
            "minecraft_textures_atlas_beds.astc",
            "minecraft_textures_atlas_blocks.astc",
            "minecraft_textures_atlas_celestials.astc",
            "minecraft_textures_atlas_chest.astc",
            "minecraft_textures_atlas_decorated_pot.astc",
            "minecraft_textures_atlas_gui.astc",
            "minecraft_textures_atlas_items.astc",
            "minecraft_textures_atlas_map_decorations.astc",
            "minecraft_textures_atlas_paintings.astc",
            "minecraft_textures_atlas_particles.astc",
            "minecraft_textures_atlas_shield_patterns.astc",
            "minecraft_textures_atlas_shulker_boxes.astc",
            "minecraft_textures_atlas_signs.astc"
    };

    private static final Map<String, String> ATLAS_TO_FILE = Map.ofEntries(
            Map.entry("minecraft:textures/atlas/armor_trims.png",
                    "minecraft_textures_atlas_armor_trims.astc"),
            Map.entry("minecraft:textures/atlas/banner_patterns.png",
                    "minecraft_textures_atlas_banner_patterns.astc"),
            Map.entry("minecraft:textures/atlas/beds.png",
                    "minecraft_textures_atlas_beds.astc"),
            Map.entry("minecraft:textures/atlas/blocks.png",
                    "minecraft_textures_atlas_blocks.astc"),
            Map.entry("minecraft:textures/atlas/celestials.png",
                    "minecraft_textures_atlas_celestials.astc"),
            Map.entry("minecraft:textures/atlas/chest.png",
                    "minecraft_textures_atlas_chest.astc"),
            Map.entry("minecraft:textures/atlas/decorated_pot.png",
                    "minecraft_textures_atlas_decorated_pot.astc"),
            Map.entry("minecraft:textures/atlas/gui.png",
                    "minecraft_textures_atlas_gui.astc"),
            Map.entry("minecraft:textures/atlas/items.png",
                    "minecraft_textures_atlas_items.astc"),
            Map.entry("minecraft:textures/atlas/map_decorations.png",
                    "minecraft_textures_atlas_map_decorations.astc"),
            Map.entry("minecraft:textures/atlas/paintings.png",
                    "minecraft_textures_atlas_paintings.astc"),
            Map.entry("minecraft:textures/atlas/particles.png",
                    "minecraft_textures_atlas_particles.astc"),
            Map.entry("minecraft:textures/atlas/shield_patterns.png",
                    "minecraft_textures_atlas_shield_patterns.astc"),
            Map.entry("minecraft:textures/atlas/shulker_boxes.png",
                    "minecraft_textures_atlas_shulker_boxes.astc"),
            Map.entry("minecraft:textures/atlas/signs.png",
                    "minecraft_textures_atlas_signs.astc")
    );


    private static final Set<String> TARGET_ATLASES =
            Set.copyOf(ATLAS_TO_FILE.keySet());

    private AstcAtlasManager() {
    }

    public static boolean manages(TextureAtlas atlas) {
        return TARGET_ATLASES.contains(
                atlas.location().toString()
        );
    }

    /**
     * Materializes the ASTC files from the actual mod resource tree.
     *
     * This is intentionally independent from Android shared storage.
     */
    public static void prepareBundledAssets() {

        try {
            Files.createDirectories(CACHE_ROOT);

            ModContainer mod = FabricLoader.getInstance()
                    .getModContainer(MOD_ID)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "MGShaders mod container not found"
                            )
                    );

            for (String fileName : ASTC_FILES) {

                String resourcePath =
                        "assets/mgshaders/astc/" + fileName;

                Path source = mod.findPath(resourcePath)
                        .orElseThrow(() ->
                                new IOException(
                                        "Missing bundled ASTC resource: "
                                                + resourcePath
                                )
                        );

                Path target =
                        CACHE_ROOT.resolve(fileName);

                boolean copy = !Files.isRegularFile(target);

                if (!copy) {
                    long sourceSize = Files.size(source);
                    long targetSize = Files.size(target);

                    copy = sourceSize != targetSize;

                    if (!copy) {
                        copy = Files.mismatch(source, target) != -1;
                    }
                }

                if (copy) {
                    Files.copy(
                            source,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }

                validateAstc(target);

                System.out.println(
                        "[MGShaders][ASTC] bundled resource OK: "
                                + resourcePath
                                + " -> "
                                + target
                );
            }

            System.out.println(
                    "[MGShaders][ASTC] all 15 bundled ASTC resources ready"
            );

        } catch (Throwable t) {

            throw new IllegalStateException(
                    "[MGShaders][ASTC][FATAL] "
                            + "Bundled ASTC preparation failed",
                    t
            );
        }
    }

    public static boolean isAstcManaged(TextureAtlas atlas) {
        return manages(atlas);
    }

    public static void install(TextureAtlas atlas) {

        if (!manages(atlas)) {
            return;
        }

        RenderSystem.assertOnRenderThread();

        GpuTexture oldTexture = atlas.getTexture();

        /*
         * Resource reload can recreate the vanilla texture on the same
         * TextureAtlas instance. Only skip when the current texture is
         * already our ASTC GlTexture.
         */
        if (oldTexture instanceof AstcGlTexture) {
            return;
        }

        String atlasId =
                atlas.location().toString();

        String fileName =
                ATLAS_TO_FILE.get(atlasId);

        if (fileName == null) {
            throw new IllegalStateException(
                    "[MGShaders][ASTC][FATAL] No ASTC mapping for "
                            + atlasId
            );
        }

        Path astcFile =
                CACHE_ROOT.resolve(fileName);

        if (!Files.isRegularFile(astcFile)) {
            throw new IllegalStateException(
                    "[MGShaders][ASTC][FATAL] Missing runtime ASTC file: "
                            + astcFile
            );
        }

        AstcHeader header;

        try {
            header = readHeader(astcFile);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "[MGShaders][ASTC][FATAL] Invalid ASTC file: "
                            + astcFile,
                    e
            );
        }

        int vanillaWidth =
                oldTexture.getWidth(0);

        int vanillaHeight =
                oldTexture.getHeight(0);

        if (header.width != vanillaWidth ||
                header.height != vanillaHeight) {

            throw new IllegalStateException(
                    "[MGShaders][ASTC][FATAL] Atlas dimension mismatch: "
                            + atlasId
                            + " vanilla="
                            + vanillaWidth
                            + "x"
                            + vanillaHeight
                            + " ASTC="
                            + header.width
                            + "x"
                            + header.height
            );
        }

        int previousBinding =
                GL11C.glGetInteger(
                        GL11C.GL_TEXTURE_BINDING_2D
                );

        clearGlErrors();

        int newGlId =
                GL11C.glGenTextures();

        if (newGlId == 0) {
            throw new IllegalStateException(
                    "[MGShaders][ASTC][FATAL] glGenTextures returned 0 for "
                            + atlasId
            );
        }

        AstcGlTexture newTexture = null;
        GpuTextureView newTextureView = null;

        try {

            GL11C.glBindTexture(
                    GL11C.GL_TEXTURE_2D,
                    newGlId
            );

            /*
             * Only mip level 0 exists in the supplied ASTC files.
             */
            GL11C.glTexParameteri(
                    GL11C.GL_TEXTURE_2D,
                    GL12C.GL_TEXTURE_BASE_LEVEL,
                    0
            );

            GL11C.glTexParameteri(
                    GL11C.GL_TEXTURE_2D,
                    GL12C.GL_TEXTURE_MAX_LEVEL,
                    0
            );

            try (FileChannel channel =
                         FileChannel.open(
                                 astcFile,
                                 StandardOpenOption.READ
                         )) {

                ByteBuffer payload =
                        channel.map(
                                FileChannel.MapMode.READ_ONLY,
                                16L,
                                header.payloadSize
                        );

                payload.order(
                        ByteOrder.nativeOrder()
                );

                /*
                 * Minecraft's bundled LWJGL exposes this overload using
                 * a native pointer (long), not ByteBuffer.
                 *
                 * Ensure no unpack PBO is bound: with a PBO active,
                 * the long argument is interpreted as a byte offset.
                 */
                int previousUnpackBuffer =
                        GL11C.glGetInteger(
                                GL21C.GL_PIXEL_UNPACK_BUFFER_BINDING
                        );

                GL15C.glBindBuffer(
                        GL21C.GL_PIXEL_UNPACK_BUFFER,
                        0
                );

                try {
                    GL13C.glCompressedTexImage2D(
                            GL11C.GL_TEXTURE_2D,
                            0,
                            KHRTextureCompressionASTCLDR
                                    .GL_COMPRESSED_RGBA_ASTC_4x4_KHR,
                            header.width,
                            header.height,
                            0,
                            header.payloadSize,
                            MemoryUtil.memAddress(payload)
                    );
                } finally {
                    GL15C.glBindBuffer(
                            GL21C.GL_PIXEL_UNPACK_BUFFER,
                            previousUnpackBuffer
                    );
                }
            }

            int glError =
                    GL11C.glGetError();

            if (glError != GL11C.GL_NO_ERROR) {

                throw new IllegalStateException(
                        String.format(
                                "[MGShaders][ASTC][FATAL] "
                                        + "ASTC GPU upload failed for %s: GL 0x%04X",
                                atlasId,
                                glError
                        )
                );
            }

            /*
             * Build a real Minecraft 1.21.11 GlTexture object around
             * the ASTC GL texture name.
             *
             * Metadata remains the vanilla texture format because the
             * Minecraft resource system represents the atlas as RGBA.
             */
            newTexture =
                    new AstcGlTexture(
                            oldTexture.usage(),
                            oldTexture.getLabel(),
                            oldTexture.getFormat(),
                            header.width,
                            header.height,
                            oldTexture.getDepthOrLayers(),
                            1,
                            newGlId
                    );

            GpuDevice device =
                    RenderSystem.getDevice();

            newTextureView =
                    device.createTextureView(
                            newTexture,
                            0,
                            1
                    );

            /*
             * Replace Minecraft's AbstractTexture GPU references and
             * TextureAtlas mip view state atomically before releasing
             * the old resources.
             */
            AbstractTextureAccess textureAccess =
                    (AbstractTextureAccess)
                            (Object) atlas;

            GpuTextureView oldTextureView =
                    atlas.getTextureView();

            GpuTextureView[] oldMipViews =
                    ((AstcAtlasMixinAccess)
                            (Object) atlas)
                            .mg$getMipViews();

            textureAccess.mg$setTexture(
                    newTexture
            );

            textureAccess.mg$setTextureView(
                    newTextureView
            );

            AstcAtlasMixinAccess atlasAccess =
                    (AstcAtlasMixinAccess)
                            (Object) atlas;

            atlasAccess.mg$setMipLevelCount(1);
            atlasAccess.mg$setMaxMipLevel(0);
            atlasAccess.mg$setMipViews(
                    new GpuTextureView[]{
                            newTextureView
                    }
            );

            /*
             * The old texture/view objects are no longer referenced by
             * TextureAtlas. Close them only after the replacement is
             * installed.
             */
            closeOldViews(
                    oldTextureView,
                    oldMipViews,
                    newTextureView
            );

            if (oldTexture != newTexture) {
                try {
                    oldTexture.close();
                } catch (Throwable closeFailure) {
                    System.err.println(
                            "[MGShaders][ASTC] old atlas texture close warning: "
                                    + closeFailure
                    );
                }
            }

            /*
             * Keep the GL state coherent with the actual GL texture name.
             */
            if (previousBinding == oldTextureGlId(oldTexture)) {
                GL11C.glBindTexture(
                        GL11C.GL_TEXTURE_2D,
                        newGlId
                );
            } else {
                GL11C.glBindTexture(
                        GL11C.GL_TEXTURE_2D,
                        previousBinding
                );
            }

            System.out.println(
                    "[MGShaders][ASTC] GPU ASTC ACTIVE: "
                            + atlasId
                            + " "
                            + header.width
                            + "x"
                            + header.height
                            + " payload="
                            + header.payloadSize
                            + " glId="
                            + newGlId
            );

        } catch (Throwable failure) {

            /*
             * Do NOT destroy the vanilla texture on failure.
             * The failure is fatal instead of silently falling back.
             */
            GL11C.glBindTexture(
                    GL11C.GL_TEXTURE_2D,
                    previousBinding
            );

            if (newTextureView != null) {
                try {
                    newTextureView.close();
                } catch (Throwable ignored) {
                }
            }

            if (newTexture != null) {
                try {
                    newTexture.close();
                } catch (Throwable ignored) {
                }
            } else if (newGlId != 0) {
                GL11C.glDeleteTextures(newGlId);
            }

            if (failure instanceof RuntimeException runtime) {
                throw runtime;
            }

            throw new IllegalStateException(
                    "[MGShaders][ASTC][FATAL] "
                            + "ASTC installation failed for "
                            + atlasId,
                    failure
            );
        }
    }

    private static int oldTextureGlId(
            GpuTexture texture
    ) {

        if (texture instanceof com.mojang.blaze3d.opengl.GlTexture gl) {
            return gl.glId();
        }

        return 0;
    }

    private static void closeOldViews(
            GpuTextureView currentView,
            GpuTextureView[] mipViews,
            GpuTextureView replacement
    ) {

        Set<GpuTextureView> closed =
                java.util.Collections.newSetFromMap(
                        new IdentityHashMap<>()
                );

        if (currentView != null &&
                currentView != replacement) {

            try {
                currentView.close();
            } catch (Throwable ignored) {
            }

            closed.add(currentView);
        }

        if (mipViews != null) {

            for (GpuTextureView view : mipViews) {

                if (view == null ||
                        view == replacement ||
                        closed.contains(view)) {
                    continue;
                }

                try {
                    view.close();
                } catch (Throwable ignored) {
                }

                closed.add(view);
            }
        }
    }

    private static void clearGlErrors() {

        while (GL11C.glGetError() != GL11C.GL_NO_ERROR) {
        }
    }

    private static void validateAstc(
            Path file
    ) throws IOException {
        readHeader(file);
    }

    private static AstcHeader readHeader(
            Path file
    ) throws IOException {

        long fileSize =
                Files.size(file);

        if (fileSize < 16) {
            throw new IOException(
                    "ASTC file smaller than 16 bytes: "
                            + file
            );
        }

        ByteBuffer header =
                ByteBuffer.allocate(16)
                        .order(
                                ByteOrder.LITTLE_ENDIAN
                        );

        try (FileChannel channel =
                     FileChannel.open(
                             file,
                             StandardOpenOption.READ
                     )) {

            while (header.hasRemaining()) {

                int read =
                        channel.read(header);

                if (read < 0) {
                    throw new IOException(
                            "Unexpected EOF in ASTC header: "
                                    + file
                    );
                }
            }
        }

        header.flip();

        int magic =
                (header.get(0) & 0xFF)
                        | ((header.get(1) & 0xFF) << 8)
                        | ((header.get(2) & 0xFF) << 16)
                        | ((header.get(3) & 0xFF) << 24);

        if (magic != 0x5CA1AB13) {
            throw new IOException(
                    String.format(
                            "Invalid ASTC magic 0x%08X: %s",
                            magic,
                            file
                    )
            );
        }

        int blockX =
                header.get(4) & 0xFF;

        int blockY =
                header.get(5) & 0xFF;

        int blockZ =
                header.get(6) & 0xFF;

        if (blockX != 4 ||
                blockY != 4 ||
                blockZ != 1) {

            throw new IOException(
                    "Only ASTC 4x4x1 is supported: "
                            + blockX
                            + "x"
                            + blockY
                            + "x"
                            + blockZ
            );
        }

        int width =
                read24(header, 7);

        int height =
                read24(header, 10);

        int depth =
                read24(header, 13);

        if (width <= 0 ||
                height <= 0 ||
                depth != 1) {

            throw new IOException(
                    "Invalid ASTC dimensions: "
                            + width
                            + "x"
                            + height
                            + "x"
                            + depth
            );
        }

        long blocksX =
                (width + 3L) / 4L;

        long blocksY =
                (height + 3L) / 4L;

        long expectedPayload =
                blocksX
                        * blocksY
                        * 16L;

        long payload =
                fileSize - 16L;

        if (payload != expectedPayload) {

            throw new IOException(
                    "ASTC payload mismatch: file="
                            + payload
                            + " expected="
                            + expectedPayload
            );
        }

        if (payload > Integer.MAX_VALUE) {
            throw new IOException(
                    "ASTC payload exceeds Java ByteBuffer limit"
            );
        }

        return new AstcHeader(
                width,
                height,
                depth,
                (int) payload
        );
    }

    private static int read24(
            ByteBuffer buffer,
            int offset
    ) {

        return (buffer.get(offset) & 0xFF)
                | ((buffer.get(offset + 1) & 0xFF) << 8)
                | ((buffer.get(offset + 2) & 0xFF) << 16);
    }

    private record AstcHeader(
            int width,
            int height,
            int depth,
            int payloadSize
    ) {
    }
}
