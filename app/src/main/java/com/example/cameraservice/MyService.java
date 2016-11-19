package com.example.cameraservice;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.FileOutputStream;
import java.security.Policy;

public class MyService extends Service implements SurfaceHolder.Callback {

    private Camera camera;
    private android.hardware.Camera.Parameters para;
    private Bitmap bmp;
    FileOutputStream fo;
    private String FLASH_MODE;
    private int QUALITY_MODE = 0;
    private boolean isFrontCamRequest = false;
    private Camera.Size picSize;
    SurfaceView sv;
    private SurfaceHolder sholder;
    private WindowManager wm;
    WindowManager.LayoutParams params;
    public Intent cameraIntent;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    int width =0, height = 0;


    public void onCreate(){
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Camera openFrontFacingCamera(){
        if (camera !=null){
            camera.stopPreview();
            camera.release();
        }
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo caminfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camin = 0; camin < cameraCount; camin++){
            Camera.getCameraInfo(camin, caminfo);
            if(caminfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                try{
                    cam = Camera.open(camin);
                } catch (RuntimeException e){
                    Log.e("Camera","Failed to open Camera");
                }
            }
        }
        return cam;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
