package com.denonexus.mgshaders.client.bench;

import com.denonexus.mgshaders.MGShaders;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public final class GpuBench {

    private static final Path OUT = Paths.get("/sdcard/MG/bench_gles.json");
    private static int delayTicks = 100;   // ~5 s após entrar no mundo
    private static boolean done = false;

    private GpuBench() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (done) return;
            if (client.player == null || client.level == null) return;
            if (--delayTicks > 0) return;
            done = true;
            try {
                run();
            } catch (Throwable t) {
                MGShaders.LOGGER.error("[bench] falhou", t);
            }
        });
    }

    private static void run() throws IOException {
        String vendor   = GL11.glGetString(GL11.GL_VENDOR);
        String renderer = GL11.glGetString(GL11.GL_RENDERER);
        String version  = GL11.glGetString(GL11.GL_VERSION);

        // ── shaders ──
        String VS = """
            #version 330 core
            layout(location=0) in vec2 a_pos;
            out vec2 v_uv;
            void main(){ v_uv = a_pos; gl_Position = vec4(a_pos, 0.0, 1.0); }
            """;
        String FS_FILL = """
            #version 330 core
            out vec4 frag;
            void main(){ frag = vec4(0.5, 0.25, 0.125, 1.0); }
            """;
        String FS_ALU = """
            #version 330 core
            in vec2 v_uv;
            out vec4 frag;
            uniform float u_seed;
            void main(){
                float s = u_seed + v_uv.x * 0.001;
                for (int i = 0; i < 500; i++)
                    s = sin(s * 1.01 + v_uv.y * 0.001) * 1.0001;
                frag = vec4(s, s*0.5, s*0.25, 1.0);
            }
            """;

        int pFill = program(VS, FS_FILL);
        int pAlu  = program(VS, FS_ALU);

        // ── VAO quad ──
        int vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);
        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        float[] quad = { -1,-1, 1,-1, -1,1, 1,1 };
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, quad, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);

        // ── VAO muitos triângulos ──
        final int NTRI = 1_000_000;
        float[] tris = new float[NTRI * 6];
        for (int i = 0; i < NTRI; i++) {
            float x = ((i * 37) % 1000) / 500.0f - 1.0f;
            float y = ((i * 53) % 1000) / 500.0f - 1.0f;
            tris[i*6+0] = x;          tris[i*6+1] = y;
            tris[i*6+2] = x + 0.005f; tris[i*6+3] = y;
            tris[i*6+4] = x;          tris[i*6+5] = y + 0.005f;
        }
        int vao2 = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao2);
        int vbo2 = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo2);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, tris, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);

        // ── FBO 1024×1024 ──
        int tex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 1024, 1024, 0,
                          GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        int fbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                                    GL11.GL_TEXTURE_2D, tex, 0);

        // ── TESTE 1: FILL ──
        GL11.glViewport(0, 0, 1024, 1024);
        GL20.glUseProgram(pFill);
        GL30.glBindVertexArray(vao);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glFinish();
        final int FILL_DRAWS = 100;
        long t0 = System.nanoTime();
        for (int i = 0; i < FILL_DRAWS; i++) GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
        GL11.glFinish();
        long t1 = System.nanoTime();
        double fill_mpx = (double)FILL_DRAWS * 1024 * 1024 / (t1 - t0);

        // ── TESTE 2: SHADER ALU ──
        GL11.glViewport(0, 0, 512, 512);
        GL20.glUseProgram(pAlu);
        int locSeed = GL20.glGetUniformLocation(pAlu, "u_seed");
        GL20.glUniform1f(locSeed, 1.234f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glFinish();
        final int SH_DRAWS = 10;
        double flopsPerPx = 500.0 * 6.0;
        t0 = System.nanoTime();
        for (int i = 0; i < SH_DRAWS; i++) GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
        GL11.glFinish();
        t1 = System.nanoTime();
        double shader_gflops = (double)SH_DRAWS * 512 * 512 * flopsPerPx / (t1 - t0);

        // ── TESTE 3: VERTEX ──
        GL11.glViewport(0, 0, 512, 512);
        GL20.glUseProgram(pFill);
        GL30.glBindVertexArray(vao2);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glFinish();
        t0 = System.nanoTime();
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, NTRI * 3);
        GL11.glFinish();
        t1 = System.nanoTime();
        double vertex_mtri = (double)NTRI / (t1 - t0);

        // ── TESTE 4: DRAW CALL ──
        GL11.glViewport(0, 0, 4, 4);
        GL30.glBindVertexArray(vao);
        final int DC = 50_000;
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glFinish();
        t0 = System.nanoTime();
        for (int i = 0; i < DC; i++) GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
        GL11.glFinish();
        t1 = System.nanoTime();
        double dc_calls = (double)DC / ((t1 - t0) / 1e9);

        // ── JSON ──
        String json = String.format("""
            {
              "vendor": "%s",
              "renderer": "%s",
              "version": "%s",
              "fill_mpx_s": %.2f,
              "shader_gflops": %.2f,
              "vertex_mtri_s": %.2f,
              "drawcall_calls_s": %.0f
            }
            """, vendor, renderer, version,
                 fill_mpx, shader_gflops, vertex_mtri, dc_calls);

        Files.createDirectories(OUT.getParent());
        Files.writeString(OUT, json, StandardCharsets.UTF_8);

        MGShaders.LOGGER.info("[bench] GLES gravado em {}", OUT);
        MGShaders.LOGGER.info("[bench] {}", json.replace("\n", " "));

        // limpa
        GL30.glDeleteFramebuffers(fbo);
        GL11.glDeleteTextures(tex);
        GL15.glDeleteBuffers(vbo);
        GL15.glDeleteBuffers(vbo2);
        GL30.glDeleteVertexArrays(vao);
        GL30.glDeleteVertexArrays(vao2);
    }

    private static int program(String vs, String fs) {
        int v = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(v, vs);
        GL20.glCompileShader(v);
        int f = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(f, fs);
        GL20.glCompileShader(f);
        int p = GL20.glCreateProgram();
        GL20.glAttachShader(p, v);
        GL20.glAttachShader(p, f);
        GL20.glLinkProgram(p);
        return p;
    }
}
