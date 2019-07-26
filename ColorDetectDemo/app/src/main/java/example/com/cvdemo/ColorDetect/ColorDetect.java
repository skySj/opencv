package example.com.cvdemo.ColorDetect;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.InputStream;

import example.com.cvdemo.MainActivity;
import example.com.cvdemo.R;
import example.com.cvdemo.Utils.ImageUtils;

import static android.graphics.Bitmap.Config.ARGB_4444;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.Config.RGB_565;

public class ColorDetect extends AppCompatActivity{
    private static final String TAG = "ColorDetectActivity";
    private ImageView img_iv_color;
    private TextView info_seekbar_tv;

    //Bitmap
    private Bitmap src_bp; //输入图像
    private Bitmap res_bp; //输出图像
    private Mat srcBitmapMat = new Mat();
    private Mat resBitmapMat = new Mat();
    private double max_size = 1024;
    private int PICK_IMAGE_REQUEST = 1;

    //记录阈值
    private int th[] = {
        45,85,160,255,120,255 //初始化滑动条数值
    };

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cv_color);

        Toast.makeText(this,"请选择一张图片",Toast.LENGTH_SHORT).show();

        img_iv_color = (ImageView)findViewById(R.id.img_cv_color);
        img_iv_color.setImageResource(R.drawable.bg);

        info_seekbar_tv = (TextView)findViewById(R.id.info_bar_process);

        SeekBar minHbar = (SeekBar)findViewById(R.id.h_min_th);
        SeekBar maxHbar = (SeekBar)findViewById(R.id.h_max_th);

        SeekBar minSbar = (SeekBar)findViewById(R.id.s_min_th);
        SeekBar maxSbar = (SeekBar)findViewById(R.id.s_max_th);

        SeekBar minVbar = (SeekBar)findViewById(R.id.v_min_th);
        SeekBar maxVbar = (SeekBar)findViewById(R.id.v_max_th);

        minHbar.setOnSeekBarChangeListener(osbcl);
        maxHbar.setOnSeekBarChangeListener(osbcl);
        minSbar.setOnSeekBarChangeListener(osbcl);
        maxSbar.setOnSeekBarChangeListener(osbcl);
        minVbar.setOnSeekBarChangeListener(osbcl);
        maxVbar.setOnSeekBarChangeListener(osbcl);

        minHbar.setProgress(th[0]);
        maxHbar.setProgress(th[1]);
        minSbar.setProgress(th[2]);
        maxSbar.setProgress(th[3]);
        minVbar.setProgress(th[4]);
        maxVbar.setProgress(th[5]);


    }


    // 设置拖动条改变监听器
    SeekBar.OnSeekBarChangeListener osbcl = new SeekBar.OnSeekBarChangeListener() {
        public int index;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {

            int tp = seekBar.getProgress();
            switch (seekBar.getId()){
                case R.id.h_min_th:
                    th[0] = tp; //h_min
                    index = 0;
                    break;
                case R.id.h_max_th:
                    th[1] = tp; //h_max
                    index = 1;
                    break;
                case R.id.s_min_th:
                    th[2] = tp; //s_min
                    index = 2;
                    break;
                case R.id.s_max_th:
                    th[3] = tp; //s_max
                    index = 3;
                    break;
                case R.id.v_min_th:
                    th[4] = tp; //v_min
                    index = 4;
                    break;
                case R.id.v_max_th:
                    th[5] = tp; //v_max
                    index = 5;
                    break;
            }

            //通过阈值调整图像
            inputImage();
            //显示数值
            showSeekBarProcess();
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

            //Log.i(TAG,"index: " + index + " value: " + th[index]);

        }

    };

    //菜单选项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_color, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.item_0:
                selectPicture();
                break;
            case R.id.item_1:
                openCamera();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

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
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                src_bp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);

                img_iv_color.setImageBitmap(src_bp);
                //res_bp = null;
                inputImage();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void inputImage(){

        try{
            Utils.bitmapToMat(src_bp,srcBitmapMat);
            if(res_bp==null){
                res_bp = Bitmap.createBitmap(src_bp.getWidth(),src_bp.getHeight(),RGB_565);
            }else{
                ImageUtils.colorDetect(srcBitmapMat.nativeObj, resBitmapMat.nativeObj, th);
                Utils.matToBitmap(resBitmapMat,res_bp);
                img_iv_color.setImageDrawable(ImageUtils.bitmap2Drawable(res_bp));
            }
        }catch (Exception e){
            //Toast.makeText(this,"请选择一张图片",Toast.LENGTH_SHORT).show();
        }

    }

    public void showSeekBarProcess(){
        String info = "minH: " + th[0] +
                      " minS: " + th[2] +
                      " minV: " + th[4] +
                      "\n" +
                      "maxH: " + th[1] +
                      " maxS: " + th[3] +
                      " maxV: " + th[5];
        info_seekbar_tv.setText(info);
    }

    public void selectPicture(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");//设置类型
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    public void openCamera(){

    }
}
