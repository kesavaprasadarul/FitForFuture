package com.bosch.vaehiclefitness;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/*

DESIGN ID:
Feature Name:

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
