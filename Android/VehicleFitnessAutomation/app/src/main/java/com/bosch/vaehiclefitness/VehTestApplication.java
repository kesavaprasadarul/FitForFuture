package com.bosch.vaehiclefitness;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Copyright Robert Bosch GmbH.
 * <p>
 * All rights reserved, also regarding any disposal, exploration, reproduction, editing,
 * distribution, as well as in the event of applications for industrial property rights.
 */
public class VehTestApplication extends Application {

    private static final String TAG = VehTestApplication.class.getSimpleName();

    public static Context sApplication;

    public static Context getAppContext() {
        return sApplication;
    }

    public static void setAppContext(Context context) {
        sApplication = context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(TAG, "Device memory is low.");
    }
}
