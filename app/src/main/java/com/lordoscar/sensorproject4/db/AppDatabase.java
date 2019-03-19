package com.lordoscar.sensorproject4.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
/*
 * @Author Marcel Laska
 * @Date 2019ish
 * @Project Assign 4 aka Pathfinder @ MAU
 *
 *
 * Room-database
 */

@Database(entities = {Step.class, UserCredentials.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AppDAO appDAO();

}
