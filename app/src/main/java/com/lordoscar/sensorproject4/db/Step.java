package com.lordoscar.sensorproject4.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "step_table")
public class Step {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "steps_username")
    private String username;

    @ColumnInfo(name = "steps_amount")
    private int amountOfSteps;

    @ColumnInfo(name = "steps_date")
    private String date;


    public Step(String username, int amountOfSteps, String date) {
        this.username = username;
        this.amountOfSteps = amountOfSteps;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAmountOfSteps() {
        return amountOfSteps;
    }

    public void setAmountOfSteps(int amountOfSteps) {
        this.amountOfSteps = amountOfSteps;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
