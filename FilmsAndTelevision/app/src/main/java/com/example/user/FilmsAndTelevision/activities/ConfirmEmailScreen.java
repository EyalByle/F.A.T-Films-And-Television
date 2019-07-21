package com.example.user.FilmsAndTelevision.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.User;

public class ConfirmEmailScreen extends AppCompatActivity
{

    TextView pinCode;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.setTheme(User.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_email_screen);
        TextView emailText = findViewById(R.id.EmailView);
        //create Intent
        Intent intent = getIntent();
        email = intent.getStringExtra("Email");

        emailText.setText("\"" + email + "\"");
        pinCode = findViewById(R.id.PinEdit);
    }

    public void gotoLogIn(View view)
    {
        //Sends written PIN to server
        String answer = Methods.sendRecv(Methods.joinArray("CONFIRM_REGISTER", 0, email, Methods.viewString(pinCode)));
        if (!answer.startsWith("Success"))
        {
            Toast.makeText(this, answer, Toast.LENGTH_LONG).show();
            return;
        }
        Intent logIntent = new Intent(ConfirmEmailScreen.this, LogInScreen.class);
        System.out.println("REGISTER END");
        User.email = email;
        startActivity(logIntent);
    }
}
