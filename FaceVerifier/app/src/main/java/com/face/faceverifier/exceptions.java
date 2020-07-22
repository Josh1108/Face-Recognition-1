package com.face.faceverifier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
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
import java.util.ArrayList;

public class exceptions extends AppCompatActivity {

    String dataset = "";
    DrawerLayout drawerLayout;
    NavigationView nv;
    ActionBarDrawerToggle actionBarDrawerToggle;
    ListView listView;
    public static String jsonResponse = null;
    ArrayList<String> datasets = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exceptions);

        Intent intent = getIntent();
        dataset = intent.getStringExtra("dataset");

        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nv = findViewById(R.id.nv);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id)
                {
                    case R.id.refresh:
                        Log.i("NAV","Refresh");
                        new Utlis().execute();
                        break;
                    case R.id.developers:
                        Log.i("NAV","Developers");
                        Intent intent = new Intent(getApplicationContext(), developers.class);
                        startActivity(intent);
                        break;
                    case R.id.how_to_use:
                        Log.i("NAV","How to use");
                        Intent inten = new Intent(getApplicationContext(), how_to_use.class);
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

        listView = findViewById(R.id.listView);
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
                new Utlis().execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    public class Utlis extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Log.i("Check","try ke andr");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("database",dataset);
                String data = jsonObject.toString();
                Log.i("Sending This JSON", data);
                String yourURL = "https://www.ai.dtu.ac.in/exception";
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
            datasets.clear();
            Log.e("Final", "onPostExecute: " + jsonResponse);
            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                String datasets_str = jsonObject.getString("exp");
                Log.i("Almost ",datasets_str);
                JSONArray arr_dataset = new JSONArray(datasets_str);
                for(int i=0;i<arr_dataset.length();i++) {
                    String data = String.valueOf(arr_dataset.getString(i));
                    String[] bits = data.split("/");
                    data = bits[bits.length - 1];
                    bits = data.split("\\.(?=[^\\.]+$)");
                    data = bits[0];
                    datasets.add(data);
                }

            }catch (Exception e) {
                e.printStackTrace();
            }
            arrayAdapter = new ArrayAdapter(exceptions.this,android.R.layout.simple_list_item_1,datasets);
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getApplicationContext(), recog_activity.class);
                    intent.putExtra("dataset",datasets.get(position));
                    startActivity(intent);
                }
            });
        }
    }

}