package com.mars_skyrunner.myband;


import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mars_skyrunner.myband.data.SensorReadingContract.ReadingEntry;

import java.util.ArrayList;


public class SampleBasedCSVFileLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    String LOG_TAG = SampleBasedCSVFileLoader.class.getSimpleName();
    Context mContext;
    long timeBasedCSVDate;
    Activity mActivity;

    public SampleBasedCSVFileLoader(Context c, Activity activity, long time){

        mContext = c;
        timeBasedCSVDate = time;
        mActivity = activity;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ReadingEntry._ID,
                ReadingEntry.COLUMN_TIME,
                ReadingEntry.COLUMN_SAMPLE_RATE,
                ReadingEntry.COLUMN_SENSOR_ID,
                ReadingEntry.COLUMN_SENSOR_VALUE};

        String sortOrder = ReadingEntry._ID;

        // This loader will execute the ContentProvider's query method on a background thread
        //<> : Not equal to

        String selection = ReadingEntry.COLUMN_SAMPLE_RATE + "=? AND " + ReadingEntry.COLUMN_TIME + ">?";

        String saveTimeSelecionArg = "" + timeBasedCSVDate;

        String[] selectionArgs = {bundle.getString("maxSampleRate"), saveTimeSelecionArg};

        return new CursorLoader(mContext,   // Parent activity context
                ReadingEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                selection,                   //  selection clause
                selectionArgs,                   //  selection arguments
                sortOrder);                  //  sort order

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        Log.v(LOG_TAG, "SampleBasedCSVFileLoader: onLoadFinished");

        ArrayList<Long> sampleTimeStamps = new ArrayList<>();

        try {

            int rowcount = c.getCount();

            if (rowcount > 0) {

                c.moveToFirst();

                for (int i = 0; i < rowcount; i++) {

                    c.moveToPosition(i);
                    sampleTimeStamps.add(Long.parseLong(c.getString(1).trim()));

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "FileWriter IOException: " + e.toString());
        }

        for(int i = 0 ; i < sampleTimeStamps.size() ; i++){

            Log.v(LOG_TAG,""+i);

            // Kick off the  loader

            long minTime = sampleTimeStamps.get(i);
            long maxTime ;

            if((i + 1) == sampleTimeStamps.size()){
                maxTime = minTime;
            }else{
                maxTime = sampleTimeStamps.get((i + 1));
            }

            Bundle bundle = new Bundle();
            bundle.putLong("minTime", minTime);
            bundle.putLong("maxTime", maxTime);

            mActivity.getLoaderManager().restartLoader(Constants.SAMPLE_BASED_LOADER, bundle, timeStampSensorReadingLoader);


        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG, "SampleBasedCSVFileLoader: onLoaderReset");
    }


    private LoaderManager.LoaderCallbacks<Cursor> timeStampSensorReadingLoader

            = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

            Log.v(LOG_TAG, "timeStampSensorReadingLoader: onCreateLoader");

            long minTime = bundle.getLong("minTime");
            long maxTime = bundle.getLong("maxTime");

            if(minTime != maxTime){

                // Define a projection that specifies the columns from the table we care about.
                String[] projection = {
                        ReadingEntry._ID,
                        ReadingEntry.COLUMN_TIME,
                        ReadingEntry.COLUMN_SAMPLE_RATE,
                        ReadingEntry.COLUMN_SENSOR_ID,
                        ReadingEntry.COLUMN_SENSOR_VALUE};

                String sortOrder = ReadingEntry._ID;

                // This loader will execute the ContentProvider's query method on a background thread
                String selection = ReadingEntry.COLUMN_TIME + ">=?  AND " +  ReadingEntry.COLUMN_TIME + "<?";

                String[] selectionArgs = { ("" + minTime) ,("" + maxTime)};

                return new CursorLoader(mContext,   // Parent activity context
                        ReadingEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selection,                   //  selection clause
                        selectionArgs,                   //  selection arguments
                        sortOrder);                  //  sort order

            }else{

                Toast.makeText(mContext,"ALL SAMPLES FOUND" , Toast.LENGTH_SHORT).show();

                return null;

            }


        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cs) {

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

}