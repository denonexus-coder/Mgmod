// gpu_bench.cpp — CPU + GPU (GLES3) + OpenCL benchmark para Android aarch64
// Compila: clang++ -O3 -shared -fPIC -std=c++17 -o libgpubench.so gpu_bench.cpp -ldl -pthread
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cstdarg>
#include <cstddef>
#include <chrono>
#include <thread>
#include <vector>
#include <atomic>
#include <cmath>
#include <dlfcn.h>

#if defined(__aarch64__)
#include <arm_neon.h>
#endif

#define EXPORT extern "C" __attribute__((visibility("default")))

// ═══════════ JSON ═══════════
struct JBuf {
    char* b; int64_t cap, pos; bool ok;
    void init(uint8_t* p, int64_t c) { b=(char*)p; cap=c; pos=0; ok=true; if(c>0) b[0]=0; }
    void s(const char* str) {
        int64_t n = (int64_t)strlen(str);
        if (pos + n + 1 > cap) { ok=false; return; }
        memcpy(b+pos, str, n); pos += n; b[pos]=0;
    }
    void f(const char* fmt, ...) {
        char tmp[512];
        va_list ap; va_start(ap, fmt);
        vsnprintf(tmp, sizeof(tmp), fmt, ap);
        va_end(ap);
        s(tmp);
    }
    int64_t end() { return ok ? pos : -5; }
};

static inline uint64_t now_ns() {
    return std::chrono::duration_cast<std::chrono::nanoseconds>(
        std::chrono::steady_clock::now().time_since_epoch()).count();
}

static double g_sink = 0.0;

// ═══════════ CPU ═══════════
static double cpu_scalar(int64_t iters) {
    double a0=1.0, a1=1.1, a2=1.2, a3=1.3;
    double b=1.0000001, c=0.5;
    uint64_t t0 = now_ns();
    for (int64_t i = 0; i < iters; i++) {
        a0 = a0*b + c;
        a1 = a1*b + c;
        a2 = a2*b + c;
        a3 = a3*b + c;
    }
    uint64_t t1 = now_ns();
    g_sink += a0+a1+a2+a3;
    return (double)(iters * 8) / (double)(t1 - t0);
}

#if defined(__aarch64__)
static double cpu_neon_f64(int64_t iters) {
    float64x2_t a0 = vdupq_n_f64(1.0), a1 = vdupq_n_f64(1.1);
    float64x2_t b  = vdupq_n_f64(1.0000001);
    float64x2_t c  = vdupq_n_f64(0.5);
    uint64_t t0 = now_ns();
    for (int64_t i = 0; i < iters; i++) {
        a0 = vfmaq_f64(a0, b, c);
        a1 = vfmaq_f64(a1, b, c);
    }
    uint64_t t1 = now_ns();
    g_sink += vgetq_lane_f64(a0, 0) + vgetq_lane_f64(a1, 0);
    return (double)(iters * 8) / (double)(t1 - t0);
}

static double cpu_neon_f32(int64_t iters) {
    float32x4_t a0 = vdupq_n_f32(1.0f), a1 = vdupq_n_f32(1.1f);
    float32x4_t b  = vdupq_n_f32(1.0000001f);
    float32x4_t c  = vdupq_n_f32(0.5f);
    uint64_t t0 = now_ns();
    for (int64_t i = 0; i < iters; i++) {
        a0 = vfmaq_f32(a0, b, c);
        a1 = vfmaq_f32(a1, b, c);
    }
    uint64_t t1 = now_ns();
    g_sink += vgetq_lane_f32(a0, 0) + vgetq_lane_f32(a1, 0);
    return (double)(iters * 16) / (double)(t1 - t0);
}
#else
static double cpu_neon_f64(int64_t) { return 0.0; }
static double cpu_neon_f32(int64_t) { return 0.0; }
#endif

volatile uint8_t g_mem_sink = 0;

