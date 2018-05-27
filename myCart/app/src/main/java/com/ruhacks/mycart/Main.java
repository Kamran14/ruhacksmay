package com.ruhacks.mycart;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.camera2.*;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static java.security.AccessController.getContext;

public class Main extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int RESULT_LOAD_IMAGE  = 100;
    private static final int REQUEST_PERMISSION_CODE = 200;
    private static final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".png";
    Uri saveImage = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/myCart/" + timeStamp ));
    String picturePath = Environment.getExternalStorageDirectory() + "/myCart/" + timeStamp;
    File imgFile = new File(picturePath);
    //Uri photoURI = FileProvider.getUriForFile(Main.this, BuildConfig.APPLICATION_ID + ".provider", new File(Environment.getExternalStorageDirectory() + "/myCart/" + timeStamp + ".png"));


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder newbuilder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(newbuilder.build());
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        File myFolder = new File(Environment.getExternalStorageDirectory() + "/myCart");

        if(!myFolder.exists()){
            myFolder.mkdir();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                } else {

                    if (imgFile.exists()) {
                        if (checkPermission()) {
                            Toast.makeText(Main.this, "Showing IMG", Toast.LENGTH_SHORT).show();
                            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                            imageView.setImageBitmap(bitmap);
                            doInBackground(imageView);
                        } else {
                            requestPermission();
                            Toast.makeText(Main.this, "Requesting Permissions", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        imageView = (ImageView) findViewById(R.id.imageView);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, saveImage);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }


                }
            }
        });
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(Main.this, new String[]{READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);

    }
    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;

    }
    public void getImage(View view) {
        // check if user has given us permission to access the gallery
        if(checkPermission()) {
            Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(choosePhotoIntent, RESULT_LOAD_IMAGE);
        }
        else {
            requestPermission();
        }

    }
//    public void showImg(){
//        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
//        imageView.setImageBitmap(bitmap);
//    }

    public byte[] toBase64(ImageView imgPreview){
        Bitmap sendImg = ((BitmapDrawable) imgPreview.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sendImg.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    protected String doInBackground(ImageView img) {
        HttpClient httpclient = HttpClients.createDefault();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        try {
            URIBuilder builder = new URIBuilder("https://southcentralus.api.cognitive.microsoft.com/customvision/v2.0/Prediction/77228d62-34c2-43ba-9de1-ad94be2a5c51/image?iterationId=04d0da0d-13ff-45c7-a5c8-fb33fda5ac24");


            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Prediction-Key", "d2e9d859d93a471d87e7779f08d2637a");


            // Request body. The parameter of setEntity converts the image to base64
            request.setEntity(new ByteArrayEntity(toBase64(img)));


            // getting a response and assigning it to the string res
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();
            String res = EntityUtils.toString(entity);

            Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
            return res;
        }
        catch (Exception e){
            return "null";
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new
                        Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }

        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
