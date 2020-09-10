package com.mars_skyrunner.myband;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;

public class SensorReading  implements  Parcelable{

    String mSensorReadingRate;
    int mSensorID;
    String mSensorReading;
    Context mContext;
    long mSensorReadingTime;
    boolean checkboxStatus;

    public boolean isProgressBarStatus() {
        return progressBarStatus;
    }

    public void setProgressBarStatus(boolean progressBarStatus) {
        this.progressBarStatus = progressBarStatus;
    }

    boolean progressBarStatus;

    public SensorReading(Context context, int sensorID, String sensorReading){
        mContext = context;
        mSensorID = sensorID;
        mSensorReading = sensorReading;
    }



    public SensorReading(Context context, int sensorID, String sensorReading, String rate, long time){

        mContext = context;
        mSensorID = sensorID;
        mSensorReading = sensorReading;
        mSensorReadingRate = rate;
        mSensorReadingTime = time;
    }

    protected SensorReading(Parcel in) {
        mSensorReadingRate = in.readString();
        mSensorID = in.readInt();
        mSensorReading = in.readString();
        mSensorReadingTime = in.readLong();
    }

    public static final Creator<SensorReading> CREATOR = new Creator<SensorReading>() {
        @Override
        public SensorReading createFromParcel(Parcel in) {
            return new SensorReading(in);
        }

        @Override
        public SensorReading[] newArray(int size) {
            return new SensorReading[size];
        }
    };



    public String getSensorName() {

        switch(mSensorID){



            case Constants.HEART_RATE_SENSOR_ID:

                return Constants.HEART_RATE_SENSOR_LABEL;

            case Constants.RR_INTERVAL_SENSOR_ID:

                return Constants.RR_INTERVAL_SENSOR_LABEL;

            case Constants.ACCELEROMETER_SENSOR_ID:

                return Constants.ACCELEROMETER_SENSOR_LABEL;

            case Constants.ALTIMETER_SENSOR_ID:

                return Constants.ALTIMETER_SENSOR_LABEL;

            case Constants.AMBIENT_LIGHT_SENSOR_ID:

                return Constants.AMBIENT_LIGHT_SENSOR_LABEL;

            case Constants.BAROMETER_SENSOR_ID:

                return Constants.BAROMETER_SENSOR_LABEL;

            case Constants.GSR_SENSOR_ID:

                return Constants.GSR_SENSOR_LABEL;

            case Constants.CALORIES_SENSOR_ID:

                return Constants.CALORIES_SENSOR_LABEL;

            case Constants.DISTANCE_SENSOR_ID:

                return Constants.DISTANCE_SENSOR_LABEL;

            case Constants.GYROSCOPE_SENSOR_ID:

                return Constants.GYROSCOPE_SENSOR_LABEL;

            case Constants.PEDOMETER_SENSOR_ID:

                return Constants.PEDOMETER_SENSOR_LABEL;

            case Constants.SKIN_TEMPERATURE_SENSOR_ID:

                return Constants.SKIN_TEMPERATURE_SENSOR_LABEL;

            case Constants.UV_LEVEL_SENSOR_ID:

                return Constants.UV_LEVEL_SENSOR_LABEL;

            case Constants.BAND_STATUS_SENSOR_ID:

                return Constants.BAND_STATUS_SENSOR_LABEL;

            case Constants.BAND_CONTACT_SENSOR_ID:

                return Constants.BAND_CONTACT_SENSOR_LABEL;





        }

        return null;
    }

    public String getSensorReading() {
        return mSensorReading;
    }

    public void setSensorReading(String mSensorReading) {
        this.mSensorReading = mSensorReading;
    }

    public long getSensorTime() {
        return mSensorReadingTime;
    }

    public String getSensorReadingRate() {
        return mSensorReadingRate;
    }

    public boolean isCheckboxStatus() {
        return checkboxStatus;
    }

    public void setCheckboxStatus(boolean checkboxStatus) {
        this.checkboxStatus = checkboxStatus;
    }

    public String getSensorReadingYear() {

        return new SimpleDateFormat("yyyy").format(mSensorReadingTime);
    }

    public String getSensorReadingMonth() {
        return  new SimpleDateFormat("MM").format(mSensorReadingTime);
    }

    public String getSensorReadingDay() {
        return  new SimpleDateFormat("dd").format(mSensorReadingTime);
    }


    public String getSensorReadingHH() {
        return new SimpleDateFormat("HH").format(mSensorReadingTime);
    }



    public String getSensorReadingMM() {
        return new SimpleDateFormat("mm").format(mSensorReadingTime);
    }


    public String getSensorReadingSS() {
        return new SimpleDateFormat("ss").format(mSensorReadingTime);
    }





    public int  getSensorID() {
        return mSensorID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mSensorReadingRate);
        parcel.writeInt(mSensorID);
        parcel.writeString(mSensorReading);
        parcel.writeLong(mSensorReadingTime);
    }
}