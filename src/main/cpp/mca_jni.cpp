#include <jni.h>
#include <cstdint>

extern "C" int64_t mca_open(const char* path);
extern "C" int32_t mca_read(int64_t handle, int32_t chunkX, int32_t chunkZ, uint8_t* output, uint32_t outputCapacity);
extern "C" void mca_close(int64_t handle);

extern "C" JNIEXPORT jlong JNICALL Java_com_denonexus_mgshaders_nativebridge_McaNativeLoader_open(
    JNIEnv* env, jclass, jstring path
) {
    if (!path) return 0;
    const char* utf = env->GetStringUTFChars(path, nullptr);
    if (!utf) return 0;
    int64_t handle = mca_open(utf);
    env->ReleaseStringUTFChars(path, utf);
    return static_cast<jlong>(handle);
}

extern "C" JNIEXPORT jint JNICALL Java_com_denonexus_mgshaders_nativebridge_McaNativeLoader_read(
    JNIEnv* env, jclass, jlong handle, jint chunkX, jint chunkZ, jobject outputBuffer, jint capacity
) {
    if (!outputBuffer) return -10;
    void* address = env->GetDirectBufferAddress(outputBuffer);
    jlong bufferCapacity = env->GetDirectBufferCapacity(outputBuffer);
    
    if (!address || bufferCapacity <= 0) return -10;
    
    if (capacity <= 0 || static_cast<jlong>(capacity) > bufferCapacity) {
        capacity = static_cast<jint>(bufferCapacity);
    }

    return static_cast<jint>(
        mca_read(
            static_cast<int64_t>(handle),
            static_cast<int32_t>(chunkX),
            static_cast<int32_t>(chunkZ),
            static_cast<uint8_t*>(address),
            static_cast<uint32_t>(capacity)
        )
    );
}

extern "C" JNIEXPORT void JNICALL Java_com_denonexus_mgshaders_nativebridge_McaNativeLoader_close(
    JNIEnv*, jclass, jlong handle
) {
    mca_close(static_cast<int64_t>(handle));
}
