package com.mars_skyrunner.myband;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mars_skyrunner.myband.data.SensorReadingContract;
import com.mars_skyrunner.myband.data.SensorReadingContract.ReadingEntry;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.HeartRateQuality;

import org.mortbay.jetty.Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.security.Provider;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class MainActivity extends AppCompatActivity {

    public static BandClient client = null;
    final String LOG_TAG = "MainActivity";
    private TextView bandStatusTxt;
    Toolbar toolbar;
    public static LinearLayout mListView;
    LinearLayout mLoadingView, mMainLayout;
    ArrayList<SensorReading> sensorReadings;
    File saveFile;
    Date date;
    long timeBasedCSVDate = 0;
    public static boolean bandSubscriptionTaskRunning = false;
    TextView clock ;
    public static SaveButton saveDataButton;
    FutureTask task = null;
    boolean saveClicked = false;
    FrameLayout holder, saveButtonHolder;
    ToggleButton toggle;
    ImageButton settingsButton;
    ArrayList<SensorReading> values = new ArrayList<SensorReading>();
    String  fileNameExtension = ".csv";
    String displayDate;
    public static String labelPrefix = "";
    String filename;
    File outputDirectory = null;
    int csvFileCounter = Constants.SAMPLE_RATE_OPTIONS.length - 1;
    int prevCsvMode;
    private ArrayList<String> sampleDataset = new ArrayList<>();
    private Long timeStampReference;
    ArrayList<Long> sampleTimeStamps;
    int sampleTimeStampsIterator;
    public static long TIMER_DURATION = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_NoActionBar);
        setContentView(R.layout.activity_main);

        //Permanent storage of the preferred output csv file format
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int defaultValue = getResources().getInteger(R.integer.csv_mode_key_default_value);
        prevCsvMode = sharedPref.getInt(getString(R.string.csv_mode_key), defaultValue);

        //Sets activitys toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializes global variables
        mListView = (LinearLayout) findViewById(R.id.sensor_list);
        initSensorListView();

        mMainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mLoadingView = (LinearLayout) findViewById(R.id.loading_layout);

        clock = findViewById(R.id.minutes); //Timer display

        date = new Date(); //updates current date for further use on sensor readings and output file label
        displayDate = new SimpleDateFormat("yyMMdd_HHmmSS").format(date);

        settingsButton = (ImageButton) findViewById(R.id.settigs_imagebutton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SettingsDialog(MainActivity.this).show();
            }
        });

        //Dynamic FrameLayout that turns red when sensor recording is being made
        saveButtonHolder = (FrameLayout) findViewById(R.id.save_button_holder);

        saveDataButton = new SaveButton(this);
        saveDataButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_system_update_alt_white_24dp));
        saveButtonHolder.addView(saveDataButton);


        toggle = (ToggleButton) findViewById(R.id.togglebutton);// 'Start' Button
        toggle.setTextOff(getResources().getString(R.string.start));
        toggle.setTextOn(getResources().getString(R.string.stop));
        toggle.setChecked(false);

        holder = (FrameLayout) findViewById(R.id.toggle_button_holder); //Dynamic FrameLayout that turns red when sensor recording is being made

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    // The toggle is enabled
                    holder.setBackground(getResources().getDrawable(R.drawable.toggle_button_off_background));
                    clearSensorTextViews();

                    if (!bandSubscriptionTaskRunning) {

                        // Kick off the  loader
                        getLoaderManager().restartLoader(Constants.BAND_SUSCRIPTION_LOADER, null, bandSensorSubscriptionLoader);

                    }


                } else {
                    // The toggle is disabled
                    bandSubscriptionTaskRunning = false;
                    holder.setBackground(getResources().getDrawable(R.drawable.toggle_button_on_background));

                    if(task != null ){

                        task.cancel(true);

                    }

                    resetTimer();
                    resetSaveDataButton();
                    clearSensorTextViews();
                    disconnectBand();

                }
            }
        });

        //shows loader
        showLoadingView(false);

        saveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (bandSubscriptionTaskRunning) {

                    boolean sensorSelected = false;

                    saveClicked = true;

                    for (SensorReading sr : sensorReadings) {

                        Log.v(LOG_TAG, "Check checkbox of " + sr.getSensorName());

                        SensorReadingView sensorReadingView = getSensorReadingView(sr);

                        if (sensorReadingView.getSensorCheckBox().isChecked()) {

                            sensorSelected = true;

                        }

                    }

                    if (!sensorSelected) {
                        stopButtonClicked();
                        //Band is connected, but no sensor is selected to take any data point
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.no_data_point), Toast.LENGTH_SHORT).show();

                    } else {
                        if (saveDataButton.isChecked()) {
                            resetSaveDataButton();
                        } else {
                            saveDataButton.setChecked(true);
                            saveButtonHolder.setBackground(getResources().getDrawable(R.drawable.save_button_on));
                        }
                    }

                } else {

                    Toast.makeText(MainActivity.this, getResources().getString(R.string.no_data_point), Toast.LENGTH_SHORT).show();
                }

            }
        });

        //Register broadcast receiver to reset activity if any checkbox is selected
        registerReceiver(resetSensorReadingReceiver, new IntentFilter(Constants.RESET_SENSOR_READING));

        //Register broadcast receiver to print values on screen from BandSensorsSubscriptionLoader
        registerReceiver(displayVaueReceiver, new IntentFilter(Constants.DISPLAY_VALUE));

        //Register broadcast receiver to save SensorReading objects from BandSensorsSubscriptionLoader
        registerReceiver(sensorReadingObjectReceiver, new IntentFilter(Constants.SENSOR_READING_OBJECT_RECEIVER));

        //Register broadcast receiver to create csv file from BandConnectionService
        //in case that MS band has been disconnected while recording data
        registerReceiver(createCSVReceiver, new IntentFilter(Constants.CREATE_CSV_RECEIVER));

        //Register broadcast receiver to reset activity if any checkbox is selected
        registerReceiver(timeReceiver, new IntentFilter(getClass().getPackage() + ".BROADCAST"));

        //Microsoft band 2 communication status textview
        bandStatusTxt = (TextView) toolbar.findViewById(R.id.band_status);

    }

    /**
     * Helper method to show loader on screen.
     *
     * @param loadingState set to true to show loader on screen, false otherwise.
     */
    private void showLoadingView(boolean loadingState) {

        if (loadingState) {
            findViewById(R.id.main_layout).setVisibility(View.GONE);
            saveDataButton.setVisibility(View.GONE);
            settingsButton.setVisibility(View.GONE);
            findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
            saveDataButton.setVisibility(View.VISIBLE);
            settingsButton.setVisibility(View.VISIBLE);
            findViewById(R.id.loading_layout).setVisibility(View.GONE);
        }

    }

    /**
     * This method retrieves the output file from root storage directory of the MyBand app
     *
     * @param dir root storage directory of the MyBand app
     * @param  samplingRate If 'Frequency based' output csv file option is selected, this sampling rate label is going to be
     *                      attached to the output files label.
     * @return File Containing the output csv files path.
     */
    private File getCsvOutputFile(File dir, String samplingRate) {

        // the name of the file to export with
        date = new Date();
        displayDate = new SimpleDateFormat("yyMMdd_HHmmSS").format(date);
        filename = labelPrefix + displayDate + "_" + samplingRate + fileNameExtension;

        return new File(dir, filename);
    }


    /**
     * This method retrieves the root storage directory from MyBand app.
     * If the directory does not exist, it creates it. This is the same directory
     * on which every output file will be stored.
     *
     * @return File Containing MyBand app root storage directory.
     */
    private File getOutputDirectory() {

        File directory = null;

        //Checks whether the android device has an external storage, if it does, the root directory
        //is going to be retrieved/created from there. If it does not, the root directory
        //is going to be retrieved/created on the internal memory.
        if (Environment.getExternalStorageState() == null) {
            //create new file directory object

            directory = new File(Environment.getDataDirectory()
                    + "/MyBand/");

            // if no directory exists, create new directory
            if (!directory.exists()) {
                directory.mkdir();
            }

        } else if (Environment.getExternalStorageState() != null) {            // if phone DOES have sd card

            // search for directory on SD card
            directory = new File(Environment.getExternalStorageDirectory()
                    + "/Myband/");

            // if no directory exists, create new directory
            if (!directory.exists()) {
                directory.mkdir();
            }

        }// end of SD card checking

        return directory;

    }


    /**
     * This method retrieves the SensorReadingView corresponding to the sensor data input in the argument.
     *
     * @param  sr SensorReading data of the sensor of interest.
     * @return SensorReadingView of the sensor of interest.
     */
    private SensorReadingView getSensorReadingView(SensorReading sr) {

        int resourceID = 0;

        switch (sr.getSensorName()) {
            case Constants.HEART_RATE_SENSOR_LABEL:
                resourceID = R.id.heart_rate_sensorview;
                break;

            case Constants.RR_INTERVAL_SENSOR_LABEL:
                resourceID = R.id.rr_interval_sensorview;
                break;


            case Constants.ACCELEROMETER_SENSOR_LABEL:
                resourceID = R.id.accelerometer_sensorview;
                break;

            case Constants.ALTIMETER_SENSOR_LABEL:
                resourceID = R.id.altimeter_sensorview;
                break;

            case Constants.AMBIENT_LIGHT_SENSOR_LABEL:
                resourceID = R.id.ambient_light_sensorview;
                break;

            case Constants.BAROMETER_SENSOR_LABEL:
                resourceID = R.id.barometer_sensorview;
                break;

            case Constants.GSR_SENSOR_LABEL:
                resourceID = R.id.gsr_sensorview;
                break;

            case Constants.CALORIES_SENSOR_LABEL:
                resourceID = R.id.calories_sensorview;
                break;

            case Constants.DISTANCE_SENSOR_LABEL:
                resourceID = R.id.distance_sensorview;
                break;

            case Constants.BAND_CONTACT_SENSOR_LABEL:
                resourceID = R.id.band_contact_sensorview;
                break;

            case Constants.GYROSCOPE_SENSOR_LABEL:
                resourceID = R.id.gyroscope_sensorview;
                break;

            case Constants.PEDOMETER_SENSOR_LABEL:
                resourceID = R.id.pedometer_sensorview;
                break;

            case Constants.SKIN_TEMPERATURE_SENSOR_LABEL:
                resourceID = R.id.skin_temperature_sensorview;
                break;

            case Constants.UV_LEVEL_SENSOR_LABEL:
                resourceID = R.id.uv_sensorview;
                break;

            default:
                resourceID = 0;
                break;
        }

        SensorReadingView sensorReadingView = null;

        if (resourceID != 0) {
            sensorReadingView = (SensorReadingView) findViewById(resourceID);
        }

        return sensorReadingView;
    }

    /**
     * This method initializes the ListView where the Sensors of interest are going to be selected.
     * First, it creates the ArrayList<SensorReading> that the ListView is going to be populated with, and then
     * assign each object with its corresponding id for later reference.
     */
    private void initSensorListView() {

        //Creates the ArrayList<SensorReading>
        sensorReadings = new ArrayList<>();
        sensorReadings.add(new SensorReading(this, Constants.HEART_RATE_SENSOR_ID, ""));
        sensorReadings.add(new SensorReading(this, Constants.RR_INTERVAL_SENSOR_ID, ""));
        sensorReadings.add(new SensorReading(this, Constants.ACCELEROMETER_SENSOR_ID, ""));
        sensorReadings.add(new SensorReading(this, Constants.ALTIMETER_SENSOR_ID, ""));
        sensorReadings.add(new SensorReading(this, Constants.AMBIENT_LIGHT_SENSOR_ID, ""));
        sensorReadings.add(new SensorReading(this, Constants.BAROMETER_SENSOR_ID, ""));
        sensorReadings.add(new SensorReading(this, Constants.GSR_SENSOR_ID, ""));
        sensorReadings.add(new SensorReading(this, Constants.CALORIES_SENSOR_ID, ""));
        sensorReadings.add(new SensorReading(this, Constants.DISTANCE_SENSOR_ID, ""));
        sensorReadings.add(new SensorReading(this, Constants.GYROSCOPE_SENSOR_ID, ""));
        sensorReadings.add(new SensorReading(this, Constants.PEDOMETER_SENSOR_ID, ""));
        sensorReadings.add(new SensorReading(this, Constants.SKIN_TEMPERATURE_SENSOR_ID, ""));
        sensorReadings.add(new SensorReading(this, Constants.UV_LEVEL_SENSOR_ID, ""));
        sensorReadings.add(new SensorReading(this, Constants.BAND_CONTACT_SENSOR_ID, ""));

        //Adds the SensorReading objects to the ListView
        for (SensorReading sr : sensorReadings) {

            SensorReadingView v = new SensorReadingView(this, sr);
            mListView.addView(v);

        }

        //Assing each SensorReading object within the ListView its corresponding id.
        for (int i = 0; i < sensorReadings.size(); i++) {

            View sensorView = mListView.getChildAt(i);

            switch (i) {
                case Constants.HEART_RATE_SENSOR_ID - 1:
                    sensorView.setId(R.id.heart_rate_sensorview);
                    break;

                case Constants.RR_INTERVAL_SENSOR_ID - 1:
                    sensorView.setId(R.id.rr_interval_sensorview);
                    break;

                case Constants.ACCELEROMETER_SENSOR_ID - 1:
                    sensorView.setId(R.id.accelerometer_sensorview);
                    break;

                case Constants.ALTIMETER_SENSOR_ID - 1:
                    sensorView.setId(R.id.altimeter_sensorview);
                    break;

                case Constants.AMBIENT_LIGHT_SENSOR_ID - 1:
                    sensorView.setId(R.id.ambient_light_sensorview);
                    break;

                case Constants.BAROMETER_SENSOR_ID - 1:
                    sensorView.setId(R.id.barometer_sensorview);
                    break;

                case Constants.GSR_SENSOR_ID - 1:
                    sensorView.setId(R.id.gsr_sensorview);
                    break;

                case Constants.CALORIES_SENSOR_ID - 1:
                    sensorView.setId(R.id.calories_sensorview);
                    break;

                case Constants.DISTANCE_SENSOR_ID - 1:
                    sensorView.setId(R.id.distance_sensorview);
                    break;

                case Constants.BAND_CONTACT_SENSOR_ID - 1:
                    sensorView.setId(R.id.band_contact_sensorview);
                    break;

                case Constants.GYROSCOPE_SENSOR_ID - 1:
                    sensorView.setId(R.id.gyroscope_sensorview);
                    break;

                case Constants.PEDOMETER_SENSOR_ID - 1:
                    sensorView.setId(R.id.pedometer_sensorview);
                    break;

                case Constants.SKIN_TEMPERATURE_SENSOR_ID - 1:
                    sensorView.setId(R.id.skin_temperature_sensorview);
                    break;

                case Constants.UV_LEVEL_SENSOR_ID - 1:
                    sensorView.setId(R.id.uv_sensorview);
                    break;

            }

        }

    }

    /**
     * This method is called when the sensor reading recording is aborted by clicking the 'Stop' button.
     */
    private void stopButtonClicked() {

        bandSubscriptionTaskRunning = false;

        resetSaveDataButton();
        resetToggleButton();
        clearSensorTextViews();
        disconnectBand();
    }

    /**
     * This method restores the saveDataButtons' default state.
     */
    private void resetSaveDataButton() {
        saveDataButton.setEnabled(true);
        saveDataButton.setChecked(false);
        saveButtonHolder.setBackground(getResources().getDrawable(R.drawable.save_button_off));
    }

    /**
     * This method restores the toggle buttons' default state.
     */
    private void resetToggleButton() {

        toggle.setChecked(false);

    }

    /**
     * This method display a message on the desired textview defined by the requestCode.
     * @param  message Message to be displayed
     * @param  requestCode Textviews unique id on which the message is going to be displayed.
     *
     */
    private void appendToUI(final String message, String requestCode) {

        View v;
        TextView sensorValueTextView = null;

        switch (requestCode) {

            case Constants.BAND_STATUS:
                sensorValueTextView = bandStatusTxt;
                break;

            case Constants.UV_LEVEL:

                v = findViewById(R.id.uv_sensorview);
                sensorValueTextView = (TextView) v.findViewById(R.id.sensor_value);
                break;

            case Constants.SKIN_TEMPERATURE:
                v = findViewById(R.id.skin_temperature_sensorview);
                sensorValueTextView = (TextView) v.findViewById(R.id.sensor_value);
                break;

            case Constants.PEDOMETER:
                v = findViewById(R.id.pedometer_sensorview);
                sensorValueTextView = (TextView) v.findViewById(R.id.sensor_value);
                break;

            case Constants.GYROSCOPE:
                v = findViewById(R.id.gyroscope_sensorview);
                sensorValueTextView = (TextView) v.findViewById(R.id.sensor_value);
                break;

            case Constants.DISTANCE:
                v = findViewById(R.id.distance_sensorview);
                sensorValueTextView = (TextView) v.findViewById(R.id.sensor_value);
                break;

            case Constants.BAND_CONTACT:
                v = findViewById(R.id.band_contact_sensorview);
                sensorValueTextView = (TextView) v.findViewById(R.id.sensor_value);
                break;

            case Constants.CALORIES:
                v = findViewById(R.id.calories_sensorview);
                sensorValueTextView = (TextView) v.findViewById(R.id.sensor_value);
                break;

            case Constants.RR_INTERVAL:
                v = findViewById(R.id.rr_interval_sensorview);
                sensorValueTextView = (TextView) v.findViewById(R.id.sensor_value);
                break;

            case Constants.GSR:
                v = findViewById(R.id.gsr_sensorview);
                sensorValueTextView = (TextView) v.findViewById(R.id.sensor_value);
                break;

            case Constants.BAROMETER:
                v = findViewById(R.id.barometer_sensorview);
                sensorValueTextView = (TextView) v.findViewById(R.id.sensor_value);
                break;

            case Constants.ACCELEROMETER:
                v = findViewById(R.id.accelerometer_sensorview);
                sensorValueTextView = (TextView) v.findViewById(R.id.sensor_value);
                break;


            case Constants.ALTIMETER:
                v = findViewById(R.id.altimeter_sensorview);
                sensorValueTextView = (TextView) v.findViewById(R.id.sensor_value);
                break;

            case Constants.HEART_RATE:
                v = findViewById(R.id.heart_rate_sensorview);
                sensorValueTextView = (TextView) v.findViewById(R.id.sensor_value);
                break;

            case Constants.AMBIENT_LIGHT:
                v = findViewById(R.id.ambient_light_sensorview);
                sensorValueTextView = (TextView) v.findViewById(R.id.sensor_value);
                break;


        }

        sensorValueTextView.setText(message);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (bandSubscriptionTaskRunning) {

            toggle.setChecked(true);

        }

    }

    /**
     * This method restores all TextViews on screen to their default values.
     *
     */
    private void clearSensorTextViews() {

        bandStatusTxt.setText(getResources().getString(R.string.select_option));

        for (int i = 0; i < mListView.getChildCount(); i++) {
            TextView sensorTextView = (TextView) mListView.getChildAt(i).findViewById(R.id.sensor_value);
            sensorTextView.setText("");
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Unregister all BroadcastReceivers
        unregisterReceiver(resetSensorReadingReceiver);
        unregisterReceiver(displayVaueReceiver);
        unregisterReceiver(sensorReadingObjectReceiver);
        unregisterReceiver(createCSVReceiver);

        try {
            unregisterSensorListeners();
        } catch (BandIOException e) {
            e.printStackTrace();
        }

        disconnectBand();

    }


    private void disconnectBand() {

        if (client != null) {

            try {
                client.disconnect().await(3, TimeUnit.SECONDS);

            } catch (TimeoutException e) {
                Log.e(LOG_TAG, "TimeoutException: " + e.toString());
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "InterruptedException: " + e.toString());
            } catch (BandException e) {
                Log.e(LOG_TAG, "BandException: " + e.toString());
            }

        }

    }

    private void unregisterSensorListeners() throws BandIOException {
        client.getSensorManager().unregisterAllListeners();
    }


    private BroadcastReceiver resetSensorReadingReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            stopButtonClicked();

        }


    };



    private BroadcastReceiver timeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            long seconds = intent.getExtras().getLong(getClass().getPackage() + ".TIME");
            clock.setText("" + (TIMER_DURATION - seconds));


            Log.v(LOG_TAG, " timeReceiver seconds: " + seconds);

            if(seconds == TIMER_DURATION){


                csvFileCounter = Constants.SAMPLE_RATE_OPTIONS.length - 1;

                Log.v(LOG_TAG, " timeReceiver csvFileCounter: " + csvFileCounter);
                Log.v(LOG_TAG, " timeReceiver bandSubscriptionTaskRunning: " + bandSubscriptionTaskRunning);

                if (bandSubscriptionTaskRunning) {

                    // Kick off saveDataCursorLoader

                    resetToggleButton();
                    resetTimer();

                    Bundle extraBundle = new Bundle();
                    extraBundle.putLong(Constants.SENSOR_TIME, timeBasedCSVDate);


                    getLoaderManager().restartLoader(Constants.CREATE_CSV_LOADER, extraBundle, saveDataCursorLoader);

                }

            }

        }


    };

    private void resetTimer() {

        clock.setText("");

    }

    private BroadcastReceiver createCSVReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.v(LOG_TAG, "createCSVReceiver onReceive");

            csvFileCounter = Constants.SAMPLE_RATE_OPTIONS.length - 1;

            Log.v(LOG_TAG, "csvFileCounter: " + csvFileCounter);

            // Kick off saveDataCursorLoader
            getLoaderManager().restartLoader(Constants.CREATE_CSV_LOADER, null, saveDataCursorLoader);

        }

    };


    private BroadcastReceiver sensorReadingObjectReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            SensorReading receivedSensor = intent.getParcelableExtra(Constants.SERVICE_EXTRA);

            Log.w(LOG_TAG, "sensorReadingObjectReceiver : onReceive:  " + receivedSensor.getSensorName());

            //values.add(receivedSensor);


        }


    };


    private BroadcastReceiver displayVaueReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

