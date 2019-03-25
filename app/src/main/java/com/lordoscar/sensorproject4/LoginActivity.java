package com.lordoscar.sensorproject4;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lordoscar.sensorproject4.db.AppDatabase;
import com.lordoscar.sensorproject4.db.UserCredentials;


public class LoginActivity extends AppCompatActivity {

    private EditText usernameField, passwordField;
    private Button buttonLogin, buttonRegister;
    AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, MainActivity.Database_name).
                allowMainThreadQueries().build();
        initializeComponents();
    }

    /*
    * Initializes components
    */
    public void initializeComponents() {
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonLogin.setOnClickListener(new ButtonListener());
        buttonRegister.setOnClickListener(new ButtonListener());
        passwordField.setOnClickListener(new ButtonListener());
        usernameField.setOnClickListener(new ButtonListener());
    }

    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.buttonLogin:
                    login(usernameField.getText().toString(), passwordField.getText().toString());
                    break;
                case R.id.buttonRegister:
                    register(usernameField.getText().toString(), passwordField.getText().toString());
                    break;
            }
        }
    }

    private void login(String username, String password){
        new Thread(() -> {
            if (appDatabase.appDAO().getUserCredentialsNameAndPassword(username, password).size() > 0){
                //Login success
                getSharedPreferences("com.lordoscar.sensorproject4", MODE_PRIVATE).edit()
                        .putBoolean("firststart", false)
                        .putString("username", username).apply();

                runOnUiThread(()-> {
                    startActivity(new Intent(this, MainActivity.class));
                });
            }else {
                //Login fail
                runOnUiThread(()-> {
                    Toast.makeText(this, "Login failed. Try again.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();

    }

    private void register(String username, String password){
        new Thread(() -> {
            if (appDatabase.appDAO().getUserCredentialsNameAndPassword(username, password).size() > 0){
                //User already exists
                runOnUiThread(()-> {
                    Toast.makeText(this, "Register failed, user already exist.", Toast.LENGTH_SHORT).show();
                });
            }else {
                //Register
                appDatabase.appDAO().insertUser(new UserCredentials(username, password));
                Log.d("Register", "registered new user: " + username);
                login(username, password);
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
