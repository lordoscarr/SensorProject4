package com.lordoscar.sensorproject4.helpers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import com.lordoscar.sensorproject4.MainActivity;

import java.util.concurrent.TimeUnit;

public class StepCountingService extends Service implements SensorEventListener {

    public MainActivity mainActivity;
    private LocalBinder mBinder;
    private SensorManager mSensorManager;
    private Sensor mStepDetectorSensor;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private int stepsSinceRegistered = 0;
    private long startTime;


    public StepCountingService() {
    }

    // when Service starts
    // gets sensorManager, sensor & Handler
    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new LocalBinder();
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if( mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null  ){
            mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            Log.d("Step detector", "sensor found");

        } else {
            Log.d("No step detector", "No sensor found");
            Toast.makeText(this, "STEP DETECTOR SENSOR NOT AVAILABLE", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sensorproject4:wakelock");
        wakeLock.acquire();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        wakeLock.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        mSensorManager.registerListener(this, mStepDetectorSensor, 0);
        return mBinder;
    }

    public class LocalBinder extends Binder {
        StepCountingService getService(){
            return StepCountingService.this;
        }
    }

    public void setListenerActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    boolean firststep = true;

    @Override
    public void onSensorChanged(SensorEvent event) {
        stepsSinceRegistered++;

        int stepsPerSec = 1;
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(SystemClock.elapsedRealtime() - startTime);
        if (firststep){
            startTime = SystemClock.elapsedRealtime();
            firststep = false;
        }else {
            if (seconds != 0){
                stepsPerSec = stepsSinceRegistered / seconds;
            }else {
                stepsPerSec = stepsSinceRegistered;
            }
        }

        mainActivity.updateSteps(stepsSinceRegistered, stepsPerSec);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}