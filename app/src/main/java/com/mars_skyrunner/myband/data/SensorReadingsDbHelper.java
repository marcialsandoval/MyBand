package com.mars_skyrunner.myband.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mars_skyrunner.myband.data.SensorReadingContract.ReadingEntry;


/**
 * Database helper for MyBand app. Manages record database creation and version management.
 */
public class SensorReadingsDbHelper extends SQLiteOpenHelper {

    public final String LOG_TAG = SensorReadingsDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "readings.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link SensorReadingsDbHelper}.
     *
     * @param context of the app
     */

    public SensorReadingsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the records table
        String SQL_CREATE_SENSOR_READING_TABLE =  "CREATE TABLE " + ReadingEntry.TABLE_NAME + " ("
                + ReadingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ReadingEntry.COLUMN_TIME + " INTEGER NOT NULL, "
                + ReadingEntry.COLUMN_SENSOR_ID + " TEXT NOT NULL, "
                + ReadingEntry.COLUMN_SAMPLE_RATE + " TEXT NOT NULL, "
                + ReadingEntry.COLUMN_SENSOR_VALUE + " TEXT NOT NULL )";

        Log.w(LOG_TAG,"SQL_CREATE_SENSOR_READING_TABLE: " + SQL_CREATE_SENSOR_READING_TABLE);

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_SENSOR_READING_TABLE);

//        // Create a String that contains the SQL statement to create the records table
//        String SQL_CREATE_MASTER_SENSOR_READING_TABLE =  "CREATE TABLE " + ReadingEntry.MASTER_TABLE_NAME + " ("
//                + ReadingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
//                + ReadingEntry.COLUMN_TIME + " INTEGER NOT NULL, "
//                + ReadingEntry.COLUMN_SENSOR_ID + " TEXT NOT NULL, "
//                + ReadingEntry.COLUMN_SAMPLE_RATE + " TEXT NOT NULL, "
//                + ReadingEntry.COLUMN_SENSOR_VALUE + " TEXT NOT NULL )";
//
//        Log.w(LOG_TAG,"SQL_CREATE_SENSOR_READING_TABLE: " + SQL_CREATE_SENSOR_READING_TABLE);
//
//
//
//        // Execute the SQL statement
//        db.execSQL(SQL_CREATE_MASTER_SENSOR_READING_TABLE);

    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }

}
