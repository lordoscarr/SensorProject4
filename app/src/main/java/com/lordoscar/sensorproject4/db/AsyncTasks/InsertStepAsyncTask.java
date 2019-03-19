package com.lordoscar.sensorproject4.db.AsyncTasks;

import android.arch.persistence.room.Room;
import android.os.AsyncTask;
import java.util.Calendar;
import java.util.List;
import com.lordoscar.sensorproject4.db.AppDatabase;
import com.lordoscar.sensorproject4.db.Step;
import com.lordoscar.sensorproject4.MainActivity;
import com.lordoscar.sensorproject4.helpers.StepCountingService;

public class InsertStepAsyncTask extends AsyncTask<Void, Void, Void> {
    private StepCountingService mainActivity;
    private String username;
    private int amountOfSteps = 1;

    /*
     * Constructor that gets context & user based on username
     */
    public InsertStepAsyncTask(StepCountingService mainActivity, String username) {
        this.mainActivity = mainActivity;
        this.username = username;
    }

    /*
     * builds db and inserts user into db
     * checks first if todays day already got some data saved or not
     */
    @Override
    protected Void doInBackground(Void... voids) {

        AppDatabase appDatabase = Room.databaseBuilder(mainActivity.getApplicationContext(), AppDatabase.class, MainActivity.Database_name)
                .allowMainThreadQueries().build();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String date = day+"/"+month+"/"+year;

        List<Step> listOfStepsToday = appDatabase.appDAO().getAllStepsForThisUserAndDate(username, date);

        // Row exists --> update the steps
        if( listOfStepsToday.size() > 0 ){
            amountOfSteps = listOfStepsToday.get(0).getAmountOfSteps() + 1;
            listOfStepsToday.get(0).setAmountOfSteps(amountOfSteps);
            appDatabase.appDAO().updateStepsForThisUser( listOfStepsToday.get(0) );
        } else {
            // Make new row for this date
            Step insertNewStep = new Step(username, 1, date);
            appDatabase.appDAO().insert(insertNewStep);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}