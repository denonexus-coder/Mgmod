import com.denonexus.mgshaders.nativebridge.NativeChunkLoader;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

// Simulador de cache preditivo de regioes, em corredor 1D de regioes.
// Cada regiao usa os 30 chunks reais capturados de real_chunks.bin (bytes e
// custo de descompressao sao reais; a topologia do mundo eh simplificada).
//
// Estados por regiao: COMPRIMIDA -> PRE_CARREGANDO -> PRONTA -> RESFRIANDO -> COMPRIMIDA
public class RegionCacheSim {

    enum State { COMPRIMIDA, PRE_CARREGANDO, PRONTA, RESFRIANDO }

    static class Region {
        int id;
        State state = State.COMPRIMIDA;
        int cooldownTicksLeft = 0;
        long lastDecompressNs = 0;

        Region(int id) { this.id = id; }
    }

    // ---- parametros ajustaveis ----
    static final double TICK_MS = 50.0;           // 20 TPS = 50ms/tick
    static final double REGION_SIZE_BLOCKS = 512; // 1 region = 32x32 chunks = 512x512 blocos
    static final int COOLDOWN_TICKS = 100;        // 5s de tolerancia antes de recomprimir
    static final int LOOKAHEAD_TICKS = 40;        // horizonte de previsao (2s a 20tps)
    static final int SIM_TICKS = 2000;            // 100s de simulacao
    static final double SPEED_BLOCKS_PER_TICK = 0.28; // ~ sprint (5.6 blocos/s)

    static List<byte[]> compChunks = new ArrayList<>();
    static List<Integer> rawLens = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        loadRealChunks("/root/Mgmod/real_chunks.bin");

        Map<Integer, Region> regions = new HashMap<>();

        double playerPos = 0;       // em blocos, ao longo do corredor 1D
        double velocity = SPEED_BLOCKS_PER_TICK;
        Random rnd = new Random(7);

        long totalPrefetchNs = 0;
        long worstTickNs = 0;
        int misses = 0;             // vezes que o player entrou numa regiao NAO pronta -> lag real
        int recompressoes = 0;
        List<Double> tickTimesMs = new ArrayList<>();

        ByteBuffer src = ByteBuffer.allocateDirect(1 << 20).order(ByteOrder.nativeOrder());
        ByteBuffer dst = ByteBuffer.allocateDirect(1 << 20).order(ByteOrder.nativeOrder());

        // regiao inicial ja deve estar pronta ao entrar no mundo
        int startRegion = regionAt(playerPos);
        Region r0 = regions.computeIfAbsent(startRegion, Region::new);
        decompressRegion(r0, src, dst);
        System.out.printf("[entrada no mundo] regiao %d pre-carregada em %.3f ms%n",
                startRegion, r0.lastDecompressNs / 1_000_000.0);

        for (int tick = 0; tick < SIM_TICKS; tick++) {
            long tickStart = System.nanoTime();

            if (tick % 137 == 0) {
                velocity = (rnd.nextBoolean() ? 1 : -1) * (0.15 + rnd.nextDouble() * 0.2);
            }

            playerPos += velocity;
            int currentRegion = regionAt(playerPos);

            double projectedPos = playerPos + velocity * LOOKAHEAD_TICKS;
            int projectedRegion = regionAt(projectedPos);

            Region cur = regions.computeIfAbsent(currentRegion, Region::new);
            if (cur.state == State.COMPRIMIDA || cur.state == State.PRE_CARREGANDO) {
                misses++;
                long t0 = System.nanoTime();
                decompressRegion(cur, src, dst);
                totalPrefetchNs += System.nanoTime() - t0;
            } else if (cur.state == State.RESFRIANDO) {
                cur.state = State.PRONTA;
                cur.cooldownTicksLeft = 0;
            }

            if (projectedRegion != currentRegion) {
                Region proj = regions.computeIfAbsent(projectedRegion, Region::new);
                if (proj.state == State.COMPRIMIDA) {
                    proj.state = State.PRE_CARREGANDO;
                    long t0 = System.nanoTime();
                    decompressRegion(proj, src, dst);
                    totalPrefetchNs += System.nanoTime() - t0;
                    proj.state = State.PRONTA;
                }
            }

            for (Region r : regions.values()) {
                if (r.id == currentRegion || r.id == projectedRegion) continue;
                if (r.state == State.PRONTA) {
                    r.state = State.RESFRIANDO;
                    r.cooldownTicksLeft = COOLDOWN_TICKS;
                } else if (r.state == State.RESFRIANDO) {
                    r.cooldownTicksLeft--;
                    if (r.cooldownTicksLeft <= 0) {
                        r.state = State.COMPRIMIDA;
                        recompressoes++;
                    }
                }
            }

            long tickNs = System.nanoTime() - tickStart;
            worstTickNs = Math.max(worstTickNs, tickNs);
            tickTimesMs.add(tickNs / 1_000_000.0);
        }

        double avgTickMs = tickTimesMs.stream().mapToDouble(d -> d).average().orElse(0);
        double simulatedTps = Math.min(20.0, 1000.0 / Math.max(avgTickMs, 1000.0 / 20.0));

        System.out.println("==================================================");
        System.out.printf("Ticks simulados          : %d (%.0f s a 20 TPS)%n", SIM_TICKS, SIM_TICKS / 20.0);
        System.out.printf("Regioes visitadas        : %d%n", regions.size());
        System.out.printf("Misses (lag real)        : %d%n", misses);
        System.out.printf("Recompressoes (cooldown) : %d%n", recompressoes);
        System.out.printf("Tempo medio por tick     : %.4f ms (orcamento: %.1f ms)%n", avgTickMs, TICK_MS);
        System.out.printf("Pior tick                : %.4f ms%n", worstTickNs / 1_000_000.0);
        System.out.printf("Tempo total em prefetch  : %.3f ms%n", totalPrefetchNs / 1_000_000.0);
        System.out.printf("TPS simulado estimado    : %.2f%n", simulatedTps);
        System.out.println("==================================================");
        if (misses == 0) {
            System.out.println("Nenhum miss: toda travessia de regiao foi coberta pelo prefetch antecipado.");
        } else {
            System.out.println("Houve miss: LOOKAHEAD_TICKS ou COOLDOWN_TICKS precisam de ajuste para essa velocidade.");
        }
    }

    static int regionAt(double posBlocks) {
        return (int) Math.floor(posBlocks / REGION_SIZE_BLOCKS);
    }

    static void decompressRegion(Region r, ByteBuffer src, ByteBuffer dst) {
        long start = System.nanoTime();
        for (int i = 0; i < compChunks.size(); i++) {
            byte[] comp = compChunks.get(i);
            src.clear(); src.put(comp); src.flip();
            dst.clear();
            NativeChunkLoader.decompressLZ4Direct(src, 0, comp.length, dst, 0, dst.capacity());
        }
        r.lastDecompressNs = System.nanoTime() - start;
        r.state = State.PRONTA;
    }

    static void loadRealChunks(String path) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
        int n = Integer.reverseBytes(in.readInt());
        for (int i = 0; i < n; i++) {
            int rawLen = Integer.reverseBytes(in.readInt());
            int compLen = Integer.reverseBytes(in.readInt());
            byte[] comp = new byte[compLen];
            in.readFully(comp);
            compChunks.add(comp);
            rawLens.add(rawLen);
        }
        in.close();
        System.out.println("Chunks reais carregados para o dataset de regiao: " + n);
    }
}
