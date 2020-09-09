package com.mars_skyrunner.myband;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import static com.mars_skyrunner.myband.MainActivity.sensorReadings;


public class SensorArrayAdapter  extends ArrayAdapter<SensorReading> {

    private static final String LOG_TAG = SensorArrayAdapter.class.getSimpleName();
    Context context;
    int mResource;


    public SensorArrayAdapter(Context context, int resource, List<SensorReading> objects) {
        super(context, resource, objects);
        this.context = context;
        mResource = resource;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(mResource, parent, false);
        }

        final SensorReading sensorReading = getItem(position);

        TextView name = convertView.findViewById(R.id.sensor_name);
        TextView id = convertView.findViewById(R.id.sensor_id);
        CheckBox checkBox = convertView.findViewById(R.id.sensor_checkbox);

        name.setText(sensorReading.getSensorName());
        id.setText(sensorReading.getSensorID() + "");

        if(sensorReading.isCheckboxStatus()){
            checkBox.setChecked(true);
        }else{
            checkBox.setChecked(false);
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sensorReadings.get(position).setCheckboxStatus(b);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}