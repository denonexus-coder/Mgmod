import com.denonexus.mgshaders.nativebridge.NativeChunkLoader;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class TestLz4RealChunks {
    public static void main(String[] args) throws IOException {
        String path = "/root/Mgmod/real_chunks.bin";
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));

        int numChunks = Integer.reverseBytes(in.readInt());
        System.out.println("Chunks reais no arquivo: " + numChunks);

        List<byte[]> rawList = new ArrayList<>();
        List<byte[]> compList = new ArrayList<>();
        for (int i = 0; i < numChunks; i++) {
            int rawLen = Integer.reverseBytes(in.readInt());
            int compLen = Integer.reverseBytes(in.readInt());
            byte[] comp = new byte[compLen];
            in.readFully(comp);
            byte[] raw = new byte[rawLen];
            rawList.add(raw);
            compList.add(comp);
        }
        in.close();

        // Recarrega raw de verdade só pra comparar depois (precisamos regravar do bin? nao temos raw salvo)
        // Vamos regenerar via decompress no proprio java lz4 nao disponivel aqui; comparamos so por decompressao nativa != erro
        int maxRaw = 0;
        for (byte[] r : rawList) maxRaw = Math.max(maxRaw, r.length);

        ByteBuffer srcBuffer = ByteBuffer.allocateDirect(1 << 20).order(ByteOrder.nativeOrder());
        ByteBuffer dstBuffer = ByteBuffer.allocateDirect(maxRaw + 1024).order(ByteOrder.nativeOrder());

        // Warmup + verificacao de erro (retorno negativo = falha na descompressao)
        int erros = 0;
        for (int i = 0; i < numChunks; i++) {
            byte[] comp = compList.get(i);
            int rawLen = rawList.get(i).length;
            srcBuffer.clear(); srcBuffer.put(comp); srcBuffer.flip();
            dstBuffer.clear();
            int result = NativeChunkLoader.decompressLZ4Direct(srcBuffer, 0, comp.length, dstBuffer, 0, dstBuffer.capacity());
            if (result != rawLen) {
                erros++;
                System.out.printf("Chunk %d: esperado %d bytes, obteve %d%n", i, rawLen, result);
            }
        }
        System.out.println("Chunks com erro de tamanho: " + erros + " / " + numChunks);

        // Benchmark de throughput real (repetindo o conjunto de chunks reais)
        int iterations = 20000;
        long totalBytes = 0;
        long start = System.nanoTime();
        for (int it = 0; it < iterations; it++) {
            for (int i = 0; i < numChunks; i++) {
                byte[] comp = compList.get(i);
                srcBuffer.clear(); srcBuffer.put(comp); srcBuffer.flip();
                dstBuffer.clear();
                NativeChunkLoader.decompressLZ4Direct(srcBuffer, 0, comp.length, dstBuffer, 0, dstBuffer.capacity());
                totalBytes += rawList.get(i).length;
            }
        }
        long durationNs = System.nanoTime() - start;
        double totalMs = durationNs / 1_000_000.0;
        double totalMB = totalBytes / (1024.0 * 1024.0);
        double perChunkUs = (totalMs * 1000.0) / (iterations * (long) numChunks);

        System.out.printf("Total: %.2f ms | %.2f MB | %.2f MB/s | %.3f us/chunk%n",
            totalMs, totalMB, totalMB / (totalMs / 1000.0), perChunkUs);
    }
}
