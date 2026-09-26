import sys

path = "src/main/cpp/native_chunk.cpp"

patch = '''
// =============================================================================
// REGION CACHE — prefetch preditivo por regiao (32x32 chunks = 512x512 blocos)
//
// Diferenca para o cache por-chunk acima (mg_cache_get/mg_cache_prefetch):
// aquele e reativo (busca sob demanda, chunk a chunk). Este e preditivo:
// o Java decide, com base na direcao/velocidade do jogador, QUANDO carregar
// uma regiao inteira ANTES do jogador chegar la (ver RegionCacheManager.java).
//
// Guarda os bytes ja descomprimidos (raw NBT), prontos para uso instantaneo.
// Uma regiao cheia (1024 chunks reais raramente todos presentes) fica em
// poucos MB — nao ha necessidade de comprimir de novo em RAM.
// =============================================================================

struct RegionChunkData {
    int32_t cx, cz;
    uint32_t len;
    uint8_t* data;
};

struct RegionEntry {
    int32_t rx, rz;
    RegionChunkData* chunks; // array alocado sob demanda
    uint32_t count;
    uint32_t capacity;
    RegionEntry* next;
};

#define REGION_BUCKETS 256
static RegionEntry* g_region_buckets[REGION_BUCKETS] = {0};

static inline uint32_t region_hash(int32_t rx, int32_t rz) {
    uint64_t h = (uint64_t)(uint32_t)rx * 0x9E3779B97F4A7C15ull;
    h ^= (uint64_t)(uint32_t)rz * 0xC2B2AE3D27D4EB4Full;
    return (uint32_t)(h ^ (h >> 32)) & (REGION_BUCKETS - 1);
}

static RegionEntry* region_find(int32_t rx, int32_t rz) {
    uint32_t b = region_hash(rx, rz);
    for (RegionEntry* e = g_region_buckets[b]; e; e = e->next)
        if (e->rx == rx && e->rz == rz) return e;
    return nullptr;
}

static void region_free(RegionEntry* e) {
    for (uint32_t i = 0; i < e->count; i++) free(e->chunks[i].data);
    free(e->chunks);
    free(e);
}

// mg_region_prefetch: carrega TODA a regiao (rx,rz) que existir no arquivo
// .mca informado, descomprimindo cada chunk presente. Sincrono — o Java
// chama isto de uma thread de background, nunca da thread do tick de jogo.
// Retorna quantidade de chunks carregados, ou <0 em erro. out_ms recebe o
// tempo gasto em milissegundos (para o log de comparacao).
EXPORT int32_t mg_region_prefetch(const char* path, int32_t rx, int32_t rz, double* out_ms) {
    if (!path) return -1;
    struct timespec t0, t1;
    clock_gettime(CLOCK_MONOTONIC, &t0);

    // Se ja existe (prefetch redundante ou jogador ainda na regiao), no-op.
    if (region_find(rx, rz)) {
        if (out_ms) *out_ms = 0.0;
        return 0;
    }

    FILE* f = fopen(path, "rb");
    if (!f) return -2;

    RegionEntry* e = (RegionEntry*)malloc(sizeof(RegionEntry));
    e->rx = rx; e->rz = rz; e->count = 0; e->capacity = 1024;
    e->chunks = (RegionChunkData*)malloc(sizeof(RegionChunkData) * e->capacity);
    e->next = nullptr;

    uint8_t hdr[4096];
    if (fread(hdr, 1, 4096, f) != 4096) { fclose(f); free(e->chunks); free(e); return -3; }

    static thread_local uint8_t buf[2 * 1024 * 1024];

    for (int i = 0; i < 1024; i++) {
        uint32_t off = ((uint32_t)hdr[i*4] << 16) | ((uint32_t)hdr[i*4+1] << 8) | hdr[i*4+2];
        uint8_t cnt = hdr[i*4+3];
        if (off == 0 || cnt == 0) continue;

        int64_t got = decompress_raw(f, off, cnt, buf, sizeof(buf));
        if (got < 0) continue;

        RegionChunkData* rc = &e->chunks[e->count++];
        rc->cx = rx * 32 + (i & 31);
        rc->cz = rz * 32 + (i >> 5);
        rc->len = (uint32_t)got;
        rc->data = (uint8_t*)malloc(got);
        memcpy(rc->data, buf, got);
    }
    fclose(f);

    uint32_t b = region_hash(rx, rz);
    e->next = g_region_buckets[b];
    g_region_buckets[b] = e;

    clock_gettime(CLOCK_MONOTONIC, &t1);
    if (out_ms) *out_ms = (t1.tv_sec - t0.tv_sec) * 1000.0 + (t1.tv_nsec - t0.tv_nsec) / 1e6;
    return (int32_t)e->count;
}

// mg_region_get_chunk: copia o chunk (cx,cz), ja descomprimido, para `out`.
// Retorna bytes copiados, 0 se o chunk nao existe no mundo, ou <0 se a
// regiao ainda nao foi pre-carregada (isto seria um MISS real).
EXPORT int64_t mg_region_get_chunk(int32_t rx, int32_t rz, int32_t cx, int32_t cz,
                                    uint8_t* out, int64_t out_cap) {
    RegionEntry* e = region_find(rx, rz);
    if (!e) return -1; // miss: regiao nao pronta
    for (uint32_t i = 0; i < e->count; i++) {
        if (e->chunks[i].cx == cx && e->chunks[i].cz == cz) {
            if ((int64_t)e->chunks[i].len > out_cap) return -2;
            memcpy(out, e->chunks[i].data, e->chunks[i].len);
            return (int64_t)e->chunks[i].len;
        }
    }
    return 0; // chunk nao gerado ainda (area vazia do mundo)
}

// mg_region_is_ready: 1 se a regiao ja esta com os chunks em RAM.
EXPORT int32_t mg_region_is_ready(int32_t rx, int32_t rz) {
    return region_find(rx, rz) ? 1 : 0;
}

// mg_region_evict: libera a regiao da RAM (fim do cooldown, ou fechamento
// de sessao). E o passo "recomprimir" do desenho: como guardamos apenas
// bytes descomprimidos sob demanda (nao mantemos todas as regioes o tempo
// todo), evict = descartar; um novo prefetch le e descomprime de novo.
EXPORT void mg_region_evict(int32_t rx, int32_t rz) {
    uint32_t b = region_hash(rx, rz);
    RegionEntry** pp = &g_region_buckets[b];
    while (*pp) {
        if ((*pp)->rx == rx && (*pp)->rz == rz) {
            RegionEntry* dead = *pp;
            *pp = dead->next;
            region_free(dead);
            return;
        }
        pp = &(*pp)->next;
    }
}

// mg_region_evict_all: chamado no fechamento da sessao do jogo.
EXPORT void mg_region_evict_all() {
    for (int b = 0; b < REGION_BUCKETS; b++) {
        RegionEntry* e = g_region_buckets[b];
        while (e) { RegionEntry* n = e->next; region_free(e); e = n; }
        g_region_buckets[b] = nullptr;
    }
}

'''

with open(path) as f:
    content = f.read()

marker = "EXPORT int32_t mg_count_chunks"
if "mg_region_prefetch" in content:
    print("Patch de regiao ja aplicado — nada a fazer.")
    sys.exit(0)

assert marker in content, "marcador nao encontrado — arquivo pode ter mudado, nao alterei nada"
content = content.replace(marker, patch + marker, 1)

# garante o include de <ctime> para clock_gettime/timespec
if "#include <ctime>" not in content:
    content = content.replace("#include <cstddef>", "#include <cstddef>\n#include <ctime>", 1)

with open(path, "w") as f:
    f.write(content)

print("Patch de cache por regiao aplicado com sucesso.")
