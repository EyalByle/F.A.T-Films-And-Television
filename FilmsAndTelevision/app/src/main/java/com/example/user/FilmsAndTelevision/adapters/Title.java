package com.example.user.FilmsAndTelevision.adapters;

import android.widget.ImageView;

import com.example.user.FilmsAndTelevision.DownloadImageTask;
import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.User;

import java.util.ArrayList;
import java.util.HashMap;

public class Title
{
    private static final String [] CREW_OPTIONS = {"directors", "producers", "writers", "cast"};
    //Present in list attributes
    public String name;
    public String date;
    public String poster;
    public String type;
    public String imdb;
    private String rating;
    private String ratingStr;
    //Full attributes
    private boolean downloaded;
    private String parentsGuide;
    private String genres;
    private String length;
    private String plot;
    private String episodes;
    private String seasons;
    private String [][] crew;
    private String languages;
    private String userRating;
    public boolean bookmarked;
    private String trailer;
    public boolean canStream;

    public Title(HashMap<String, String> map)
    {
        this.name = map.get("name");
        this.date = map.get("date");
        this.poster = map.get("poster");
        if (this.poster == null)
        {
            this.poster = "DEFAULT";
        }
        this.type = map.get("type");
        System.out.println("MOVIE TYPE: " + this.type + "|OVER");

        //Part 2
        this.parentsGuide = map.get("parents_guide");
        this.genres = map.get("genres");
        this.length = map.get("length");
        this.plot = map.get("plot");
        this.rating = map.get("rating");
        this.setRating(true);
        this.episodes = map.get("episodes");
        this.seasons = map.get("seasons");
        this.crew = new String[CREW_OPTIONS.length][2];
        for (int i = 0; i < CREW_OPTIONS.length; i++)
        {
            this.crew[i][0] = CREW_OPTIONS[i];
            this.crew[i][1] = map.get(CREW_OPTIONS[i]);
            System.out.println(CREW_OPTIONS[i] + ": " + this.crew[i][1]);
        }
        this.languages = map.get("languages");
        this.userRating = "0";
        if (map.containsKey("user_rating"))
        {
            this.userRating = map.get("user_rating");
        }
        if (map.containsKey("bookmark"))
        {
            this.bookmarked = map.get("bookmark").equals("True");
        }
        else
        {
            this.bookmarked = false;
        }
        this.imdb = map.get("imdb");
        this.trailer = map.get("trailer");
        this.canStream = map.get("can_stream").equals("True");
    }

    public Title(String [] terms, String type)
    {
        this.name = terms[0];
        this.date = terms[1];
        this.poster = terms[2];
        this.imdb = terms[3];
        this.rating = "";
        this.ratingStr = "";
        if (terms.length > 4)
        {
            this.rating = terms[4];
        }
        this.setRating(false);
        this.type = type;
        this.downloaded = false;
        System.out.println("\nTitle\tName: " + this.name +
                "\n\tDate: " + this.date +
                "\n\tPoster: " + this.poster +
                "\n\tType: " + this.type);
    }

    public String getRatingString()
    {
        return this.ratingStr;
    }

    public void setRating(String newRating, boolean full)
    {
        this.rating = newRating;
        this.setRating(full);
    }

    public void setRating(boolean full)
    {
        this.ratingStr = "";
        if (full)
        {
            this.ratingStr = "Rating: ";
        }
        this.ratingStr += this.rating;

        if ((this.rating.length() > 0)&&(!this.rating.equals("N/A")))
        {
            this.ratingStr += "/10★";
        }
    }

    public String getUserRating()
    {
        if (this.userRating.equals("0"))
        {
            return "You did not rate this title yet.";
        }
        return "You rated this title " + this.userRating + "/10★";
    }

    public void setUserRating(String rating)
    {
        this.userRating = rating;
    }

    public String getTrailer()
    {
        return this.trailer;
    }

    public void setImage(ImageView image)
    {
        if (this.downloaded)
        {
            return;
        }
        if ((this.poster == null)||(this.poster.equals("DEFAULT")))
        {
            image.setImageResource(R.drawable.default_list_poster);
            return;
        }
        //DownloadImageTask task = new DownloadImageTask(image);
        new DownloadImageTask(image).execute(this.poster);
        //this.downloaded = true;
        //DownloadImageTask task = new DownloadImageTask();
        //task.execute(this.poster);
        //this.downloaded = task.getBmImage();
        //image.setImageBitmap(this.downloaded);
    }

    public String requestTitle()
    {
        return Methods.joinArray("GET_TITLE", 0, this.type, this.imdb, User.email);
        //return Methods.joinArray("GET_TITLE", 0, this.name, this.date, this.type, User.email);
    }

    public ArrayList<String> show()
    {
        //String [] arr = new String[2];
        ArrayList<String> list = new ArrayList<>();
        list.add(this.name + " (" + this.type + ")");
        String info = String.format("%s | %s | %s | %s", this.parentsGuide, this.genres,
                this.length, this.date);
        if ((this.episodes != null)&&(this.seasons != null))
        {
            info += String.format(" | %s | %s", this.seasons + "se", this.episodes + "ep");
        }
        list.add(info);
        list.add(this.plot);
        list.add(this.ratingStr);
        list.add(this.getUserRating());
        list.add(this.userRating);
        if (this.trailer != null)
        {
            list.add(this.trailer);
        }
        return list;
    }

    public String [][] getCrew()
    {
        return Methods.copy2DArray(this.crew);
    }

    @Override
    public String toString()
    {
        return "\nStart\tName: " + this.name +
                "\tDate: " + this.date +
                "\tPoster: " + this.poster;
    }
}
