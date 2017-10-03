package xyz.yukisako.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Timer mSlideCounter;
    TextView mTimerText;
    Button mStartButton;
    Button mPauseButton;
    Button mResetbutton;
    ImageView mImageView;

    ArrayList<Uri> imageUriArray = new ArrayList<>();

    //カウンターの数字
    int mCountNum = 0;

    Handler mHandler = new Handler();

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimerText = (TextView) findViewById(R.id.timer);
        mStartButton = (Button) findViewById(R.id.start_button);
        mPauseButton = (Button) findViewById(R.id.pause_button);
        mResetbutton = (Button) findViewById(R.id.reset_button);
        mImageView = (ImageView) findViewById(R.id.imageView);
        //起動した時にパーミッションの許可状態を確認
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                //許可されている
                Log.d("ANDROID","許可されている");
                getContentsInfo();
            } else {
                Log.d("ANDROID","許可されていない");
                //ダイアログ表示
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSIONS_REQUEST_CODE);
            }
        } else {
            getContentsInfo();
        }

        mStartButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mSlideCounter == null){
                    //タイマーの作成
                    mSlideCounter = new Timer();
                    mSlideCounter.schedule(new TimerTask(){
                        @Override
                        public void run(){
                            mCountNum += 1;

                            mHandler.post(new Runnable(){
                                @Override
                                public void run(){
                                    int num = mCountNum%imageUriArray.size();
                                    mImageView.setImageURI(imageUriArray.get(num));
                                    mTimerText.setText(String.format("%d枚目を表示中",num+1));
                                }
                            });
                        }
                    },1000,1000); //最初に始動させるまで100ms，ループ感覚を100m
                }
            }
        });

        mPauseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mSlideCounter != null){
                    mSlideCounter.cancel();
                    mSlideCounter = null;
                }
            }
        });

        mResetbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mCountNum = 0;
                mImageView.setImageURI(imageUriArray.get(0));
                mTimerText.setText(String.format("%d枚目を表示中",mCountNum+1));
                if(mSlideCounter != null){
                    mSlideCounter.cancel();
                    mSlideCounter = null;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case PERMISSIONS_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo(){
        //画像の情報を取得
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if(cursor.moveToFirst()){
            do{
                //indexからIDを取得
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageURi = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id);

                Log.d("ANDROID","URI:"+imageURi.toString());
                imageUriArray.add(imageURi);

            } while(cursor.moveToNext());
        }
        mImageView.setImageURI(imageUriArray.get(0));
        cursor.close();
    }
}
