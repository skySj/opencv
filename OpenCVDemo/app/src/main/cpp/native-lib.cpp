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

//图片处理
class ImageUtils{
public:
    //转灰度
    void imageToGray(Mat *, Mat *);
    //提取轮廓
    void imageExtract(Mat *, Mat *, int, int);

private:


};

void ImageUtils::imageToGray(Mat * src, Mat * res){
    //打印相关信息
    //LOGI("OpenCV version: %s", CV_VERSION);
    //LOGI("mat size: %d %s %d", src->cols,"x",src->rows);

    //灰度转换
    cvtColor(*src, *res, COLOR_BGR2GRAY);
}

void ImageUtils::imageExtract(Mat * src, Mat * res, int _th1, int _th2){
    //灰度转换
    ImageUtils::imageToGray(src, res);

    //findcontours
    cv::Canny(*res,*res,_th1, _th2);
    cv::threshold(*res,*res, 127, 255,cv::THRESH_BINARY_INV);
    //std::vector<std::vector<Point>> contours;
    //std::vector<Vec4i> hierarchy;
    //findContours(*src, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE, Point(0, 0));
}


extern "C" {

    JNIEXPORT void JNICALL
        Java_example_com_cvdemo_MainActivity_image2gray(
            JNIEnv* env,
            jobject, /* this */
            jlong src_matPtr,
            jlong res_matPtr) {

        Mat *src_mat = (Mat*)src_matPtr;
        Mat *res_mat = (Mat*)res_matPtr;
        ImageUtils().imageToGray(src_mat, res_mat);
    }

    JNIEXPORT void JNICALL
        Java_example_com_cvdemo_MainActivity_imageextract(
            JNIEnv* env,
            jobject, /* this */
            jlong src_matPtr,
            jlong res_matPtr,
            jint th1,
            jint th2) {

        Mat *src_mat = (Mat*)src_matPtr;
        Mat *res_mat = (Mat*)res_matPtr;
        ImageUtils().imageExtract(src_mat, res_mat, th1, th2);
    }

    JNIEXPORT jstring JNICALL
    Java_example_com_cvdemo_MainActivity_getVersion(
            JNIEnv* env,
            jobject /* this */) {
        std::string name = "OpenCV: ";
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