static double cpu_membw() {
    const size_t N = 64 * 1024 * 1024;
    std::vector<uint8_t> a(N, 0xAB), b(N, 0);
    uint64_t t0 = now_ns();
    memcpy(b.data(), a.data(), N);
    // Força materializar — impede -O3 de apagar o memcpy
    uint8_t s = 0;
    for (size_t i = 0; i < N; i += 4096) s ^= b[i];
    g_mem_sink = s;
    uint64_t t1 = now_ns();
    return (double)(N * 2) / (double)(t1 - t0);  // GB/s
}

static double cpu_cache_lat() {
    const size_t N = 4 * 1024 * 1024;
    const size_t step = 16;
    std::vector<uint32_t> next(N, 0);
    size_t m = N / step;
    for (size_t i = 0; i < m; i++) next[i*step] = (uint32_t)(((i+1) % m) * step);
    const int64_t iters = 5'000'000;
    uint32_t k = 0;
    uint64_t t0 = now_ns();
    for (int64_t i = 0; i < iters; i++) k = next[k];
    uint64_t t1 = now_ns();
    g_sink += k;
    return (double)(t1 - t0) / (double)iters;
}

static double cpu_mt(int nthreads, int64_t iters) {
    std::vector<std::thread> ts;
    ts.reserve(nthreads);
    uint64_t t0 = now_ns();
    for (int t = 0; t < nthreads; t++) {
        ts.emplace_back([iters]() {
            double a0=1.0, a1=1.1, b=1.0000001, c=0.5;
            for (int64_t i = 0; i < iters; i++) { a0=a0*b+c; a1=a1*b+c; }
            g_sink += a0 + a1;
        });
    }
    for (auto& t : ts) t.join();
    uint64_t t1 = now_ns();
    return (double)(nthreads * iters * 4) / (double)(t1 - t0);
}

static int bench_cpu(JBuf& j) {
    int cpus = (int)std::thread::hardware_concurrency();
    if (cpus < 1) cpus = 1;
    double sc  = cpu_scalar(100'000'000);
    double ne64 = cpu_neon_f64(100'000'000);
    double ne32 = cpu_neon_f32(100'000'000);
    double bw  = cpu_membw();
    double cl  = cpu_cache_lat();
    double m1 = cpu_mt(1, 50'000'000);
    double mn = cpu_mt(cpus, 50'000'000);

    j.s("{\n  \"cpu_cores\": "); j.f("%d", cpus); j.s(",\n");
    j.s("  \"scalar_gflops\": ");   j.f("%.2f", sc);   j.s(",\n");
    j.s("  \"neon_f64_gflops\": "); j.f("%.2f", ne64); j.s(",\n");
    j.s("  \"neon_f32_gflops\": "); j.f("%.2f", ne32); j.s(",\n");
    j.s("  \"mem_bw_gbps\": ");   j.f("%.2f", bw); j.s(",\n");
    j.s("  \"cache_lat_ns\": ");  j.f("%.2f", cl); j.s(",\n");
    j.s("  \"mt_1_gflops\": ");   j.f("%.2f", m1); j.s(",\n");
    j.s("  \"mt_n_gflops\": ");   j.f("%.2f", mn); j.s("\n}");
    return (int)j.end();
}

// ═══════════ GLES 3.2 ═══════════
typedef void*   EGLDisplay;
typedef void*   EGLContext;
typedef void*   EGLSurface;
typedef void*   EGLConfig;
typedef int32_t EGLint;
typedef uint32_t EGLBoolean;
typedef void*   (*PFN_eglGetProcAddress)(const char*);

