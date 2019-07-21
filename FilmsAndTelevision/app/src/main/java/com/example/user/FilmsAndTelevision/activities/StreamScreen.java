package com.example.user.FilmsAndTelevision.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.user.FilmsAndTelevision.Client;
import com.example.user.FilmsAndTelevision.CustomMediaController;
import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.SubtitlesView;
import com.example.user.FilmsAndTelevision.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class StreamScreen extends AppCompatActivity
        implements MediaPlayer.OnPreparedListener, View.OnTouchListener,
        CustomMediaController.MediaPlayerControl, SurfaceHolder.Callback
{
    //layout parameters options for the subtitles (0 bottom of screen while 1 is above the controller)
    private static RelativeLayout.LayoutParams [] subtitlesParams = {
            new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
            new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    };


    private static final String NONE_STRING = "None";

    //MediaPlayer of the video
    MediaPlayer player;
    //Surface of the video
    SurfaceView surfaceView;
    //Surface holder of the video
    SurfaceHolder surfaceHolder;
    //Subtitles of the video
    SubtitlesView subtitles;
    //MediaController of the video
    CustomMediaController controller;
    //Handler (Might not be important anymore)
    Handler handler = new Handler();
    //Raw uri address to the video
    String rawUri;
    //Current language index
    int current_language = 0;
    //IMDb link of the title
    String imdbLink;
    //Subtitles dialog
    AlertDialog.Builder subtitlesDialog;
    //Boolean that checks if the video has already been initiated
    boolean createdSurface = false;

    //Called when the activity is being created
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_screen);

        //Initiates the views
        surfaceView = (SurfaceView)findViewById(R.id.VideoSurface);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        controller = (CustomMediaController)findViewById(R.id.CustomController);
        subtitles = findViewById(R.id.SubtitlesView);

        //Initiates the subtitles' parameters
        subtitlesParams[0].addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        subtitlesParams[1].addRule(RelativeLayout.ABOVE, controller.getId());
        int margin = (int)getResources().getDimension(R.dimen.padding_small);
        subtitlesParams[0].setMargins(0, 0, 0, margin);
        subtitlesParams[1].setMargins(0, 0, 0, margin);
        subtitles.setLayoutParams(subtitlesParams[0]);

        surfaceView.setOnTouchListener(this);

        //Gets the video url
        Intent intent = getIntent();
        imdbLink = intent.getStringExtra("title");
        if (imdbLink == null)
        {
            Toast.makeText(this, "No video", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //Requests to start streaming
        String request = Methods.joinArray("STREAM", 0, imdbLink, User.email);
        String response = Methods.sendRecv(request);
        if (response.startsWith("Error"))
        {
            Toast.makeText(this, response, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //Find the streaming uri
        System.out.println(response);
        String [] resp_arr = Methods.splitString(response);
        rawUri = "http://" + Client.getServerIp() + resp_arr[0];
        imdbLink = imdbLink.substring(imdbLink.lastIndexOf("/") + 1);

        //Subtitles languages list
        ArrayList<String> list = new ArrayList<>(Arrays.asList(Methods.splitString(resp_arr[1])));
        list.add(0, NONE_STRING);
        String [] languages = list.toArray(new String[list.size()]);
        subtitlesDialog = dialogBuilder(languages);

        //Default subtitles
        if (resp_arr.length > 2)
        {
            current_language = -1;
            subtitles.setTrack(resp_arr[2]);
        }
    }

    //Hides the system's UI
    private void hideSystemUI()
    {
        // Enables regular immersive mode.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                //View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        hideSystemUI();
        super.onWindowFocusChanged(hasFocus);
    }

    //When the mediaPlayer pauses
    @Override
    public void onPause()
    {
        if (controller != null)
        {
            if ((player != null)&&(player.isPlaying()))
            {
                player.pause();
                controller.updatePausePlay();
            }
        }
        super.onPause();
    }

    // Called when the surfaceView is created
    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        if (createdSurface)
        {
            surfaceHolder = surfaceView.getHolder();
            player.setDisplay(surfaceHolder);
            return;
        }
        //Created
        player = new MediaPlayer();
        player.setDisplay(surfaceHolder);
        player.setOnPreparedListener(this);

        try
        {
            player.setDataSource(rawUri);
            subtitles.setPlayer(player);
            player.prepareAsync();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
        createdSurface = true;
        hideSystemUI();
    }

    // Called when the player is ready to start the video
    @Override
    public void onPrepared(MediaPlayer mp)
    {
        controller.setMediaPlayer(this);
        controller.setAnchorView((RelativeLayout)findViewById(R.id.LayoutContainer));
        final AlertDialog dialog = subtitlesDialog.create();

        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.TOP;
        window.setAttributes(params);


        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                controller.setEnabled(true);
                onTouch(surfaceView, null);
                controller.setSubtitlesListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.show();
                    }
                });
            }
        });
        player.start();
    }

    // Called when the surface changes
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {

    }

    // Called when the surface is destroyed
    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {

    }

    // Called when the user touches the surface
    @Override
    public boolean onTouch(View view, MotionEvent event)
    {
        if (controller.isShowing())
        {
            controller.setVisibility(View.INVISIBLE);
            controller.hide();
            subtitles.setLayoutParams(subtitlesParams[0]);
        }
        else
        {
            controller.setVisibility(View.VISIBLE);
            controller.show();
            subtitles.setLayoutParams(subtitlesParams[1]);
        }
        return false;
    }

    // Changes the subtitles' languages
    public void changeSubtitles(String language)
    {
        if (language.equals(NONE_STRING))
        {
            subtitles.setTrack(null);
            return;
        }
        player.pause();
        String request = Methods.joinArray("SUBTITLES", 0, imdbLink, language);
        String response = Methods.sendRecv(request);
        if (response.startsWith("Error"))
        {
            Toast.makeText(this, response, Toast.LENGTH_LONG).show();
        }
        else
        {
            subtitles.setTrack(response);
        }
        player.start();
    }

    // Creates the subtitles' dialog
    public AlertDialog.Builder dialogBuilder(final String [] languages)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogDarkStyle);
        builder.setTitle("Choose subtitles language");
        builder.setItems(languages, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (which != current_language)
                {
                    changeSubtitles(languages[which]);
                    current_language = which;
                }
                ((AlertDialog)dialog).hide();
            }
        });
        return builder;
    }

    //Overrides requested methods from the CustomMediaController
    @Override
    public void start()
    {
        player.start();
    }
    @Override
    public void pause()
    {
        player.pause();
    }
    @Override
    public int getDuration()
    {
        return player.getDuration();
    }
    @Override
    public int getCurrentPosition()
    {
        return player.getCurrentPosition();
    }
    @Override
    public void seekTo(int pos)
    {
        // Subtitles track goes back in case user goes back as well
        if (player.getCurrentPosition() > pos)
        {
            subtitles.lastSubtitlesPos = 0;
        }
        player.seekTo(pos);
    }
    @Override
    public boolean isPlaying()
    {
        return player.isPlaying();
    }

    // Turns off everything when back key is pressed and then goes back to the last activity
    @Override
    public void onBackPressed()
    {
        this.controller.setMediaPlayer(null);
        this.subtitles.setPlayer(null);
        this.player.release();
        this.player = null;
        super.onBackPressed();
    }
}
