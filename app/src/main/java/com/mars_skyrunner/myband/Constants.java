package com.mars_skyrunner.myband;

import java.util.ArrayList;

public class Constants {


    private static final String packageName = "com.mars_skyrunner.myband";

    public static final int SAVE_DATAPOINT_LOADER = 100;
    public static final int BAND_SUSCRIPTION_LOADER = 200;
    public static final int CREATE_CSV_LOADER = 300;
    public static final int SAMPLE_BASED_LOADER = 400;
    public static final int TIME_STAMP_SENSOR_READING_LOADER = 500;

    public static final String RESET_SENSOR_READING = "com.mars_skyrunner.myband.RESET_SENSOR_READING";
    public static final String SHOW_CONSENT_DIALOG = "com.mars_skyrunner.myband.SHOW_CONSENT_DIALOG";
    public static final String UV_LEVEL = "com.mars_skyrunner.myband.UV_LEVEL";
    public static final String SKIN_TEMPERATURE = "com.mars_skyrunner.myband.SKIN_TEMPERATURE";
    public static final String PEDOMETER = "com.mars_skyrunner.myband.PEDOMETER";
    public static final String GYROSCOPE = "com.mars_skyrunner.myband.GYROSCOPE";
    public static final String BAND_CONTACT = "com.mars_skyrunner.myband.BAND_CONTACT";
    public static final String CALORIES  = "com.mars_skyrunner.myband.CALORIES";
    public static final String RR_INTERVAL = "com.mars_skyrunner.myband.RR_INTERVAL";
    public static final String GSR = "com.mars_skyrunner.myband.GSR";
    public static final String BAROMETER = "com.mars_skyrunner.myband.BAROMETER";
    public static final String BAND_STATUS = "com.mars_skyrunner.myband.BAND_STATUS";
    public static final String ACCELEROMETER = "com.mars_skyrunner.myband.ACCELEROMETER";
    public static final String ALTIMETER = "com.mars_skyrunner.myband.ALTIMETER";
    public static final String HEART_RATE = "com.mars_skyrunner.myband.HEART_RATE";
    public static final String AMBIENT_LIGHT = "com.mars_skyrunner.myband.AMBIENT_LIGHT";
    public static final String DISTANCE = "com.mars_skyrunner.myband.DISTANCE";




    public static final int HEART_RATE_AQUIRING = 1;
    public static final int HEART_RATE_LOCKED = 2;

    public static final int HEART_RATE_SENSOR_ID = 1;
    public static final int RR_INTERVAL_SENSOR_ID = 2;
    public static final int ACCELEROMETER_SENSOR_ID = 3;
    public static final int ALTIMETER_SENSOR_ID = 4;
    public static final int AMBIENT_LIGHT_SENSOR_ID = 5;
    public static final int BAROMETER_SENSOR_ID = 6;
    public static final int GSR_SENSOR_ID = 7;
    public static final int CALORIES_SENSOR_ID  = 8;
    public static final int DISTANCE_SENSOR_ID = 9;
    public static final int GYROSCOPE_SENSOR_ID = 10;
    public static final int PEDOMETER_SENSOR_ID = 11;
    public static final int SKIN_TEMPERATURE_SENSOR_ID = 12;
    public static final int UV_LEVEL_SENSOR_ID = 13;
    public static final int BAND_CONTACT_SENSOR_ID = 14;




    public static final int BAND_STATUS_SENSOR_ID = 15;


    public static final String HEART_RATE_SENSOR_LABEL =  "heart_rate";
    public static final String RR_INTERVAL_SENSOR_LABEL = "rr_interval";
    public static final String ACCELEROMETER_SENSOR_LABEL = "accelerometer";
    public static final String ALTIMETER_SENSOR_LABEL = "altimeter";
    public static final String AMBIENT_LIGHT_SENSOR_LABEL = "ambient_light";
    public static final String BAROMETER_SENSOR_LABEL = "barometer";
    public static final String GSR_SENSOR_LABEL = "gsr";
    public static final String CALORIES_SENSOR_LABEL  = "calories";
    public static final String DISTANCE_SENSOR_LABEL = "distance";
    public static final String BAND_CONTACT_SENSOR_LABEL = "contact";
    public static final String GYROSCOPE_SENSOR_LABEL = "gyroscope";
    public static final String PEDOMETER_SENSOR_LABEL = "pedometer";
    public static final String SKIN_TEMPERATURE_SENSOR_LABEL = "skin_temperature";
    public static final String UV_LEVEL_SENSOR_LABEL = "uv";
    public static final String BAND_STATUS_SENSOR_LABEL = "status";



    public static final String SENSOR = packageName + ".SENSOR";
    public static final String VALUE = packageName + ".VALUE";
    public static final String DISPLAY_VALUE = packageName + ".DISPLAY_VALUE";
    public static final String BAND_CONNECTION_FAIL = "Band Connection failed. Please try again.";
    public static final String CREATE_SENSOR_READING_OBJECT_SERVICE =  packageName + ".CREATE_SENSOR_READING_OBJECT_SERVICE";
    public static final String SERVICE_EXTRA = packageName + ".SERVICE_EXTRA";
    public static final String SENSOR_DATE = packageName + ".SENSOR_DATE";
    public static final String SENSOR_TIME = packageName + ".SENSOR_TIME";
    public static final String SENSOR_NAME = packageName + ".SENSOR_NAME";
    public static final String SENSOR_VALUE = packageName + ".SENSOR_VALUE";
    public static final String SENSOR_RATE = packageName + ".SENSOR_RATE";
    public static final String SENSOR_READING_OBJECT_RECEIVER = packageName + ".SENSOR_READING_OBJECT_RECEIVER";

    public static final String INSERT_SENSOR_READING = packageName + ".INSERT_SENSOR_READING";
    public static final String CREATE_CSV_RECEIVER = packageName + ".CREATE_CSV_RECEIVER";

    public static final String SR_02 = "0.2";
    public static final String SR_1 = "1";
    public static final String SR_2 = "2";
    public static final String SR_5 = "5";
    public static final String SR_8 = "8";
    public static final String SR_31 = "31";
    public static final String SR_62 = "62";
    public static final String SR_VALUE_CHANGE = "Value change";


    public static final String[] SAMPLE_RATE_OPTIONS ={

            SR_02, SR_1, SR_2, SR_5, SR_8, SR_31, SR_62, SR_VALUE_CHANGE


    };




    public Constants(){

    }



}
