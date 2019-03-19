package com.lordoscar.sensorproject4.db.AsyncTasks;

import android.arch.persistence.room.Room;
import android.os.AsyncTask;
import com.lordoscar.sensorproject4.db.AppDatabase;
import com.lordoscar.sensorproject4.MainActivity;

public class DeleteStepAsyncTask extends AsyncTask<Void, Void, Void> {
    private MainActivity mainActivity;
    private String username;

    /*
     * Constructor that gets context & current user based on his username
     */
    public DeleteStepAsyncTask(MainActivity mainActivity, String username) {
        this.mainActivity = mainActivity;
        this.username = username;
    }

    /*
     * Builds database & deletes all history for this user
     * however it does not delete user from database, hence that code stands as a comment!
     */
    @Override
    protected Void doInBackground(Void... voids) {
        AppDatabase appDatabase = Room.databaseBuilder(mainActivity.getApplicationContext(), AppDatabase.class, MainActivity.Database_name)
                .allowMainThreadQueries().build();
        appDatabase.appDAO().deleteAllStepsForUser(username);
        //appDatabase.appDAO().deleteAllUsers();
        return null;
    }
}
