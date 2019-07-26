package example.com.cvdemo.Track;

import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import example.com.cvdemo.R;
import example.com.cvdemo.Utils.ImageUtils;

public class Track extends AppCompatActivity implements View.OnClickListener{

    private ImageUtils myImageUtils;
    private ImageView img_iv_track;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cv_track);

        img_iv_track = (ImageView)findViewById(R.id.img_cv_track);
        img_iv_track.setImageResource(R.drawable.bg);

    }

    @Override
    public void onClick(View v){

    }

}