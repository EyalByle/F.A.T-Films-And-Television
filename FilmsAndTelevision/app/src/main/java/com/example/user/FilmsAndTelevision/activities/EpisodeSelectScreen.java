package com.example.user.FilmsAndTelevision.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.User;

import java.util.ArrayList;

public class EpisodeSelectScreen extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    String imdbLink;

    int toggleSeason = 0;
    String [] seasons;
    ArrayList<ArrayAdapter<String>> episodesAdapters = new ArrayList<>();

    ListView episodesList;

    int stringZfill;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.setTheme(User.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_select_screen);

        Intent intent = getIntent();
        imdbLink = intent.getStringExtra("IMDb");

        String request = Methods.joinArray("EPISODES_GUIDE", imdbLink);
        String response = Methods.sendRecv(request);
        String [] seasonsGuide = Methods.splitString(response), temp;
        seasons = new String[seasonsGuide.length - 1];
        ArrayAdapter<String> tempAdapter;

        stringZfill = Integer.parseInt(seasonsGuide[seasonsGuide.length - 1]);
        for (int i = 0; i < seasonsGuide.length - 1; i++)
        {
            temp = Methods.splitString(seasonsGuide[i]);
            seasons[i] = temp[0];
            System.out.println("Adding season: " + seasons[i]);
            tempAdapter = new ArrayAdapter<>(this, R.layout.simple_text_view);
            for (int j = 1; j < temp.length; j++)
            {
                tempAdapter.add("Episode " + temp[j]);
            }
            episodesAdapters.add(tempAdapter);
        }

        ((Button)(findViewById(R.id.SeasonToggle))).setText("Season " + seasons[0]);
        episodesList = (ListView)(findViewById(R.id.EpisodesList));
        episodesList.setOnItemClickListener(this);
        episodesList.setAdapter(episodesAdapters.get(0));
    }

    public void changeSeason(View view)
    {
        toggleSeason = (toggleSeason + 1) % seasons.length;
        ((Button)view).setText("Season " + seasons[toggleSeason]);
        episodesList.setAdapter(episodesAdapters.get(toggleSeason));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        String selectedEpisode = imdbLink.substring(imdbLink.lastIndexOf("/") + 1);
        String episode = Methods.viewString(view), season = seasons[toggleSeason];
        episode = episode.substring(episode.lastIndexOf(" ") + 1);

        season = "S" + Methods.zfill(season, stringZfill);
        episode = "E" + Methods.zfill(episode, stringZfill);
        selectedEpisode += season + episode;
        Intent stream = new Intent(this, StreamScreen.class);
        stream.putExtra("title", selectedEpisode);
        startActivity(stream);
    }
}
