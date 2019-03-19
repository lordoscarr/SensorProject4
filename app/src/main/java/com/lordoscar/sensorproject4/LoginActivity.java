package com.lordoscar.sensorproject4;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lordoscar.sensorproject4.db.AsyncTasks.LoginUserAsyncTask;
import com.lordoscar.sensorproject4.db.AsyncTasks.RegisterUserAsyncTask;

/*
 * @Author Marcel Laska
 * @Date 2019ish
 * @Project Assign 4 aka Pathfinder @ MAU
 *
 *
 * Login-activity that lets user login and/or register.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editTextName, editTextPassword;
    private Button buttonLogin, buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeComponents();
    }

    /*
    * Initializes components
    */
    public void initializeComponents() {
        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonLogin.setOnClickListener(new ButtonListener());
        buttonRegister.setOnClickListener(new ButtonListener());
        editTextPassword.setOnClickListener(new ButtonListener());
        editTextName.setOnClickListener(new ButtonListener());
    }

    /*
     * Listener for buttons that handles the clicks
     */
    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(v.equals(editTextName)){
                editTextName.setText("");

            } else if (v.equals(editTextPassword)) {
                editTextPassword.setText("");
            }
            else if (v.equals(buttonLogin)) {
                LoginUserAsyncTask loginUserAsyncTask = new LoginUserAsyncTask(getApplicationContext(), editTextName.getText().toString(),
                        editTextPassword.getText().toString());
                loginUserAsyncTask.execute();
            } else if (v.equals(buttonRegister)) {
                RegisterUserAsyncTask registerUserAsyncTask = new RegisterUserAsyncTask(getApplicationContext(),
                        editTextName.getText().toString(), editTextPassword.getText().toString());
                registerUserAsyncTask.execute();
            }
        }
    }
}
