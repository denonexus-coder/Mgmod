package com.denonexus.mgshaders.client.astc;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.systems.RenderSystem;
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
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class AstcAtlasManager {

    private static final Path ASTC_ROOT =
            Path.of("/storage/emulated/0/minecraft_astc/astc_compressed");

    private static final Set<String> TARGET_ATLASES = Set.of(
            "minecraft:textures/atlas/armor_trims.png",
            "minecraft:textures/atlas/banner_patterns.png",
            "minecraft:textures/atlas/beds.png",
            "minecraft:textures/atlas/blocks.png",
            "minecraft:textures/atlas/celestials.png",
            "minecraft:textures/atlas/chest.png",
            "minecraft:textures/atlas/decorated_pot.png",
            "minecraft:textures/atlas/gui.png",
            "minecraft:textures/atlas/items.png",
            "minecraft:textures/atlas/map_decorations.png",
            "minecraft:textures/atlas/paintings.png",
            "minecraft:textures/atlas/particles.png",
            "minecraft:textures/atlas/shield_patterns.png",
            "minecraft:textures/atlas/shulker_boxes.png",
            "minecraft:textures/atlas/signs.png"
    );

    private static final Set<TextureAtlas> ACTIVE =
            Collections.newSetFromMap(new WeakHashMap<>());

    private static final Set<TextureAtlas> DISABLED =
            Collections.newSetFromMap(new WeakHashMap<>());

    private static final int ASTC_MAGIC = 0x5CA1AB13;

    private AstcAtlasManager() {
    }

    public static boolean manages(TextureAtlas atlas) {
        return TARGET_ATLASES.contains(atlas.location().toString());
    }

    public static boolean isAstcActive(TextureAtlas atlas) {
        return ACTIVE.contains(atlas);
    }

    public static boolean isAstcDisabled(TextureAtlas atlas) {
        return DISABLED.contains(atlas);
    }

    public static boolean isAstcManaged(TextureAtlas atlas) {
        return manages(atlas) &&
                (ACTIVE.contains(atlas) || DISABLED.contains(atlas));
    }

    public static void install(TextureAtlas atlas) {
        if (!manages(atlas)) {
            return;
        }

        if (ACTIVE.contains(atlas) || DISABLED.contains(atlas)) {
            return;
        }

        RenderSystem.assertOnRenderThread();

        final String atlasId = atlas.location().toString();
        final Path astcFile = astcPath(atlas.location());

        System.out.println(
                "[MGShaders][ASTC] Installing direct GPU atlas: "
                        + atlasId
                        + " -> "
                        + astcFile
        );

        GpuTexture gpuTexture = atlas.getTexture();

        if (!(gpuTexture instanceof GlTexture glTexture)) {
            disable(
                    atlas,
                    null,
                    "Minecraft is not using the OpenGL GlTexture backend: "
                            + gpuTexture.getClass().getName()
            );
            return;
        }

        GlTextureAccessMixin access = (GlTextureAccessMixin) (Object) glTexture;

        int oldId = access.mg$getId();

        try {
            if (!Files.isRegularFile(astcFile)) {
                throw new IOException(
                        "ASTC file does not exist: " + astcFile
                );
            }

            AstcHeader header = readHeader(astcFile);

            int atlasWidth = gpuTexture.getWidth(0);
            int atlasHeight = gpuTexture.getHeight(0);

            if (header.width != atlasWidth ||
                    header.height != atlasHeight) {

                throw new IOException(
                        "ATLAS DIMENSION MISMATCH: "
                                + atlasId
                                + " vanilla="
                                + atlasWidth
                                + "x"
                                + atlasHeight
                                + " ASTC="
                                + header.width
                                + "x"
                                + header.height
                );
            }

            if (header.blockX != 4 ||
                    header.blockY != 4 ||
                    header.blockZ != 1) {

                throw new IOException(
                        "Unsupported ASTC block "
                                + header.blockX
                                + "x"
                                + header.blockY
                                + "x"
                                + header.blockZ
                );
            }

            if (header.depth != 1) {
                throw new IOException(
                        "Unsupported ASTC depth: " + header.depth
                );
            }

            if (header.payloadSize != header.expectedPayloadSize) {
                throw new IOException(
                        "ASTC payload mismatch: file="
                                + header.payloadSize
                                + " expected="
                                + header.expectedPayloadSize
                );
            }

            int previousBinding =
                    GL11C.glGetInteger(GL11C.GL_TEXTURE_BINDING_2D);

            while (GL11C.glGetError() != GL11C.GL_NO_ERROR) {
                // Clear stale GL errors before the critical upload.
            }

            int newId = GL11C.glGenTextures();

            if (newId == 0) {
                throw new IOException(
                        "glGenTextures returned 0"
                );
            }

            try {
                GL11C.glBindTexture(
                        GL11C.GL_TEXTURE_2D,
                        newId
                );

                /*
                 * The ASTC files contain only mip level 0.
                 *
                 * Restrict the texture object to level 0 so the
                 * existing Minecraft sampler cannot request an
                 * undefined mip level.
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
                                    16,
                                    header.payloadSize
                            );

                    payload.order(ByteOrder.nativeOrder());

                    GL13C.glCompressedTexImage2D(
                            GL11C.GL_TEXTURE_2D,
                            0,
                            KHRTextureCompressionASTCLDR
                                    .GL_COMPRESSED_RGBA_ASTC_4x4_KHR,
                            header.width,
                            header.height,
                            0,
                            header.payloadSize,
                            payload
                    );
                }

                int error = GL11C.glGetError();

                if (error != GL11C.GL_NO_ERROR) {
                    throw new IOException(
                            String.format(
                                    "glCompressedTexImage2D failed: 0x%04X",
                                    error
                            )
                    );
                }

                /*
                 * Upload succeeded.
                 *
                 * If Minecraft happened to have the old atlas bound,
                 * bind the new texture before deleting the old name.
                 */
                if (previousBinding == oldId) {
                    GL11C.glBindTexture(
                            GL11C.GL_TEXTURE_2D,
                            newId
                    );
                } else {
                    GL11C.glBindTexture(
                            GL11C.GL_TEXTURE_2D,
                            previousBinding
                    );
                }

                /*
                 * The vanilla PNG-backed GL texture is now destroyed.
                 * There is deliberately NO PNG fallback.
                 */
                if (oldId != 0) {
                    GL11C.glDeleteTextures(oldId);
                }

                access.mg$setId(newId);

                ACTIVE.add(atlas);

                System.out.println(
                        "[MGShaders][ASTC] GPU upload SUCCESS: "
                                + atlasId
                                + " "
                                + header.width
                                + "x"
                                + header.height
                                + " payload="
                                + header.payloadSize
                                + " bytes"
                                + " glId="
                                + newId
                );

            } catch (Throwable uploadFailure) {

                GL11C.glBindTexture(
                        GL11C.GL_TEXTURE_2D,
                        previousBinding
                );

                GL11C.glDeleteTextures(newId);

                throw uploadFailure;
            }

        } catch (Throwable failure) {

            disable(
                    atlas,
                    oldId,
                    failure.getMessage() == null
                            ? failure.toString()
                            : failure.getMessage()
            );
        }
    }

    private static void disable(
            TextureAtlas atlas,
            Integer oldId,
            String reason
    ) {
        try {
            GpuTexture gpuTexture = atlas.getTexture();

            if (gpuTexture instanceof GlTexture glTexture) {
                GlTextureAccessMixin access =
                        (GlTextureAccessMixin) (Object) glTexture;

                int id = oldId != null
                        ? oldId
                        : access.mg$getId();

                if (id != 0) {
                    GL11C.glDeleteTextures(id);
                }

                /*
                 * Deliberately make this atlas unusable.
                 *
                 * This is NOT a fallback.
                 * Rendering with this atlas therefore produces no
                 * valid texture instead of silently returning to PNG.
                 */
                access.mg$setId(0);
            }
        } catch (Throwable t) {
            System.err.println(
                    "[MGShaders][ASTC] Failed to disable atlas "
                            + atlas.location()
                            + ": "
                            + t
            );
        }

        DISABLED.add(atlas);

        System.err.println(
                "[MGShaders][ASTC][FATAL] Atlas disabled: "
                        + atlas.location()
                        + " | "
                        + reason
        );
    }

    private static Path astcPath(Identifier id) {
        String name = id.toString();

        if (!name.endsWith(".png")) {
            throw new IllegalArgumentException(
                    "Unexpected atlas identifier: " + name
            );
        }

        String stem =
                name.substring(0, name.length() - 4)
                        .replace(':', '_')
                        .replace('/', '_');

        return ASTC_ROOT.resolve(stem + ".astc");
    }

    private static AstcHeader readHeader(Path file)
            throws IOException {

        long fileSize = Files.size(file);

        if (fileSize < 16) {
            throw new IOException(
                    "ASTC file smaller than 16-byte header: " + file
            );
        }

        ByteBuffer header =
                ByteBuffer.allocate(16)
                        .order(ByteOrder.LITTLE_ENDIAN);

        try (FileChannel channel =
                     FileChannel.open(
                             file,
                             StandardOpenOption.READ
                     )) {

            while (header.hasRemaining()) {
                int n = channel.read(header);

                if (n < 0) {
                    throw new IOException(
                            "Unexpected EOF reading ASTC header"
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

        if (magic != ASTC_MAGIC) {
            throw new IOException(
                    String.format(
                            "Invalid ASTC magic: 0x%08X",
                            magic
                    )
            );
        }

        int blockX = header.get(4) & 0xFF;
        int blockY = header.get(5) & 0xFF;
        int blockZ = header.get(6) & 0xFF;

        int width =
                read24(header, 7);

        int height =
                read24(header, 10);

        int depth =
                read24(header, 13);

        if (width <= 0 || height <= 0 || depth <= 0) {
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
                (width + blockX - 1L) / blockX;

        long blocksY =
                (height + blockY - 1L) / blockY;

        long blocksZ =
                (depth + blockZ - 1L) / blockZ;

        long expectedPayload =
                blocksX
                        * blocksY
                        * blocksZ
                        * 16L;

        long payloadSize =
                fileSize - 16L;

        if (expectedPayload != payloadSize) {
            throw new IOException(
                    "Invalid ASTC payload: "
                            + payloadSize
                            + " != "
                            + expectedPayload
            );
        }

        if (payloadSize > Integer.MAX_VALUE) {
            throw new IOException(
                    "ASTC payload too large for GL upload: "
                            + payloadSize
            );
        }

        return new AstcHeader(
                width,
                height,
                depth,
                blockX,
                blockY,
                blockZ,
                (int) payloadSize,
                (int) expectedPayload
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
            int blockX,
            int blockY,
            int blockZ,
            int payloadSize,
            int expectedPayloadSize
    ) {
    }
}
