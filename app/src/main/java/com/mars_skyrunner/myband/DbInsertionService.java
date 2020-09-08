package com.mars_skyrunner.myband;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.mars_skyrunner.myband.data.SensorReadingContract;

public class DbInsertionService extends IntentService {



    private final String LOG_TAG = DbInsertionService.class.getSimpleName();
    SensorReading sensorReading;
    Context mContext = DbInsertionService.this;

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */

    int extra = 0 ;

    public DbInsertionService() {
        super("DbInsertionService");

        Log.v(LOG_TAG,"DbInsertionService() constructor");
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        //Log.v(LOG_TAG,"onHandleIntent"  );

        SensorReading receivedSensor = intent.getParcelableExtra(Constants.SERVICE_EXTRA);

        //Log.v(LOG_TAG,"receivedSensor: " + receivedSensor.getSensorName() );

        // Create a ContentValues object where column names are the keys,
        // and sensorReadings values are the values.

        ContentValues values = new ContentValues();

        values.put(SensorReadingContract.ReadingEntry.COLUMN_TIME, "" + receivedSensor.getSensorTime());
        values.put(SensorReadingContract.ReadingEntry.COLUMN_SENSOR_ID, receivedSensor.getSensorID());
        values.put(SensorReadingContract.ReadingEntry.COLUMN_SAMPLE_RATE,  receivedSensor.getSensorReadingRate());
        values.put(SensorReadingContract.ReadingEntry.COLUMN_SENSOR_VALUE, receivedSensor.getSensorReading());

        // Insert sensorReadings into master Database
      //  Uri masterUri = mContext.getContentResolver().insert(SensorReadingContract.ReadingEntry.MASTER_CONTENT_URI, values);

        String result = "";
//        // Show a toast message depending on whether or not the insertion was successful.
//        if (masterUri == null) {
//
//            result = "masterUri"  + mContext.getResources().getString(R.string.sensor_data_saving_failed);
//
//
//        } else {
//
//            result =  "masterUri" + mContext.getResources().getString(R.string.sensor_data_saving_success);
//
//        }
//
//        Log.w(LOG_TAG,"masterUri:" + result);


        Uri newUri;
        // Insert sensorReadings into interval Database
        newUri = mContext.getContentResolver().insert(SensorReadingContract.ReadingEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful.
        if (newUri == null) {

            result = mContext.getResources().getString(R.string.sensor_data_saving_failed);

        } else {

            result = mContext.getResources().getString(R.string.sensor_data_saving_success);

        }


        // If the new content URI is null, then there was an error with insertion.
        Log.w(LOG_TAG,"newUri:" + result);



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "service " + extra + " starting", Toast.LENGTH_SHORT).show();
        Log.v(LOG_TAG,"onStartCommand"  );

        SensorReading receivedSensor = intent.getParcelableExtra(Constants.SERVICE_EXTRA);

        Log.v(LOG_TAG,"receivedSensor: " + receivedSensor.getSensorName() );

        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(this, "service" + extra + " destroy", Toast.LENGTH_SHORT).show();
        Log.v(LOG_TAG,"onDestroy"  );

    }
}