#define EGL_DEFAULT_DISPLAY ((void*)0)
#define EGL_NO_DISPLAY      ((void*)0)
#define EGL_NO_CONTEXT      ((void*)0)
#define EGL_NO_SURFACE      ((void*)0)
#define EGL_FALSE 0
#define EGL_TRUE  1
#define EGL_OPENGL_ES_API 0x30A0
#define EGL_PBUFFER_BIT   0x0001
#define EGL_SURFACE_TYPE  0x3033
#define EGL_RENDERABLE_TYPE 0x3040
#define EGL_OPENGL_ES3_BIT 0x00000040
#define EGL_RED_SIZE   0x3024
#define EGL_GREEN_SIZE 0x3023
#define EGL_BLUE_SIZE  0x3022
#define EGL_ALPHA_SIZE 0x3021
#define EGL_NONE 0x3038
#define EGL_WIDTH  0x3057
#define EGL_HEIGHT 0x3056
#define EGL_CONTEXT_CLIENT_VERSION 0x3098

typedef uint32_t GLenum;
typedef uint32_t GLuint;
typedef int32_t  GLint;
typedef int32_t  GLsizei;
typedef float    GLfloat;
typedef char     GLchar;
typedef uint8_t  GLboolean;
typedef ptrdiff_t GLsizeiptr;
typedef uint32_t GLbitfield;

#define GL_VERTEX_SHADER   0x8B31
#define GL_FRAGMENT_SHADER 0x8B30
#define GL_COMPILE_STATUS  0x8B81
#define GL_LINK_STATUS     0x8B82
#define GL_ARRAY_BUFFER    0x8892
#define GL_STATIC_DRAW     0x88E4
#define GL_FLOAT           0x1406
#define GL_TRIANGLES       0x0004
#define GL_TRIANGLE_STRIP  0x0005
#define GL_COLOR_ATTACHMENT0 0x8CE0
#define GL_FRAMEBUFFER     0x8D40
#define GL_TEXTURE_2D      0x0DE1
#define GL_RGBA            0x1908
#define GL_RGBA8           0x8058
#define GL_UNSIGNED_BYTE   0x1401
#define GL_COLOR_BUFFER_BIT 0x00004000
#define GL_FRAMEBUFFER_COMPLETE 0x8CD5

struct Gles {
    void* h_egl = nullptr;
    void* h_gl  = nullptr;

    EGLDisplay (*eglGetDisplay)(void*) = nullptr;
    EGLBoolean (*eglInitialize)(EGLDisplay, EGLint*, EGLint*) = nullptr;
    EGLBoolean (*eglChooseConfig)(EGLDisplay, const EGLint*, EGLConfig*, EGLint, EGLint*) = nullptr;
    EGLSurface (*eglCreatePbufferSurface)(EGLDisplay, EGLConfig, const EGLint*) = nullptr;
    EGLContext (*eglCreateContext)(EGLDisplay, EGLConfig, EGLContext, const EGLint*) = nullptr;
    EGLBoolean (*eglMakeCurrent)(EGLDisplay, EGLSurface, EGLSurface, EGLContext) = nullptr;
    EGLBoolean (*eglDestroySurface)(EGLDisplay, EGLSurface) = nullptr;
    EGLBoolean (*eglDestroyContext)(EGLDisplay, EGLContext) = nullptr;
    EGLBoolean (*eglTerminate)(EGLDisplay) = nullptr;
    const char* (*eglQueryString)(EGLDisplay, EGLint) = nullptr;
    PFN_eglGetProcAddress eglGetProcAddress = nullptr;

