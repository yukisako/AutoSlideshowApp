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
    Button mNextButton;
    Button mBackbutton;
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
        mNextButton = (Button) findViewById(R.id.next_button);
        mBackbutton = (Button) findViewById(R.id.back_button);
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
                //画像の取得に成功しているかつ画像が1枚以上あったらタイマーを作動
                if(imageUriArray.size() != 0){
                    if(mSlideCounter == null) {
                        //タイマーの作成
                        mSlideCounter = new Timer();
                        //カウンターを作成したら，ボタンを停止に変える
                        mStartButton.setText("停止");

                        mSlideCounter.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mCountNum += 1;

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        int num = mCountNum % imageUriArray.size();
                                        mImageView.setImageURI(imageUriArray.get(num));
                                        mTimerText.setText(String.format("%d枚目を表示中", num + 1));
                                    }
                                });
                            }
                        }, 2000, 2000);
                    } else {
                        //カウンターを止める
                        mSlideCounter.cancel();
                        mSlideCounter = null;
                        mStartButton.setText("再生");
                    }
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //画像の取得に成功しているかつ画像が1枚以上あったら次へ
                if(imageUriArray.size() != 0){
                    mCountNum += 1;
                    int num = mCountNum % imageUriArray.size();
                    mImageView.setImageURI(imageUriArray.get(num));
                    mTimerText.setText(String.format("%d枚目を表示中", num + 1));
                } else {
                    mTimerText.setText(String.format("写真へのアクセスを許可した後に，画像を1枚以上追加してください"));
                }
            }
        });

        mBackbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //画像の取得に成功しているかつ画像が1枚以上あったら戻る
                if(imageUriArray.size() != 0){
                    mCountNum -= 1;
                    int num = mCountNum % imageUriArray.size();
                    mImageView.setImageURI(imageUriArray.get(num));
                    mTimerText.setText(String.format("%d枚目を表示中", num + 1));
                } else {
                    mTimerText.setText(String.format("写真へのアクセスを許可した後に，画像を1枚以上追加してください"));
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
                mTimerText.setText(String.format("写真へのアクセスを許可してください"));
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
