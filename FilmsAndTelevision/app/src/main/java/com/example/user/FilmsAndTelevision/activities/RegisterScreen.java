package com.example.user.FilmsAndTelevision.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.FilmsAndTelevision.DateWatcher;
import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.User;

public class RegisterScreen extends AppCompatActivity
{
    EditText nickname;
    EditText email;
    EditText confirmEmail;
    EditText password;
    EditText confirmPassword;
    EditText date;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.setTheme(User.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);
        nickname = findViewById(R.id.NicknameEdit);
        email = findViewById(R.id.EmailEdit);
        password = findViewById(R.id.PasswordEdit);

        confirmEmail = findViewById(R.id.ConfirmEmailEdit);
        confirmPassword = findViewById(R.id.ConfirmPasswordEdit);

        date = findViewById(R.id.BirthDateEdit);
        date.addTextChangedListener(new DateWatcher());
    }

    public void gotoEmailConfirm(View view)
    {
        String regNick = Methods.viewString(nickname);
        String regMail = Methods.viewString(email);
        String regPass = Methods.viewString(password);
        String regDate = Methods.viewString(date);

        // If any field is not filled
        if (regNick.length() * regMail.length() * regPass.length() * regDate.length() == 0)
        {
            Toast.makeText(RegisterScreen.this, "Please fill all of the fields.", Toast.LENGTH_LONG).show();
            return;
        }

        // If emails do not match
        if (!Methods.viewString(confirmEmail).equals(regMail))
        {
            Toast.makeText(RegisterScreen.this, "Emails don't match", Toast.LENGTH_LONG).show();
            return;
        }

        // If passwords do not match
        if (!Methods.viewString(confirmPassword).equals(regPass))
        {
            Toast.makeText(RegisterScreen.this, "Passwords don't match", Toast.LENGTH_LONG).show();
            return;
        }

        // If date is not complete
        if (!DateWatcher.isDate(regDate))
        {
            Toast.makeText(this, "Birthday is not a real date", Toast.LENGTH_LONG).show();
            return;
        }
        User.email = regMail;
        String data = Methods.joinArray("REGISTER", 0, regNick, regMail, regPass, regDate);//String.format("%s|%s|%s|%s", regNick, regMail, regPass, regDate);
        System.out.println(data);
        String answer = Methods.sendRecv(data);
        if (!answer.equals("SUCCESS"))
        {
            Toast.makeText(RegisterScreen.this, answer, Toast.LENGTH_LONG).show();
            return;
        }
        //Sends everything to server (nickname, email, password, birthday) (encrypted)
        Intent emailConfirm = new Intent(RegisterScreen.this, ConfirmEmailScreen.class);
        emailConfirm.putExtra("Email", email.getText().toString());
        startActivity(emailConfirm);
    }
}
