package com.example.user.FilmsAndTelevision.activities;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.User;
import com.example.user.FilmsAndTelevision.adapters.Title;
import com.example.user.FilmsAndTelevision.adapters.TitlesAdapter;

import java.util.HashMap;

public class ViewRatingScreen extends MenuActivity implements AdapterView.OnItemClickListener
{
    final int MAX_STARS = 10;
    final String [] BUTTON_TOGGLE = {"Movies", "Shows"};
    SparseIntArray [] indexToStars = new SparseIntArray[2];  // 1 For movies and 1 for shows
//    SparseIntArray indexToStars = new SparseIntArray();
    String [] ratings;
    ListView[][] lists;
    ConstraintLayout screen;
    Button starsButton;
    int stars_toggle = 0;
    int type_toggle = 0;
    final int JUMPS = 1;
    boolean emptyFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.setTheme(User.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rating_screen);

        TextView error404 = findViewById(R.id.NotFoundView);
        screen = findViewById(R.id.ScreenWrapper);
        ConstraintLayout listsHolder = findViewById(R.id.ListsHolder);

        starsButton = findViewById(R.id.ToggleStars);
        Button typesButton = findViewById(R.id.ToggleListsButtonRating);

        Intent intent = getIntent();
        String request = intent.getStringExtra("request");
        String response = Methods.sendRecv(request);
        System.out.println("RESPONSE: " + response);
        System.out.println("EQUALS: " + response.equals("Error"));
        if (response.equals("Error"))
        {
            Intent goBack = new Intent(this, PersonalScreen.class);
            goBack.putExtra("TOAST", "No rated things");
            startActivity(goBack);
            return;
        }
        ratings = new String[MAX_STARS];
        ratings[0] = "1 Star";
        for (int i = 1; i < ratings.length; i++)
        {
            ratings[i] = (i + 1) + " Stars";
        }
        for (String st: ratings)
        {
            System.out.println(st);
        }

        if (response.startsWith("Error"))
        {
            starsButton.setVisibility(View.INVISIBLE);
            typesButton.setVisibility(View.INVISIBLE);
            //Toast.makeText(this, response, Toast.LENGTH_LONG).show();
            error404.setVisibility(View.VISIBLE);
            error404.setText(response);
            emptyFlag = true;
            return;
        }
        String [] titles, types, allRatings = Methods.splitString(response);
        String type;
        int stars_count;
        TitlesAdapter[][] adapters = new TitlesAdapter[2][allRatings.length];
        lists = new ListView[2][allRatings.length];
        ViewGroup.LayoutParams params;

        indexToStars[0] = new SparseIntArray();
        indexToStars[1] = new SparseIntArray();

        System.out.println("First length: " + allRatings.length);
        //All ratings
        for (int i = 0; i < allRatings.length; i++)
        {
            // Gets current tested rating
            stars_count = allRatings[i].indexOf("]");
            stars_count = Integer.parseInt(allRatings[i].substring(1, stars_count));

            // Goes through the movies and shows
            types = Methods.splitString(allRatings[i]);
            for (int j = 0; j < types.length; j++)
            {
                lists[j][i] = new ListView(this);
                listsHolder.addView(lists[j][i]);
                params = lists[j][i].getLayoutParams();
                if (params == null)
                {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                }
                else
                {
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = 0;
                }
                lists[j][i].setLayoutParams(params);
                lists[j][i].setId(View.generateViewId());
                titles = Methods.splitString(types[j]);
                type = BUTTON_TOGGLE[j];
                type = type.substring(0, type.length() - 1);
                adapters[j][i] = new TitlesAdapter(this, R.layout.titles_list);
                for (int k = 1; k < titles.length; k++)
                {
                    adapters[j][i].add(new Title(Methods.splitString(titles[0] + titles[k]), type));
                }
                if (adapters[j][i].getCount() > 0)
                {
                    indexToStars[j].append(i, stars_count - 1);
                    lists[j][i].setAdapter(adapters[j][i]);
                    lists[j][i].setOnItemClickListener(this);
                    lists[j][i].setVisibility(View.INVISIBLE);
                }
            }
        }
        lists[0][0].setVisibility(View.VISIBLE);
        starsButton.setText(ratings[indexToStars[0].get(0)]);
        typesButton.setText(BUTTON_TOGGLE[0]);
    }

    public void changeRating(View view)
    {
        Button button = (Button)(view);
        lists[type_toggle][stars_toggle].setVisibility(View.INVISIBLE);
        stars_toggle = (stars_toggle + JUMPS) % indexToStars[type_toggle].size();
        lists[type_toggle][stars_toggle].setVisibility(View.VISIBLE);
        button.setText(ratings[indexToStars[type_toggle].get(stars_toggle)]);
        System.out.println("Currently at: " + BUTTON_TOGGLE[type_toggle] + " AND " + ratings[indexToStars[type_toggle].get(stars_toggle)]);
    }

    public void changeType(View view)
    {
        Button button = (Button)(view);
        lists[type_toggle][stars_toggle].setVisibility(View.INVISIBLE);
        type_toggle = (type_toggle + 1) % BUTTON_TOGGLE.length;
        stars_toggle = 0;
        button.setText(BUTTON_TOGGLE[type_toggle]);
        lists[type_toggle][stars_toggle].setVisibility(View.VISIBLE);
        starsButton.setText(ratings[indexToStars[type_toggle].get(stars_toggle)]);
        System.out.println("Currently at: " + BUTTON_TOGGLE[type_toggle] + " AND " + ratings[indexToStars[type_toggle].get(stars_toggle)]);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        System.out.println("Pos: " + position);
        Title title = (Title)parent.getAdapter().getItem(position);
        String response = Methods.sendRecv(title.requestTitle());
        String [] data = Methods.splitString(response);
        String [] splitArr;
        HashMap<String, String> map = new HashMap<>();
        for (int i = 1; i < data.length; i++)
        {
            splitArr = Methods.splitString(data[0] + data[i]);
            map.put(splitArr[0], splitArr[1]);
        }
        System.out.println("DONE GETTING TITLE");
        Methods.map = map;
        Intent titleScreen = new Intent(this, TitleScreen.class);
        titleScreen.putExtra("title", true);
        startActivity(titleScreen);
    }
}
