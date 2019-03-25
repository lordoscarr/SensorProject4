package com.lordoscar.sensorproject4;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lordoscar.sensorproject4.db.AppDAO;
import com.lordoscar.sensorproject4.db.AppDatabase;
import com.lordoscar.sensorproject4.db.Step;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView listView;
    private AppDatabase database;
    private AppDAO appDAO;
    @Override

    /*
     * onCreate-method that calls other methods
     * that should be running from the start.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initializeComponents();
        setListView();
    }

    /*
     * Initializes UI-components
     */
    public void initializeComponents() {
        listView = findViewById(R.id.listView);
    }

    /*
     * Sets the list which the listview is gonna show.
     */
    public void setListView() {
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, MainActivity.Database_name)
                .allowMainThreadQueries().build();
        appDAO = database.appDAO();
        String username = getSharedPreferences("com.lordoscar.sensorproject4", MODE_PRIVATE).getString("username", "anon");
        List<Step> stepList = appDAO.getAllStepsForThisUser(username);
        Collections.reverse(stepList);
        Log.d("History", "count: " + stepList.size());

            StepAdapter adapter = new StepAdapter(this, stepList);
            listView.setAdapter(adapter);
    }

    class StepAdapter extends ArrayAdapter<Step>{

        public StepAdapter(@NonNull Context context, @NonNull List<Step> objects) {
            super(context, R.layout.step_item, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Step step = getItem(position);
            if (convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.step_item, parent, false);
            }

            TextView userTv = convertView.findViewById(R.id.userTv);
            TextView dateTv = convertView.findViewById(R.id.dateTv);
            TextView stepsTv = convertView.findViewById(R.id.stepsTv);

            userTv.setText(step.getUsername());
            if (step.getDate().equals(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()))){
                dateTv.setText("TODAY");
            }else {
                dateTv.setText(step.getDate());
            }
            stepsTv.setText(step.getAmountOfSteps() + "");

            return convertView;
        }
    }
}
