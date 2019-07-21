package com.example.user.FilmsAndTelevision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
{
    private static Map<String, Bitmap> map = new HashMap<>();
    ImageView bmImage;


    public DownloadImageTask(ImageView bmImage)
    {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls)
    {
        String url = urls[0];
        if (map.containsKey(url))
        {
            return map.get(url);
        }
        System.out.println("\t\t\t\tDownloading: " + url);
        Bitmap mIcon11 = null;
        try
        {
            InputStream in = new java.net.URL(url).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        }
        catch (Exception e)
        {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        map.put(url, mIcon11);
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result)
    {
        bmImage.setImageBitmap(result);
    }
}