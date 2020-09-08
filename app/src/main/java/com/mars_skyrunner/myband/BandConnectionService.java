package com.mars_skyrunner.myband;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.mars_skyrunner.myband.data.SensorReadingContract;
import com.microsoft.band.ConnectionState;

import java.sql.Connection;

import static com.mars_skyrunner.myband.MainActivity.saveDataButton;

public class BandConnectionService extends IntentService {


    private final String LOG_TAG = BandConnectionService.class.getSimpleName();
    SensorReading sensorReading;
    Context mContext = BandConnectionService.this;

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */

    int extra = 0;

    public BandConnectionService() {
        super("BandConnectionService");

        Log.v(LOG_TAG, "BandConnectionService() constructor");
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Log.v(LOG_TAG, "onHandleIntent");

        ConnectionState bandConnection = ConnectionState.CONNECTED;

        while ( MainActivity.client != null  && bandConnection == ConnectionState.CONNECTED ) {

            bandConnection = MainActivity.client.getConnectionState();
            //Log.v(LOG_TAG, "bandConnection: " + bandConnection.toString());

        }

        Log.v(LOG_TAG, "while loop FINISHED");


        if(MainActivity.client != null){

            Log.v(LOG_TAG, "MainActivity.client != null");

            if(bandConnection == ConnectionState.BOUND){ //this means the android device cannot find MS Band

                Log.v(LOG_TAG, "ConnectionState.BOUND");

                if(saveDataButton.isChecked()){ // Create CSV file with collected data

                    Log.v(LOG_TAG, "saveDataButton is checked");

                    Intent createCsvIntent = new Intent(Constants.CREATE_CSV_RECEIVER);
                    createCsvIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.sendBroadcast(createCsvIntent);

                }else{

                    Log.v(LOG_TAG, "saveDataButton not checked");

                }

            }

            Intent resetReadingsIntent = new Intent(Constants.RESET_SENSOR_READING);
            resetReadingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.sendBroadcast(resetReadingsIntent);



        }else{

            Log.v(LOG_TAG, "MainActivity.client == null");

        }


    }





    private String getDisplayMessage(ConnectionState bandConnection) {

        String userMsg = "";

        switch (bandConnection){

            case CONNECTED:

                userMsg = "Band is bound to MS Health's band communication service and connected to its corresponding MS Band";

                break;

            case BOUND:
                userMsg = "Band is bound to MS Health's band comm. service";
                break;

            case BINDING:
                userMsg = "Band is binding to MS Health's band comm. service";
                break;

            case UNBOUND:
                userMsg = "Band is not bound to MS Health's band comm. service";
                break;

            case DISPOSED:
                userMsg = "Band has been disposed of by MS Health's band comm. service";
                break;

            case UNBINDING:
                userMsg = "Band is unbinding from MS Health's band comm. service";
                break;

            case INVALID_SDK_VERSION:
                userMsg = "MS Health band comm. service version mismatch";
                break;

            default:
                userMsg = "Band Suscription failed.";
                break;

        }

        Log.v(LOG_TAG,"getDisplayMessage: userMsg: " + userMsg);

        return userMsg;
    }

    private void appendToUI(String value, String sensor) {

        Intent appendToUiIntent = new Intent(Constants.DISPLAY_VALUE);
        appendToUiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        appendToUiIntent.putExtra(Constants.SENSOR,sensor);
        appendToUiIntent.putExtra(Constants.VALUE,value);
        mContext.sendBroadcast(appendToUiIntent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.v(LOG_TAG, "onStartCommand");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(this, "service" + extra + " destroy", Toast.LENGTH_SHORT).show();
        Log.v(LOG_TAG, "onDestroy");

    }
}
