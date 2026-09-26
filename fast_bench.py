import os
import struct
import time
import lz4.frame
import zstandard as zstd
import zlib

region_dir = "/storage/emulated/0/com.movtery.zalithlauncher.v2/files/.minecraft/versions/1.21.11 Fabric 0.19.3/saves/flat_r200/region/"

def extract_real_chunks():
    chunks = []
    if not os.path.exists(region_dir):
        return chunks
    files = [f for f in os.listdir(region_dir) if f.endswith('.mca')]
    if not files:
        return chunks
    
    with open(os.path.join(region_dir, files[0]), "rb") as f:
        header = f.read(4096)
        for i in range(1024):
            offset_entry = header[i*4:i*4+4]
            offset = (offset_entry[0] << 16) | (offset_entry[1] << 8) | offset_entry[2]
            sector_count = offset_entry[3]
            if offset == 0 or sector_count == 0:
                continue
            f.seek(offset * 4096)
            length_bytes = f.read(4)
            if len(length_bytes) < 4:
                continue
            length = struct.unpack(">I", length_bytes)[0]
            compression_type = f.read(1)[0]
            data = f.read(length - 1)
            try:
                if compression_type == 2:
                    raw_data = zlib.decompress(data)
                elif compression_type == 3:
                    raw_data = data
                elif compression_type == 4:
                    raw_data = lz4.frame.decompress(data)
                else:
                    continue
                chunks.append(raw_data)
                if len(chunks) >= 15:
                    break
            except Exception:
                continue
    return chunks

def run_test():
    chunks = extract_real_chunks()
    if not chunks:
        print("⚠️ Nenhum chunk encontrado.")
        return
        
    print(f"🚀 Testando {len(chunks)} chunks reais no ARM64...\n" + "="*50)
    
    algos = {
        "LZ4 (Frame)": (lambda d: lz4.frame.compress(d), lambda d: lz4.frame.decompress(d)),
        "Zstd (Nível 1)": (lambda d: zstd.ZstdCompressor(level=1).compress(d), lambda d: zstd.ZstdDecompressor().decompress(d)),
        "Zlib (Nível 1 - Rápido)": (lambda d: zlib.compress(d, level=1), lambda d: zlib.decompress(d))
    }
    
    for name, (comp, decomp) in algos.items():
        iterations = 200
        start = time.perf_counter()
        for _ in range(iterations):
            for c in chunks:
                decomp(comp(c))
        duration = time.perf_counter() - start
        print(f"📦 {name}: Ciclos completos (Comp+Decomp) em {duration:.4f}s")

if __name__ == "__main__":
    run_test()
