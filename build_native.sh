#!/bin/bash
echo "🔨 Compilando libcompress-decompress.so para ARM64 com LZ4 real..."

JAVA_HOME_PATH=$(dirname $(dirname $(readlink -f $(which javac))))
JNI_INCLUDE="-I${JAVA_HOME_PATH}/include -I${JAVA_HOME_PATH}/include/linux"
LZ4_INC="-I/data/data/com.termux/files/usr/include"
LZ4_LIB="/data/data/com.termux/files/usr/lib/liblz4.so"

/tmp/tc/clang++ -O3 -shared -fPIC \
    -std=gnu++17 \
    -march=armv8-a+simd \
    -ftree-vectorize \
    -ffast-math \
    -fvisibility=hidden \
    ${JNI_INCLUDE} ${LZ4_INC} \
    native_lz4.cpp \
    -Wl,--gc-sections -Wl,--exclude-libs,ALL -nostdlib++ \
    $HOME/hashfix.o $HOME/assertfix.o \
    $HOME/libc++_static.a $HOME/libc++abi.a $HOME/libunwind.a \
    ${LZ4_LIB} \
    -o libcompress-decompress.so

if [ -f "libcompress-decompress.so" ]; then
    echo "✅ SUCESSO"
    ls -lh libcompress-decompress.so
    readelf -d libcompress-decompress.so | grep NEEDED
else
    echo "❌ ERRO na compilação."
fi
