package com.denonexus.mgshaders.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import net.minecraft.client.renderer.chunk.SectionBuffers;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Núcleo do pipeline de multi-draw.
 *
 * Ciclo de vida por frame:
 *   GameRenderer.render HEAD    → beginFrame()
 *   prepareChunkRenders RETURN  → capture(ChunkSectionsToRender)
 *   RenderSection.upload RETURN → onSectionUploaded(section, mesh)
 *   ChunkSectionsToRender.renderGroup HEAD → beforeRenderGroup(group)
 *   ChunkSectionsToRender.renderGroup TAIL → afterRenderGroup(group)
 *   LevelRenderer.endFrame HEAD → endFrame()
 *   GameRenderer.render TAIL    → finishFrame()
 *
 * A chamada final glMultiDrawElementsBaseVertexEXT ocorre no backend
 * MobileGlues/native bridge através de MGNativeDrawer.
 *
 * Imports confirmados pelo mapping Mojang 1.21.11:
 *   VertexFormat       → com.mojang.blaze3d.vertex.VertexFormat
 *   GpuBuffer          → com.mojang.blaze3d.buffers.GpuBuffer
 *   SectionBuffers     → net.minecraft.client.renderer.chunk.SectionBuffers
 *   CompiledSectionMesh → net.minecraft.client.renderer.chunk.CompiledSectionMesh
 *   getVertexBuffer()  → SectionBuffers método público (mapping line 21)
 *   getIndexBuffer()   → SectionBuffers método público (mapping line 25)
 *   getIndexCount()    → SectionBuffers método público (mapping line 33)
 *   getIndexType()     → SectionBuffers método público (mapping line 37)
 *   getBuffers(layer)  → CompiledSectionMesh método público (mapping line 76)
 */
public final class MGChunkBatch {

    // Dimensões de uma região de agrupamento (em seções de chunk = 16 blocos cada)
    public static final int REGION_X = 8;
    public static final int REGION_Y = 4;
    public static final int REGION_Z = 8;

    /** Registro permanente de seções com mesh carregado na GPU, indexado por sectionNode */
    private static final Map<Long, SectionRecord> UPLOADED_MAP = new ConcurrentHashMap<>(1024);

    /** Contador de frames */
    private static final AtomicLong FRAME_COUNTER = new AtomicLong(0);

    /** Frame atual – atualizado em beginFrame() */
    private static volatile long currentFrame = 0;

    /** ChunkSectionsToRender do frame atual, capturado de prepareChunkRenders */
    private static volatile ChunkSectionsToRender pendingSections = null;

    /** Câmera do frame atual */
    private static double cameraX, cameraY, cameraZ;

    private MGChunkBatch() {
    }

    // ─── Ciclo de frame ───────────────────────────────────────────────────────

    /** Chamado em GameRenderer.render HEAD – inicia novo frame. */
    public static void beginFrame() {
        currentFrame = FRAME_COUNTER.incrementAndGet();
        pendingSections = null;
    }

    /**
     * Chamado em prepareChunkRenders RETURN.
     * Armazena o objeto ChunkSectionsToRender para consulta posterior.
     */
    public static void capture(
            ChunkSectionsToRender sections,
            double camX,
            double camY,
            double camZ
    ) {
        if (sections == null) return;
        pendingSections = sections;
        cameraX = camX;
        cameraY = camY;
        cameraZ = camZ;
    }

    /** Chamado em LevelRenderer.endFrame HEAD. */
    public static void endFrame() {
        // sem-op: o UPLOADED_MAP é persistente entre frames
    }

    /** Chamado em GameRenderer.render TAIL – finaliza frame. */
    public static void finishFrame() {
        // ponto de extensão para métricas futuras
    }

    /** Chamado em LevelRenderer.clearVisibleSections e allChanged. */
    public static void invalidateAll() {
        UPLOADED_MAP.clear();
        pendingSections = null;
    }

    // ─── Upload de seções ─────────────────────────────────────────────────────

