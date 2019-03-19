package com.lordoscar.sensorproject4;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lordoscar.sensorproject4.db.AsyncTasks.DeleteStepAsyncTask;
import com.lordoscar.sensorproject4.helpers.ServiceConnection;
import com.lordoscar.sensorproject4.helpers.StepCountingService;

/*
 * @Author Marcel Laska
 * @Date 2019ish
 * @Project Assign 4 aka Pathfinder @ MAU
 *
 *
 * Main-activity that shows the compass along with two buttons
 * and handles compass & shake-thingy along with some other stuff.
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // UI & DB variables
    private ImageView imageViewCompass;
    private TextView textViewStepsTotal, textViewStepsPerSecond, textViewName;
    private Button buttonReset, buttonHistory;
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer, sensorMagnetometer;
    private Animation animation;
    private boolean isAnimationRunning = false;
    public static final String Database_name = "database";
    private String username;

    // Compass variables
    private int mAzimuth;
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    // Service-variables
    public StepCountingService stepCountingService;
    public boolean bound;
    private ServiceConnection serviceConnection;
    private Intent stepsIntent;


    /*
     * onCreate-method that calls several other methods
     * that should be running from the start
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeComponents();
        getShakeSensors();
        setWelcomeName();
        firstStartCheck();
    }

    /*
     * Initializes UI-components
     */
    public void initializeComponents() {
        imageViewCompass = findViewById(R.id.imageViewCompass);
        textViewStepsTotal = findViewById(R.id.textViewStepsTotal);
        textViewStepsPerSecond = findViewById(R.id.textViewStepsPerSecond);
        textViewName = findViewById(R.id.textViewWelcome);
        buttonReset = findViewById(R.id.buttonReset);
        buttonHistory = findViewById(R.id.buttonHistory);
        buttonHistory.setOnClickListener(new ButtonListener());
        buttonReset.setOnClickListener(new ButtonListener());
    }

    /*
     * Sets the "Welcome User"-text by taking username from the Login-activity.
     */
    public void setWelcomeName() {
        textViewName.setText("" + getIntent().getStringExtra("name"));
    }

    /*
     * Sets the username so it can be passed forward to the stepCountingService.
     */
    public void setUsername() {
        username = textViewName.getText().toString();
        this.username = username;
    }

    /*
     * Get-method so the stepCountingService can grab the username
     */
    public String getUsername() {
        return username;
    }

    /*
     *  Method that updates the steps-textviews with data from stepCountingService.
     */
    public void updateSteps(int stepCounter, int stepsPerSecond) {
        textViewStepsTotal.setText("TOTAL STEPS: " + stepCounter);
        textViewStepsPerSecond.setText("STEPS PER SECOND: " + stepsPerSecond);
    }

    /*
     * Method that checks if it is a first start.
     * If yes --> user has to login or register.
     * If not --> starts serviceConnection & bounds the service.
     */
    public void firstStartCheck() {
        if (textViewName.getText().equals("") || textViewName.getText().equals("null") || textViewName.getText().equals("Insert username here")) {
            Intent login = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(login);
        } else {
            setUsername();
            serviceConnection = new ServiceConnection(this);
            stepsIntent = new Intent(this, StepCountingService.class);
            bindService(stepsIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            bound = true;
            Toast.makeText(this, "SERVICE BOUNDED", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Method called when user presses the Deletebutton.
     * It deletes this users stephistory from the database.
     */
    public void deleteAllFromDatabase() {
        DeleteStepAsyncTask deleteStepAsyncTask = new DeleteStepAsyncTask(this, textViewName.getText().toString());
        deleteStepAsyncTask.execute();
    }

    ///////////// COMPASS AND SHAKE ///////////////////////////

    /*
     * Method that sets the sensorManager & gets the sensor for the compass & shake-thingy.
     */
    public void getShakeSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            Toast.makeText(this, "ACCELEROMETER SENSOR NOT AVAILABLE", Toast.LENGTH_SHORT).show();
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            sensorMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        } else {
            Toast.makeText(this, "MAGNETOMETER SENSOR NOT AVAILABLE", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Method that rotates the compass-image.
     */
    public void compassRotation() {
        isAnimationRunning = true;
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        animation.setFillAfter(true);
        imageViewCompass.startAnimation(animation);
    }

    /*
     * Method that finds NORTH and makes the compass-image point that way.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        shakeDetector(event);
        mAzimuth = Math.round(mAzimuth);
        imageViewCompass.setRotation(-mAzimuth);
    }

    /*
     * Method that detects a shake & calls the "animation"-method.
     */
    public void shakeDetector(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] values = event.values;
            // Detects movement/shake
            float x = values[0];
            float y = values[1];
            float z = values[2];

            float accelerationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
            if (accelerationSquareRoot >= 5) {
                compassRotation();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /*
     * Unregs. the sensors.
     */
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, sensorAccelerometer);
        sensorManager.unregisterListener(this, sensorMagnetometer);
        Toast.makeText(this, "UNREGISTERED", Toast.LENGTH_SHORT).show();
    }

    /*
     * Reg. the sensors.
     */
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, sensorMagnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    /*
     * Shows toast & goes back to Login-activity,
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "GOODBYE :)", Toast.LENGTH_SHORT).show();
    }

    /*
     * Listeners for buttons.
     */
    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.equals(buttonReset)) {
                deleteAllFromDatabase();
            } else if (v.equals(buttonHistory)) {
                Intent openHistoryForThisUser = new Intent(MainActivity.this, HistoryActivity.class);
                openHistoryForThisUser.putExtra("name", textViewName.getText().toString());
                startActivity(openHistoryForThisUser);
            }
        }
    }
}
