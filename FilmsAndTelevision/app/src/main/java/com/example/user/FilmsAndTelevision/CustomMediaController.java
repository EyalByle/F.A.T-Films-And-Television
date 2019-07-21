package com.example.user.FilmsAndTelevision;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

public class CustomMediaController extends FrameLayout
{
    public static final String TAG = "CustomMediaController";

    public static final int JUMP_TIME = 10000; // 10 Seconds - Jumps forward and backward
    public static final long PROGRESS_BAR_SIZE = 100; // Size of the progress bar
    private MediaPlayerControl player; // MediaPlayer of the video
    private Context context;  // Context of the video
    private ViewGroup anchor;  // Anchor of the video
    private View root;  // Root view of the video
    private ProgressBar progress; // Progress bar of the controller
    private TextView currentTime, endTime; // Current time and end time of the controller
    private boolean showing;  //Is the controller showing
    private boolean dragging; //Is the player dragging the time
    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private boolean isInitiated; // Is the controller initiated
    StringBuilder formatBuilder; // Builder of the formatter
    Formatter formatter; // Formats the timestamps to HH:MM:SS
    private ImageButton pauseButton; // Play/Pause button
    private ImageButton forwardButton; // Forward jump button
    private ImageButton rewindButton; // Backward jump button
    private ImageButton restartButton; // Restart video button
    private ImageButton subtitlesButton; // Subtitles button
    private Handler handler = new MessageHandler(this); // Handler

    public int height;  // Height of the controller
    private boolean showDuration = true;  // Show duration or time left (true is duration)

    // Controller constructors
    public CustomMediaController(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.root = null;
        this.context = context;
        this.isInitiated = false;
        this.height = 0;
        Log.i(TAG, TAG);
    }
    public CustomMediaController(Context context)
    {
        super(context);
        this.context = context;
        this.isInitiated = false;
        this.height = 0;
        Log.i(TAG, TAG);
    }

    // Initiates the views when the controller is ready
    @Override
    public void onFinishInflate()
    {
        if (this.root != null)
        {
            initControllerView(this.root);
        }
        super.onFinishInflate();
    }

    // Sets the mediaplayer of the controller
    public void setMediaPlayer(MediaPlayerControl player)
    {
        this.player = player;
        updatePausePlay();
    }

    // Sets the anchor view of the controller
    public void setAnchorView(ViewGroup view)
    {
        this.anchor = view;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        removeAllViews();
        View v = makeControllerView();
        addView(v, params);
    }

    // Inflates the controller and initiates the views
    protected View makeControllerView()
    {
        LayoutInflater inflate = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.root = inflate.inflate(R.layout.custom_controller, null, false);

        initControllerView(this.root);

        return this.root;
    }

    // Initiates the views inside the controller (Buttons, Progress bar, Texts)
    private void initControllerView(View v)
    {
        this.restartButton = v.findViewById(R.id.RestartButton);
        this.rewindButton = v.findViewById(R.id.RewindButton);
        this.pauseButton = v.findViewById(R.id.PauseButton);
        this.forwardButton = v.findViewById(R.id.ForwardButton);
        this.subtitlesButton = v.findViewById(R.id.SubtitlesButton);
        this.currentTime = v.findViewById(R.id.CurrentTimeText);
        this.progress = v.findViewById(R.id.TimeProgressBar);
        this.endTime = v.findViewById(R.id.TotalTimeText);
        this.formatBuilder = new StringBuilder();
        this.formatter = new Formatter(this.formatBuilder, Locale.getDefault());
        this.pauseButton.requestFocus();

        // Sets listeners for the clickable views
        this.restartButton.setOnClickListener(restartListener);
        this.rewindButton.setOnClickListener(rewindListener);
        this.pauseButton.setOnClickListener(pauseListener);
        this.forwardButton.setOnClickListener(forwardListener);
        this.endTime.setOnClickListener(endTimeToggleListener);

        if (this.progress instanceof SeekBar)
        {
            SeekBar seeker = (SeekBar)this.progress;
            seeker.setOnSeekBarChangeListener(seekBarListener);
        }
        progress.setMax((int)PROGRESS_BAR_SIZE);

        this.isInitiated = true;
    }

