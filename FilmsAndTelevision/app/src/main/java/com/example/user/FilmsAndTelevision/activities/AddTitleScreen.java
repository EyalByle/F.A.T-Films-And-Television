package com.example.user.FilmsAndTelevision.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.user.FilmsAndTelevision.DateWatcher;
import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.User;

import java.util.HashMap;

public class AddTitleScreen extends MenuActivity implements Runnable
{
    EditText name;
    Spinner type;
    EditText year;
    ProgressBar bar;
    AddTitle addTitle;
    static String addTitleRequest;
    static String addTitleResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.setTheme(User.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_title_screen);
        name = (EditText)(findViewById(R.id.TitleEdit));
        year = (EditText)(findViewById(R.id.YearEdit));
        year.addTextChangedListener(new DateWatcher());
        type = (Spinner)(findViewById(R.id.TypeSpinner));
        bar = (ProgressBar)(findViewById(R.id.LoaderBar));
        bar.setVisibility(View.INVISIBLE);
        addTitle = new AddTitle(this);
    }

    public class AddTitle implements Runnable
    {
        private AddTitleScreen activity;

        public AddTitle(AddTitleScreen activity)
        {
            this.activity = activity;
        }

        @Override
        public void run()
        {
            System.out.println("RECEIVING DATA NOW");
            addTitleResponse = Methods.sendRecv(addTitleRequest);
            if (addTitleResponse.equals("Exists"))
            {
                System.out.println("BAD RECEIVED: " + addTitleResponse);
                addTitleResponse = "0";
                runOnUiThread(activity);
                return;
            }
            if (!addTitleResponse.startsWith("("))
            {
                System.out.println("BAD RECEIVED: " + addTitleResponse);
                addTitleResponse = "1";
                runOnUiThread(activity);
                return;
            }
            System.out.println("FINISHED RECEIVING DATA NOW");
            String[] arr = Methods.splitString(addTitleResponse), temp;
            System.out.println(String.format("|%s|%s|%s|%s|", arr[0], arr[1], arr[2], arr[3]));
            System.out.println(String.format("|%s|%s|%s|%s|", arr[arr.length - 4], arr[arr.length - 3], arr[arr.length - 2], arr[arr.length - 1]));
            HashMap<String, String> map = new HashMap<>();
            for (int i = 1; i < arr.length; i++)
            {
                //key = arr[i];
                //value = arr[i + 1];
                temp = arr[i].split(arr[0]);
                System.out.println(String.format("\n\t\tKey:%s\n\t\tVal:%s", temp[0], temp[1]));
                //movieScreen.putExtra(key, value);
                map.put(temp[0], temp[1]);
            }
            Methods.map = map;
            runOnUiThread(activity);
        }
    }

    @Override
    public void run()
    {
        if (addTitleResponse.equals("0"))
        {
            System.out.println("Option1");
            bar.setVisibility(View.INVISIBLE);
            Toast.makeText(AddTitleScreen.this, "Another title with the same data already exists", Toast.LENGTH_LONG).show();
            return;
        }
        if (addTitleResponse.equals("1"))
        {
            System.out.println("Option2");
            bar.setVisibility(View.INVISIBLE);
            Toast.makeText(AddTitleScreen.this, "Unknown Error", Toast.LENGTH_LONG).show();
            return;
        }
        System.out.println("Option3");
        Intent movieScreen = new Intent(AddTitleScreen.this, TitleScreen.class);
        movieScreen.putExtra("title", true);
        bar.setVisibility(View.INVISIBLE);
        startActivity(movieScreen);
    }

    public void addConnector(View view)
    {
        System.out.println(bar.getVisibility());
        bar.setVisibility(View.VISIBLE);
        System.out.println("VISIBILITY0:" + bar.getVisibility());
        System.out.println("X0:" + bar.getX() + "\tY0:" + bar.getY());
        String titleName = name.getText().toString();
        String titleRelease = year.getText().toString();
        String titleType = type.getSelectedItem().toString();
        addTitleRequest = Methods.joinArray("ADD_TITLE", 0, titleName, titleRelease, titleType, User.email);
        System.out.println("Adding title: " + addTitleRequest);
        new Thread(addTitle).start();
    }
}