    /**
     * Chamado em RenderSection.upload RETURN.
     * Registra a seção recém-carregada na GPU no índice permanente.
     *
     * Mapping 1.21.11:
     *   CompletableFuture upload(Map, CompiledSectionMesh) → hts$a.a
     *   getSectionNode() → método público em RenderSection
     */
    public static void onSectionUploaded(
            SectionRenderDispatcher.RenderSection section,
            CompiledSectionMesh mesh
    ) {
        if (section == null || mesh == null) return;

        long node = section.getSectionNode();
        long frame = currentFrame;

        UPLOADED_MAP.put(node, new SectionRecord(section, mesh, node, frame));
    }

    // ─── Ciclo de renderGroup ─────────────────────────────────────────────────

    /** Chamado em ChunkSectionsToRender.renderGroup HEAD. */
    public static void beforeRenderGroup(ChunkSectionLayerGroup group) {
        // Extensão: submeter batch nativo para as seções do grupo antes do draw vanilla
    }

    /** Chamado em ChunkSectionsToRender.renderGroup TAIL. */
    public static void afterRenderGroup(ChunkSectionLayerGroup group) {
        // Extensão: cleanup após o draw vanilla do grupo
    }

    // ─── Lifecycle de mundo ───────────────────────────────────────────────────

    /**
     * Chamado em ClientLevel.unload(LevelChunk).
     * Remove do índice todas as seções pertencentes ao chunk descarregado
     * ANTES de o vanilla liberar os GpuBuffers, evitando uso pós-free.
     */
    public static void onChunkUnload(ChunkPos pos) {
        if (pos == null) return;
        UPLOADED_MAP.entrySet().removeIf(entry -> {
            SectionKey key = SectionKey.fromNode(entry.getKey());
            return key != null && key.chunkX() == pos.x && key.chunkZ() == pos.z;
        });
    }

    // ─── Snapshots para o native bridge ──────────────────────────────────────

    /**
     * Retorna snapshot imutável de todas as seções com mesh na GPU.
     */
    public static List<SectionRecord> snapshot() {
        return List.copyOf(UPLOADED_MAP.values());
    }

    /**
     * Retorna as seções agrupadas por região (REGION_X × REGION_Y × REGION_Z seções).
     * Cada região produz um único DrawBatch para o native bridge.
     */
    public static Map<RegionKey, List<SectionRecord>> snapshotByRegion() {
        Map<RegionKey, List<SectionRecord>> map = new HashMap<>();
        for (SectionRecord rec : UPLOADED_MAP.values()) {
            SectionKey sk = SectionKey.fromNode(rec.sectionNode());
            if (sk == null) continue;
            RegionKey rk = RegionKey.of(sk);
            map.computeIfAbsent(rk, k -> new ArrayList<>()).add(rec);
        }
        return map;
    }

    /**
     * Constrói um DrawBatch por layer para a lista de seções fornecida.
     * Retorna null se nenhuma seção tiver geometria para o layer.
     *
     * Os getters de SectionBuffers são chamados diretamente pois são
     * métodos públicos confirmados pelo mapping 1.21.11 (lines 21,25,33,37).
     */
    public static DrawBatch buildBatch(
            List<SectionRecord> sections,
            ChunkSectionLayer layer
    ) {
        List<GpuBuffer> vertexBuffers = new ArrayList<>();
        List<GpuBuffer> indexBuffers = new ArrayList<>();
        List<Integer> indexCounts = new ArrayList<>();
        List<VertexFormat.IndexType> indexTypes = new ArrayList<>();

        for (SectionRecord rec : sections) {
            // getBuffers() é método público em CompiledSectionMesh (mapping line 76)
            SectionBuffers buf = rec.mesh().getBuffers(layer);
            if (buf == null) continue;

            GpuBuffer vb = buf.getVertexBuffer();
            GpuBuffer ib = buf.getIndexBuffer();
            int count = buf.getIndexCount();
            if (vb == null || ib == null || count <= 0) continue;

            vertexBuffers.add(vb);
            indexBuffers.add(ib);
            indexCounts.add(count);
            indexTypes.add(buf.getIndexType());
        }

        if (vertexBuffers.isEmpty()) return null;

        return new DrawBatch(
                layer,
                vertexBuffers.toArray(new GpuBuffer[0]),
                indexBuffers.toArray(new GpuBuffer[0]),
                indexCounts.stream().mapToInt(Integer::intValue).toArray(),
                indexTypes.toArray(new VertexFormat.IndexType[0])
        );
    }

