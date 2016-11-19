package com.example.cameraservice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static android.R.attr.bitmap;

public class MainActivity extends AppCompatActivity {

    Camera  cam;
    Camera.PictureCallback mcall;
    Bitmap image;
    int rn = (int)(Math.random()*1000);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.CAMERA)
//                    != PackageManager.PERMISSION_GRANTED) {
//
//                requestPermissions(new String[]{Manifest.permission.CAMERA},
//                        101);
//            }
//        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);




        int cameraCount = 0;
        cam = null;
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

        CameraPreview cp = new CameraPreview(this, cam);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(cp);

        mcall = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream out = null;

                Log.e("unique","unique2");

                image = BitmapFactory.decodeByteArray(data, 0, data.length);


//                ByteArrayOutputStream oss = new ByteArrayOutputStream();
//                image.compress(Bitmap.CompressFormat.JPEG, 100, oss);
//                String enImg = Base64.encodeToString(oss.toByteArray(), Base64.DEFAULT);


                try {

                    out = new FileOutputStream("/sdcard/image" +rn +".jpg");
                    out.write(data);
                    out.close();
                } catch (FileNotFoundException e) {
                    Log.e(getString(R.string.app_name), "Fail1");
                } catch (IOException e) {
                    Log.e(getString(R.string.app_name), "Fail2");
                }
                releaseCam();
                String path = "/sdcard/image"+rn+".jpg";
                upload(path);


            }
        };

        Log.e("unique","unique1");

        //cam.takePicture(null,null,mcall);
        //cam.stopPreview();
        Log.e("unique","unique3");
        //releaseCam();

    }

    public void upload(String aa) {
        try {

            // the following code is referenced from
            // http://www.myandroidtuts.com/2012/11/upload-video-in-server.html
            int serverResponseCode = 0;
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(aa);
            FileInputStream fileInputStream = new FileInputStream(sourceFile);

            URL url = new URL("http://14.136.76.143:9999/cam.php");
            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", "image"+rn+".jpg");

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + "image" +rn+".jpg" + "\"" + lineEnd);

            dos.writeBytes(lineEnd);

            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);

//            if(serverResponseCode == 200){
//
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(MainActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }

            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();
            Log.i("uploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);
        } catch (IOException e) {
            Log.e("ImageUploader", "Error uploading image", e);
        }
    }

    public void takefoto(View v){

        cam.takePicture(null,null,mcall);
    }


//    private boolean safeCameraOpen(int id){
//        boolean co =false;
//        try {
//            releaseCam();
//            cam = Camera.open(id);
//            co = (cam!=null);
//        } catch(Exception e){
//            Log.e(getString(R.string.app_name), "Fail");
//            e.printStackTrace();
//        }
//        return co;
//    }

    private void releaseCam(){
        if(cam !=null){
            cam.release();
            cam = null;
        }
    }
}
