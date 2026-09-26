// native_chunk.cpp v2 — leitor .mca ultra-rápido
// libdeflate + arena + cache RAM + LRU + FFM-friendly
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cstddef>
#include <ctime>
#include <libdeflate.h>

#define EXPORT extern "C" __attribute__((visibility("default")))

struct Arena { uint8_t* base; size_t cap; size_t used; size_t peak; };
static Arena g_ent_arena;

static void arena_init(Arena* a, size_t cap) {
    a->base = (uint8_t*)malloc(cap);
    a->cap = cap; a->used = 0; a->peak = 0;
}
static void* arena_alloc(Arena* a, size_t n) {
    n = (n + 15) & ~(size_t)15;
    if (a->used + n > a->cap) return nullptr;
    void* p = a->base + a->used;
    a->used += n;
    if (a->used > a->peak) a->peak = a->used;
    return p;
}

struct CacheEnt {
    int32_t cx, cz;
    uint32_t len;
    uint8_t* data;
    int ref;
    CacheEnt* hnext;
    CacheEnt* lru_prev;
    CacheEnt* lru_next;
};

#define N_BUCKETS (1u << 17)

static CacheEnt* g_buckets[N_BUCKETS] = {0};
static CacheEnt* g_lru_head = nullptr;
static CacheEnt* g_lru_tail = nullptr;
static size_t    g_n_chunks = 0;
static size_t    g_max_chunks = 20000;
static uint64_t  g_hits = 0, g_misses = 0, g_evicts = 0;

static inline uint32_t hash_key(int32_t cx, int32_t cz) {
    uint64_t h = (uint64_t)(uint32_t)cx * 0x9E3779B97F4A7C15ull;
    h ^= (uint64_t)(uint32_t)cz * 0xC2B2AE3D27D4EB4Full;
    return (uint32_t)(h ^ (h >> 32)) & (N_BUCKETS - 1);
}

static CacheEnt* cache_find(int32_t cx, int32_t cz) {
    uint32_t b = hash_key(cx, cz);
    for (CacheEnt* e = g_buckets[b]; e; e = e->hnext)
        if (e->cx == cx && e->cz == cz) return e;
    return nullptr;
}

static void lru_unlink(CacheEnt* e) {
    if (e->lru_prev) e->lru_prev->lru_next = e->lru_next; else g_lru_head = e->lru_next;
    if (e->lru_next) e->lru_next->lru_prev = e->lru_prev; else g_lru_tail = e->lru_prev;
}
static void lru_push_front(CacheEnt* e) {
    e->lru_prev = nullptr; e->lru_next = g_lru_head;
    if (g_lru_head) g_lru_head->lru_prev = e;
    g_lru_head = e;
    if (!g_lru_tail) g_lru_tail = e;
}
static void hash_unlink(CacheEnt* e) {
    uint32_t b = hash_key(e->cx, e->cz);
    CacheEnt** pp = &g_buckets[b];
    while (*pp && *pp != e) pp = &(*pp)->hnext;
    if (*pp) *pp = e->hnext;
}
static void cache_evict() {
    CacheEnt* e = g_lru_tail;
    while (e && e->ref) {
        e->ref = 0;
        CacheEnt* prev = e->lru_prev;
        lru_unlink(e); lru_push_front(e);
        e = prev;
    }
    if (!e) return;
    lru_unlink(e); hash_unlink(e);
    free(e->data);
    g_evicts++; g_n_chunks--;
}

static libdeflate_decompressor* g_decomp = nullptr;
static libdeflate_compressor*   g_comp   = nullptr;

struct McaEntry { uint32_t sector_off; uint8_t sector_cnt; };

