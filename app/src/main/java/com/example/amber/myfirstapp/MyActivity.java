package com.example.amber.myfirstapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MyActivity extends ActionBarActivity {
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        final Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText usrnameW = (EditText)findViewById(R.id.usernameInput);
                username = usrnameW.getText().toString();
                EditText passwordW = (EditText)findViewById(R.id.passwordInput);
                password = passwordW.getText().toString();
                setContentView(R.layout.welcome);
                setUpWelcomePage();
            }
        });


    }

    public void setUpWelcomePage(){

        final Button enterDataButton = (Button) findViewById(R.id.enterDataButton);
        enterDataButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(R.layout.input_data);
                setUpInputPage();
            }
        });

        final Button showStatButton = (Button) findViewById(R.id.showStatButton);
        showStatButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(R.layout.stat);
            }
        });

        final Button showMapButton = (Button) findViewById(R.id.showMapButton);
        showMapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browse = new Intent(Intent.ACTION_VIEW , Uri.parse("attu.cs.washington.edu:8000") );
                startActivity(browse);
            }
        });


    }

    public void setUpInputPage(){
        final Button submitDataButton = (Button) findViewById(R.id.submitDataButton);
        submitDataButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            try {
                                submitData();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();

            }
        });

        final Button goBackToWelcomeButton = (Button) findViewById(R.id.goBackToWelcomeButton);
        goBackToWelcomeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(R.layout.welcome);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
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


    void showStat(){
        setContentView(R.layout.stat);
    }


    public void submitData() throws JSONException, UnsupportedEncodingException {
        EditText humidityW = (EditText)findViewById(R.id.humidityInput);
        int humidity = Integer.parseInt(humidityW.getText().toString());
        EditText temperatureW = (EditText)findViewById(R.id.temperatureInput);
        int temperature = Integer.parseInt(temperatureW.getText().toString());
        EditText gasW = (EditText)findViewById(R.id.gasInput);
        int gas = Integer.parseInt(gasW.getText().toString());
        EditText particulateW = (EditText)findViewById(R.id.particulateInput);
        int particulate = Integer.parseInt(particulateW.getText().toString());

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //2015-05-06T14:09:00Z
        Date date = new Date();
        String timeStamp = dateFormat.format(date);

        GPSTracker gps = new GPSTracker(MyActivity.this);
        if(!gps.canGetLocation()) {
            gps.showSettingsAlert();
        }
        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();

        JSONObject data = new JSONObject();

        data.put("timestamp", timeStamp);
        data.put("xcoord", latitude);
        data.put("ycoord", longitude);
        data.put("humidity", humidity);
        data.put("temperature", temperature);
        data.put("gas", gas);
        data.put("particulate", particulate);

        //for debugging
        Log.v("MyActivity", data.toString());

        HttpClient httpclient = new DefaultHttpClient();
        //HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            HttpPost httpPost = new HttpPost("http://attu.cs.washington.edu:8000/data/");
            StringEntity dataString = new StringEntity(data.toString());
            httpPost.setEntity(dataString);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", getB64Auth("admin", "password"));
            Log.v("MyActivity", getB64Auth("admin", "password"));
            HttpResponse httpResponse = httpclient.execute(httpPost);

            InputStream is = httpResponse.getEntity().getContent();
            InputStreamReader isr = new InputStreamReader(is);
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(isr);
            String read = br.readLine();

            while(read != null) {
                sb.append(read);
                read = br.readLine();
            }

            Log.v("MyActivity", sb.toString());
        } catch (IOException e) {

        }

        //ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

//
//   public void submitDataUsingValuePairs() throws JSONException, UnsupportedEncodingException {
//        EditText humidityW = (EditText)findViewById(R.id.humidityInput);
//        String humidity = humidityW.getText().toString();
//        EditText temperatureW = (EditText)findViewById(R.id.temperatureInput);
//        String temperature = temperatureW.getText().toString();
//        EditText gasW = (EditText)findViewById(R.id.gasInput);
//        String gas = gasW.getText().toString();
//        EditText particulateW = (EditText)findViewById(R.id.particulateInput);
//        String particulate = particulateW.getText().toString();
//
//        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss.SSSZ");
//        Date date = new Date();
//        String timeStamp = dateFormat.format(date);
//
//        GPSTracker gps = new GPSTracker(MyActivity.this);
//        if(!gps.canGetLocation()) {
//            gps.showSettingsAlert();
//        }
//        double latitude = gps.getLatitude();
//        double longitude = gps.getLongitude();
//
//        JSONObject data = new JSONObject();
//
//       List<NameValuePair> pairs = new ArrayList<NameValuePair>();
//       pairs.add(new BasicNameValuePair("key1", "value1"));
//       pairs.add(new BasicNameValuePair("key2", "value2"));
//       post.setEntity(new UrlEncodedFormEntity(pairs));
//        data.put("timestamp", timeStamp);
//        data.put("xcoord", latitude);
//        data.put("ycoord", longitude);
//        data.put("humidity", humidity);
//        data.put("temperature", temperature);
//        data.put("gas", gas);
//        data.put("particulate", particulate);
//
//        //for debugging
//        Log.v("MyActivity", data.toString());
//
//        HttpClient httpclient = new DefaultHttpClient();
//        //HttpClient httpClient = HttpClientBuilder.create().build();
//
//        try {
//            HttpPost httpPost = new HttpPost("http://attu.cs.washington.edu:8000");
//            StringEntity dataString =new StringEntity(data.toString());
//            httpPost.setEntity(dataString);
//            httpPost.setHeader("Accept", "application/json");
//            httpPost.setHeader("Content-type", "application/json");
//            httpPost.setHeader("Authorization", getB64Auth("admin", "password"));
//            HttpResponse httpResponse = httpclient.execute(httpPost);
//            InputStream inputStream = httpResponse.getEntity().getContent();
//
//            Log.v("MyActivity", inputStream.toString());//for testing
//        } catch (IOException e) {
//
//        }
//
//        //ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
//    }
//

    private String getB64Auth (String username, String password) {
        String source = username+":"+ password;
        return "Basic "+Base64.encodeToString(source.getBytes(),Base64.URL_SAFE|Base64.NO_WRAP);
    }

}


/*
  requests.post('<ENDPOINT>/data/', data="<JSON DATA>",
  headers={"Content-Type": "application/json",
    'Authorization': 'Basic %s' % base64.b64encode('<USERNAME>:<PASSWORD>')})


        {
        "id": 1,
        "userid": "admin",
        "timestamp": "2015-04-09T14:30:00Z",
        "xcoord": 1.0,
        "ycoord": 2.0,
        "humidity": 3.0,
        "temperature": 4.0,
        "gas": 5.0,
        "particulate": 6.0
    }
*/
