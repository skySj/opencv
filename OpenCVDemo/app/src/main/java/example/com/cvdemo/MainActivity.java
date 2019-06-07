package example.com.cvdemo;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.InputStream;

import static android.graphics.Bitmap.Config.ARGB_8888;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //按钮
    private Button action_rotate;
    private Button action_togray;
    private Button action_contours;
    private Button action_getVersion;
    private Button action_select;
    //滑条
    private SeekBar seb_l;
    private SeekBar seb_h;
    //ImageView
    private ImageView img_iv;
    //Bitmap
    private Bitmap src_bp;
    private Bitmap res_bp;
    //阈值
    private int th1_val;
    private int th2_val;
    //图片相关
    private int rotate_dir = 1; //转动方向 // 1:左 2:上 3:右 4:下
    private Mat srcBitmapMat = new Mat();
    private Mat resBitmapMat = new Mat();
    private double max_size = 1024;
    private int PICK_IMAGE_REQUEST = 1;


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    //OpenCV库静态加载并初始化
    private void staticLoadCVLibraries(){
        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.i("CV", "Open CV Libraries loaded...");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setListener();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.action_select:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");//设置类型
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                break;
            case R.id.action_rotate:
                rotatePicture();
                break;
            case R.id.action_togray:
                jni_image2gray();
                break;
            case R.id.action_contours:
                jni_extract(th1_val, th2_val);
                break;
            case R.id.action_getVersion:
                Toast.makeText(this,getVersion(),Toast.LENGTH_SHORT).show();
                break;

        }
    }

    SeekBar.OnSeekBarChangeListener osbcl = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if(seekBar.getId() == R.id.threshold1){
                th1_val = seekBar.getProgress();
            }else if(seekBar.getId() == R.id.threshold2){
                th2_val = seekBar.getProgress();
            }else{
                //
            }
            //通过阈值调整图像
            jni_extract(th1_val, th2_val);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
//            Toast.makeText(getApplicationContext(), "onStartTrackingTouch",
//                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
//            Toast.makeText(getApplicationContext(), "onStopTrackingTouch",
//                    Toast.LENGTH_SHORT).show();
        }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Log.d("image-tag", "start to decode selected image now...");
                InputStream input = getContentResolver().openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(input, null, options);
                int raw_width = options.outWidth;
                int raw_height = options.outHeight;
                int max = Math.max(raw_width, raw_height);
                int newWidth = raw_width;
                int newHeight = raw_height;
                int inSampleSize = 1;
                if(max > max_size) {
                    newWidth = raw_width / 2;
                    newHeight = raw_height / 2;
                    while((newWidth/inSampleSize) > max_size || (newHeight/inSampleSize) > max_size) {
                        inSampleSize *=2;
                    }
                }

                options.inSampleSize = inSampleSize;
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = ARGB_8888;
                src_bp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);

                img_iv.setImageBitmap(src_bp);
                res_bp = null;
                jni_extract(th1_val, th2_val);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initView(){
        action_rotate = (Button)findViewById(R.id.action_rotate);
        action_togray = (Button)findViewById(R.id.action_togray);
        action_contours = (Button)findViewById(R.id.action_contours);
        action_getVersion = (Button)findViewById(R.id.action_getVersion);
        action_select = (Button)findViewById(R.id.action_select);

        img_iv = (ImageView)findViewById(R.id.img_view);
        img_iv.setImageResource(R.drawable.bg);

        seb_l = (SeekBar)findViewById(R.id.threshold1);
        seb_h = (SeekBar)findViewById(R.id.threshold2);

    }

    public void setListener(){
        action_rotate.setOnClickListener(this);
        action_togray.setOnClickListener(this);
        action_contours.setOnClickListener(this);
        action_getVersion.setOnClickListener(this);
        action_select.setOnClickListener(this);

        seb_l.setOnSeekBarChangeListener(osbcl);
        seb_h.setOnSeekBarChangeListener(osbcl);
    }

    //图片处理。。
    //转灰度图
    private void jni_image2gray(){
        try{
            Utils.bitmapToMat(src_bp,srcBitmapMat);
            if(res_bp==null){
                res_bp = Bitmap.createBitmap(src_bp.getWidth(),src_bp.getHeight(),ARGB_8888);
            }else{
                image2gray(srcBitmapMat.nativeObj, resBitmapMat.nativeObj);
                Utils.matToBitmap(resBitmapMat,res_bp);
                img_iv.setImageDrawable(bitmap2Drawable(res_bp));
            }
        }catch (Exception e){
            Toast.makeText(this, "请选择一张图片", Toast.LENGTH_SHORT).show();
        }
    }

    //提取轮廓
    private void jni_extract(int a, int b){
        try{
            Utils.bitmapToMat(src_bp,srcBitmapMat);
            if(res_bp==null){
                res_bp = Bitmap.createBitmap(src_bp.getWidth(),src_bp.getHeight(),ARGB_8888);
            }else{
                imageextract(srcBitmapMat.nativeObj, resBitmapMat.nativeObj, a, b);
                Utils.matToBitmap(resBitmapMat,res_bp);
                img_iv.setImageDrawable(bitmap2Drawable(res_bp));
            }
        }catch (Exception e){
            //Toast.makeText(this, "请选择一张图片", Toast.LENGTH_SHORT).show();
        }

    }

    //图片旋转
    private void rotatePicture(){
        if(rotate_dir>4){
            rotate_dir=1;
        }
        switch (rotate_dir){
            case 1: reversePicture(0);break;
            case 2: reversePicture(1);break;
            case 3: reversePicture(0);break;
            case 4: reversePicture(1);break;
        }
        rotate_dir++;
    }

    //图片反转
    private void reversePicture(int flag){
        try{
            Drawable curImageDrawable=bitmap2Drawable(src_bp);
            Bitmap reversedBitmap=MyImageProcessor.reversePicture(curImageDrawable,flag);
            Drawable reversePicture=new BitmapDrawable(this.getResources(),reversedBitmap);
            img_iv.setImageDrawable(reversePicture);
            src_bp = drawable2Bitmap(reversePicture);
        }catch (Exception e){
            Toast.makeText(this, "请选择一张图片", Toast.LENGTH_SHORT).show();
        }

    }

    //drawble转换为bitmap
    Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

    //bitmap转换为drawable
    Drawable bitmap2Drawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

    public Bitmap getBitmap(Uri url) {
        try{
            Bitmap photoBmp;
            if (url !=null) {
                ContentResolver mContentResolver = this.getContentResolver();
                photoBmp = MediaStore.Images.Media.getBitmap(mContentResolver, url);
                return photoBmp;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void image2gray(long src_mat, long res_mat);
    public native void imageextract(long src_mat, long res_mat, int th1, int th2);
    public native String stringFromJNI();
    public native String getVersion();
}
