package com.face.faceverifier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

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

public class recog_activity extends AppCompatActivity {

    String dataset ="";
    String present = "";
    Button capture;
    String msg=null;
    ImageView imageView;
    int flag = 0;
    ImageView ans1;
    ImageView ans2;
    ImageView ans3;
    ProgressBar progressBar;
    String encodedImage ="";
    String result=null;
    String currentPhotoPath;
    Button textans1;
    Button textans2;
    Button textans3;
    Button verify;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recog_activity);

        Intent intent = getIntent();
        dataset = intent.getStringExtra("dataset");

        Log.i("Dataset",dataset);

        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        capture = findViewById(R.id.capture);
        imageView = findViewById(R.id.imageView1);
        progressBar = findViewById(R.id.progressBar);
        ans1 = findViewById(R.id.ans1);
        ans2 = findViewById(R.id.ans2);
        ans3 = findViewById(R.id.ans3);
        textans1 = findViewById(R.id.textans1);
        textans2 = findViewById(R.id.textans2);
        textans3 = findViewById(R.id.textans3);
        textans1.setEnabled(false);
        textans2.setEnabled(false);
        textans3.setEnabled(false);
        verify = findViewById(R.id.Check);

        textans1.setVisibility(View.INVISIBLE);
        textans2.setVisibility(View.INVISIBLE);
        textans3.setVisibility(View.INVISIBLE);
        ans1.setVisibility(View.INVISIBLE);
        ans2.setVisibility(View.INVISIBLE);
        ans3.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        if(!haveNetworkConnection()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please connect to the internet and restart the application.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        nv = findViewById(R.id.nv);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id)
                {
                    case R.id.developers:
                        Log.i("NAV","Developers");
                        Intent intent = new Intent(getApplicationContext(), developers.class);
                        startActivity(intent);
                        break;
                    case R.id.how_to_use:
                        Log.i("NAV","How to use");
                        Intent inten = new Intent(getApplicationContext(), developers.class);
                        startActivity(inten);
                        break;
                    case R.id.back:
                        Log.i("NAV","Change Database");
                        finish();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = 0;
                present = "";
                verify.setEnabled(true);
                progressBar.setVisibility(View.INVISIBLE);
                ans1.setVisibility(View.INVISIBLE);
                ans2.setVisibility(View.INVISIBLE);
                ans3.setVisibility(View.INVISIBLE);
                textans1.setVisibility(View.INVISIBLE);
                textans2.setVisibility(View.INVISIBLE);
                textans1.setEnabled(false);
                textans2.setEnabled(false);
                textans3.setEnabled(false);
                textans3.setVisibility(View.INVISIBLE);
                selectImage(recog_activity.this);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectImage(Context context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose the Image of the person");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Log.i("Camera_alert","Reached");
                    camerapermission();

                } else if (options[item].equals("Choose from Gallery")) {
                    Log.i("Gallery_alert","Reached");
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

    public boolean haveNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        if (cm.getActiveNetwork() != null) {
            // connected to the internet
            return true;
        } else {
            // not connected to the internet
            return false;
        }
    }

    public void camerapermission() {
        Log.i("Camera_Per","Reached");
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
        Log.i("Camera","Function Reached");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch (IOException ex) {

            }
            if(photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,"com.face.faceverifier",photoFile);
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
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 1888:
                    if (resultCode == RESULT_OK) {
                        File f = new File(currentPhotoPath);
                        imageView.setImageURI(Uri.fromFile(f));
                        Bitmap selectedImage = BitmapFactory.decodeFile(currentPhotoPath);
                        check(selectedImage);
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Log.i("Gallery","Reached");
                        Uri selectedImage = data.getData();
                        imageView.setImageURI(selectedImage);
                        Log.i("Gallery","Imageset");
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArrayImage = byteArrayOutputStream.toByteArray();
        // get the base 64 string
        encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
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

    public void onclick(View view){
        if(!encodedImage.isEmpty()) {
            flag=0;
            capture.setEnabled(false);
            verify.setEnabled(false);
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
                jsonObject.put("dataset",dataset);
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
            if(flag==1) {
                Toast.makeText(recog_activity.this, "Server Down", Toast.LENGTH_LONG).show();
                return;
            }
            if (result == null || result.equals("NoFaceDetected")) {
                Toast.makeText(recog_activity.this, "No face detected", Toast.LENGTH_LONG).show();
                return;
            }
            if (result.equals("lessthanfifty")) {
                Toast.makeText(recog_activity.this, "No Match Found!!", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                final String[] arr = result.split("#", 6);
                String one,two,three,four,five,six;
                one = arr[0];
                two = arr[1];
                three = arr[2];
                String[] temp = one.split(".",2);
                one = temp[0];
                temp = two.split(".",2);
                two = temp[0];
                temp = three.split(".",2);
                three = temp[0];
                four = arr[3];
                five = arr[4];
                six = arr[5];
                Log.i("Roll No", one + " "  + two + " " + three + " " + four + " "  + five + " " + six );
                textans1.setText(one + " " + four);
                textans2.setText(two + " " + five);
                textans3.setText(three + " " + six);
//                    Bitmap b = getBitmapFromAssets(one);
//                    ans1.setImageBitmap(b);
//                    b = getBitmapFromAssets(two);
//                    ans2.setImageBitmap(b);
//                    b = getBitmapFromAssets(three);
//                    ans3.setImageBitmap(b);
                ans1.setVisibility(View.VISIBLE);
                ans2.setVisibility(View.VISIBLE);
                ans3.setVisibility(View.VISIBLE);
                textans1.setEnabled(true);
                textans2.setEnabled(true);
                textans3.setEnabled(true);
                textans1.setVisibility(View.VISIBLE);
                textans2.setVisibility(View.VISIBLE);
                textans3.setVisibility(View.VISIBLE);

                textans1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        present = arr[0];
                        AlertDialog.Builder builder = new AlertDialog.Builder(recog_activity.this);
                        builder.setMessage("By confirming you will mark this person as present.")
                                .setCancelable(false)
                                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton("Mark Present", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        markattendence();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
                textans2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        present = arr[1];
                        AlertDialog.Builder builder = new AlertDialog.Builder(recog_activity.this);
                        builder.setMessage("By confirming you will mark this person as present.")
                                .setCancelable(false)
                                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton("Mark Present", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        markattendence();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
                textans3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        present = arr[2];
                        AlertDialog.Builder builder = new AlertDialog.Builder(recog_activity.this);
                        builder.setMessage("By confirming you will mark this person as present.")
                                .setCancelable(false)
                                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton("Mark Present", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        markattendence();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
                result = null;
                return;
            } catch (Exception e){
                e.printStackTrace();
                Toast.makeText(recog_activity.this, "No face detected", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    public void markattendence() {
    }
    private class attend extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
        }


        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.i("Check","try ke andr");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("database",dataset);
                jsonObject.put("attendance",present);
                String data = jsonObject.toString();
                String yourURL = "";
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
                msg = output.toString();
                Log.i("Response", msg);
                connection.disconnect();
                Log.i("Check", "Done");
                present=null;

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("Check","firse fail ho gya re");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.INVISIBLE);
            ans1.setVisibility(View.INVISIBLE);
            ans2.setVisibility(View.INVISIBLE);
            ans3.setVisibility(View.INVISIBLE);
            textans1.setVisibility(View.INVISIBLE);
            textans2.setVisibility(View.INVISIBLE);
            textans1.setEnabled(false);
            textans2.setEnabled(false);
            textans3.setEnabled(false);
            textans3.setVisibility(View.INVISIBLE);
            if (msg == null) {
                Toast.makeText(recog_activity.this, "Could not mark attendance!", Toast.LENGTH_LONG).show();
            }
            if (msg.equals("done")) {
                Toast.makeText(recog_activity.this, "Attendance Marked!", Toast.LENGTH_LONG).show();
            }
            msg=null;
            return;
        }

    }
}