static bool read_mca_header(FILE* f, int cx, int cz, McaEntry* out) {
    int idx = (cx & 31) + (cz & 31) * 32;
    if (fseek(f, (long)idx * 4, SEEK_SET) != 0) return false;
    uint8_t b[4];
    if (fread(b, 1, 4, f) != 4) return false;
    out->sector_off = ((uint32_t)b[0] << 16) | ((uint32_t)b[1] << 8) | b[2];
    out->sector_cnt = b[3];
    return true;
}

static int64_t decompress_raw(FILE* f, uint32_t sector_off, uint8_t sector_cnt,
                              uint8_t* out, int64_t out_cap) {
    if (fseek(f, (long)sector_off * 4096L, SEEK_SET) != 0) return -1;
    uint8_t hdr[5];
    if (fread(hdr, 1, 5, f) != 5) return -1;
    uint32_t length = ((uint32_t)hdr[0] << 24) | ((uint32_t)hdr[1] << 16) |
                      ((uint32_t)hdr[2] << 8) | hdr[3];
    uint8_t comp = hdr[4];
    if (length <= 1) return -1;
    if (length > (uint32_t)sector_cnt * 4096u) return -1;
    uint32_t clen = length - 1;
    if (comp == 3) {
        if ((int64_t)clen > out_cap) return -1;
        size_t rd = fread(out, 1, clen, f);
        return (rd == clen) ? (int64_t)clen : -1;
    }
    uint8_t* tmp = (uint8_t*)malloc(clen);
    if (!tmp) return -1;
    size_t rd = fread(tmp, 1, clen, f);
    if (rd != clen) { free(tmp); return -1; }
    size_t got = 0;
    libdeflate_result rc;
    if (comp == 2)      rc = libdeflate_zlib_decompress(g_decomp, tmp, clen, out, out_cap, &got);
    else if (comp == 1) rc = libdeflate_gzip_decompress(g_decomp, tmp, clen, out, out_cap, &got);
    else { free(tmp); return -4; }
    free(tmp);
    return (rc == LIBDEFLATE_SUCCESS) ? (int64_t)got : -5;
}

EXPORT int32_t mg_cache_init(uint32_t max_chunks) {
    if (!g_decomp) g_decomp = libdeflate_alloc_decompressor();
    if (!g_comp)   g_comp   = libdeflate_alloc_compressor(1);
    g_max_chunks = max_chunks ? max_chunks : 20000;
    arena_init(&g_ent_arena, 8 * 1024 * 1024);
    return 0;
}

EXPORT void mg_cache_shutdown() {
    for (size_t i = 0; i < N_BUCKETS; i++) {
        CacheEnt* e = g_buckets[i];
        while (e) { CacheEnt* n = e->hnext; free(e->data); e = n; }
        g_buckets[i] = nullptr;
    }
    g_lru_head = g_lru_tail = nullptr;
    g_n_chunks = 0;
    if (g_decomp) { libdeflate_free_decompressor(g_decomp); g_decomp = nullptr; }
    if (g_comp)   { libdeflate_free_compressor(g_comp);     g_comp   = nullptr; }
    if (g_ent_arena.base) { free(g_ent_arena.base); g_ent_arena.base = nullptr; }
}

EXPORT int64_t mg_read_chunk(const char* path, int32_t cx, int32_t cz,
                             uint8_t* out, int64_t out_cap) {
    if (!path || !out || out_cap <= 0) return -1;
    FILE* f = fopen(path, "rb");
    if (!f) return -1;
    McaEntry me;
    if (!read_mca_header(f, cx, cz, &me) || me.sector_off == 0 || me.sector_cnt == 0) {
        fclose(f); return -2;
    }
    int64_t n = decompress_raw(f, me.sector_off, me.sector_cnt, out, out_cap);
    fclose(f);
    return n;
}

