package com.lordoscar.sensorproject4;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lordoscar.sensorproject4.db.AppDatabase;
import com.lordoscar.sensorproject4.db.Step;
import com.lordoscar.sensorproject4.helpers.ServiceConnection;
import com.lordoscar.sensorproject4.helpers.StepCountingService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // region variables

    private ImageView compassImg;
    private TextView totalStepsTv, stepPerSecTv, helloTv;
    private Button buttonReset, buttonHistory;
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer, sensorMagnetometer;
    private Animation animation;
    public static final String Database_name = "database";
    private String username;

    private int mAzimuth;
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    boolean isAnimating = false;

    public StepCountingService stepCountingService;
    public boolean bound;
    private ServiceConnection serviceConnection;
    private Intent stepsIntent;

    //endregion

    // region initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firstStartCheck();
        initialize();
        getShakeSensors();
        setName();
    }

    public void initialize() {
        compassImg = findViewById(R.id.compassImg);
        totalStepsTv = findViewById(R.id.totalStepsTv);
        stepPerSecTv = findViewById(R.id.stepPerSecTv);
        helloTv = findViewById(R.id.helloTv);
        buttonReset = findViewById(R.id.buttonReset);
        buttonHistory = findViewById(R.id.buttonHistory);
        buttonHistory.setOnClickListener(new ButtonListener());
        buttonReset.setOnClickListener(new ButtonListener());
    }

    // endregion

    // region ui and database functions

    public void setName() {
        username = getSharedPreferences("com.lordoscar.sensorproject4", Context.MODE_PRIVATE).getString("username", "anon");
        if (username.equals("anon")){
            showLogin();
        }else {
            helloTv.setText("HELLO " + username.toUpperCase());
        }
    }

    public String getUsername() {
        return username;
    }

    public void updateSteps(int stepCounter, int stepsPerSecond) {
        totalStepsTv.setText(stepCounter + " steps");
        stepPerSecTv.setText(stepsPerSecond + " sps");

        AppDatabase appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, MainActivity.Database_name)
                .allowMainThreadQueries().build();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(calendar.getTime());
        Log.d("Date", date);


        List<Step> listOfStepsToday = appDatabase.appDAO().getAllStepsForThisUserAndDate(username, date);

        // Row exists --> update the steps
        if( listOfStepsToday.size() > 0 ){
            int steps = listOfStepsToday.get(0).getAmountOfSteps() + 1;
            listOfStepsToday.get(0).setAmountOfSteps(steps);
            appDatabase.appDAO().updateStepsForThisUser( listOfStepsToday.get(0) );
        } else {
            // Make new row for this date
            Step insertNewStep = new Step(username, 1, date);
            appDatabase.appDAO().insert(insertNewStep);
        }
    }

    private void clearDatabase() {
        getSharedPreferences("com.lordoscar.sensorproject4", MODE_PRIVATE).edit().clear().commit();
        showLogin();
    }

    // endregion

    // region first start / launch

    public void firstStartCheck() {
        SharedPreferences prefs = getSharedPreferences("com.lordoscar.sensorproject4", Context.MODE_PRIVATE);
        boolean firststart = prefs.getBoolean("firststart", true);
        if (firststart){
            showLogin();
        }else {
            serviceConnection = new ServiceConnection(this);
            stepsIntent = new Intent(this, StepCountingService.class);
            bindService(stepsIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            bound = true;
            Toast.makeText(this, "SERVICE BOUNDED", Toast.LENGTH_SHORT).show();
            Log.d("Service bound","step service initiated");
        }
    }

    private void showLogin(){
        Intent login = new Intent(MainActivity.this, LoginActivity.class);
        login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(login);
        finish();
    }

    // endregion

    // region Compass implementation

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

    private void compassRotation(){
        isAnimating = true;
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        animation.setFillAfter(true);
        compassImg.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    long lastupdate = SystemClock.elapsedRealtime();

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isAnimating){
            return;
        }
        if (SystemClock.elapsedRealtime() - lastupdate < 200){
            shakeDetector(event);
            return;
        }
        int prevAzi = -mAzimuth;
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            int degree = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
            mAzimuth = degree - (degree % 15);
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
            int degree = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
            mAzimuth = degree - (degree % 15);
        }

        AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(new LinearInterpolator());
        animSet.setFillAfter(true);
        animSet.setFillEnabled(true);

        //find whether we should rotate left or right
        Log.d("azimuth - (-)prev diff", "" + (mAzimuth - (-prevAzi)) + "(prevazi= " + prevAzi + ", mazi= " +mAzimuth + ")");

        RotateAnimation animRotate;

        animRotate = new RotateAnimation(prevAzi, -mAzimuth,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        animRotate.setDuration(200);
        animRotate.setFillAfter(true);
        animSet.addAnimation(animRotate);

        compassImg.startAnimation(animSet);
        shakeDetector(event);
        lastupdate = SystemClock.elapsedRealtime();
    }

    public void shakeDetector(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] values = event.values;
            // Detects movement/shake
            float x = values[0];
            float y = values[1];
            float z = values[2];

            float accelerationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
            if (accelerationSquareRoot >= 5) {
                isAnimating = true;
                mAzimuth = 0;
                compassImg.setRotation(mAzimuth);
                compassRotation();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // endregion shake i

    // region State implementation (pause, resume)

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, sensorAccelerometer);
        sensorManager.unregisterListener(this, sensorMagnetometer);
        Toast.makeText(this, "Unregistered listeners", Toast.LENGTH_SHORT).show();
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, sensorMagnetometer, SensorManager.SENSOR_DELAY_UI);
        Toast.makeText(this, "Registered listeners", Toast.LENGTH_SHORT).show();
    }

    // endregion

    // region ButtonListener

    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.buttonReset:
                    clearDatabase();
                    break;
                case R.id.buttonHistory:
                    Intent openHistoryForThisUser = new Intent(MainActivity.this, HistoryActivity.class);
                    openHistoryForThisUser.putExtra("name", helloTv.getText().toString());
                    startActivity(openHistoryForThisUser);
                    break;
            }
        }
    }

    // endregion

}