    // Sets the listener to the subtitles button
    public void setSubtitlesListener(View.OnClickListener listener)
    {
        if (this.isInitiated)
        {
            this.subtitlesButton.setOnClickListener(listener);
        }
    }

    //Shows the controller until 'hide' is called
    public void show()
    {
        show(0);
    }

    // Shows the controller until :timeout: passes
    public void show(int timeout)
    {
        if (!this.showing)
        {
            setProgress();
            if (this.pauseButton != null)
            {
                this.pauseButton.requestFocus();
            }
            this.showing = true;
        }
        updatePausePlay();

        this.handler.sendEmptyMessage(SHOW_PROGRESS);
        Message message = this.handler.obtainMessage(FADE_OUT);
        if (timeout != 0)
        {
            this.handler.removeMessages(FADE_OUT);
            this.handler.sendMessageDelayed(message, timeout);
        }
        if (this.height == 0)
        {
            this.height = this.getHeight();
        }
    }

    // Returns whether the controller is showing or not
    public boolean isShowing()
    {
        return this.showing;
    }

    // Hides the controller
    public void hide()
    {
        try
        {
           this.handler.removeMessages(SHOW_PROGRESS);
        }
        catch (IllegalArgumentException exception)
        {
            Log.w("MediaController", "already removed");
        }
        this.showing = false;
    }

