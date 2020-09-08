package com.mars_skyrunner.myband;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.concurrent.Callable;

public class CounterCallable implements Callable {

    private static final String LOG_TAG = CounterCallable.class.getSimpleName();
    private long start;
    private long end;
    private int thread;
    Context mContext;

    public CounterCallable(Context context, long start, long end , int thread) {
        this.start = start;
        this.end = end;
        this.thread = thread;
        mContext = context;
    }

    @Override
    public Object call() throws Exception {

        while (start != end) {

            start++;

            Thread.sleep(1000);

            Intent appendToUiIntent = new Intent(getClass().getPackage() + ".BROADCAST");
            appendToUiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appendToUiIntent.putExtra(getClass().getPackage() + ".TIME",start);
            mContext.sendBroadcast(appendToUiIntent);

        }

        return null;

    }


}