package com.example.user.FilmsAndTelevision.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.User;

public class SettingsScreen extends MenuActivity implements View.OnClickListener, Spinner.OnItemSelectedListener
{

    final int NAME_STATE = 0;
    final int PASSWORD_STATE = 1;
    final int LANGUAGES_STATE = 2;
    final int CANCEL_STATE = 3;
    final int APPLY_STATE = 4;

    final String [] LANGUAGE_CHOICES = {"First", "Second", "Third"};
    final String [] COMMANDS = {"name", "password", "languages"};

    View [] layouts = new View[4];
    View passwordConfirm;
    View [] editTexts = new View[3];

    String [] languages = new String[3];
    int currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.setTheme(User.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);

        layouts[0] = findViewById(R.id.TopScreenName);
        layouts[1] = findViewById(R.id.TopScreenPassword);
        layouts[2] = findViewById(R.id.PreferredLanguagesScreen);
        layouts[3] = findViewById(R.id.SettingsWrapper);
        currentState = 3;
        passwordConfirm = findViewById(R.id.BottomScreen);

        editTexts[0] = findViewById(R.id.NewNameEdit);
        editTexts[1] = findViewById(R.id.PasswordEdit);
        editTexts[2] = findViewById(R.id.ConfirmPasswordEdit);

        View tempButton = findViewById(R.id.NameChangeButton);
        tempButton.setOnClickListener(this);
        tempButton.setTag(0); //State 0
        tempButton = findViewById(R.id.PasswordChangeButton);
        tempButton.setOnClickListener(this);
        tempButton.setTag(1); // State 1
        tempButton = findViewById(R.id.PreferredLanguageChangeButton);
        tempButton.setOnClickListener(this);
        tempButton.setTag(2); // State 2
        tempButton = findViewById(R.id.CancelButton);
        tempButton.setOnClickListener(this);
        tempButton.setTag(3); // State 3
        tempButton = findViewById(R.id.ApplyButton);
        tempButton.setOnClickListener(this);
        tempButton.setTag(4); // State 3
        tempButton = findViewById(R.id.CancelLanguagesButton);
        tempButton.setOnClickListener(this);
        tempButton.setTag(3); // State 3
        tempButton = findViewById(R.id.ConfirmLanguagesButton);
        tempButton.setOnClickListener(this);
        tempButton.setTag(4); // State 3


        final LinearLayout languagesLayout = findViewById(R.id.PreferencesList);
        final Space [] spaces = new Space[3];
        Spinner tempSpinner;

        View tempView = null;
        for (int i = 0; i < spaces.length; i++)
        {
            spaces[i] = new Space(this);
            tempView = LayoutInflater.from(this).inflate(R.layout.languages_list, null);
            ((TextView)(tempView.findViewById(R.id.PreferredLanguageText))).setText(LANGUAGE_CHOICES[i] + " choice");

            tempSpinner = tempView.findViewById(R.id.LanguagesSpinner);
            tempSpinner.setSelection(User.languages[i]);
            tempSpinner.setOnItemSelectedListener(this);
            tempSpinner.setTag(i);
            languages[i] = "None";

            languagesLayout.addView(tempView);
            languagesLayout.addView(spaces[i]);
            System.out.println("Temp id: " + tempSpinner.getId());
        }
        final View languageView = tempView;

        Handler handler = new Handler();
        handler.post(new Runnable()
         {
            @Override
            public void run()
            {
                int margin = (languagesLayout.getHeight() - languageView.getHeight() * 3 - 20) / 3;
                for(Space space: spaces)
                {
                    space.setMinimumHeight(margin);
                }
            }
        });
    }

    @Override
    public void onClick(View view)
    {
        int tag = (int)view.getTag();

        switch (tag)
        {
            case NAME_STATE:  // Name
                layouts[tag].setVisibility(View.VISIBLE);
                layouts[currentState].setVisibility(View.INVISIBLE);
                passwordConfirm.setVisibility(View.VISIBLE);
                break;
            case PASSWORD_STATE:  // Password
                layouts[tag].setVisibility(View.VISIBLE);
                layouts[currentState].setVisibility(View.INVISIBLE);
                passwordConfirm.setVisibility(View.VISIBLE);
                break;
            case LANGUAGES_STATE:  // Languages
                layouts[tag].setVisibility(View.VISIBLE);
                layouts[currentState].setVisibility(View.INVISIBLE);
                break;
            case CANCEL_STATE:  // Cancel
            case APPLY_STATE:  // Apply
                if (tag == 4)
                {
                    // Apply
                    applyChanges();
                }
                tag = Math.min(tag, 3);
                layouts[tag].setVisibility(View.VISIBLE);
                layouts[currentState].setVisibility(View.INVISIBLE);
                passwordConfirm.setVisibility(View.INVISIBLE);
                break;
        }
        currentState = tag;
    }

    public void applyChanges()
    {
        String command = COMMANDS[currentState];
        String extra, request, response;

        if (currentState == LANGUAGES_STATE)  // Name
        {
            extra = Methods.joinArray(languages);
            request = Methods.joinArray("EDIT_USER", 0, command, User.email, extra);
            response = Methods.sendRecv(request);
            if (!response.startsWith("SUCCESS"))
            {
                Toast.makeText(this, response, Toast.LENGTH_LONG).show();
            }
            return;
        }
        extra = Methods.viewString(editTexts[currentState]);
        if (currentState == PASSWORD_STATE)
        {
            String confirmPass = Methods.viewString(editTexts[2]);
            if (!extra.equals(confirmPass))
            {
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_LONG).show();
                return;
            }
        }

        String oldPass = Methods.viewString(findViewById(R.id.BottomScreenPasswordEdit));

        if (extra.length() * oldPass.length() == 0)
        {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_LONG).show();
            return;
        }

        request = Methods.joinArray("EDIT_USER", 0, command, User.email, extra, oldPass);
        response = Methods.sendRecv(request);
        if (!response.startsWith("SUCCESS"))
        {
            Toast.makeText(this, response, Toast.LENGTH_LONG).show();
            return;
        }
        ((EditText)findViewById(R.id.BottomScreenPasswordEdit)).setText("");
        if (currentState == 0)
        {
            User.username = extra;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        int index = (int)(parent.getTag());
        languages[index] = Methods.viewString(view);
        User.languages[index] = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    @Override
    public void onBackPressed()
    {
        if (currentState == 3)
        {
            super.onBackPressed();
            return;
        }
        layouts[currentState].setVisibility(View.INVISIBLE);
        currentState = 3;
        layouts[currentState].setVisibility(View.VISIBLE);
        passwordConfirm.setVisibility(View.INVISIBLE);
    }
}
