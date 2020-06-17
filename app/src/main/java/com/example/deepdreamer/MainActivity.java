package com.example.deepdreamer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 2;
    private static final String TAG = "dbg_log";
    Button pickPhotoButton;
    Button sendButton;
    ImageView imageView;
    WebView webView;
    TextView text;
    ProgressBar progressBar;
    public static final String URL = "http://ec2-3-83-32-26.compute-1.amazonaws.com:80/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pickPhotoButton = findViewById(R.id.pickPhotoButton);
        sendButton = findViewById(R.id.sendButton);
        imageView = findViewById(R.id.imageView);
        webView = findViewById(R.id.webView);
        text = findViewById(R.id.text);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        pickPhotoButton.setEnabled(false);
        imageView.setVisibility(View.GONE);

        pickPhotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                String[] mimeTypes = {"image/jpeg", "image/png"};
                i.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        } else {
            pickPhotoButton.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickPhotoButton.setEnabled(true);
                }
                return;
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null ) {

            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                //data.getData returns the content URI for the selected Image
                Uri selectedImage = data.getData();
                imageView.setImageURI(selectedImage);

                try {
                    InputStream is = getContentResolver().openInputStream(selectedImage);
                    uploadImage(getBytes(is));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int ready = 0;
            }
        }
    }

    public byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
        int buffSize = 1024;
        byte[] buff = new byte[buffSize];
        int len = 0;
        while ((len = is.read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }
        return byteBuff.toByteArray();
    }

    private void uploadImage(byte[] imageBytes) {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);
        Call<MyResponse> call = retrofitInterface.uploadImage(body);
        progressBar.setVisibility(View.VISIBLE);
        call.enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, retrofit2.Response<MyResponse> response) {

                progressBar.setVisibility(View.GONE);


                if (response.isSuccessful()) {
                    Log.e("Success", new Gson().toJson(response.body()));
                    MyResponse responseBody = response.body();
                    Log.e("Success", response.body().path);
                    webView.getSettings().setBuiltInZoomControls(true);
                    //webView.loadUrl(URL + response.body().path);
                    System.out.println("XXXXXXXXXX ");
                    webView.loadUrl(URL + "converted.png");
                    imageView.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                } else {
                    Log.e("unsSuccess", new Gson().toJson(response.errorBody()));
                    ResponseBody errorBody = response.errorBody();
                    Gson gson = new Gson();
                    try {
                        Log.d(TAG, errorBody.string());
                        MyResponse errorResponse = gson.fromJson(errorBody.string(), MyResponse.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "onFailure: "+t.getLocalizedMessage());
            }
        });
    }

    private void showImage() {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
//        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
//        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);
//        Call<MyResponse> call = retrofitInterface.uploadImage(body);
//        progressBar.setVisibility(View.VISIBLE);
//        call.enqueue(new Callback<MyResponse>() {
//            @Override
//            public void onResponse(Call<MyResponse> call, retrofit2.Response<MyResponse> response) {
//
//                progressBar.setVisibility(View.GONE);
//
//                if (response.isSuccessful()) {
//                    MyResponse responseBody = response.body();
//                    text.setText(responseBody.getMessage());
//                } else {
//                    ResponseBody errorBody = response.errorBody();
//                    Gson gson = new Gson();
//                    try {
//                        Log.d(TAG, errorBody.string());
//                        MyResponse errorResponse = gson.fromJson(errorBody.string(), MyResponse.class);
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            @Override
//            public void onFailure(Call<MyResponse> call, Throwable t) {
//                progressBar.setVisibility(View.GONE);
//                Log.d(TAG, "onFailure: "+t.getLocalizedMessage());
//            }
//        });
    }
}
