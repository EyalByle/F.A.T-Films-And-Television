package com.example.user.FilmsAndTelevision.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.User;
import com.example.user.FilmsAndTelevision.adapters.Title;
import com.example.user.FilmsAndTelevision.adapters.TitlesAdapter;

public class TitleListScreen extends MenuActivity implements AdapterView.OnItemClickListener
{
    final String [] textToggle = {"Movies", "Shows"};
    final int [] visibleToggle = {View.VISIBLE, View.INVISIBLE};
    int toggler = 0;
    TitlesAdapter[] adapters;
    //ListView list1 = null;
    //ListView list2 = null;
    ListView [] lists;
    boolean directly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.setTheme(User.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_list_screen);
        Intent intent = getIntent();

        boolean fromPersonals = intent.getBooleanExtra("FromPersonals", false);
        System.out.println("From personals:" + fromPersonals);
        //If search, the request will be SEARCH:____
        //If user list, the request will be GET_LIST:
        String response = intent.getStringExtra("response");
        if (response == null)
        {
            String request = intent.getStringExtra("request");
            response = Methods.sendRecv(request);
        }
        System.out.println("RESPONSE: " + response);
        System.out.println("EQUALS: " + response.startsWith("Error"));
        if (response.startsWith("Error"))
        {
            System.out.println("Here");
            Intent goBack = new Intent(this, SearchScreen.class);
            goBack.putExtra("TOAST", "Found 0 results");
            startActivity(goBack);
            return;
        }
        TextView title = findViewById(R.id.TitleView);
        String titleString = intent.getStringExtra("header");
        if (titleString == null)
        {
            title.setVisibility(View.GONE);
        }
        else
        {
            title.setText(titleString);
        }
        String [] titles, results = Methods.splitString(response);
        System.out.println("RESULTS1:" + results[0]);
        System.out.println("RESULTS2:" + results[1]);
        String type;
        adapters = new TitlesAdapter[results.length];
        lists = new ListView[results.length];
        lists[0] = findViewById(R.id.MoviesListView);
        lists[1] = findViewById(R.id.ShowsListView);
        int resultsCount = 0, index = 0;
        for (int i = 0; i < results.length; i++)
        {
            titles = Methods.splitString(results[i]);
            //type = titles[titles.length - 1];
            type = textToggle[toggler];
            type = type.substring(0, type.length() - 1);
            toggler++;
            System.out.println("TYPE:" + type);
            adapters[i] = new TitlesAdapter(this, R.layout.titles_list);
            for (int j = 1; j < titles.length; j++)
            {
                adapters[i].add(new Title(Methods.splitString(titles[0] + titles[j]), type));
            }
            resultsCount += adapters[i].getCount();
            lists[i].setAdapter(adapters[i]);
            lists[i].setOnItemClickListener(this);
            if (adapters[i].getCount() == 1)
            {
                index = i;
            }
        }
        toggler = 0;
        lists[1].setVisibility(View.INVISIBLE);
        if (resultsCount == 0)
        {
            TextView error404 = findViewById(R.id.NotFoundView);
            error404.setVisibility(View.VISIBLE);
        }

        else if ((resultsCount == 1)&&(!fromPersonals))
        {
            directly = true;
            onItemClick(lists[index], lists[index], 0, 0);
        }
    }

    public void changeLists(View view)
    {
        Button button = (Button)(view);
        lists[1].setVisibility(visibleToggle[toggler]);
        toggler = (toggler + 1) % 2;
        button.setText(textToggle[toggler]);
        lists[0].setVisibility(visibleToggle[toggler]);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        System.out.println("Pos: " + position);
        Title title = (Title)parent.getAdapter().getItem(position);
        String response = Methods.sendRecv(title.requestTitle());
//        String [] data = Methods.splitString(response), splitArr;
//        HashMap<String, String> map = new HashMap<>();
//
//        for (int i = 1; i < data.length; i++)
//        {
//            splitArr = Methods.splitString(data[0] + data[i]);
//            map.put(splitArr[0], splitArr[1]);
//        }
//        System.out.println("DONE GETTING TITLE");
        Methods.map = Methods.stringToTitle(response);
        Intent titleScreen = new Intent(TitleListScreen.this, TitleScreen.class);
        titleScreen.putExtra("title", true);
        titleScreen.putExtra("directly", directly);
        startActivity(titleScreen);
    }
}
