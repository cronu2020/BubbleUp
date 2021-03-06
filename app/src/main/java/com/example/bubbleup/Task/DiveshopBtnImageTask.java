package com.example.bubbleup.Task;




import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import com.example.bubbleup.R;

public class DiveshopBtnImageTask extends AsyncTask<Object, Integer, Bitmap> {
    private final static String TAG = "DiveshopBtnImageTask";
    private String url, ds_no;
    private int imageSize;
    private int position = -1;


    /* ImageTask的屬性strong reference到BookListAdapter內的imageView不好，
     * 會導致BookListAdapter進入背景時imageView被參考到而無法被釋放，
     * 而且imageView會參考到Context，也會導致Activity無法被回收。
     * 改採weak參照就不會阻止imageView被回收
     */
    private WeakReference<Button> buttonViewWeakReference;




    public DiveshopBtnImageTask(String url, String ds_no, int imageSize, Button button) {
        this.url = url;
        this.ds_no = ds_no;
        this.imageSize = imageSize;
        this.buttonViewWeakReference = new WeakReference<>(button);
    }




    @Override
    protected Bitmap doInBackground(Object... objects) {
        JsonObject jsonObject = new JsonObject();
        String action = position != -1 ? "getDSPics" : "getDpic";
        jsonObject.addProperty("action", action);
        jsonObject.addProperty("ds_no", ds_no);
        jsonObject.addProperty("imageSize", imageSize);
        if (position != -1) {
            jsonObject.addProperty("position", position);
        }
        return getRemoteImage(url, jsonObject.toString());
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        Button button = buttonViewWeakReference.get();
        if (isCancelled() || button == null) {
            return;
        }
         if(bitmap != null){
            button.setBackgroundDrawable(new BitmapDrawable(bitmap));
        } else{
            button.setBackgroundResource(R.drawable.text_logo);
        }
    }

    private Bitmap getRemoteImage(String url, String jsonOut) {
        HttpURLConnection connection = null;
        Bitmap bitmap = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true); // allow inputs
            connection.setDoOutput(true); // allow outputs
            connection.setUseCaches(false); // do not use a cached copy
            connection.setRequestMethod("POST");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            bw.write(jsonOut);
            Log.d(TAG, "output: " + jsonOut);
            bw.close();

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                bitmap = BitmapFactory.decodeStream(new BufferedInputStream(connection.getInputStream()));
            } else {
                Log.d(TAG, "response code: " + responseCode);
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return bitmap;
    }
}

