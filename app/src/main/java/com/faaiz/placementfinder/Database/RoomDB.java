package com.faaiz.placementfinder.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.faaiz.placementfinder.Converters;
import com.faaiz.placementfinder.Employer;
import com.faaiz.placementfinder.JobPost;
import com.faaiz.placementfinder.User;

@Database(entities = {User.class, Employer.class, JobPost.class}, version = 5  , exportSchema = false)
@TypeConverters(Converters.class)
public abstract class RoomDB extends RoomDatabase {
    public static RoomDB database;
    public static String DATABASE_NAME = "RoomDB";

    public synchronized static RoomDB getInstance(Context context){
        if(database == null){
            database = Room.databaseBuilder(context.getApplicationContext(), RoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    public abstract DAO dao();
}