//            Log.v(LOG_TAG, "displayVaueReceiver: onReceive ");

            String sensor = intent.getStringExtra(Constants.SENSOR);
            String value = intent.getStringExtra(Constants.VALUE);

            Log.v(LOG_TAG, "displayVaueReceiver: sensor: " + sensor);
            Log.v(LOG_TAG, "displayVaueReceiver: value: " + value);

            //TODO: TEST COMMENTED
           // appendToUI(value, sensor);

        }


    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.v(LOG_TAG, "onActivityResult");
    }


    private LoaderManager.LoaderCallbacks<ConnectionState> bandSensorSubscriptionLoader

            = new LoaderManager.LoaderCallbacks<ConnectionState>() {

        @Override
        public Loader<ConnectionState> onCreateLoader(int i, Bundle bundle) {

            Log.v(LOG_TAG, "bandSensorSubscriptionLoader: onCreateLoader");

            showLoadingView(true);

            return new BandSensorsSubscriptionLoader(MainActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<ConnectionState> loader, ConnectionState cs) {

            Log.v(LOG_TAG, "bandSensorSubscriptionLoader: onLoadFinished ");

            showLoadingView(false);


            getLoaderManager().destroyLoader(loader.getId());

            String userMsg = "";

            if (cs != null) {
                Log.v(LOG_TAG, "cs != null");
                Log.v(LOG_TAG, cs.toString());

                switch (cs) {

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

                Log.v(LOG_TAG, userMsg);

                if (!cs.equals(ConnectionState.CONNECTED)) {
                    Log.v(LOG_TAG, "ConnectionState != CONNECTED");
                    resetToggleButton();
                    appendToUI(userMsg, Constants.BAND_STATUS);

                } else {


                    Log.v(LOG_TAG, "ConnectionState == CONNECTED");
                    bandSubscriptionTaskRunning = true;

                }

            } else {

                //Band isnt paired with phone
                resetToggleButton();
                appendToUI("Band isn't paired with your phone.", Constants.BAND_STATUS);
                Log.v(LOG_TAG, "cs == null");
                Log.v(LOG_TAG, "Band isn't paired with your phone.");

            }

        }

        @Override
        public void onLoaderReset(Loader<ConnectionState> loader) {

            Log.v(LOG_TAG, "bandSensorSubscriptionLoader: onLoaderReset");

        }
    };


    private LoaderManager.LoaderCallbacks<Cursor> saveDataCursorLoader
            = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

            Log.v(LOG_TAG, "saveDataCursorLoader: onCreateLoader");

            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            int defaultValue = getResources().getInteger(R.integer.csv_mode_key_default_value);
            int csvMode = sharedPref.getInt(getString(R.string.csv_mode_key), defaultValue);

            switch (csvMode) {

                case 0:

                    Log.v(LOG_TAG,"FREQUENCY BASED CSV");

                    // Define a projection that specifies the columns from the table we care about.
                    String[] projection = {
                            ReadingEntry._ID,
                            ReadingEntry.COLUMN_TIME,
                            ReadingEntry.COLUMN_SAMPLE_RATE,
                            ReadingEntry.COLUMN_SENSOR_ID,
                            ReadingEntry.COLUMN_SENSOR_VALUE};

                    String sortOrder = ReadingEntry._ID;

                    // This loader will execute the ContentProvider's query method on a background thread
                    String selection = ReadingEntry.COLUMN_SAMPLE_RATE + "=? AND " + ReadingEntry.COLUMN_TIME + ">?";

                    Log.v(LOG_TAG, "selection: " + selection);
                    Log.v(LOG_TAG, "csvFileCounter: " + csvFileCounter);
                    Log.v(LOG_TAG, "COLUMN_SAMPLE_RATE : " + Constants.SAMPLE_RATE_OPTIONS[csvFileCounter]);

                    String saveTimeSelecionArg = "" + timeBasedCSVDate;

                    String[] selectionArgs = {Constants.SAMPLE_RATE_OPTIONS[csvFileCounter], saveTimeSelecionArg};

                    Log.v(LOG_TAG, "selectionArgs1: " + selectionArgs[0]);
                    Log.v(LOG_TAG, "selectionArgs2: " + selectionArgs[1]);

                    return new CursorLoader(MainActivity.this,   // Parent activity context
                            ReadingEntry.CONTENT_URI,   // Provider content URI to query
                            projection,             // Columns to include in the resulting Cursor
                            selection,                   //  selection clause
                            selectionArgs,                   //  selection arguments
                            sortOrder);                  //  sort order


                case 1:


                    Log.v(LOG_TAG,"TIME BASED CSV");

                    // Define a projection that specifies the columns from the table we care about.
                    String[] projection2 = {
                            ReadingEntry._ID,
                            ReadingEntry.COLUMN_TIME,
                            ReadingEntry.COLUMN_SAMPLE_RATE,
                            ReadingEntry.COLUMN_SENSOR_ID,
                            ReadingEntry.COLUMN_SENSOR_VALUE};

                    // This loader will execute the ContentProvider's query method on a background thread

                    String selection2 = ReadingEntry.COLUMN_TIME + ">?";
                    //String selection2 = null;

                    String selectionArg = "" + timeBasedCSVDate;

                    String[] selectionArgs2 = {selectionArg};
                    //String[] selectionArgs2 = null;

                    String sortOrder2 = ReadingEntry.COLUMN_TIME;
                    //String sortOrder2 = ReadingEntry._ID;

                    return new CursorLoader(MainActivity.this,   // Parent activity context
                            ReadingEntry.CONTENT_URI,   // Provider content URI to query
                            projection2,             // Columns to include in the resulting Cursor
                            selection2,                   //  selection clause
                            selectionArgs2,                   //  selection arguments
                            sortOrder2);                  //  sort order


                case 2:

                    Log.v(LOG_TAG,"SAMPLE BASED CSV");

                    // Define a projection that specifies the columns from the table we care about.
                    String[] projection3 = {
                            "MAX(" + ReadingEntry.COLUMN_SAMPLE_RATE + ")",
                            ReadingEntry.COLUMN_SENSOR_ID
                    };

                    String selection3 = ReadingEntry.COLUMN_TIME + ">?";
                    //String selection2 = null;

                    String selectionArg3 = "" + timeBasedCSVDate;

                    String[] selectionArgs3 = {selectionArg3};
                    //String[] selectionArgs2 = null;

                    String sortOrder3 = ReadingEntry.COLUMN_TIME;
                    //String sortOrder2 = ReadingEntry._ID;

                    return new CursorLoader(MainActivity.this,   // Parent activity context
                            ReadingEntry.CONTENT_URI,   // Provider content URI to query
                            projection3,             // Columns to include in the resulting Cursor
                            selection3,                   //  selection clause
                            selectionArgs3,                   //  selection arguments
                            sortOrder3);                  //  sort order


            }


            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor c) {

            Log.v(LOG_TAG, "saveDataCursorLoader: onLoadFinished");


            switch (loader.getId()) {

                case Constants.CREATE_CSV_LOADER:

                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                    int defaultValue = getResources().getInteger(R.integer.csv_mode_key_default_value);
                    int csvMode = sharedPref.getInt(getString(R.string.csv_mode_key), defaultValue);

                    switch (csvMode) {

                        case 0:

                            Log.v(LOG_TAG, "FREQUENCY BASED CSV");

                            FileWriter fw = null;

                            try {

                                int rowcount = c.getCount();
                                int colcount = c.getColumnCount();

                                if (rowcount > 0) {

                                    outputDirectory = getOutputDirectory();

                                    String srLabel = getSensorSRlabel(Constants.SAMPLE_RATE_OPTIONS[csvFileCounter]);

                                    Log.v(LOG_TAG, "srLabel: " + srLabel);

                                    saveFile = getCsvOutputFile(outputDirectory, srLabel);

                                    fw = new FileWriter(saveFile);

                                    BufferedWriter bw = new BufferedWriter(fw);

                                    c.moveToFirst();

//                            for (int i = 0; i < colcount; i++) {
//
//                                if (i != (colcount - 1)) {
//
//                                    bw.write(c.getColumnName(i) + ",");
//
//                                } else {
//
//                                    bw.write(c.getColumnName(i));
//
//                                }
//                            }

//                            bw.newLine();

                                    for (int i = 0; i < rowcount; i++) {

                                        c.moveToPosition(i);

                                        for (int j = 0; j < colcount; j++) {

                                            String cellValue = c.getString(j);
                                            Log.w(LOG_TAG, j + " : " + i + " = " + cellValue);

                                            String fileValue = "";

                                            if (j == 1) { //Time column

                                                //TODO: TEST CODE

                                                cellValue = "" + (Long.parseLong(cellValue.trim()) - timeBasedCSVDate);

//                                                long time = Long.parseLong(cellValue.trim());
//
//                                                String year = new SimpleDateFormat("yyyy").format(time);
//                                                String month = new SimpleDateFormat("MM").format(time);
//                                                String day = new SimpleDateFormat("dd").format(time);
//                                                String hour = new SimpleDateFormat("HH").format(time);
//                                                String minute = new SimpleDateFormat("mm").format(time);
//                                                String sec = new SimpleDateFormat("ss").format(time);
//
//                                                cellValue = year + "," + month + "," + day + "," + hour + "," + minute + "," + sec;
//
                                            }

                                            if (j != (colcount - 1)) {

                                                Log.w(LOG_TAG, j + " != " + (colcount - 1));
                                                fileValue = cellValue + ",";

                                            } else {
                                                Log.w(LOG_TAG, j + " == " + (colcount - 1));
                                                fileValue = cellValue;
                                            }

                                            bw.write(fileValue);

                                        }

                                        bw.newLine();
                                    }

                                    bw.flush();

                                    Log.w(LOG_TAG, "Datapoint Exported Successfully.");


                                    //If SaveDatapoint button is clicked while MSBand still connected, it saves the
                                    //newest values
                                    values.clear();


                                    //Save datapoint loader destroyed, so that if user comes back from
                                    //CSV file viewer, it does not create a new one
                                    getLoaderManager().destroyLoader(Constants.CREATE_CSV_LOADER);


                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(LOG_TAG, "FileWriter IOException: " + e.toString());
                            }

                            Log.v(LOG_TAG, "csvFileCounter: " + csvFileCounter);

                            if (csvFileCounter == 0) {

                                showLoadingView(false);
                                //shows "OPEN CSV" action on a snackbar
                                Snackbar mySnackbar = Snackbar.make(mMainLayout,
                                        "Files saved at" + outputDirectory.getAbsolutePath().toString(), Snackbar.LENGTH_LONG);
                                //mySnackbar.setAction(R.string.open, new OpenCSVFileListener());
                                mySnackbar.show();

                            } else {

                                csvFileCounter -= 1;
                                // Kick off saveDataCursorLoader
                                getLoaderManager().restartLoader(Constants.CREATE_CSV_LOADER, null, saveDataCursorLoader);
                            }


                            break;

                        case 1:

                            Log.v(LOG_TAG, "TIME BASED CSV");

                            FileWriter fw2 = null;

                            try {

                                int rowcount = c.getCount();
                                int colcount = c.getColumnCount();

                                Log.v(LOG_TAG, "rowcount: " + rowcount);
                                Log.v(LOG_TAG, "colcount: " + colcount);

                                if (rowcount > 0) {

                                    outputDirectory = getOutputDirectory();

                                    Log.v(LOG_TAG, "outputDirectory.getAbsolutePath().toString(): " + outputDirectory.getAbsolutePath().toString());

                                    String srLabel = "TB";

                                    Log.v(LOG_TAG, "srLabel: " + srLabel);

                                    saveFile = getCsvOutputFile(outputDirectory, srLabel);

                                    fw2 = new FileWriter(saveFile);

                                    BufferedWriter bw = new BufferedWriter(fw2);

                                    c.moveToFirst();

                                    for (int i = 0; i < rowcount; i++) {

                                        c.moveToPosition(i);

                                        for (int j = 0; j < colcount; j++) {

                                            String cellValue = c.getString(j);
                                            Log.w(LOG_TAG, j + " : " + i + " = " + cellValue);

                                            String fileValue = "";

                                            if (j == 1) { //Time column
                                                cellValue = "" + (Long.parseLong(cellValue.trim()) - timeBasedCSVDate);

//                                                long time = Long.parseLong(cellValue.trim());
//
//                                                String year = new SimpleDateFormat("yyyy").format(time);
//                                                String month = new SimpleDateFormat("MM").format(time);
//                                                String day = new SimpleDateFormat("dd").format(time);
//                                                String hour = new SimpleDateFormat("HH").format(time);
//                                                String minute = new SimpleDateFormat("mm").format(time);
//                                                String sec = new SimpleDateFormat("ss").format(time);
//
//                                                cellValue = year + "," + month + "," + day + "," + hour + "," + minute + "," + sec;
//
                                            }

                                            if (j != (colcount - 1)) {

                                                Log.w(LOG_TAG, j + " != " + (colcount - 1));
                                                fileValue = cellValue + ",";

                                            } else {
                                                Log.w(LOG_TAG, j + " == " + (colcount - 1));
                                                fileValue = cellValue;
                                            }

                                            bw.write(fileValue);

                                        }

                                        bw.newLine();
                                    }

                                    bw.flush();

                                    Log.w(LOG_TAG, "Datapoint Exported Successfully.");


                                    //If SaveDatapoint button is clicked while MSBand still connected, it saves the
                                    //newest values
                                    values.clear();


                                    //Save datapoint loader destroyed, so that if user comes back from
                                    //CSV file viewer, it does not create a new one
                                    getLoaderManager().destroyLoader(Constants.CREATE_CSV_LOADER);


                                    showLoadingView(false);
                                    //shows "OPEN CSV" action on a snackbar
                                        Snackbar mySnackbar = Snackbar.make(mMainLayout,
                                            "Files saved at" + outputDirectory.getAbsolutePath().toString(), Snackbar.LENGTH_LONG);
                                    //mySnackbar.setAction(R.string.open, new OpenCSVFileListener());
                                    mySnackbar.show();


                                }

                                else{
                                    //Save datapoint loader destroyed, so that if user comes back from
                                    //CSV file viewer, it does not create a new one
                                    getLoaderManager().destroyLoader(Constants.CREATE_CSV_LOADER);
                                    Toast.makeText(MainActivity.this,"No records to export CSV",Toast.LENGTH_SHORT).show();
                                }


                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(LOG_TAG, "FileWriter IOException: " + e.toString());
                            }


                            break;

                        case 2:

                            Log.v(LOG_TAG, "SAMPLE BASED CSV");


                            try {

                                int rowcount = c.getCount();
                                int colcount = c.getColumnCount();

                                Log.v(LOG_TAG, "rowcount: " + rowcount);
                                Log.v(LOG_TAG, "colcount: " + colcount);

                                if (rowcount > 0) {

                                    c.moveToFirst();
                                    c.moveToPosition(0);
                                    String maxSampleRate = c.getString(0).trim();
                                    String maxSampleRateSensorID = c.getString(1).trim();

                                    Log.v(LOG_TAG,"Max Sample Rate Sensor ID : " + maxSampleRateSensorID);

                                    Bundle bundle = new Bundle();
                                    bundle.putString("maxSampleRate", maxSampleRate);
                                    bundle.putString("maxSampleRateSensorID", maxSampleRateSensorID);

                                    // Kick off the  loader
                                    getLoaderManager().restartLoader(Constants.SAMPLE_BASED_LOADER, bundle, SampleBasedCSVFileLoader);


                                    //Save datapoint loader destroyed, so that if user comes back from
                                    //CSV file viewer, it does not create a new one
                                    getLoaderManager().destroyLoader(Constants.CREATE_CSV_LOADER);

                                } else{
                                    //Save datapoint loader destroyed, so that if user comes back from
                                    //CSV file viewer, it does not create a new one
                                    getLoaderManager().destroyLoader(Constants.CREATE_CSV_LOADER);
                                    Toast.makeText(MainActivity.this,"No records to export CSV",Toast.LENGTH_SHORT).show();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(LOG_TAG, "FileWriter IOException: " + e.toString());
                            }

                            break;

                    }


            }


        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

            Log.v(LOG_TAG, "saveDataCursorLoader: onLoaderReset");

        }

    };


    private LoaderManager.LoaderCallbacks<Cursor> SampleBasedCSVFileLoader
            = new LoaderManager.LoaderCallbacks<Cursor>() {

        //Once the max sample rate has been selected, proceed to retrieve samples timestamps

        Context mContext = MainActivity.this;

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

            Log.v(LOG_TAG, "SampleBasedCSVFileLoader: onCreateLoader");
            showLoadingView(true);


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

            String selection = ReadingEntry.COLUMN_SAMPLE_RATE + "=? AND " + ReadingEntry.COLUMN_TIME + ">? AND " + ReadingEntry.COLUMN_SENSOR_ID + "=?";

            String saveTimeSelecionArg = "" + timeBasedCSVDate;

            String[] selectionArgs = {bundle.getString("maxSampleRate"), saveTimeSelecionArg, bundle.getString("maxSampleRateSensorID")};

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

            //sampleTimeStamps stores sample time stamps that are going to be searched for all sensors,
            // in order to concatenate
            // each sensor reading into a single vector for each timestamp

            sampleTimeStamps = new ArrayList<>();

            try {

                int rowcount = c.getCount();

                if (rowcount > 0) {

                    c.moveToFirst();

                    for (int i = 0; i < rowcount; i++) {

                        c.moveToPosition(i);

                        long timeStamp = Long.parseLong(c.getString(1).trim());
                        sampleTimeStamps.add(timeStamp);

                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "FileWriter IOException: " + e.toString());
            }

            timeStampReference = sampleTimeStamps.get(0);

            for (int i= 0; i< sampleTimeStamps.size();i++){
                Log.v(LOG_TAG,"CSV TIME STAMP: " + (sampleTimeStamps.get(i) - timeStampReference));
            }

            long minTime = sampleTimeStamps.get(0);
            long maxTime;

            maxTime = sampleTimeStamps.get((0 + 1));

            sampleTimeStampsIterator = 0;

            Bundle bundle = new Bundle();
            bundle.putLong("minTime", minTime);
            bundle.putLong("maxTime", maxTime);




          // Kick off the  loader
            getLoaderManager().restartLoader(Constants.TIME_STAMP_SENSOR_READING_LOADER, bundle, timeStampSensorReadingLoader);

            //Save datapoint loader destroyed, so that if user comes back from
            //CSV file viewer, it does not create a new one
            getLoaderManager().destroyLoader(Constants.SAMPLE_BASED_LOADER);

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            Log.v(LOG_TAG, "SampleBasedCSVFileLoader: onLoaderReset");
        }

    };

    private LoaderManager.LoaderCallbacks<Cursor> timeStampSensorReadingLoader
            = new LoaderManager.LoaderCallbacks<Cursor>() {


        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

            Log.v(LOG_TAG, "timeStampSensorReadingLoader: onCreateLoader");

            long minTime = bundle.getLong("minTime");
            long maxTime = bundle.getLong("maxTime");

            Log.v(LOG_TAG,"timeStampSensorReadingLoader: minTime: " + minTime);
            Log.v(LOG_TAG,"timeStampSensorReadingLoader: maxTime: " + maxTime);

            if (minTime != maxTime) {

                // Define a projection that specifies the columns from the table we care about.
                String[] projection = {
                        ReadingEntry._ID,
                        ReadingEntry.COLUMN_TIME,
                        ReadingEntry.COLUMN_SAMPLE_RATE,
                        ReadingEntry.COLUMN_SENSOR_ID,
                        ReadingEntry.COLUMN_SENSOR_VALUE};

                String sortOrder = ReadingEntry._ID;

                // This loader will execute the ContentProvider's query method on a background thread

                String selection = ReadingEntry.COLUMN_TIME + ">=?  AND " + ReadingEntry.COLUMN_TIME + "<?";

                String[] selectionArgs = {("" + minTime), ("" + maxTime)};

                return new CursorLoader(MainActivity.this,   // Parent activity context
                        ReadingEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selection,                   //  selection clause
                        selectionArgs,                   //  selection arguments
                        sortOrder);                  //  sort order

            }

            return  null;


        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor c) {

            Log.v(LOG_TAG, "timeStampSensorReadingLoader: onLoadFinished ");

            Bundle b = c.getExtras();

            CursorLoader p = (CursorLoader) loader;
            String[] selArgs = p.getSelectionArgs();
            String timeStamp = selArgs[0];

            ArrayList<Integer> selectedSensorID = getSelectedSensorID();
            String header = "class,time," + getColumnsLabelsHeader(selectedSensorID);

            String[] sensorReadings = new String[selectedSensorID.size()];

            FileWriter fw2 = null;

            try {

                int rowcount = c.getCount();
                int colcount = c.getColumnCount();

                Log.v(LOG_TAG, "rowcount: " + rowcount);
                Log.v(LOG_TAG, "colcount: " + colcount);

                if (rowcount > 0) {

                    sensorReadings = getSensorReadings(c , selectedSensorID);
                    String values = "";

                    for (int i = 0 ; i < selectedSensorID.size() ; i++){

                        values += sensorReadings[i] + ",";
//                        values += sensorReadings[i];

                    }

                    int classLabel = 100 ;

                    switch (MainActivity.labelPrefix){

                        case "up_":
                            classLabel = 0;
                            break;

                        case "dw_":
                            classLabel = 1;
                            break;


                        case "si_":
                            classLabel = 2;
                            break;

                        case "st_":
                            classLabel = 3;
                            break;

                    }

                    String sample = classLabel + "," + (Long.parseLong(timeStamp.trim()) - timeStampReference) + "," + values;

                    Log.v(LOG_TAG,"timeStampSensorReadingLoader sample:  "  + sampleTimeStampsIterator );
                    Log.v(LOG_TAG,"sample:  "  + sample );

                    sampleDataset.add(sample);

                } else {

                    Toast.makeText(MainActivity.this, "No existen registros guardados", Toast.LENGTH_SHORT).show();

                }


            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "FileWriter IOException: " + e.toString());
            }

            sampleTimeStampsIterator++;

            if(sampleTimeStampsIterator == (sampleTimeStamps.size() - 1)){

                sampleTimeStampsIterator = 0;
                createSampleBasedCSV(header);
                //Save datapoint loader destroyed, so that if user comes back from
                //CSV file viewer, it does not create a new one
                getLoaderManager().destroyLoader(Constants.TIME_STAMP_SENSOR_READING_LOADER);

         }else{

                long minTime = sampleTimeStamps.get(sampleTimeStampsIterator);
                long maxTime;

                maxTime = sampleTimeStamps.get((sampleTimeStampsIterator + 1));

                Bundle bundle = new Bundle();
                bundle.putLong("minTime", minTime);
                bundle.putLong("maxTime", maxTime);

                // Kick off the  loader
                getLoaderManager().restartLoader(Constants.TIME_STAMP_SENSOR_READING_LOADER, bundle, timeStampSensorReadingLoader);


            }




        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

            Log.v(LOG_TAG, "timeStampSensorReadingLoader: onLoaderReset");

        }
    };

    private String getColumnsLabelsHeader(ArrayList<Integer> selectedSensorID) {
        
        String result = ""; 
        
        for(Integer sensorID : selectedSensorID){
            
            result += getSensorHeader(sensorID) ;
            result += ",";
            
        }

        result = result.substring(0,(result.length() - 1 )); // takes the last comma off
        
        return result;
    }

    private String getSensorHeader(Integer sensorID) {

        String result = "";

        switch (sensorID) {

            case Constants.HEART_RATE_SENSOR_ID:
                result = Constants.HEART_RATE_SENSOR_LABEL + "_bpm," + Constants.HEART_RATE_SENSOR_LABEL + "_q" ;
                break;

            case Constants.RR_INTERVAL_SENSOR_ID:
                result = Constants.RR_INTERVAL_SENSOR_LABEL;
                break;

            case Constants.ACCELEROMETER_SENSOR_ID:
                result = Constants.ACCELEROMETER_SENSOR_LABEL + "_x," + Constants.ACCELEROMETER_SENSOR_LABEL + "_y," + Constants.ACCELEROMETER_SENSOR_LABEL + "_z";
                break;

            case Constants.ALTIMETER_SENSOR_ID:
                result = Constants.ALTIMETER_SENSOR_LABEL + "_gain," + Constants.ALTIMETER_SENSOR_LABEL + "_loss," + Constants.ALTIMETER_SENSOR_LABEL + "_diff";
                break;

            case Constants.BAROMETER_SENSOR_ID:
                result = Constants.BAROMETER_SENSOR_LABEL + "_pressure," + Constants.BAROMETER_SENSOR_LABEL + "_temp" ;
                break;

            case Constants.AMBIENT_LIGHT_SENSOR_ID:
                result = Constants.AMBIENT_LIGHT_SENSOR_LABEL;
                break;

            case Constants.GSR_SENSOR_ID:
                result = Constants.GSR_SENSOR_LABEL;
                break;

            case Constants.CALORIES_SENSOR_ID:
                result = Constants.CALORIES_SENSOR_LABEL;
                break;

            case Constants.DISTANCE_SENSOR_ID:
                //result = Constants.DISTANCE_SENSOR_LABEL + "_motion," + Constants.DISTANCE_SENSOR_LABEL + "_today," + Constants.DISTANCE_SENSOR_LABEL + "_pace, " +Constants.DISTANCE_SENSOR_LABEL + "_speed";
                result = Constants.DISTANCE_SENSOR_LABEL + "_today," + Constants.DISTANCE_SENSOR_LABEL + "_pace, " +Constants.DISTANCE_SENSOR_LABEL + "_speed";
                break;

            case Constants.BAND_CONTACT_SENSOR_ID:
                result = Constants.BAND_CONTACT_SENSOR_LABEL;
                break;

            case Constants.GYROSCOPE_SENSOR_ID:
                result = Constants.GYROSCOPE_SENSOR_LABEL + "_x," + Constants.GYROSCOPE_SENSOR_LABEL + "_y," + Constants.GYROSCOPE_SENSOR_LABEL + "_z";
                break;

            case Constants.PEDOMETER_SENSOR_ID:
                result = Constants.PEDOMETER_SENSOR_LABEL;
                break;

            case Constants.SKIN_TEMPERATURE_SENSOR_ID:
                result = Constants.SKIN_TEMPERATURE_SENSOR_LABEL;
                break;

            case Constants.UV_LEVEL_SENSOR_ID:
                result = Constants.UV_LEVEL_SENSOR_LABEL;
                break;

            case Constants.BAND_STATUS_SENSOR_ID:
                result = Constants.BAND_STATUS_SENSOR_LABEL;
                break;

        }

        return result;
    }

    private void createTimeBasedCSV() {

        timeBasedCSVDate = timeStampReference;

        final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.csv_mode_key), 1);
        editor.commit();


        // Kick off saveDataCursorLoader
        getLoaderManager().restartLoader(Constants.CREATE_CSV_LOADER, null, saveDataCursorLoader);

    }

    private void createSampleBasedCSV(String header) {

        Log.v(LOG_TAG,"createSampleBasedCSV");

        outputDirectory = getOutputDirectory();

        Log.v(LOG_TAG, "outputDirectory.getAbsolutePath().toString(): " + outputDirectory.getAbsolutePath().toString());

        String srLabel = "SB";

        saveFile = getCsvOutputFile(outputDirectory, srLabel);

        try {
            FileWriter fw = new FileWriter(saveFile);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write(header);
            bw.newLine();

            Log.v(LOG_TAG,"sampleDataset.size() :"  + sampleDataset.size());

            for (int i = 0; i < sampleDataset.size(); i++) {

                bw.write(sampleDataset.get(i));
                bw.newLine();
            }

            bw.flush();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "FileWriter IOException: " + e.toString());
        }

        Log.w(LOG_TAG, "Datapoint Exported Successfully.");

        //If SaveDatapoint button is clicked while MSBand still connected, it saves the
        //newest values
        sampleDataset.clear();

        //createTimeBasedCSV(); // TODO: TESTING CODE

        showLoadingView(false);

        //shows "OPEN CSV" action on a snackbar
        Snackbar mySnackbar = Snackbar.make(mMainLayout,
                "Files saved at" + outputDirectory.getAbsolutePath().toString(), Snackbar.LENGTH_LONG);
        //mySnackbar.setAction(R.string.open, new OpenCSVFileListener());
        mySnackbar.show();

        getLoaderManager().destroyLoader(Constants.TIME_STAMP_SENSOR_READING_LOADER);

    }

    private String[] getSensorReadings(Cursor c, ArrayList<Integer> selectedSensorID) {

        String[] answer = new String[selectedSensorID.size()];

        for(int i = 0 ; i < selectedSensorID.size(); i++){

            answer[i] = getReading(c, selectedSensorID.get(i));

        }

        return answer;
    }

    private String getReading(Cursor c, Integer sensorID) {

        /*QUERY
        *                 String[] projection = {
                        ReadingEntry._ID,
                        ReadingEntry.COLUMN_TIME,
                        ReadingEntry.COLUMN_SAMPLE_RATE,
                        ReadingEntry.COLUMN_SENSOR_ID,
                        ReadingEntry.COLUMN_SENSOR_VALUE};

        * */

        int rowcount = c.getCount();
        c.moveToFirst();

        String reading;

        Log.v(LOG_TAG,"getReading sensorID: "  + sensorID);

        if(sensorID == Constants.ALTIMETER_SENSOR_ID || sensorID == Constants.ACCELEROMETER_SENSOR_ID || sensorID == Constants.GYROSCOPE_SENSOR_ID || sensorID == Constants.DISTANCE_SENSOR_ID){

            reading= "NaN,NaN,NaN";

        }else{

            if(sensorID == Constants.HEART_RATE_SENSOR_ID  ){

                reading= "NaN,NaN";

            }else{

                reading= "NaN";

            }


        }

        for (int i = 0 ; i < rowcount; i++){

            c.moveToPosition(i);

            String currentID = c.getString(3);
            String interestID = "" + sensorID;

            if(currentID.equals(interestID)){

                reading = c.getString(4);

            }

        }

        return reading;
    }


    private ArrayList<Integer> getSelectedSensorID() {


        ArrayList<Integer> result = new ArrayList<>();

        for (int i = 0; i < sensorReadings.size(); i++) {

            SensorReadingView sensorView = (SensorReadingView) mListView.getChildAt(i);
            boolean check = sensorView.getSensorCheckBox().isChecked();

            if(check){

                int sensorID = 0;

                switch (sensorView.getId()) {

                    case R.id.heart_rate_sensorview:

                        sensorID =  Constants.HEART_RATE_SENSOR_ID;

                        break;

                    case R.id.rr_interval_sensorview:

                        sensorID = Constants.RR_INTERVAL_SENSOR_ID;

                        break;

                    case R.id.accelerometer_sensorview:

                        sensorID =  Constants.ACCELEROMETER_SENSOR_ID;

                        break;

                    case R.id.altimeter_sensorview:

                        sensorID =  Constants.ALTIMETER_SENSOR_ID;

                        break;

                    case R.id.ambient_light_sensorview:

                        sensorID =  Constants.AMBIENT_LIGHT_SENSOR_ID;

                        break;

                    case R.id.barometer_sensorview:

                        sensorID =  Constants.BAROMETER_SENSOR_ID;

                        break;

                    case R.id.gsr_sensorview:

                        sensorID =  Constants.GSR_SENSOR_ID;

                        break;

                    case R.id.calories_sensorview:

                        sensorID =  Constants.CALORIES_SENSOR_ID;

                        break;

                    case R.id.distance_sensorview:

                        sensorID =  Constants.DISTANCE_SENSOR_ID;

                        break;

                    case R.id.band_contact_sensorview:

                        sensorID =  Constants.BAND_CONTACT_SENSOR_ID;

                        break;

                    case R.id.gyroscope_sensorview:

                        sensorID =  Constants.GYROSCOPE_SENSOR_ID;

                        break;

                    case R.id.pedometer_sensorview:

                        sensorID =  Constants.PEDOMETER_SENSOR_ID;

                        break;

                    case R.id.skin_temperature_sensorview:

                        sensorID =  Constants.SKIN_TEMPERATURE_SENSOR_ID;
                        break;

                    case R.id.uv_sensorview:

                        sensorID =  Constants.UV_LEVEL_SENSOR_ID;

                        break;

                }

                result.add(sensorID);

            }

        }

        return  result;

    }

    private String getSensorSRlabel(String sensorSR) {

        String label = "";

        switch (sensorSR) {

            case Constants.SR_1:
                label = "SR1";
                break;

            case Constants.SR_2:
                label = "SR2";
                break;

            case Constants.SR_5:
                label = "SR5";
                break;

            case Constants.SR_8:
                label = "SR8";
                break;

            case Constants.SR_31:
                label = "SR31";
                break;

            case Constants.SR_62:
                label = "SR62";
                break;

            case Constants.SR_02:
                label = "SR02";
                break;

            case Constants.SR_VALUE_CHANGE:
                label = "SRVC";
                break;


        }

        return label;
    }

    private String getSensorLabel(Integer sensorID) {

        String label = "";

        switch (sensorID) {
            
            case Constants.HEART_RATE_SENSOR_ID:
                label = Constants.HEART_RATE_SENSOR_LABEL;
                break;

            case Constants.RR_INTERVAL_SENSOR_ID:
                label = Constants.RR_INTERVAL_SENSOR_LABEL;
                break;

            case Constants.ACCELEROMETER_SENSOR_ID:
                label = Constants.ACCELEROMETER_SENSOR_LABEL;
                break;

            case Constants.ALTIMETER_SENSOR_ID:
                label = Constants.ALTIMETER_SENSOR_LABEL;
                break;

            case Constants.BAROMETER_SENSOR_ID:
                label = Constants.BAROMETER_SENSOR_LABEL;
                break;

            case Constants.AMBIENT_LIGHT_SENSOR_ID:
                label = Constants.AMBIENT_LIGHT_SENSOR_LABEL;
                break;

            case Constants.GSR_SENSOR_ID:
                label = Constants.GSR_SENSOR_LABEL;
                break;

            case Constants.CALORIES_SENSOR_ID:
                label = Constants.CALORIES_SENSOR_LABEL;
                break;

            case Constants.DISTANCE_SENSOR_ID:
                label = Constants.DISTANCE_SENSOR_LABEL;
                break;
            case Constants.BAND_CONTACT_SENSOR_ID:
                label = Constants.BAND_CONTACT_SENSOR_LABEL;
                break;

            case Constants.GYROSCOPE_SENSOR_ID:
                label = Constants.GYROSCOPE_SENSOR_LABEL;
                break;
                
            case Constants.PEDOMETER_SENSOR_ID:
                label = Constants.PEDOMETER_SENSOR_LABEL;
                break;

            case Constants.SKIN_TEMPERATURE_SENSOR_ID:
                label = Constants.SKIN_TEMPERATURE_SENSOR_LABEL;
                break;

            case Constants.UV_LEVEL_SENSOR_ID:
                label = Constants.UV_LEVEL_SENSOR_LABEL;
                break;
                
            case Constants.BAND_STATUS_SENSOR_ID:
                label = Constants.BAND_STATUS_SENSOR_LABEL;
                break;

        }

        return label;
    }


    public class SaveButton extends android.support.v7.widget.AppCompatImageButton implements Checkable {

        boolean isChecked = false;

        public SaveButton(Context context) {
            super(context);
        }

        @Override
        public void setChecked(boolean b) {

            isChecked = b;

            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            int defaultValue = getResources().getInteger(R.integer.csv_mode_key_default_value);
            int csvMode = sharedPref.getInt(getString(R.string.csv_mode_key), defaultValue);

            if (b) {

                SaveButton.this.setEnabled(false);

                Log.v(LOG_TAG,"timeFab setOnClickListener");
                clock.setText("" + TIMER_DURATION);
                task = new FutureTask(new CounterCallable(MainActivity.this,0, TIMER_DURATION,1));


                ExecutorService pool = Executors.newSingleThreadExecutor();
                pool.submit(task);
                pool.shutdown();

                timeBasedCSVDate = System.currentTimeMillis();

            }
        }

        @Override
        public boolean isChecked() {

            //Log.v(LOG_TAG,"SaveButton: isChecked: " + isChecked);

            return isChecked;
        }

        @Override
        public void toggle() {

            //Log.v(LOG_TAG,"SaveButton: toggle");

        }


        @Override
        public void setBackground(Drawable background) {

            // Create an array of the attributes we want to resolve
            // using values from a theme
            // android.R.attr.selectableItemBackground requires API LEVEL 11
            int[] attrs = new int[]{android.R.attr.selectableItemBackground /* index 0 */};

            // Obtain the styled attributes. 'themedContext' is a context with a
            // theme, typically the current Activity (i.e. 'this')
            TypedArray ta = obtainStyledAttributes(attrs);

            // Now get the value of the 'listItemBackground' attribute that was
            // set in the theme used in 'themedContext'. The parameter is the index
            // of the attribute in the 'attrs' array. The returned Drawable
            // is what you are after
            Drawable drawableFromTheme = ta.getDrawable(0 /* index */);

            // Finally free resources used by TypedArray
            ta.recycle();

            // imageButton.setBackgroundDrawable(drawableFromTheme);
            super.setBackground(drawableFromTheme);
        }


    }

    private class SettingsDialog extends AppCompatDialog {

        public SettingsDialog(Context context) {

            super(context);

            setContentView(R.layout.edit_label_dialog);

            final EditText datePatternEditText = (EditText) findViewById(R.id.date_pattern);
            datePatternEditText.setHint(labelPrefix);

            TextView dateTextView = (TextView) findViewById(R.id.date_text_view);
            dateTextView.setText(displayDate);

            final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            int defaultValue = getResources().getInteger(R.integer.csv_mode_key_default_value);
            int csvMode = sharedPref.getInt(getString(R.string.csv_mode_key), defaultValue);

            int rbID = 0;

            switch (csvMode) {

                case 0:
                    rbID = R.id.frequency_rb;
                    break;

                case 1:
                    rbID = R.id.time_rb;
                    break;

                case 2:
                    rbID = R.id.sample_rb;
                    break;

            }


            RadioGroup radioGroup = (RadioGroup) findViewById(R.id.csv_options_rg);
            radioGroup.check(rbID);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    // find which radio button is selected

                    int csvOutputFileMode;


                    SharedPreferences.Editor editor = sharedPref.edit();

                    switch (checkedId) {

                        case R.id.frequency_rb:

                            csvOutputFileMode = 0;
                            editor.putInt(getString(R.string.csv_mode_key), csvOutputFileMode);

                            break;

                        case R.id.time_rb:

                            csvOutputFileMode = 1;
                            editor.putInt(getString(R.string.csv_mode_key), csvOutputFileMode);

                            break;

                        case R.id.sample_rb:

                            csvOutputFileMode = 2;
                            editor.putInt(getString(R.string.csv_mode_key), csvOutputFileMode);

                            break;

                    }

                    editor.commit();

                }

            });


            Button okButton = (Button) findViewById(R.id.btnSave);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String newPrefix = datePatternEditText.getText().toString();

                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                    int defaultValue = getResources().getInteger(R.integer.csv_mode_key_default_value);
                    int newCsvMode = sharedPref.getInt(getString(R.string.csv_mode_key), defaultValue);


                    if (TextUtils.isEmpty(newPrefix) && (prevCsvMode == newCsvMode)) {

                        Toast.makeText(MainActivity.this, "No changes made.", Toast.LENGTH_SHORT).show();

                    } else {

                        if (prevCsvMode != newCsvMode) {
                            prevCsvMode = newCsvMode;
                        }

                        if (!TextUtils.isEmpty(newPrefix)) {
                            labelPrefix = newPrefix;
                        }

                        Toast.makeText(MainActivity.this, "Changes saved.", Toast.LENGTH_SHORT).show();

                    }

                    cancel();

                }
            });


            Button cancelButton = (Button) findViewById(R.id.btnCancel);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancel();
                }
            });


        }


    }

}
