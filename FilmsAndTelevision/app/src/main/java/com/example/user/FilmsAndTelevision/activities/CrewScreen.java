package com.example.user.FilmsAndTelevision.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.example.user.FilmsAndTelevision.User;
import com.example.user.FilmsAndTelevision.adapters.Human;
import com.example.user.FilmsAndTelevision.adapters.HumansAdapter;
import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;

public class CrewScreen extends MenuActivity
{
    final String [] textToggle = {"directors", "producers", "writers", "cast"};
    ListView [] lists;
    int toggleNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.setTheme(User.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew_screen);
        lists = new ListView[textToggle.length];
        lists[0] = findViewById(R.id.DirectorsListView);
        lists[1] = findViewById(R.id.ProducersListView);
        lists[2] = findViewById(R.id.WritersListView);
        lists[3] = findViewById(R.id.CastListView);
        Intent intent = getIntent();
        HumansAdapter[] humans = new HumansAdapter[textToggle.length];
        String [] tempArr1, tempArr2;
        boolean condition;
        for (int i = 0; i < textToggle.length; i++)
        {
            humans[i] = new HumansAdapter(this, R.layout.crew_list, "crew");
            System.out.println("KEY:" + textToggle[i]);
            System.out.println("VALUE: " + intent.getStringExtra(textToggle[i]));
            tempArr1 = Methods.splitString(intent.getStringExtra(textToggle[i]));
            condition = tempArr1[0].startsWith("(");
            System.out.println("Condition: " + condition);
            for (int j = 0; j < tempArr1.length; j++)
            {
                System.out.println("J:" + tempArr1[j]);
                if (tempArr1[j].startsWith("("))
                {
                    continue;
                }
                if (condition)
                {
                    tempArr2 = Methods.splitString(tempArr1[0] + tempArr1[j]);
                    humans[i].add(new Human(tempArr2[0], tempArr2[1]));
                }
                else
                {
                    humans[i].add(new Human(tempArr1[j], textToggle[i].substring(0, textToggle[i].length() - 1)));
                }
            }
            lists[i].setAdapter(humans[i]);
        }
    }

    public void toggleLists(View view)
    {
        Button button = (Button)(view);
        lists[toggleNum].setVisibility(View.INVISIBLE);
        toggleNum = (toggleNum + 1) % 4;
        lists[toggleNum].setVisibility(View.VISIBLE);
        button.setText(textToggle[toggleNum]);
    }
}
