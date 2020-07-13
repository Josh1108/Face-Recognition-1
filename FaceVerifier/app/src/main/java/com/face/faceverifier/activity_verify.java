package com.face.faceverifier;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import xyz.farhanfarooqui.pinview.PinView;


public class activity_verify extends AppCompatActivity {

    String jsonResponse, user_pin,password = null;
    PinView pinview;
    Button validate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        pinview = findViewById(R.id.pinview);
        validate = findViewById(R.id.validate);
        validate.setEnabled(false);

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
        else {
            try {
                new passcode().execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Implement pin verification
            validate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user_pin = pinview.getPin();
                    if(password==null || password.equals("fail")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity_verify.this);
                        builder.setMessage("No pin found from server, Please contact to the Database Manager")
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
                    if(user_pin==null){
                        Toast.makeText(getApplicationContext(),"Please Enter a 6-digit Pin!",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.i("Actual ",password);
                        Log.i("User ",user_pin);
                        if (user_pin.equals(password)) {
                            Intent intent = new Intent(getApplicationContext(), activity_dataset.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "Incorrect Pin!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            }
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

    public class passcode extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Log.i("Check","try ke andr");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ButterChicken","ButterChicken");
                String data = jsonObject.toString();
                Log.i("Sending This JSON", data);
                String yourURL = "https://www.ai.dtu.ac.in/creds";
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
                jsonResponse = output.toString();
                Log.i("Response", jsonResponse);
                connection.disconnect();
                Log.i("Check", "Done");

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("Check","fail ho gya re");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            validate.setEnabled(true);
            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                password = jsonObject.getString("creds");
                Log.i("Pincode:",password);
                return;
            }catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}