    //Translate milliseconds to a time string
    private String millisecondsToTime(int milliseconds)
    {
        int seconds = milliseconds / 1000;
        int hours = seconds / 3600;
        int minutes = (seconds / 60) % 60;
        seconds %= 60;

        this.formatBuilder.setLength(0);
        if (hours > 0)
        {
            return this.formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        }
        return this.formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    // Sets the progress of the video
    private int setProgress()
    {
        if ((this.player == null)||(this.dragging)||(!this.isInitiated))
        {
            return 0;
        }

        int position = this.player.getCurrentPosition();
        int duration = this.player.getDuration();

        if (this.progress != null) // Just in case
        {
            if (duration > 0)
            {
                long pos = PROGRESS_BAR_SIZE * position / duration;
                this.progress.setProgress((int)pos);
            }
            else
            {
                Log.w("Oh shit", "Duration is 0");
            }
        }

        if (this.currentTime != null) // Just in case
        {
            this.currentTime.setText(millisecondsToTime(position));
        }
        if (this.endTime != null) // Just in case
        {
            this.setEndTime(duration, position);
        }
        return position;
    }

    // Sets the endTime (Checks which of the states is on and sets the text
    public void setEndTime(int duration, int position)
    {
        if (this.showDuration)
        {
            this.endTime.setText(millisecondsToTime(duration));
        }
        else
        {
            this.endTime.setText("-" + millisecondsToTime(duration - position));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        show(DEFAULT_TIMEOUT);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event)
    {
        show(DEFAULT_TIMEOUT);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (this.player == null)
        {
            return true;
        }

        int keyCode = event.getKeyCode();
        final boolean uniqueDown = (event.getRepeatCount() == 0)
                && (event.getAction() == KeyEvent.ACTION_DOWN);
        if ((keyCode == KeyEvent.KEYCODE_HEADSETHOOK)
                ||(keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                ||(keyCode == KeyEvent.KEYCODE_SPACE))
        {
            if (uniqueDown)
            {
                doPauseResume();
                show(DEFAULT_TIMEOUT);
                if (this.pauseButton != null)
                {
                    this.pauseButton.requestFocus();
                }
            }
        }
        show(DEFAULT_TIMEOUT);
        return super.dispatchKeyEvent(event);
    }

    // Update the image of the pauseButton, since its also the play button
    public void updatePausePlay()
    {
        if ((this.root == null)||(this.pauseButton == null)||(this.player == null))
        {
            return;
        }

        if (this.player.isPlaying())
        {
            this.pauseButton.setImageResource(R.drawable.ic_pause_button);
        }
        else
        {
            this.pauseButton.setImageResource(R.drawable.ic_play_button);
        }
    }

    // Toggles between pause and resume
    private void doPauseResume()
    {
        if (this.player == null)
        {
            return;
        }

        if (this.player.isPlaying())
        {
            player.pause();
        }
        else
        {
            player.start();
        }
        updatePausePlay();
    }

    // Onclick listener for the restart button
    private View.OnClickListener restartListener = new View.OnClickListener()
    {
        public void onClick(View view)
        {
            if (player == null)
            {
                return;
            }
            player.seekTo(0);
            setProgress();

            show(DEFAULT_TIMEOUT);
        }
    };

    // Onclick listener for the rewind button
    private View.OnClickListener rewindListener = new View.OnClickListener()
    {
        public void onClick(View view)
        {
            if (player == null)
            {
                return;
            }
            int pos = player.getCurrentPosition();
            pos -= JUMP_TIME;
            player.seekTo(pos);
            setProgress();

            show(DEFAULT_TIMEOUT);
        }
    };

    // Onclick listener for the pause button
    private View.OnClickListener pauseListener = new View.OnClickListener()
    {
        public void onClick(View view)
        {
            doPauseResume();
            show(DEFAULT_TIMEOUT);
        }
    };

    // Onclick listener for the forward button
    private View.OnClickListener forwardListener = new View.OnClickListener()
    {
        public void onClick(View view)
        {
            if (player == null)
            {
                return;
            }
            int pos = player.getCurrentPosition();
            pos += JUMP_TIME;
            player.seekTo(pos);
            setProgress();

            show(DEFAULT_TIMEOUT);
        }
    };

    // Onclick listener for the endTime text
    private View.OnClickListener endTimeToggleListener = new View.OnClickListener()
    {
        public void onClick(View view)
        {
            if (player == null)
            {
                return;
            }
            showDuration = !showDuration;
            setEndTime(player.getDuration(), player.getCurrentPosition());
        }
    };

    // Onchange listener for the seekbar of the video
    private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            if (player == null)
            {
                return;
            }
            if (!fromUser)  // Programmatically generated changes
            {
                return;
            }
            long duration = player.getDuration();
            long newPos = (duration * progress) / PROGRESS_BAR_SIZE;
            player.seekTo((int)newPos);
            if (currentTime != null)
            {
                currentTime.setText(millisecondsToTime((int)newPos));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
            show(3600000);
            dragging = true;

            handler.removeMessages(SHOW_PROGRESS);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
            dragging = false;
            setProgress();
            updatePausePlay();
            show(DEFAULT_TIMEOUT);

            handler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    // Sets the views to be enabled
    @Override
    public void setEnabled(boolean enabled)
    {
        if (this.isInitiated)
        {
            this.restartButton.setEnabled(enabled);
            this.rewindButton.setEnabled(enabled);
            this.pauseButton.setEnabled(enabled);
            this.forwardButton.setEnabled(enabled);
            this.subtitlesButton.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }

    public interface MediaPlayerControl
    {
        void start();
        void pause();
        int getDuration();
        int getCurrentPosition();
        void seekTo(int pos);
        boolean isPlaying();
    }

    private static class MessageHandler extends Handler
    {
        private final WeakReference<CustomMediaController> controller;

        public MessageHandler(CustomMediaController controller)
        {
            this.controller = new WeakReference<>(controller);
        }

        @Override
        public void handleMessage(Message message)
        {
            CustomMediaController view = controller.get();
            if ((view == null)||(view.player == null))
            {
                return;
            }
            switch (message.what)
            {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    if ((!view.dragging)&&(view.showing)&&(view.player.isPlaying()))
                    {
                        int pos = view.setProgress();
                        message = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(message, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }
}
