package com.lordoscar.sensorproject4.db.AsyncTasks;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;
import java.util.List;
import com.lordoscar.sensorproject4.db.AppDatabase;
import com.lordoscar.sensorproject4.db.UserCredentials;
import com.lordoscar.sensorproject4.MainActivity;

/*
 * @Author Marcel Laska
 * @Date 2019ish
 * @Project Assign 4 aka Pathfinder @ MAU
 *
 *
 * AsyncTask that checks if username & password does match
 */
public class LoginUserAsyncTask extends AsyncTask<Void, Void, Void> {
    private Context context;
    private String username, password;
    private boolean usernameAndPasswordMatch;

    public LoginUserAsyncTask(Context context, String username, String password) {
        this.context = context;
        this.username = username;
        this.password = password;
    }

    /*
     * Iterates through all registered users on the db to check if this username & password match
     * sets a boolean for yes/no
     */
    @Override
    protected Void doInBackground(Void... voids) {
        AppDatabase appDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, MainActivity.Database_name).
                allowMainThreadQueries().build();

        List<UserCredentials> listOfAllUsernameAndPasswordCombos = appDatabase.appDAO().getUserCredentialsNameAndPassword(username, password);

        if( listOfAllUsernameAndPasswordCombos.size() > 0 ){
                usernameAndPasswordMatch = true;
            }
            else {
                usernameAndPasswordMatch = false;
            }
        return null;
    }

    // After the check-of-user-exist-in-db
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if( context != null ) {
            // shows Toast-msg if username & password does not match
            if(!usernameAndPasswordMatch) {
                Toast.makeText(context, "USERNAME AND PASSWORD DOES NOT MATCH", Toast.LENGTH_SHORT).show();
            } else {
                // Starts mainActivity and sends name to be set in WelcomeUserTextView
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("name", username);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }
}