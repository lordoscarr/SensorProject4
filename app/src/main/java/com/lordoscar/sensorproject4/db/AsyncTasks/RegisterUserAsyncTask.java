package com.lordoscar.sensorproject4.db.AsyncTasks;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;
import java.util.List;
import com.lordoscar.sensorproject4.*;
import com.lordoscar.sensorproject4.db.AppDatabase;
import com.lordoscar.sensorproject4.db.UserCredentials;

public class RegisterUserAsyncTask extends AsyncTask<Void, Void, Void> {
    private Context context;
    private String username, password;
    private boolean doesUserExists = false;

    public RegisterUserAsyncTask(Context context, String username, String password) {
        this.context = context;
        this.username = username;
        this.password = password;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        AppDatabase appDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, MainActivity.Database_name).
                allowMainThreadQueries().build();

        List<UserCredentials> listOfAllRegisteredUsers = appDatabase.appDAO().getUserCredentials(username);

        // userDoesExists == true
        if (listOfAllRegisteredUsers.size() > 0){
            doesUserExists = true;
        } else {
            // Username is available --> registers user
            UserCredentials newUser = new UserCredentials(username, password);
            appDatabase.appDAO().insertUser(newUser);
        }

        return null;
    }

    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if( context != null ) {
            if(doesUserExists) {
                // Shows Toast-msg that username is taken
                Toast.makeText(context, "USERNAME IS TAKEN!\nTRY A DIFFERENT ONE!", Toast.LENGTH_SHORT).show();
            } else {
                // Starts mainActivity and sends username so it can be used to set WelcomeTextView.
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("name", username);
                context.startActivity(intent);
            }
        }
    }
}