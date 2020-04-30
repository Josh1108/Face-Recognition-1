package com.example.faceverifier;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    Button capture;
    ImageView imageView;
    int flag = 0;
    ImageView ans1;
    ImageView ans2;
    ImageView ans3;
    ProgressBar progressBar;
    String encodedImage ="";
    String result=null;
    String currentPhotoPath;
    TextView textans1;
    TextView textans2;
    TextView textans3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        capture = findViewById(R.id.capture);
        imageView = findViewById(R.id.imageView1);
        progressBar = findViewById(R.id.progressBar);
        ans1 = findViewById(R.id.ans1);
        ans2 = findViewById(R.id.ans2);
        ans3 = findViewById(R.id.ans3);
        textans1 = findViewById(R.id.textans1);
        textans2 = findViewById(R.id.textans2);
        textans3 = findViewById(R.id.textans3);

        textans1.setVisibility(View.INVISIBLE);
        textans2.setVisibility(View.INVISIBLE);
        textans3.setVisibility(View.INVISIBLE);
        ans1.setVisibility(View.INVISIBLE);
        ans2.setVisibility(View.INVISIBLE);
        ans3.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = 0;
                progressBar.setVisibility(View.INVISIBLE);
                ans1.setVisibility(View.INVISIBLE);
                ans2.setVisibility(View.INVISIBLE);
                ans3.setVisibility(View.INVISIBLE);
                textans1.setVisibility(View.INVISIBLE);
                textans2.setVisibility(View.INVISIBLE);
                textans3.setVisibility(View.INVISIBLE);
                selectImage(MainActivity.this);
            }
        });
    }

    private void selectImage(Context context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose the Image of the person");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    camerapermission();

                } else if (options[item].equals("Choose from Gallery")) {
                    if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},5);
                    }else {
                        getPhoto();
                    }

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void camerapermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }
        else{
            dispatchTakePictureIntent();
        }
    }

    public void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,1);
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch (IOException ex) {

            }
            if(photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,"com.example.faceverifier",photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                startActivityForResult(takePictureIntent,1888);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                dispatchTakePictureIntent();
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }

        if(requestCode == 5)
        {
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoto();
            }
            else
            {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 1888:
                    if (resultCode == RESULT_OK ) {
                        File f = new File(currentPhotoPath);
                        imageView.setImageURI(Uri.fromFile(f));
                        Bitmap selectedImage = BitmapFactory.decodeFile(currentPhotoPath);
                        check(selectedImage);
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage =  data.getData();
                        imageView.setImageURI(selectedImage);
                        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                        Bitmap a = drawable.getBitmap();
                        check(a);
                    }
                    break;
            }
        }
    }

    public void check(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArrayImage = byteArrayOutputStream.toByteArray();
        // get the base 64 string
        encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
    }

    public void onclick(View view){
        if(!encodedImage.isEmpty()) {
            flag=0;
            capture.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            textans1.setVisibility(View.INVISIBLE);
            textans2.setVisibility(View.INVISIBLE);
            textans3.setVisibility(View.INVISIBLE);
            ans1.setVisibility(View.INVISIBLE);
            ans2.setVisibility(View.INVISIBLE);
            ans3.setVisibility(View.INVISIBLE);
            new UploadImages().execute();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private class UploadImages extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Log.i("Check","try ke andr");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("imageString", encodedImage);
                String data = jsonObject.toString();
                String yourURL = "http://ai.dtu.ac.in:8000/";
                URL url = new URL(yourURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(data.getBytes().length);
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(data);
                writer.flush();
                writer.close();
                out.close();
                connection.connect();
                Log.i("PRE", "CONN");
                InputStream in = connection.getInputStream();
                Log.i("POST", "CONN");
                StringBuilder output = new StringBuilder();
                if (in != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(in, Charset.forName("UTF-8"));
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    String line = reader.readLine();
                    while (line != null) {
                        output.append(line);
                        line = reader.readLine();
                    }
                }
                result = output.toString();
                Log.i("Response", result);
                connection.disconnect();
                Log.i("Check", "Done");

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("Check","fail ho gya re");
                flag = 1;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.INVISIBLE);
            capture.setEnabled(true);
            if (result == null || result.equals("NoFaceDetected") || flag==1) {
                Toast.makeText(MainActivity.this, "No face detected", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                try {
                    String[] arr = result.split(".jpg", 4);
                    String one,two,three,four;
                    four = arr[3];
                    String[] ar = four.split("%", 3);
                    String perone,pertwo,perthree;
                    perone = arr[0] + " " + ar[0] + "%";
                    pertwo = arr[1] + " " + ar[1] + "%";
                    perthree = arr[2] + " " + ar[2]+ "%";
                    one = arr[0] + ".jpg";
                    two = arr[1] + ".jpg";
                    three = arr[2]+ ".jpg";
                    Log.i("Roll No", perone + " "  + pertwo + " " + perthree);
                    textans1.setText(perone);
                    textans2.setText(pertwo);
                    textans3.setText(perthree);
                    Bitmap b = getBitmapFromAssets(one);
                    ans1.setImageBitmap(b);
                    b = getBitmapFromAssets(two);
                    ans2.setImageBitmap(b);
                    b = getBitmapFromAssets(three);
                    ans3.setImageBitmap(b);
                    ans1.setVisibility(View.VISIBLE);
                    ans2.setVisibility(View.VISIBLE);
                    ans3.setVisibility(View.VISIBLE);
                    textans1.setVisibility(View.VISIBLE);
                    textans2.setVisibility(View.VISIBLE);
                    textans3.setVisibility(View.VISIBLE);
                    result = null;
                    return;
                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "No face detected", Toast.LENGTH_SHORT).show();
                    return;
                }
             }
        }
    }
    public Bitmap getBitmapFromAssets(String fileName) throws IOException {
        AssetManager assetManager = getAssets();

        InputStream istr = assetManager.open("dataset/"+fileName);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);

        return bitmap;
    }
}
