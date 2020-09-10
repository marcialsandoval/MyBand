package com.mars_skyrunner.myband;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.mars_skyrunner.myband.MainActivity.sensorReadings;


public class SensorArrayAdapter extends ArrayAdapter<SensorReading> {

    private static final String LOG_TAG = SensorArrayAdapter.class.getSimpleName();
    Context mContext;
    int mResource;
    ArrayList<SensorReading> inputDataset;
    int spinnerSelection;

    public SensorArrayAdapter(Context context, int resource, List<SensorReading> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        inputDataset = (ArrayList<SensorReading>) objects;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(mResource, parent, false);
        }

        final SensorReading sensorReading = getItem(position);

        TextView name = convertView.findViewById(R.id.sensor_name);
        TextView id = convertView.findViewById(R.id.sensor_id);
        TextView samplingRateTV = convertView.findViewById(R.id.sample_rate_display);
        final Spinner mSpinner = convertView.findViewById(R.id.sample_rate_spinner);
        LinearLayout container =  convertView.findViewById(R.id.sample_rate_spinner_container);
        ProgressBar bar =  convertView.findViewById(R.id.progress_bar);

        if(sensorReading.isProgressBarStatus()){
            bar.setVisibility(View.VISIBLE);
        }else{
            bar.setVisibility(View.GONE);
        }

        final CheckBox checkBox = convertView.findViewById(R.id.sensor_checkbox);

        name.setText(sensorReading.getSensorName());
        id.setText(sensorReading.getSensorID() + "");

        if (sensorReading.isCheckboxStatus()) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean b = checkBox.isChecked();
                sensorReadings.get(sensorReading.getSensorID() - 1).setCheckboxStatus(b);

                notifyDataSetChanged();
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Intent stopReadingIntent = new Intent(Constants.RESET_SENSOR_READING);
                stopReadingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.sendBroadcast(stopReadingIntent);
            }
        });

        ArrayList<String> options = new ArrayList<>();
        ArrayAdapter<String> dataAdapter;

        switch (sensorReading.getSensorID()) {

            case Constants.ACCELEROMETER_SENSOR_ID:
            case Constants.GYROSCOPE_SENSOR_ID:
                samplingRateTV.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
                options.add("8");
                options.add("31");
                options.add("62");
                dataAdapter = new ArrayAdapter<String>(mContext, R.layout.sample_rate_option_textview, options);
                mSpinner.setAdapter(dataAdapter);
                break;

            case Constants.GSR_SENSOR_ID:
                samplingRateTV.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
                options.add("5");
                options.add("0.2");
                dataAdapter = new ArrayAdapter<String>(mContext, R.layout.sample_rate_option_textview, options);
                mSpinner.setAdapter(dataAdapter);
                break;

            default:

                samplingRateTV.setVisibility(View.VISIBLE);
                container.setVisibility(View.GONE);
                String sampleRate = "";

                switch (sensorReading.getSensorID()) {

                    case Constants.HEART_RATE_SENSOR_ID:
                    case Constants.SKIN_TEMPERATURE_SENSOR_ID:
                    case Constants.BAROMETER_SENSOR_ID:
                    case Constants.ALTIMETER_SENSOR_ID:
                    case Constants.UV_LEVEL_SENSOR_ID:
                        sampleRate = "1 hz";
                        break;

                    case Constants.AMBIENT_LIGHT_SENSOR_ID:
                        sampleRate = "2 hz";
                        break;

                    default:
                        sampleRate = "Value change";
                        break;

                }

                samplingRateTV.setText(sampleRate);

                break;

        }

        spinnerSelection = 0;
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                //If spinner selection changes, stopButtonClicked() Method is called on MainActivity class
                if (arg2 != spinnerSelection) {

                    spinnerSelection = arg2;

                    Intent stopReadingIntent = new Intent(Constants.RESET_SENSOR_READING);
                    stopReadingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.sendBroadcast(stopReadingIntent);

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}

        });


        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}