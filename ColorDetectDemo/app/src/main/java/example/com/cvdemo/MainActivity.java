package example.com.cvdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import example.com.cvdemo.ColorDetect.ColorDetect;
import example.com.cvdemo.Track.Track;
import example.com.cvdemo.Utils.ImageUtils;

public class MainActivity extends AppCompatActivity{

    private ImageUtils myImageUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    //跳转到颜色检测页面
    public void toColorPage(View v){
        Intent intent = new Intent(MainActivity.this,ColorDetect.class);
        startActivityForResult(intent, 1);
    }

    //跳转到目标跟踪界面
    public void toTrackPage(View v){
        Intent intent = new Intent(MainActivity.this,Track.class);
        startActivityForResult(intent, 2);
    }

    /**
     * 点击查看opencv版本
     */
    public void showVersion(View v){
        Toast.makeText(this, myImageUtils.getVersion(),Toast.LENGTH_SHORT).show();
    }

}
