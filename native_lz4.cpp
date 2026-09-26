#include <jni.h>
#include <lz4.h>
#include <cstring>
#include <cstdlib>

extern "C" {

JNIEXPORT jint JNICALL Java_com_denonexus_mgshaders_nativebridge_NativeChunkLoader_decompressLZ4Direct(
    JNIEnv* env, jclass clazz,
    jobject srcBuf, jint srcOff, jint srcLen,
    jobject dstBuf, jint dstOff, jint maxDstLen) {

    if (!srcBuf || !dstBuf) return -1;

    char* src = static_cast<char*>(env->GetDirectBufferAddress(srcBuf));
    char* dst = static_cast<char*>(env->GetDirectBufferAddress(dstBuf));

    if (!src || !dst) return -2;

    int decompressed = LZ4_decompress_safe(src + srcOff, dst + dstOff, srcLen, maxDstLen);
    return (decompressed < 0) ? -3 : decompressed;
}

JNIEXPORT jint JNICALL Java_com_denonexus_mgshaders_nativebridge_NativeChunkLoader_compressLZ4Direct(
    JNIEnv* env, jclass clazz,
    jobject srcBuf, jint srcOff, jint srcLen,
    jobject dstBuf, jint dstOff, jint maxDstLen) {

    if (!srcBuf || !dstBuf) return -1;

    char* src = static_cast<char*>(env->GetDirectBufferAddress(srcBuf));
    char* dst = static_cast<char*>(env->GetDirectBufferAddress(dstBuf));

    if (!src || !dst) return -2;

    int compressed = LZ4_compress_default(src + srcOff, dst + dstOff, srcLen, maxDstLen);
    return (compressed <= 0) ? -3 : compressed;
}

}
