package com.example.user.drowsinessdetection;


import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import static org.opencv.imgproc.Imgproc.cvtColor;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{

    // Used to load the 'native-lib' library on application startup.
    JavaCameraView javaCameraView;
    Mat matInput,matOutput;
    int cnt=0,k=0,x;
    MediaPlayer mp;


    public static final String TAG="MainActivity";

    static {
        System.loadLibrary("MyLibs");
    }
    BaseLoaderCallback mLoaderCallBack=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    javaCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
String mCamera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        javaCameraView =(JavaCameraView) findViewById(R.id.javaCameraView);
        javaCameraView.setCameraIndex(1);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        //javaCameraView.setMaxFrameSize(320,240);

    }

    protected void onResume()
    {
        super.onResume();

        if(OpenCVLoader.initDebug())
        {
            Log.i(TAG,"OpenCV Loaded Successfully...!!");
            mLoaderCallBack.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
        else
        {
            Log.i(TAG,"OpenCV not Loaded :( ");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,this,mLoaderCallBack);
        }
        NativeClass.getMessage();

    }
    @Override
    protected void onPause()
    {
        super.onPause();
        if(javaCameraView!=null)
            javaCameraView.disableView();
    }

    protected void onDestroy()
    {
        super.onDestroy();
        if(javaCameraView!=null)
            javaCameraView.disableView();
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        if(javaCameraView!=null)
            javaCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width,int height)
    {
        matInput=new Mat(height,width,CvType.CV_8UC3);
        matOutput=new Mat(matInput.rows(),matInput.cols(), CvType.CV_8UC3);
       //
    }

    @Override
    public void onCameraViewStopped()
    {
        matInput.release();
        matOutput.release();
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        matInput =inputFrame.rgba();

            x = NativeClass.LandmarkDetection(matInput.getNativeObjAddr(), matOutput.getNativeObjAddr());

            if (k == 0) {
                if(mp!=null) {
                   // mp.reset();
                    mp.release();
                }

                mp = MediaPlayer.create(this, R.raw.warning);
                if (x == 5)
                    mp = MediaPlayer.create(this, R.raw.threshold);

                if (x == 1 || x == 5) {
                    k = 1;
                    mp.start();
                }
            }
            if (k == 2) {
                cnt++;
            }
            if (cnt >= 10) {
                k = 0;
                cnt = 0;
            }

            //Toast.makeText(this, "shot fired", Toast.LENGTH_SHORT).show();
           mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    k = 2;
                    mp.stop();
                    if (mp != null) {
                        mp.release();
                    }

                }
            });

       // System.gc();
        return matOutput;
    }

}

