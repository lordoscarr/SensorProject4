package com.lordoscar.sensorproject4.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import java.util.List;

@Dao
public interface AppDAO {

    @Insert
    void insert(Step step);

    @Query("SELECT * FROM step_table WHERE steps_username = :username")
    List<Step> getAllStepsForThisUser(String username);

    @Query("DELETE FROM step_table WHERE steps_username = :username")
    void deleteAllStepsForUser(String username);

    @Query("SELECT * FROM step_table WHERE steps_username = :username AND steps_date = :date")
    List<Step> getAllStepsForThisUserAndDate(String username, String date);


    ///////////////////////////////////////////////////

    @Update
    void updateStepsForThisUser(Step step);

    @Insert
    void insertUser(UserCredentials userCredentials);

    @Query("SELECT * FROM user_table WHERE user_name = :username")
    List<UserCredentials> getUserCredentials(String username);

    @Query("SELECT * FROM user_table WHERE user_name = :username AND user_password = :password")
    List<UserCredentials> getUserCredentialsNameAndPassword(String username, String password);

    @Query("DELETE FROM user_table")
    void deleteAllUsers();
}
