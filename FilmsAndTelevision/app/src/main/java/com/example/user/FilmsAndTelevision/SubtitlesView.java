package com.example.user.FilmsAndTelevision;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.text.Html;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatTextView;

import java.util.ArrayList;

public class SubtitlesView extends AppCompatTextView implements Runnable
{
    private static final String TAG = "SubtitlesView";
    private static final boolean DEBUG = false;
    private static final int UPDATE_INTERVAL = 300;

    private static final int [] TIMES_MILLISECONDS = {60, 60, 1000, 1};

    private MediaPlayer player;
    private ArrayList<Line> track;
    public int lastSubtitlesPos = 0;

    public SubtitlesView(Context context)
    {
        super(context);
    }
    public SubtitlesView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    public SubtitlesView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    // Runs while the player runs, post subtitles if subtitles track is set
    @Override
    public void run()
    {
        if (this.player == null)
        {
            return;
        }
        if (this.track != null)
        {
            CharSequence text = getTimedText(this.player.getCurrentPosition());
            setText(text);
        }
        postDelayed(this, UPDATE_INTERVAL);
    }

    // Gets the subtitles to the current position in the video
    private CharSequence getTimedText(long currentPosition)
    {
        CharSequence result = "";
        Line temp;

        for (int i = this.lastSubtitlesPos; i < track.size(); i++)
        {
            temp = track.get(i);
            if (temp.from > currentPosition)
            {
                break;
            }
            if (temp.to > currentPosition)
            {
                result = temp.text;
                this.lastSubtitlesPos = i;
            }
        }
        return result;
    }

    // Calls the "run" function when attached to a video
    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        postDelayed(this, UPDATE_INTERVAL);
    }

    //Calls the "run" function when detached from a window
    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        postDelayed(this, UPDATE_INTERVAL);
    }

    // Sets the subtitles' player
    public void setPlayer(MediaPlayer player)
    {
        this.player = player;
    }

    //Turns a timestamp [hh:mm:ss,mmm] to milliseconds
    public static long timestampParse(String timestamp)
    {
        long milliseconds = 0;
        int mul = 0;
        for (String unit : timestamp.split("[:,]"))
        {
            milliseconds += Integer.parseInt(unit);
            milliseconds *= TIMES_MILLISECONDS[mul];
            mul += 1;
        }
        return milliseconds;
    }

    // Sets the subtitles track
    public void setTrack(String subtitles)
    {
        if (subtitles == null)
        {
            this.setText("");
            this.track = null;
            return;
        }
        this.track = new ArrayList<>();
        this.lastSubtitlesPos = 0;

        CharSequence specialText;
        for (String text : subtitles.split("\n\n"))
        {
            // [cue, timestamp, lines of text]
            // timestamp is like 00:08:53,575 --> 00:08:56,912
            // formulated as [start]hh:mm:ss,mmm --> [end]hh:mm:ss,mmm
            int index1 = text.indexOf("\n"), index2 = text.indexOf("\n", index1 + 1);
            String [] timestamp = text.substring(index1, index2).split("-->");
            long startTime = timestampParse(timestamp[0].trim());
            long endTime = timestampParse(timestamp[1].trim());
            text = text.substring(index2 + 1);

            if (text.contains("<"))
            {
                text = text.replaceAll("\n", "<br />");
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N)
                {
                    specialText = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
                }
                else
                {
                    specialText = Html.fromHtml(text);
                }
                track.add(new Line(startTime, endTime, specialText));
            }
            else
            {
                track.add(new Line(startTime, endTime, text));
            }
            System.out.println(startTime + "-->" + endTime + ":::\n" + text);
        }
    }

    //Simple class to represent a line
    public static class Line
    {
        long from;
        long to;
        CharSequence text;

        public Line(long startLine, long endLine, CharSequence text)
        {
            this.from = startLine;
            this.to = endLine;
            this.text = text;
        }

    }
}
