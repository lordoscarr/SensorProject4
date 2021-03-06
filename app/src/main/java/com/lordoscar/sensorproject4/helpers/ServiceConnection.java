package com.lordoscar.sensorproject4.helpers;

import android.content.ComponentName;
import android.os.IBinder;

import com.lordoscar.sensorproject4.MainActivity;

public class ServiceConnection implements android.content.ServiceConnection {

    private MainActivity mainActivity;

    public ServiceConnection(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        StepCountingService.LocalBinder binder = (StepCountingService.LocalBinder) service;
        mainActivity.stepCountingService = binder.getService();
        mainActivity.stepCountingService.setListenerActivity(mainActivity);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mainActivity.bound = false;
    }
}
