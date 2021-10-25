package com.nxt.faceuploader;

import android.Manifest;


import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener {


    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_STORAGE_PERM = 3;
    private static final int RC_CAPTURE_IMAGE = 0;
    private static final int RC_CHOOSE_IMAGE = 1;
    private static final int PICK_IMAGE = 007;
    private ApiService mApiService;




    final String[] PERMISSIONS_CAMERA = new String[]{Manifest.permission.CAMERA};
    final String[] PERMISSIONS_STORAGE = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final String TAG = MainActivity.class.getSimpleName();
    private Button button;

    private String currentImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        button=(Button)findViewById(R.id.bt_choose_image);
        mApiService = ApiManager.getClient().create(ApiService.class);
        button.setOnClickListener(this);

    }

    public void showToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }


    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.CAMERA)) {

            // Display a SnackBar with an explanation and a button to trigger the request.

            Snackbar.make(this.findViewById(android.R.id.content), "This app needs access to your camera",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS_CAMERA,
                                    RC_HANDLE_CAMERA_PERM);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS_CAMERA, RC_HANDLE_CAMERA_PERM);
        }
    }

    private void requestStoragePermission() {
        Log.w(TAG, "Storage permission is not granted. Requesting permission");

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            // Display a SnackBar with an explanation and a button to trigger the request.

            Snackbar.make(this.findViewById(android.R.id.content), "This app needs access to your storage",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS_STORAGE,
                                    RC_HANDLE_STORAGE_PERM);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS_STORAGE, RC_HANDLE_STORAGE_PERM);
        }
    }

    @Override
    public void onClick(View v) {

        Toast.makeText(this,"TESTING",Toast.LENGTH_LONG).show();


        switch (v.getId()){

            case R.id.bt_choose_image:
                int cameraGranted = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
                int storageGranted = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (cameraGranted == PackageManager.PERMISSION_GRANTED) {
                    if(storageGranted == PackageManager.PERMISSION_GRANTED){
                        showChooserDialog();
                    }else{
                        requestStoragePermission();
                    }
                }else{
                    requestCameraPermission();
                }
                break;

        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                        "com.nxt.uploadimage",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent,MainActivity.RC_CAPTURE_IMAGE);
            }
        }
    }

    private File createImageFile() throws IOException {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

          currentImagePath = image.getPath();
            return image;
        }

    private void showChooserDialog(){
        CharSequence[] items =  getResources().getStringArray(R.array.dialog_chooser);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Choose!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        dispatchTakePictureIntent();
                        break;

                    case 1:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_PICK);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                        //startActivityForResult(new Intent(Intent.ACTION_PICK,
                               // MediaStore.Images.Media.EXTERNAL_CONTENT_URI), MainActivity.RC_CHOOSE_IMAGE);
                        break;

                    case 2:
                        dialog.cancel();
                        break;
                }
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "ACTIVITY REQUEST CODE " + requestCode);
        switch (requestCode) {
            case RC_CAPTURE_IMAGE:
                Log.i(TAG, "RESULT CODE " + resultCode);
                if (resultCode == RESULT_OK) {
                    //if(currentImagePath.length() > 0)
                        //sendImageToServer(currentImagePath);
                }else{
                    currentImagePath = "";
                    Toast.makeText(MainActivity.this,"Erroruring image",Toast.LENGTH_SHORT).show();
                }
                break;

            case PICK_IMAGE:
                if (data != null && resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    sendImageToServer(selectedImage);
                }else{
                    Toast.makeText(MainActivity.this,"Errorsing image",Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private  String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = this.getBaseContext().getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    private void sendImageToServer(Uri uri){


       String filepath =  getRealPathFromUri(uri);
        final File image = new File(filepath);

        if(image != null) {

            Log.d("tag", "postImageToServer: " + uri.getPath() + " filepath :" + filepath);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), image);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", image.getName(), requestFile);
            Log.d("tag", "beforeAPI: " + filepath);
                            Call<ResponseBody> call = mApiService.uploadPhoto(body);
            Log.d("tag", "afterAPI: " + uri.getPath());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.code() == 200){
                        Toast.makeText(MainActivity.this,"Imageaded!",Toast.LENGTH_SHORT).show();

                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    showToast("Failed Uploading image" + " " + t.getMessage());
                    t.printStackTrace();
                }
            });
        }
    }



}
