import java.lang.foreign.*;
import java.nio.ByteBuffer;

public class Lz4Bench {
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("🚀 TESTE COMPARATIVO: JNI vs FFM (Java 21)");
        System.out.println("==========================================");

        int iterations = 100_000;
        byte[] dummyData = new byte[4096]; // 4KB de dados simulados de chunk

        // 1. Simulação de Teste via ByteBuffer tradicional / JNI-like overhead
        long startJni = System.nanoTime();
        ByteBuffer buffer = ByteBuffer.allocateDirect(dummyData.length);
        for (int i = 0; i < iterations; i++) {
            buffer.clear();
            buffer.put(dummyData);
            buffer.flip();
        }
        long durationJni = System.nanoTime() - startJni;

        // 2. Teste via FFM (Foreign Function & Memory API - Java 21)
        long startFfm = System.nanoTime();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment nativeSegment = arena.allocate(dummyData.length);
            for (int i = 0; i < iterations; i++) {
                MemorySegment.copy(dummyData, 0, nativeSegment, ValueLayout.JAVA_BYTE, 0, dummyData.length);
            }
        }
        long durationFfm = System.nanoTime() - startFfm;

        System.out.printf("📦 Abordagem Tradicional (Buffer/JNI-style): %.2f ms\n", durationJni / 1_000_000.0);
        System.out.printf("⚡ Abordagem Moderna (FFM / Panama):     %.2f ms\n", durationFfm / 1_000_000.0);
        
        double speedup = (double) durationJni / durationFfm;
        if (speedup > 1.0) {
            System.out.printf("🏆 FFM foi %.2fx mais eficiente no gerenciamento de memória!\n", speedup);
        } else {
            System.out.printf("⚖️ Desempenho equivalente (Fator: %.2fx)\n", speedup);
        }
        System.out.println("==========================================");
    }
}