    GLuint (*glCreateShader)(GLenum) = nullptr;
    void (*glShaderSource)(GLuint, GLsizei, const GLchar* const*, const GLint*) = nullptr;
    void (*glCompileShader)(GLuint) = nullptr;
    void (*glGetShaderiv)(GLuint, GLenum, GLint*) = nullptr;
    void (*glGetShaderInfoLog)(GLuint, GLsizei, GLsizei*, GLchar*) = nullptr;
    GLuint (*glCreateProgram)() = nullptr;
    void (*glAttachShader)(GLuint, GLuint) = nullptr;
    void (*glLinkProgram)(GLuint) = nullptr;
    void (*glGetProgramiv)(GLuint, GLenum, GLint*) = nullptr;
    void (*glGetProgramInfoLog)(GLuint, GLsizei, GLsizei*, GLchar*) = nullptr;
    void (*glUseProgram)(GLuint) = nullptr;
    GLint (*glGetUniformLocation)(GLuint, const GLchar*) = nullptr;
    void (*glUniform1f)(GLint, GLfloat) = nullptr;
    void (*glGenFramebuffers)(GLsizei, GLuint*) = nullptr;
    void (*glBindFramebuffer)(GLenum, GLuint) = nullptr;
    void (*glFramebufferTexture2D)(GLenum, GLenum, GLenum, GLuint, GLint) = nullptr;
    GLenum (*glCheckFramebufferStatus)(GLenum) = nullptr;
    void (*glGenTextures)(GLsizei, GLuint*) = nullptr;
    void (*glBindTexture)(GLenum, GLuint) = nullptr;
    void (*glTexImage2D)(GLenum, GLint, GLint, GLsizei, GLsizei, GLint, GLenum, GLenum, const void*) = nullptr;
    void (*glGenBuffers)(GLsizei, GLuint*) = nullptr;
    void (*glBindBuffer)(GLenum, GLuint) = nullptr;
    void (*glBufferData)(GLenum, GLsizeiptr, const void*, GLenum) = nullptr;
    void (*glGenVertexArrays)(GLsizei, GLuint*) = nullptr;
    void (*glBindVertexArray)(GLuint) = nullptr;
    void (*glEnableVertexAttribArray)(GLuint) = nullptr;
    void (*glVertexAttribPointer)(GLuint, GLint, GLenum, GLboolean, GLsizei, const void*) = nullptr;
    void (*glViewport)(GLint, GLint, GLsizei, GLsizei) = nullptr;
    void (*glClearColor)(GLfloat, GLfloat, GLfloat, GLfloat) = nullptr;
    void (*glClear)(GLbitfield) = nullptr;
    void (*glDrawArrays)(GLenum, GLint, GLsizei) = nullptr;
    GLenum (*glGetError)() = nullptr;
    const uint8_t* (*glGetString)(GLenum) = nullptr;
    void (*glFinish)() = nullptr;

    bool ok = false;
    const char* err = nullptr;
};

static void* dl_first(const char* const* paths, int n) {
    for (int i = 0; i < n; i++) {
        void* h = dlopen(paths[i], RTLD_NOW | RTLD_GLOBAL);
        if (h) return h;
    }
    return nullptr;
}

static bool load_gles(Gles& g) {
    const char* ep[] = { "/system/lib64/libEGL.so", "/vendor/lib64/libEGL.so",
                         "/system/vendor/lib64/libEGL.so", "libEGL.so", nullptr };
    const char* gp[] = { "/system/lib64/libGLESv2.so", "/vendor/lib64/libGLESv2.so",
                         "/system/vendor/lib64/libGLESv2.so", "libGLESv2.so", nullptr };
    g.h_egl = dl_first(ep, 4);
    g.h_gl  = dl_first(gp, 4);
    if (!g.h_egl) { g.err = "libEGL.so not found"; return false; }
    if (!g.h_gl)  { g.err = "libGLESv2.so not found"; return false; }

    #define LE(n) g.n = (decltype(g.n))dlsym(g.h_egl, #n)
    LE(eglGetDisplay);   LE(eglInitialize);  LE(eglChooseConfig);
    LE(eglCreatePbufferSurface); LE(eglCreateContext); LE(eglMakeCurrent);
    LE(eglDestroySurface); LE(eglDestroyContext); LE(eglTerminate);
    LE(eglQueryString);  LE(eglGetProcAddress);
    #undef LE

    #define LG(n) g.n = (decltype(g.n))dlsym(g.h_gl, #n)
    LG(glCreateShader); LG(glShaderSource); LG(glCompileShader);
    LG(glGetShaderiv); LG(glGetShaderInfoLog);
    LG(glCreateProgram); LG(glAttachShader); LG(glLinkProgram);
    LG(glGetProgramiv); LG(glGetProgramInfoLog);
    LG(glUseProgram); LG(glGetUniformLocation); LG(glUniform1f);
    LG(glGenFramebuffers); LG(glBindFramebuffer); LG(glFramebufferTexture2D);
    LG(glCheckFramebufferStatus);
    LG(glGenTextures); LG(glBindTexture); LG(glTexImage2D);
    LG(glGenBuffers); LG(glBindBuffer); LG(glBufferData);
    LG(glGenVertexArrays); LG(glBindVertexArray);
    LG(glEnableVertexAttribArray); LG(glVertexAttribPointer);
    LG(glViewport); LG(glClearColor); LG(glClear);
    LG(glDrawArrays); LG(glGetError); LG(glGetString); LG(glFinish);
    #undef LG

    g.ok = true;
    return true;
}

