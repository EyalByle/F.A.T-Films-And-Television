package com.example.user.FilmsAndTelevision.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.FilmsAndTelevision.Client;
import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.User;

public class SearchScreen extends MenuActivity
{
    private EditText search;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.setTheme(User.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);
        Log.d("Finished searching", "Search screen");
        Log.i("Finished searching", "Search screen");
        Log.v("Finished searching", "Search screen");
        search = findViewById(R.id.SearchEdit);

        Intent intent = getIntent();
        if (intent.hasExtra("TOAST"))
        {
            Toast.makeText(this, intent.getStringExtra("TOAST"), Toast.LENGTH_LONG).show();
        }
    }

    public void gotoTitleList(View text)
    {
        //Doesn't send anything to server
        //Instead, gives data to titleList and that screen requests
        String term = Methods.viewString(search);
        if (term.length() == 0)
        {
            Toast.makeText(SearchScreen.this, "Please fill all of the fields.", Toast.LENGTH_LONG).show();
            return;
        }
        String query = Methods.joinArray("SEARCH", 0, term);
        Intent listScreen = new Intent(SearchScreen.this, TitleListScreen.class);
        listScreen.putExtra("request", query);
        listScreen.putExtra("header", "Showing results for: \"" + term + "\"");
        startActivity(listScreen);
    }

    public void gotoAddTitle(View text)
    {
        String request = Methods.joinArray("REQUEST", 0, "admin", User.email); //String.format("REQUEST:%s|%s", User.email, "admin");
        String reply = Methods.sendRecv(request);
        if (reply.equals("False"))
        {
            System.out.println("BAD RECEIVED: " + Client.received_text);
            Toast.makeText(SearchScreen.this, "Access denied", Toast.LENGTH_LONG).show();
            return;
        }
        Intent addTitleIntent = new Intent(SearchScreen.this, AddTitleScreen.class);
        startActivity(addTitleIntent);
    }

    public void gotoStreams(View view)
    {
        String request = Methods.joinArray("POSSIBLE_STREAMS", "Empty");
        String response = Methods.sendRecv(request);
        Intent streams = new Intent(this, TitleListScreen.class);
        streams.putExtra("response", response);
        streams.putExtra("header", "Showing stream options");
        startActivity(streams);
    }

    public void startRatedList(int orderBy, String header)
    {
        String request = Methods.joinArray("EDGE_RATINGS", "" + orderBy);
        Intent listScreen = new Intent(SearchScreen.this, TitleListScreen.class);
        listScreen.putExtra("request", request);
        listScreen.putExtra("header", header);
        startActivity(listScreen);
    }

    public void gotoTopRatedList(View view)
    {
        startRatedList(1, "Showing top ratings");
    }

    public void gotoBottomRatedList(View view)
    {
        startRatedList(-1, "Showing bottom ratings");
    }

    @Override
    public void onBackPressed()
    {
        System.out.println("ON BACK KEY");
        Intent logInIntent = new Intent(this, LogInScreen.class);
        logInIntent.putExtra("LoggedOff", true);
        startActivity(logInIntent);
    }
}
