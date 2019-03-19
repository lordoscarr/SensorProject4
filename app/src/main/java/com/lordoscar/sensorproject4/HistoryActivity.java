package com.lordoscar.sensorproject4;

import android.arch.persistence.room.Room;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.lordoscar.sensorproject4.db.AppDAO;
import com.lordoscar.sensorproject4.db.AppDatabase;
import com.lordoscar.sensorproject4.db.Step;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private String username;
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
        username = getIntent().getStringExtra("name");
        List<Step> currentList = appDAO.getAllStepsForThisUser(username);
        List<String> newList = new ArrayList<String>();

        // Iterates through list of all existing users and picks everything about the current user
        // sets it to a new list and shows the list.
        for (int i = 0; i < currentList.size(); i++) {
            newList.add("USER: " + currentList.get(i).getUsername().toString() + "\nDATE: " + currentList.get(i).getDate().toString() +
                    "\nSTEPS: " + currentList.get(i).getAmountOfSteps());
            ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, newList);
            listView.setAdapter(adapter);
        }
    }
}
