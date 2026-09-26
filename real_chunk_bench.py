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
        print(f"❌ Diretório não encontrado: {region_dir}")
        return chunks
    
    files = [f for f in os.listdir(region_dir) if f.endswith('.mca')]
    if not files:
        print("❌ Nenhum arquivo .mca encontrado na pasta do mundo.")
        return chunks
        
    mca_path = os.path.join(region_dir, files[0])
    print(f"📂 Lendo chunks reais do arquivo: {files[0]}...")
    
    with open(mca_path, "rb") as f:
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
                if compression_type == 1: # GZip
                    import gzip
                    raw_data = gzip.decompress(data)
                elif compression_type == 2: # Zlib / Deflate
                    raw_data = zlib.decompress(data)
                elif compression_type == 3: # Uncompressed
                    raw_data = data
                elif compression_type == 4: # LZ4
                    raw_data = lz4.frame.decompress(data)
                else:
                    continue
                chunks.append(raw_data)
                if len(chunks) >= 15: # Coletar até 15 chunks reais
                    break
            except Exception:
                continue
                
    return chunks

def run_real_benchmark():
    chunks = extract_real_chunks()
    if not chunks:
        print("⚠️ Nenhum chunk válido foi extraído.")
        return
        
    print(f"\n🚀 Sucesso! {len(chunks)} chunks REAIS carregados do save.")
    print("=" * 60)
    
    algorithms = {
        "Zlib (Padrão MCA)": {
            "compress": lambda d: zlib.compress(d, level=6),
            "decompress": lambda d: zlib.decompress(d)
        },
        "LZ4 (Ultra Rápido)": {
            "compress": lambda d: lz4.frame.compress(d),
            "decompress": lambda d: lz4.frame.decompress(d)
        },
        "Zstd (Nível 1)": {
            "compress": lambda d: zstd.ZstdCompressor(level=1).compress(d),
            "decompress": lambda d: zstd.ZstdDecompressor().decompress(d)
        },
        "Zstd (Nível 3)": {
            "compress": lambda d: zstd.ZstdCompressor(level=3).compress(d),
            "decompress": lambda d: zstd.ZstdDecompressor().decompress(d)
        }
    }
    
    for name, algo in algorithms.items():
        total_comp_time = 0
        total_decomp_time = 0
        total_compressed_size = 0
        total_original_size = 0
        iterations = 100
        
        for chunk in chunks:
            original_size = len(chunk)
            total_original_size += original_size * iterations
            
            # Aquecimento
            comp_data = algo["compress"](chunk)
            
            # Teste de Compressão
            start = time.perf_counter()
            for _ in range(iterations):
                comp_data = algo["compress"](chunk)
            total_comp_time += time.perf_counter() - start
            total_compressed_size += len(comp_data) * iterations
            
            # Teste de Descompressão
            start = time.perf_counter()
            for _ in range(iterations):
                algo["decompress"](comp_data)
            total_decomp_time += time.perf_counter() - start
            
        avg_ratio = (total_compressed_size / total_original_size) * 100
        speed_comp = (total_original_size / (1024 * 1024)) / total_comp_time
        speed_decomp = (total_original_size / (1024 * 1024)) / total_decomp_time
        
        print(f"📦 {name}")
        print(f"   Tamanho Médio Comprimido: {total_compressed_size / (len(chunks) * iterations) / 1024:.2f} KB ({avg_ratio:.1f}% do original)")
        print(f"   Velocidade Compressão   : {speed_comp:.2f} MB/s")
        print(f"   Velocidade Descompressão: {speed_decomp:.2f} MB/s")
        print("-" * 60)

if __name__ == "__main__":
    run_real_benchmark()