static GLuint compile(Gles& g, GLenum type, const char* src, char* log, int n) {
    GLuint s = g.glCreateShader(type);
    if (!s) return 0;
    g.glShaderSource(s, 1, &src, nullptr);
    g.glCompileShader(s);
    GLint ok = 0;
    g.glGetShaderiv(s, GL_COMPILE_STATUS, &ok);
    if (!ok) { GLsizei k=0; g.glGetShaderInfoLog(s, n, &k, log); return 0; }
    return s;
}

static GLuint program(Gles& g, const char* vs, const char* fs, char* log, int n) {
    GLuint v = compile(g, GL_VERTEX_SHADER, vs, log, n);
    if (!v) return 0;
    GLuint f = compile(g, GL_FRAGMENT_SHADER, fs, log, n);
    if (!f) return 0;
    GLuint p = g.glCreateProgram();
    g.glAttachShader(p, v); g.glAttachShader(p, f);
    g.glLinkProgram(p);
    GLint ok = 0;
    g.glGetProgramiv(p, GL_LINK_STATUS, &ok);
    if (!ok) { GLsizei k=0; g.glGetProgramInfoLog(p, n, &k, log); return 0; }
    return p;
}

static int bench_gles(JBuf& j) {
    Gles g;
    if (!load_gles(g)) {
        j.s("{\n  \"available\": false,\n  \"error\": \""); j.s(g.err ? g.err : "load failed");
        j.s("\"\n}"); return (int)j.end();
    }

    EGLDisplay dpy = g.eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (!dpy) { j.s("{\"available\":false,\"error\":\"eglGetDisplay null\"}"); return (int)j.end(); }
    EGLint vmaj = 0, vmin = 0;
    g.eglInitialize(dpy, &vmaj, &vmin);

    EGLint cfg_attrs[] = {
        EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
        EGL_RED_SIZE,8, EGL_GREEN_SIZE,8, EGL_BLUE_SIZE,8, EGL_ALPHA_SIZE,8,
        EGL_NONE
    };
    EGLConfig cfg = nullptr; EGLint ncfg = 0;
    g.eglChooseConfig(dpy, cfg_attrs, &cfg, 1, &ncfg);
    if (!ncfg) { j.s("{\"available\":false,\"error\":\"no ES3 config\"}"); return (int)j.end(); }

    EGLint pb_attrs[] = { EGL_WIDTH,64, EGL_HEIGHT,64, EGL_NONE };
    EGLSurface surf = g.eglCreatePbufferSurface(dpy, cfg, pb_attrs);

    EGLint ctx_attrs[] = { EGL_CONTEXT_CLIENT_VERSION,3, EGL_NONE };
    EGLContext ctx = g.eglCreateContext(dpy, cfg, EGL_NO_CONTEXT, ctx_attrs);
    if (!ctx) { j.s("{\"available\":false,\"error\":\"eglCreateContext failed\"}"); return (int)j.end(); }
    if (!g.eglMakeCurrent(dpy, surf, surf, ctx)) {
        j.s("{\"available\":false,\"error\":\"eglMakeCurrent failed\"}"); return (int)j.end();
    }

    const char* vendor   = (const char*)g.glGetString(0x1F00);
    const char* renderer = (const char*)g.glGetString(0x1F01);
    const char* version  = (const char*)g.glGetString(0x1F02);

    char log[1024];

    // --- shaders ---
    const char* VS = R"(#version 300 es
in vec2 a_pos;
out vec2 v_uv;
void main(){ v_uv = a_pos; gl_Position = vec4(a_pos, 0.0, 1.0); })";

    const char* FS_FILL = R"(#version 300 es
