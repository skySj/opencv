#include <jni.h>
#include <string>
#include <Android/log.h>
#include <Android/asset_manager.h>
#include <Android/asset_manager_jni.h>

#include <opencv2/opencv.hpp>

#define TAG "com.example.cvdemo.jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__)

using namespace cv;
using namespace std;

extern "C" {
    JNIEXPORT jstring JNICALL
    Java_example_com_cvdemo_MainActivity_getVersion(
            JNIEnv* env,
            jobject /* this */) {
        std::string name = "OpenCV Version: ";
        std::string version = (name + CV_VERSION);
        return env->NewStringUTF(version.c_str());
    }

    JNIEXPORT jstring JNICALL
    Java_example_com_cvdemo_MainActivity_stringFromJNI(
            JNIEnv* env,
            jobject /* this */) {

        std::string hello = "Hello from C++";
        return env->NewStringUTF(hello.c_str());
    }

}
