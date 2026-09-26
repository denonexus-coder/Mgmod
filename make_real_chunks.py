import os, struct, zlib, gzip
import lz4.frame
import lz4.block

region_dir = "/storage/emulated/0/com.movtery.zalithlauncher.v2/files/.minecraft/versions/1.21.11 Fabric 0.19.3/saves/flat_r200/region/"
out_path = os.path.expanduser("~/Mgmod/real_chunks.bin")

def extract_real_chunks(max_chunks=30):
    chunks = []
    files = [f for f in os.listdir(region_dir) if f.endswith('.mca')]
    if not files:
        print("Nenhum .mca encontrado.")
        return chunks
    for fn in files:
        path = os.path.join(region_dir, fn)
        with open(path, "rb") as f:
            header = f.read(4096)
            for i in range(1024):
                e = header[i*4:i*4+4]
                offset = (e[0] << 16) | (e[1] << 8) | e[2]
                sectors = e[3]
                if offset == 0 or sectors == 0:
                    continue
                f.seek(offset * 4096)
                lb = f.read(4)
                if len(lb) < 4:
                    continue
                length = struct.unpack(">I", lb)[0]
                ctype = f.read(1)[0]
                data = f.read(length - 1)
                try:
                    if ctype == 1:
                        raw = gzip.decompress(data)
                    elif ctype == 2:
                        raw = zlib.decompress(data)
                    elif ctype == 3:
                        raw = data
                    elif ctype == 4:
                        raw = lz4.frame.decompress(data)
                    else:
                        continue
                    chunks.append(raw)
                    if len(chunks) >= max_chunks:
                        return chunks
                except Exception:
                    continue
    return chunks

chunks = extract_real_chunks()
print(f"{len(chunks)} chunks reais extraidos.")

# Formato do arquivo binario:
# [uint32 num_chunks]
# por chunk: [uint32 raw_len][uint32 comp_len][bytes comp_data (LZ4 block)]
with open(out_path, "wb") as out:
    out.write(struct.pack("<I", len(chunks)))
    total_raw = 0
    total_comp = 0
    for raw in chunks:
        comp = lz4.block.compress(raw, store_size=False)
        out.write(struct.pack("<II", len(raw), len(comp)))
        out.write(comp)
        total_raw += len(raw)
        total_comp += len(comp)

print(f"Tamanho medio original: {total_raw/len(chunks)/1024:.1f} KB")
print(f"Tamanho medio comprimido: {total_comp/len(chunks)/1024:.1f} KB")
print(f"Arquivo salvo: {out_path}")
