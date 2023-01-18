package com.example.ikram.myapplicationinf8405;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.webkit.WebView;

public class CalibrationActivity extends AppCompatActivity {

    //Declare sensors
    SensorManager sensorManager;
    Sensor sensorAccelerometer;

    //Assign initial values to acceleration, first time sensor change, initial device position
    boolean firstTime = false;
    double[] posXYZ = {0, 0, 0};
    double[] linear_acceleration = {0, 0, 0};
    double[] relative_linear_acceleration = {0, 0, 0};

    // Buttons
    Button buttonMin;
    Button buttonMax;
    Button buttonStart;
    Button buttonReset;

    // The min and max acceleration for the x axis.
    double max_acceleration = 0.000;
    double min_acceleration = 0.000;

    // TextViews
    TextView textMin, textMax, textPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        // Buttons
        buttonMin = this.findViewById(R.id.buttonMin);
        buttonMax = this.findViewById(R.id.buttonMax);
        buttonStart = this.findViewById(R.id.buttonStart);
        buttonReset = this.findViewById(R.id.buttonReset);
        buttonStart.setEnabled(false);

        //Set up sensors and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Set TextView
        textMin = findViewById(R.id.minValue);
        textMax = findViewById(R.id.maxValue);
        textPosition = findViewById(R.id.positionValue);

        // Set event listeners for buttons
        buttonMin = findViewById(R.id.buttonMin);
        buttonMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textMin.setText(String.valueOf(relative_linear_acceleration[0]));
                min_acceleration = relative_linear_acceleration[0];

                if (!textMin.getText().toString().equals("0.0") && !textMax.getText().toString().equals("0.000")) {
                    buttonStart.setEnabled(true);
                }
            }
        });

        buttonMax = findViewById(R.id.buttonMax);
        buttonMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textMax.setText(String.valueOf(relative_linear_acceleration[0]));
                max_acceleration = relative_linear_acceleration[0];

                if (!textMin.getText().toString().equals("0.000") && !textMax.getText().toString().equals("0.0")) {
                    buttonStart.setEnabled(true);
                }
            }
        });

        buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                max_acceleration = max_acceleration * -1; // To remove the negative

                Intent i = new Intent(CalibrationActivity.this, VideoActivity.class);

                Bundle extras = getIntent().getExtras();
                String adresseIP;
                if (extras != null && !extras.getString("ip").equals("")) {
                    adresseIP = extras.getString("ip");
                } else {
                    adresseIP = "http://webcam.aui.ma/axis-cgi/mjpg/video.cgi?resolution=CIF&amp";
                }

                i.putExtra("ip", adresseIP);
                i.putExtra("maxAcceleration", max_acceleration);
                i.putExtra("minAcceleration", min_acceleration);
                i.putExtra("posXYZ", posXYZ);
                startActivity(i);
            }
        });

        buttonReset = findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                min_acceleration = 0.000;
                max_acceleration = 0.000;
                firstTime = false;
                textMax.setText("0.000");
                textMin.setText("0.000");
                buttonStart.setEnabled(false);
            }
        });
    }

    //On resume, register accelerometer listener
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(accelerometerListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //On stop, unregister accelerometer listener
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(accelerometerListener);
    }

    //Accelerometer listener, set the values
    public SensorEventListener accelerometerListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) { }

        public void onSensorChanged(SensorEvent event) {

            // Get the acceleration from sensors. Raw data
            linear_acceleration[0] = event.values[0] ;
            linear_acceleration[1] = event.values[1] ;
            linear_acceleration[2] = event.values[2] ;

            // If first time sensor change, keep initial device position
            if (firstTime == false){
                posXYZ[0] = linear_acceleration[0];  // initial X
                posXYZ[1] = linear_acceleration[1];  // initial Y
                posXYZ[2] = linear_acceleration[2];  // initial Z
                firstTime = true;
            }

            //Loop acceleration in XYZ
            for (int i = 0; i < linear_acceleration.length; i++){

                // Find the max value between the new position and the initial device position
                // Calculate the difference between them
                double m = Math.max(linear_acceleration[i], posXYZ[i]);
                double sendValue;

                if (m == linear_acceleration[i]){
                    sendValue = m - posXYZ[i];
                }
                else{
                    sendValue = m - linear_acceleration[i];
                }

                // Round to 3 decimal places
//                sendValue = sendValue * 1000;
//                sendValue = Math.round(sendValue);
//                sendValue = sendValue / 1000;

                // display the acceleration in negative value or in positive value depending of the position
                if(linear_acceleration[i] < posXYZ[i]){
                    //Log.d("direction", "pos " + i + " : " + (int)(sendValue * -1) + " m/s^2");
                    sendValue = sendValue * -1;
                    relative_linear_acceleration[i] = sendValue;
                }
                else{
                    //Log.d("direction", "pos " + i + " : " + (int)sendValue + " m/s^2");
                    sendValue = sendValue * 1;
                    relative_linear_acceleration[i] = sendValue;
                }

                if(relative_linear_acceleration[0] > 0)
                {
                    buttonMin.setEnabled(true);
                    buttonMax.setEnabled(false);
                }
                else
                {
                    buttonMin.setEnabled(false);
                    buttonMax.setEnabled(true);
                }
                textPosition.setText(String.valueOf(relative_linear_acceleration[0]));
            }
        }
    };

}
