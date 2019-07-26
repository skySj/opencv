#include <jni.h>
#include <string>
#include <Android/log.h>
#include <Android/asset_manager.h>
#include <Android/asset_manager_jni.h>

#include <opencv2/opencv.hpp>

//#define DEBUG
#define TAG "com.example.cvdemo.jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__)

using namespace cv;
using namespace std;

//检测颜色并画出轮廓
/*
    th:
    0 - hmin
    1 - hmax
    2 - smin
    3 - smax
    4 - vmin
    5 - vmax
*/
//参考：http://www.it1352.com/532885.html
void callbackColorDetect(Mat * src, Mat * res, int * th){
    Mat imgHSV, imgThresholded, imgTemp;
    imgTemp = *src;
    //to hsv
    cvtColor(imgTemp, imgHSV, COLOR_BGR2HSV);
    inRange(imgHSV, Scalar((*th), *(th+2), *(th+4)), Scalar(*(th+1), *(th+3), *(th+5)), imgThresholded);
    //开操作 (去除一些噪点)
    Mat element = getStructuringElement(MORPH_RECT, Size(5, 5));
    morphologyEx(imgThresholded, imgThresholded, MORPH_OPEN, element);
    //闭操作 (连接一些连通域)
    morphologyEx(imgThresholded, imgThresholded, MORPH_CLOSE, element);

    //找轮廓
    vector<vector <Point> >contours;
    findContours(imgThresholded,contours,RETR_TREE,CHAIN_APPROX_SIMPLE);
    //寻找最大轮廓
    double maxArea = 0;
    vector<Point> maxContour;
    Rect maxRect;
    for(size_t j = 0; j < contours.size(); j++)
    {
        double area = contourArea(contours[j]);
        if (area > maxArea)
        {
            maxArea = area;
            maxContour = contours[j];
            // 将轮廓转为矩形框
            maxRect = boundingRect(maxContour);
        }
    }
    //绘制轮廓
    rectangle(imgTemp, maxRect, Scalar(72,61,139), 5);
    *res = imgTemp;
}

extern "C" {

    JNIEXPORT void JNICALL
    Java_example_com_cvdemo_Utils_ImageUtils_colorDetect(
        JNIEnv* env,
        jobject,
        jlong src_mat,
        jlong res_mat,
        jintArray arr) {

        jint *th;
        th = env->GetIntArrayElements(arr, NULL);

    #if defined(DEBUG)
        LOGI("xxxx: %s", "=======================================");
        for(jint i=0;i<6;i++){
            LOGI("th: %d %s %d", i, "==", *(th+i));
        }
    #endif

        Mat * src = (Mat *) src_mat;
        Mat * res = (Mat *) res_mat;

    #if defined(DEBUG)
        LOGI("res size: %d %s %d", res->cols, "x", res->rows);
    #endif

        callbackColorDetect(src, res, th);

    }


    JNIEXPORT jstring JNICALL
    Java_example_com_cvdemo_Utils_ImageUtils_getVersion(
            JNIEnv* env,
            jobject /* this */) {
        std::string name = "OpenCV Version: ";
        std::string version = (name + CV_VERSION);
        return env->NewStringUTF(version.c_str());
    }

    JNIEXPORT jstring JNICALL
    Java_example_com_cvdemo_Utils_ImageUtils_stringFromJNI(
            JNIEnv* env,
            jobject /* this */) {

        std::string hello = "Hello from C++";
        return env->NewStringUTF(hello.c_str());
    }

}