precision highp float;
out vec4 frag;
void main(){ frag = vec4(0.5, 0.25, 0.125, 1.0); })";

    char fs_shader[1024];
    snprintf(fs_shader, sizeof(fs_shader), R"(#version 300 es
precision highp float;
in vec2 v_uv;
out vec4 frag;
uniform float u_seed;
void main(){
    float s = u_seed + v_uv.x * 0.001;
    for (int i = 0; i < %d; i++) {
        s = sin(s * 1.01 + v_uv.y * 0.001) * 1.0001;
    }
    frag = vec4(s, s*0.5, s*0.25, 1.0);
})", 500);

    GLuint pFill = program(g, VS, FS_FILL, log, sizeof(log));
    GLuint pSh   = program(g, VS, fs_shader, log, sizeof(log));
    if (!pFill || !pSh) {
        j.s("{\"available\":false,\"error\":\"shader fail: "); j.s(log); j.s("\"}");
        return (int)j.end();
    }

    // --- VAO com quad full-screen ---
    GLuint vao, vbo;
    g.glGenVertexArrays(1, &vao);
    g.glBindVertexArray(vao);
    g.glGenBuffers(1, &vbo);
    g.glBindBuffer(GL_ARRAY_BUFFER, vbo);
    float quad[8] = { -1,-1,  1,-1, -1,1,  1,1 };
    g.glBufferData(GL_ARRAY_BUFFER, sizeof(quad), quad, GL_STATIC_DRAW);
    g.glEnableVertexAttribArray(0);
    g.glVertexAttribPointer(0, 2, GL_FLOAT, 0, 0, (const void*)0);

    // --- VAO com muitos triângulos pequenos (para vertex rate) ---
    const int NTRI = 1'000'000;
    std::vector<float> tri(NTRI * 6);
    for (int i = 0; i < NTRI; i++) {
        float x = ((i * 37) % 1000) / 500.0f - 1.0f;
        float y = ((i * 53) % 1000) / 500.0f - 1.0f;
        tri[i*6+0] = x;         tri[i*6+1] = y;
        tri[i*6+2] = x + 0.005f; tri[i*6+3] = y;
        tri[i*6+4] = x;         tri[i*6+5] = y + 0.005f;
    }
    GLuint vao2, vbo2;
    g.glGenVertexArrays(1, &vao2);
    g.glBindVertexArray(vao2);
    g.glGenBuffers(1, &vbo2);
    g.glBindBuffer(GL_ARRAY_BUFFER, vbo2);
    g.glBufferData(GL_ARRAY_BUFFER, (GLsizeiptr)(tri.size()*sizeof(float)), tri.data(), GL_STATIC_DRAW);
    g.glEnableVertexAttribArray(0);
    g.glVertexAttribPointer(0, 2, GL_FLOAT, 0, 0, (const void*)0);

    // --- FBO + textura ---
    GLuint tex, fbo;
    g.glGenTextures(1, &tex);
    g.glBindTexture(GL_TEXTURE_2D, tex);
    g.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1024, 1024, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
    g.glGenFramebuffers(1, &fbo);
    g.glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    g.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tex, 0);
    if (g.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
        j.s("{\"available\":false,\"error\":\"FBO incomplete\"}"); return (int)j.end();
    }

    const int FB_W = 1024, FB_H = 1024;
    g.glViewport(0, 0, FB_W, FB_H);

    // ─── TESTE 1: FILL RATE ───
    g.glUseProgram(pFill);
    g.glBindVertexArray(vao);
    g.glClearColor(0,0,0,1); g.glClear(GL_COLOR_BUFFER_BIT);
    g.glFinish();
    const int FILL_DRAWS = 100;
    uint64_t t0 = now_ns();
    for (int i = 0; i < FILL_DRAWS; i++) g.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    g.glFinish();
    uint64_t t1 = now_ns();
    double fill_mpx_s = (double)FILL_DRAWS * FB_W * FB_H / (double)(t1 - t0);

    // ─── TESTE 2: SHADER ALU ───
    const int SH_W = 512, SH_H = 512;
    g.glViewport(0, 0, SH_W, SH_H);
    g.glUseProgram(pSh);
    GLint loc_seed = g.glGetUniformLocation(pSh, "u_seed");
    g.glUniform1f(loc_seed, 1.234f);
    g.glBindVertexArray(vao);
    g.glClear(GL_COLOR_BUFFER_BIT);
    g.glFinish();
    const int SH_DRAWS = 10;
    const double flops_per_px = 500.0 * 6.0;
    t0 = now_ns();
    for (int i = 0; i < SH_DRAWS; i++) g.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    g.glFinish();
    t1 = now_ns();
    double shader_gflops = (double)SH_DRAWS * SH_W * SH_H * flops_per_px / (double)(t1 - t0);

    // ─── TESTE 3: VERTEX RATE ───
    g.glViewport(0, 0, 512, 512);
    g.glUseProgram(pFill);
    g.glBindVertexArray(vao2);
    g.glClear(GL_COLOR_BUFFER_BIT);
    g.glFinish();
    t0 = now_ns();
    g.glDrawArrays(GL_TRIANGLES, 0, NTRI * 3);
    g.glFinish();
    t1 = now_ns();
    double vertex_mtri_s = (double)NTRI / (double)(t1 - t0);

    // ─── TESTE 4: DRAW CALL OVERHEAD ───
    g.glViewport(0, 0, 4, 4);
    g.glBindVertexArray(vao);
    const int DC = 50'000;
    g.glClear(GL_COLOR_BUFFER_BIT);
    g.glFinish();
    t0 = now_ns();
    for (int i = 0; i < DC; i++) g.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    g.glFinish();
    t1 = now_ns();
    double dc_calls_s = (double)DC / ((double)(t1 - t0) / 1e9);

    GLenum glerr = g.glGetError();

    j.s("{\n  \"available\": true,\n");
    j.s("  \"egl_version\": \""); j.f("%d.%d", vmaj, vmin); j.s("\",\n");
    j.s("  \"vendor\": \"");   j.s(vendor   ? vendor   : "?"); j.s("\",\n");
    j.s("  \"renderer\": \""); j.s(renderer ? renderer : "?"); j.s("\",\n");
    j.s("  \"version\": \"");  j.s(version  ? version  : "?"); j.s("\",\n");
    j.s("  \"fill_mpx_s\": ");       j.f("%.2f", fill_mpx_s);    j.s(",\n");
    j.s("  \"shader_gflops\": ");    j.f("%.2f", shader_gflops); j.s(",\n");
    j.s("  \"vertex_mtri_s\": ");    j.f("%.2f", vertex_mtri_s); j.s(",\n");
    j.s("  \"drawcall_calls_s\": "); j.f("%.0f", dc_calls_s);    j.s(",\n");
    j.s("  \"gl_error\": ");         j.f("%u", (unsigned)glerr); j.s("\n}");

    g.eglMakeCurrent(dpy, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    g.eglDestroySurface(dpy, surf);
    g.eglDestroyContext(dpy, ctx);
    g.eglTerminate(dpy);
    return (int)j.end();
}