    // ─── Tipos de dados ───────────────────────────────────────────────────────

    /**
     * Registro de uma seção com mesh na GPU.
     * Acesso aos GpuBuffers via métodos públicos de CompiledSectionMesh e SectionBuffers.
     */
    public record SectionRecord(
            SectionRenderDispatcher.RenderSection section,
            CompiledSectionMesh mesh,
            long sectionNode,
            long uploadFrame
    ) {
        /** SectionBuffers para o layer informado, ou null se vazio/não compilado. */
        public SectionBuffers buffers(ChunkSectionLayer layer) {
            if (mesh == null) return null;
            return mesh.getBuffers(layer);
        }

        /** Vertex GpuBuffer para o layer, ou null. */
        public GpuBuffer vertexBuffer(ChunkSectionLayer layer) {
            SectionBuffers b = buffers(layer);
            return b != null ? b.getVertexBuffer() : null;
        }

        /** Index GpuBuffer para o layer, ou null. */
        public GpuBuffer indexBuffer(ChunkSectionLayer layer) {
            SectionBuffers b = buffers(layer);
            return b != null ? b.getIndexBuffer() : null;
        }

        /** Número de índices para o layer, ou 0. */
        public int indexCount(ChunkSectionLayer layer) {
            SectionBuffers b = buffers(layer);
            return b != null ? b.getIndexCount() : 0;
        }

        /** IndexType (GL_UNSIGNED_SHORT ou GL_UNSIGNED_INT) para o layer, ou null. */
        public VertexFormat.IndexType indexType(ChunkSectionLayer layer) {
            SectionBuffers b = buffers(layer);
            return b != null ? b.getIndexType() : null;
        }

        /**
         * Verifica se este record ainda é válido (seção não foi descartada).
         * Usa apenas sectionNode: se a seção foi descartada, o UPLOADED_MAP
         * terá removido este record via onChunkUnload ou invalidateAll.
         */
        public boolean isValid() {
            return section != null && mesh != null;
        }
    }

    /**
     * Batch de draws para um layer em uma região.
     * Enviado ao native bridge como uma única chamada multi-draw.
     */
    public record DrawBatch(
            ChunkSectionLayer layer,
            GpuBuffer[] vertexBuffers,
            GpuBuffer[] indexBuffers,
            int[] indexCounts,
            VertexFormat.IndexType[] indexTypes
    ) {
        public int drawCount() {
            return vertexBuffers.length;
        }
    }

    /**
     * Chave de região: agrupa seções em blocos REGION_X × REGION_Y × REGION_Z.
     */
    public record RegionKey(int rx, int ry, int rz) {
        public static RegionKey of(SectionKey sk) {
            return new RegionKey(
                    Math.floorDiv(sk.chunkX(), REGION_X),
                    Math.floorDiv(sk.sectionY(), REGION_Y),
                    Math.floorDiv(sk.chunkZ(), REGION_Z)
            );
        }
    }

    /**
     * Decodifica um sectionNode em coordenadas de seção.
     *
     * SectionPos.asLong() format (Mojang 1.21.11):
     *   x: bits 42..63 (22 bits, signed)
     *   z: bits 20..41 (22 bits, signed)
     *   y: bits  0..19 (20 bits, signed)
     */
    public record SectionKey(int chunkX, int sectionY, int chunkZ) {
        public static SectionKey fromNode(long node) {
            try {
                int x = (int) (node >> 42);
                int z = (int) ((node >> 20) & 0x3FFFFFL);
                int y = (int) (node << 44 >> 44);
                return new SectionKey(x, y, z);
            } catch (Throwable t) {
                return null;
            }
        }
    }
}
