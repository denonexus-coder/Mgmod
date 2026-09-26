import com.denonexus.mgshaders.nativebridge.NativeChunkLoader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class TestLz4Jni {
    public static void main(String[] args) {
        int rawChunkSize = 64 * 1024;
        byte[] mockChunk = generateMockChunk(rawChunkSize);
        ByteBuffer srcBuffer = ByteBuffer.allocateDirect(mockChunk.length).order(ByteOrder.nativeOrder());
        ByteBuffer dstBuffer = ByteBuffer.allocateDirect(rawChunkSize * 2).order(ByteOrder.nativeOrder());
        srcBuffer.put(mockChunk); srcBuffer.flip();

        for (int i = 0; i < 1000; i++)
            NativeChunkLoader.decompressLZ4Direct(srcBuffer, 0, mockChunk.length, dstBuffer, 0, dstBuffer.capacity());

        long start = System.nanoTime();
        int iterations = 500_000;
        for (int i = 0; i < iterations; i++)
            NativeChunkLoader.decompressLZ4Direct(srcBuffer, 0, mockChunk.length, dstBuffer, 0, dstBuffer.capacity());
        long durationNs = System.nanoTime() - start;

        double totalMs = durationNs / 1_000_000.0;
        double totalDataMB = (double)(rawChunkSize * (long) iterations) / (1024 * 1024);
        System.out.printf("Tempo: %.2f ms | Throughput: %.2f MB/s | Latencia: %.3f us%n",
            totalMs, totalDataMB / (totalMs / 1000.0), (totalMs * 1000.0) / iterations);
    }

    private static byte[] generateMockChunk(int size) {
        byte[] data = new byte[size];
        Random rand = new Random(42);
        for (int i = 0; i < size; i++) data[i] = (i % 10 == 0) ? (byte) rand.nextInt(256) : 0;
        return data;
    }
}
