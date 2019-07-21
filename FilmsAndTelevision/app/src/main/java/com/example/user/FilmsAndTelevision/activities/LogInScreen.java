package com.example.user.FilmsAndTelevision.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.FilmsAndTelevision.Client;
import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.User;

import java.util.Arrays;

public class LogInScreen extends Activity implements Runnable
{
    EditText email;
    EditText password;
    CheckBox box;
    ConstraintLayout loading;
    ConstraintLayout login;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        System.out.println("STARTING");
        Intent intent = getIntent();
        email = findViewById(R.id.EmailEdit);
        password = findViewById(R.id.PasswordEdit);
        box = findViewById(R.id.RememberCheckBox);
        SharedPreferences preferences = this.getSharedPreferences("Login", MODE_PRIVATE);
        if (intent.hasExtra("LoggedOff"))
        {
            System.out.println("DELETING DATA");
            preferences.edit().clear().apply();
            User.email = null;
            return;
        }
        else
        {
            System.out.println("GETTING DATA");
            email.setText(preferences.getString("email", null));
            password.setText(preferences.getString("password", null));
        }
        if ((Methods.viewString(email).length() == 0) && (User.email != null))
        {
            email.setText(User.email);
        }
        loading = findViewById(R.id.StartScreen);
        login = findViewById(R.id.LogInLayout);
        if (!Client.is_initiated)
        {
            loading.setVisibility(View.VISIBLE);
            login.setVisibility(View.INVISIBLE);
            EstablishConnection connection = new EstablishConnection(this);
            new Thread(connection).start();
        }

        if (!preferences.contains("theme"))
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("theme", R.style.DarkTheme);
            editor.apply();
        }
        User.theme = preferences.getInt("theme", R.style.DarkTheme);
        this.setTheme(User.theme);
    }


    public class EstablishConnection implements Runnable
    {
        private LogInScreen activity;

        public EstablishConnection(LogInScreen activity)
        {
            this.activity = activity;
        }

        @Override
        public void run()
        {
            new Thread(new Client.ClientThread()).start();
            Client.waitForSock();
            new Thread(new Client.ReceiveFromServer()).start();
            Client.waitForSock();
            Methods.setStringSeparators(Client.received_text);
            runOnUiThread(activity);
        }
    }

    @Override
    public void run()
    {
        System.out.println("Visible toggle");
        loading.setVisibility(View.INVISIBLE);
        login.setVisibility(View.VISIBLE);

        if (email.getText().length() * password.getText().length() != 0)
        {
            findViewById(R.id.LogInButton).performClick();
        }
    }

    public void gotoRegister(View text)
    {
        //Doesn't send anything to server
        Intent registerIntent = new Intent(LogInScreen.this, RegisterScreen.class);
        startActivity(registerIntent);
    }

    public void gotoSearch(View view)
    {
        String logMail = Methods.viewString(email);
        String logPass = Methods.viewString(password);
        if (logMail.length() * logPass.length() == 0)
        {
            Toast.makeText(LogInScreen.this, "Please fill all of the fields.", Toast.LENGTH_LONG).show();
            return;
        }
        //Sends encrypted login to server
        String response = Methods.sendRecv(Methods.joinArray("LOGIN", 0, logMail, logPass));
        System.out.println("RECEIVED: " + response);
        if (!response.startsWith("Success:"))
        {
            if (response.equals("ACTIVATE"))
            {
                User.email = logMail;
                Intent confirmEmail = new Intent(LogInScreen.this, ConfirmEmailScreen.class);
                confirmEmail.putExtra("Email", logMail);
                startActivity(confirmEmail);
            }
            System.out.println("BAD RECEIVED: " + response);
            Toast.makeText(LogInScreen.this, "False Login: " + response, Toast.LENGTH_LONG).show();
            return;
        }
        response = response.substring(8); //removing the "Success:"
        String [] split = Methods.splitString(response);
        User.username = split[0];
        User.requests.addAll(Arrays.asList(Methods.splitString(split[1])));

        if (split.length > 2)
        {
            String [] temp = Methods.splitString(split[2]);
            String [] languagesConstant = getResources().getStringArray(R.array.languages);
            for (int i = 0; i < temp.length; i++)
            {
                for (int j = 0; j < languagesConstant.length; j++)
                {
                    if (languagesConstant[j].equals(temp[i]))
                    {
                        User.languages[i] = j;
                        break;
                    }
                }
            }
        }

        User.email = logMail;
        if (box.isChecked())
        {
            SharedPreferences preferences = getSharedPreferences("Login", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("email", logMail);
            editor.putString("password", logPass);
            editor.apply();
        }
        Intent searchIntent = new Intent(LogInScreen.this, SearchScreen.class);
        startActivity(searchIntent);
    }

    @Override
    public void onBackPressed()
    {
        Toast.makeText(this, "Nothing to get back to", Toast.LENGTH_LONG).show();
    }
}