EXPORT const uint8_t* mg_cache_get(const char* path, int32_t cx, int32_t cz,
                                   uint32_t* out_len) {
    CacheEnt* e = cache_find(cx, cz);
    if (e) { e->ref = 1; *out_len = e->len; g_hits++; return e->data; }
    g_misses++;
    FILE* f = fopen(path, "rb");
    if (!f) return nullptr;
    McaEntry me;
    if (!read_mca_header(f, cx, cz, &me) || me.sector_off == 0 || me.sector_cnt == 0) {
        fclose(f); return nullptr;
    }
    uint8_t* buf = (uint8_t*)malloc(2 * 1024 * 1024);
    if (!buf) { fclose(f); return nullptr; }
    int64_t got = decompress_raw(f, me.sector_off, me.sector_cnt, buf, 2 * 1024 * 1024);
    fclose(f);
    if (got < 0) { free(buf); return nullptr; }

    while (g_n_chunks >= g_max_chunks) cache_evict();
    CacheEnt* ne = (CacheEnt*)arena_alloc(&g_ent_arena, sizeof(CacheEnt));
    if (!ne) return nullptr;
    ne->cx = cx; ne->cz = cz; ne->len = (uint32_t)got; ne->data = buf; ne->ref = 1;
    uint32_t b = hash_key(cx, cz);
    ne->hnext = g_buckets[b];
    g_buckets[b] = ne;
    lru_push_front(ne);
    g_n_chunks++;
    *out_len = ne->len;
    return ne->data;
}

EXPORT void mg_cache_prefetch(const char* path, int32_t cx, int32_t cz) {
    if (cache_find(cx, cz)) return;
    FILE* f = fopen(path, "rb");
    if (!f) return;
    McaEntry me;
    read_mca_header(f, cx, cz, &me);
    fclose(f);
}

EXPORT void mg_cache_stats(uint64_t* hits, uint64_t* misses, uint64_t* evicts, size_t* n) {
    if (hits)   *hits = g_hits;
    if (misses) *misses = g_misses;
    if (evicts) *evicts = g_evicts;
    if (n)      *n = g_n_chunks;
}


// LZ4 native bridge usado pelo Region System.
// Detecta zlib vs gzip pelo cabecalho e descomprime via libdeflate.
EXPORT int64_t mg_inflate_auto(const uint8_t* src, int64_t src_len,
                                uint8_t* out, int64_t out_cap) {
    if (!src || src_len <= 0 || !out || out_cap <= 0) return -1;
    if (!g_decomp) g_decomp = libdeflate_alloc_decompressor();
    if (!g_decomp) return -2;
    size_t got = 0;
    libdeflate_result rc;
    if (src_len >= 2 && (uint8_t)src[0] == 0x1F && (uint8_t)src[1] == 0x8B) {
        rc = libdeflate_gzip_decompress(g_decomp, src, (size_t)src_len, out, (size_t)out_cap, &got);
    } else {
        rc = libdeflate_zlib_decompress(g_decomp, src, (size_t)src_len, out, (size_t)out_cap, &got);
    }
    if (rc != LIBDEFLATE_SUCCESS) return -3;
    return (int64_t)got;
}


// =============================================================================
// REGION CACHE — prefetch preditivo por regiao (32x32 chunks = 512x512 blocos)
//
// Diferenca para o cache por-chunk acima (mg_cache_get/mg_cache_prefetch):
// aquele e reativo (busca sob demanda, chunk a chunk). Este e preditivo:
// o Java decide, com base na direcao/velocidade do jogador, QUANDO carregar
// chunks individuais antecipados pelo Region System.
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

EXPORT int32_t mg_count_chunks(const char* path) {
    FILE* f = fopen(path, "rb");
    if (!f) return -1;
    uint8_t hdr[4096];
    size_t rd = fread(hdr, 1, 4096, f);
    fclose(f);
    if (rd != 4096) return -1;
    int c = 0;
    for (int i = 0; i < 1024; i++) {
        uint32_t off = ((uint32_t)hdr[i*4] << 16) | ((uint32_t)hdr[i*4+1] << 8) | hdr[i*4+2];
        if (off) c++;
    }
    return c;
}