// ═══════════ OpenCL (detecção) ═══════════
typedef int32_t cl_int;
typedef uint32_t cl_uint;
typedef uint64_t cl_ulong;
typedef void* cl_platform_id;
typedef void* cl_device_id;
typedef cl_int (*PFN_clGetPlatformIDs)(cl_uint, cl_platform_id*, cl_uint*);
typedef cl_int (*PFN_clGetDeviceIDs)(cl_platform_id, cl_ulong, cl_uint, cl_device_id*, cl_uint*);
typedef cl_int (*PFN_clGetDeviceInfo)(cl_device_id, cl_uint, size_t, void*, size_t*);

#define CL_DEVICE_TYPE_GPU 0x00000004
#define CL_DEVICE_NAME     0x102B
#define CL_DEVICE_VENDOR   0x102C
#define CL_DEVICE_VERSION  0x102F

static int bench_opencl(JBuf& j) {
    const char* paths[] = {
        "/vendor/lib64/libOpenCL.so",
        "/system/vendor/lib64/libOpenCL.so",
        "/system/lib64/libOpenCL.so",
        "/data/data/com.termux/files/home/opencl_libs/libOpenCL.so",
        "libOpenCL.so", nullptr
    };
    void* h = dl_first(paths, 4);
    if (!h) {
        j.s("{\n  \"available\": false,\n  \"error\": \"libOpenCL.so not found\"\n}");
        return (int)j.end();
    }
    auto pGetPlatforms = (PFN_clGetPlatformIDs)dlsym(h, "clGetPlatformIDs");
    auto pGetDevices   = (PFN_clGetDeviceIDs)  dlsym(h, "clGetDeviceIDs");
    auto pGetInfo      = (PFN_clGetDeviceInfo) dlsym(h, "clGetDeviceInfo");
    if (!pGetPlatforms || !pGetDevices || !pGetInfo) {
        j.s("{\"available\":false,\"error\":\"symbols missing\"}");
        return (int)j.end();
    }
    cl_uint nplats = 0;
    if (pGetPlatforms(0, nullptr, &nplats) != 0 || nplats == 0) {
        j.s("{\"available\":false,\"error\":\"no platform\"}"); return (int)j.end();
    }
    std::vector<cl_platform_id> plats(nplats);
    pGetPlatforms(nplats, plats.data(), nullptr);

    j.s("{\n  \"available\": true,\n  \"platforms\": [");
    for (cl_uint p = 0; p < nplats; p++) {
        cl_uint ndev = 0;
        pGetDevices(plats[p], CL_DEVICE_TYPE_GPU, 0, nullptr, &ndev);
        if (p == 0) j.s("\n");
        j.s("    {\"gpu_count\": "); j.f("%u", ndev);
        if (ndev > 0) {
            std::vector<cl_device_id> devs(ndev);
            pGetDevices(plats[p], CL_DEVICE_TYPE_GPU, ndev, devs.data(), nullptr);
            char name[256] = {0}, vend[256] = {0}, ver[128] = {0};
            size_t n = 0;
            pGetInfo(devs[0], CL_DEVICE_NAME,    sizeof(name), name, &n);
            pGetInfo(devs[0], CL_DEVICE_VENDOR,  sizeof(vend), vend, &n);
            pGetInfo(devs[0], CL_DEVICE_VERSION, sizeof(ver),  ver,  &n);
            j.s(", \"name\": \"");   j.s(name);
            j.s("\", \"vendor\": \""); j.s(vend);
            j.s("\", \"version\": \""); j.s(ver); j.s("\"");
        }
        j.s("}");
        if (p + 1 < nplats) j.s(",");
        j.s("\n");
    }
    j.s("  ]\n}");
    return (int)j.end();
}

// ═══════════ Exports ═══════════
EXPORT int64_t mg_bench_cpu(uint8_t* out, int64_t cap) {
    JBuf j; j.init(out, cap); return bench_cpu(j);
}
EXPORT int64_t mg_bench_gles(uint8_t* out, int64_t cap) {
    JBuf j; j.init(out, cap); return bench_gles(j);
}
EXPORT int64_t mg_bench_opencl(uint8_t* out, int64_t cap) {
    JBuf j; j.init(out, cap); return bench_opencl(j);
}
