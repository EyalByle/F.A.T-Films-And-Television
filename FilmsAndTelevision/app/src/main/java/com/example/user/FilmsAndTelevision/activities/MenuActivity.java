package com.example.user.FilmsAndTelevision.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.user.FilmsAndTelevision.R;

public class MenuActivity extends AppCompatActivity
{
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.SearchItem:
                if (!this.getClass().getSimpleName().equals("SearchScreen"))
                {
                    System.out.println("Search screen");
                    Intent searchIntent = new Intent(this, SearchScreen.class);
                    startActivity(searchIntent);
                }
                break;
            case R.id.PrivateItem:
                if (!this.getClass().getSimpleName().equals("PersonalScreen"))
                {
                    System.out.println("Personal screen");
                    Intent personalIntent = new Intent(this, PersonalScreen.class);
                    startActivity(personalIntent);
                }
                break;
            case R.id.SettingsItem:
                if (!this.getClass().getSimpleName().equals("SettingsScreen"))
                {
                    System.out.println("Settings screen");
                    Intent settingsIntent = new Intent(this, SettingsScreen.class);
                    startActivity(settingsIntent);
                }
                break;
            case R.id.LogOffItem:
                System.out.println("Log off screen");
                Intent logOffIntent = new Intent(this, LogInScreen.class);
                logOffIntent.putExtra("LoggedOff", true);
                startActivity(logOffIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
