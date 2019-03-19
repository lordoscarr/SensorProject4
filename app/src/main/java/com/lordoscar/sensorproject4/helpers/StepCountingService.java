package com.lordoscar.sensorproject4.helpers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import com.lordoscar.sensorproject4.db.AsyncTasks.InsertStepAsyncTask;
import com.lordoscar.sensorproject4.MainActivity;

/*
 * @Author Marcel Laska
 * @Date 2019ish
 * @Project Assign 4 aka Pathfinder @ MAU
 *
 *
 * Service-class that keeps track of steps and sends data to mainActivity
 */
public class StepCountingService extends Service implements SensorEventListener {

    public MainActivity mainActivity;
    private LocalBinder mBinder;
    private SensorManager mSensorManager;
    private Sensor mStepDetectorSensor;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private int stepsSinceRegistered = 0;
    private int stepsPerSecond = 0;
    private long startTime;
    private Handler handler;


    public StepCountingService() {
    }

    // when Service starts
    // gets sensorManager, sensor & Handler
    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new LocalBinder();
        startTime = System.currentTimeMillis();
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if( mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null  ){
            mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        } else {
            Toast.makeText(this, "STEP DETECTOR SENSOR NOT AVAILABLE", Toast.LENGTH_SHORT).show();
        }

        // Create the Handler object (on the main thread by default)
        handler = new Handler();
        // Define the code block to be executed
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread
                stepsPerSecond = 0;
                // Repeat this the same runnable code block again another 0.5 seconds
                // 'this' is referencing the Runnable object
                handler.postDelayed(this, 0);
            }
        };
        // Start the initial runnable task by posting through the handler
        handler.post(runnableCode);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        InsertStepAsyncTask insertStepAsyncTask = new InsertStepAsyncTask(this, mainActivity.getUsername());
        insertStepAsyncTask.execute();
        stepsSinceRegistered++;
        stepsPerSecond++;
        mainActivity.updateSteps(stepsSinceRegistered, stepsPerSecond);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}