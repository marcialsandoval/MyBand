package com.mars_skyrunner.myband;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.mars_skyrunner.myband.MainActivity.sensorReadings;


public class SensorArrayAdapter  extends ArrayAdapter<SensorReading> {

    private static final String LOG_TAG = SensorArrayAdapter.class.getSimpleName();
    Context mContext;
    int mResource;
    ArrayList<SensorReading> inputDataset;

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
        final CheckBox checkBox = convertView.findViewById(R.id.sensor_checkbox);

        name.setText(sensorReading.getSensorName());
        id.setText(sensorReading.getSensorID() + "");

        if(sensorReading.isCheckboxStatus()){
            checkBox.setChecked(true);
        }else{
            checkBox.setChecked(false);
        }

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean b = checkBox.isChecked();
                sensorReadings.get(sensorReading.getSensorID() - 1).setCheckboxStatus(b);

                Intent appendToUiIntent = new Intent(Constants.CHANGED_SENSOR_READINGS);
                appendToUiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.sendBroadcast(appendToUiIntent);
            }
        });

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}