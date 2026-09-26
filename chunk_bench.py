import time
import os
import random
import lz4.frame
import zstandard as zstd
import zlib

def generate_mock_chunk_data(size_kb=64):
    chunk = bytearray()
    while len(chunk) < size_kb * 1024:
        if random.random() < 0.65:
            chunk.extend(b'\x00' * random.randint(64, 512))
        else:
            chunk.extend(os.urandom(random.randint(32, 256)))
    return bytes(chunk[:size_kb * 1024])

def run_benchmark():
    data = generate_mock_chunk_data(64)
    iterations = 2000
    
    print("=" * 60)
    print(f"🚀 BENCHMARK DE CHUNKS - ARM64 (Tamanho: {len(data)/1024:.1f} KB, Iterações: {iterations})")
    print("=" * 60)
    
    algorithms = {
        "Zlib (Padrão Ansi/MCA - Nível 6)": {
            "compress": lambda d: zlib.compress(d, level=6),
            "decompress": lambda d: zlib.decompress(d)
        },
        "LZ4 (Ultra Rápido - lz4-java)": {
            "compress": lambda d: lz4.frame.compress(d),
            "decompress": lambda d: lz4.frame.decompress(d)
        },
        "Zstd (Nível 1 - Rápido)": {
            "compress": lambda d: zstd.ZstdCompressor(level=1).compress(d),
            "decompress": lambda d: zstd.ZstdDecompressor().decompress(d)
        },
        "Zstd (Nível 3 - Balanceado)": {
            "compress": lambda d: zstd.ZstdCompressor(level=3).compress(d),
            "decompress": lambda d: zstd.ZstdDecompressor().decompress(d)
        }
    }
    
    for name, algo in algorithms.items():
        temp_comp = algo["compress"](data)
        algo["decompress"](temp_comp)
        
        start = time.perf_counter()
        for _ in range(iterations):
            compressed = algo["compress"](data)
        duration_comp = time.perf_counter() - start
        
        start = time.perf_counter()
        for _ in range(iterations):
            algo["decompress"](compressed)
        duration_decomp = time.perf_counter() - start
        
        ratio = (len(compressed) / len(data)) * 100
        speed_comp = (len(data) * iterations / (1024 * 1024)) / duration_comp
        speed_decomp = (len(data) * iterations / (1024 * 1024)) / duration_decomp
        
        print(f"📦 {name}")
        print(f"   Tamanho Comprimido : {len(compressed)/1024:.2f} KB ({ratio:.1f}% do original)")
        print(f"   Compressão         : {duration_comp:.4f}s | {speed_comp:.2f} MB/s")
        print(f"   Descompressão      : {duration_decomp:.4f}s | {speed_decomp:.2f} MB/s")
        print("-" * 60)

if __name__ == "__main__":
    run_benchmark()
