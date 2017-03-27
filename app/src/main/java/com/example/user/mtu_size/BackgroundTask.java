package com.example.user.mtu_size;

/**
 * Created by car on 2017/3/7.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by car on 2017/3/6.
 */

public class BackgroundTask extends AsyncTask<String, Void, String> {

    Context ctx;


    BackgroundTask(Context ctx) {
        this.ctx = ctx;
    }

    protected String doInBackground(String... params) {

        String reg_url = "https://1-dot-ecgproject-1069.appspot.com/";
        StringBuilder sb = new StringBuilder();
        String echo;
        //String reg_url = "http://140.122.184.227/~40147029S/insert.php";
        String method = params[0];
        if (method.equals("insert")) {
            //String deviceid = params[1];
            //String time = params[2];
            //String ecgdata = params[3];
            //String country=params[4];
            try {
                URL url = new URL(reg_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("charset", "utf-8");
                httpURLConnection.setDoOutput(true);
                OutputStream os = httpURLConnection.getOutputStream();

                //BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                //String data = URLEncoder.encode("deviceid", "UTF-8") + "=" + URLEncoder.encode(deviceid, "UTF-8") + "&" +
                //        URLEncoder.encode("time", "UTF-8") + "=" + URLEncoder.encode(time, "UTF-8") + "&" +
                //        URLEncoder.encode("data", "UTF-8") + "=" + URLEncoder.encode(ecgdata, "UTF-8");
                //bufferedWriter.write(data);
                //bufferedWriter.flush();
                //bufferedWriter.close();
                Date t4 = new Date();
                SimpleDateFormat time4 = new SimpleDateFormat("HH:mm:ss.SSS");
                String T4 = time4.format(t4);
                Log.d("date", String.format("Time before insert: " + T4));
                os.write(params[1].getBytes());
                os.flush();
                os.close();
                InputStream IS = httpURLConnection.getInputStream();
                //BufferedReader reader = new BufferedReader(new InputStreamReader(IS, "UTF-8"));
                Date t5 = new Date();
                SimpleDateFormat time5 = new SimpleDateFormat("HH:mm:ss.SSS");
                String T5 = time4.format(t5);
                Log.d("date", String.format("Time after insert: " + T5));
                //while ((echo=reader.readLine())!=null){
                   // sb.append(echo);
                //}
                IS.close();
                return T4 + T5;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(ctx, result, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
