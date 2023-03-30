package com.example.ikram.myapplicationinf8405;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.webkit.WebView;

public class CalibrationActivity extends AppCompatActivity {

    //Declare sensors
    SensorManager sensorManager;
    Sensor sensorAccelerometer;

    //Assign initial values to acceleration, first time sensor change, initial device position
    boolean firstTime = false;
    boolean isChecked ;
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



        //System.out.println("ligne000");
        Bundle extras = getIntent().getExtras();
        // System.out.println(extras.getString("ip"));
        //  System.out.println("ligne111");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        // Buttons
        buttonMin = this.findViewById(R.id.buttonMin);
        buttonMax = this.findViewById(R.id.buttonMax);
        buttonStart = this.findViewById(R.id.buttonStart);
        buttonReset = this.findViewById(R.id.buttonReset);
        buttonStart.setEnabled(true);

        //Set up sensors and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Switch switchMode = findViewById(R.id.switch2);
        switchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked2) {
               Intent auto = new Intent(CalibrationActivity.this, VideoActivity.class);

                if (isChecked2) {
                    switchMode.setText("Mode GYROSCOPE");

                    isChecked= false;
                    Log.d("mode", String.valueOf(isChecked));
                    auto.putExtra("isCheckedCaliber", isChecked);


                } else {
                    switchMode.setText("Mode JOYSTICK   ");
                    isChecked= true;
                    Log.d("mode", String.valueOf(isChecked));
                    auto.putExtra("isCheckedCaliber", isChecked);
                }
            }
        });

        // Set TextView
        textMin = findViewById(R.id.minValue);
        textMax = findViewById(R.id.maxValue);
        textPosition = findViewById(R.id.positionValue);
        /// System.out.println("textPositionn1:" + textPosition.getText().toString( ));
        //  System.out.println("textPositionn2:" + textMax.getText().toString( ));
        //  System.out.println("textPositionn3:" + textMin.getText().toString( ));

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
                System.out.println("testtt");
                Bundle extras = getIntent().getExtras();
                System.out.println(extras.getString("ip"));
                max_acceleration = max_acceleration * -1; // To remove the negative

                Intent auto = new Intent(CalibrationActivity.this, VideoActivity.class);
                Intent man = new Intent(CalibrationActivity.this, VideoActivityMan.class);

                String adresseIP;
                if (extras != null && !extras.getString("ip").equals("")) {
                    adresseIP = extras.getString("ip");


                } else {
                    adresseIP = "http://webcam.aui.ma/axis-cgi/mjpg/video.cgi?resolution=CIF&amp";
                }

                auto.putExtra("ip", adresseIP);
                auto.putExtra("maxAcceleration", max_acceleration);
                auto.putExtra("minAcceleration", min_acceleration);
                auto.putExtra("posXYZ", posXYZ);


                man.putExtra("ip", adresseIP);
                man.putExtra("maxAcceleration", max_acceleration);
                man.putExtra("minAcceleration", min_acceleration);
                man.putExtra("posXYZ", posXYZ);
                auto.putExtra("isCheckedCaliber", isChecked);
                startActivity(auto);
                //startActivity(i);
//                if (isChecked){
//                    startActivity(man);
//                    System.out.println("isCheckedtrue" + isChecked);
//                    System.out.println(isChecked);
//
//                } else {
//                    System.out.println("isCheckedfalse" + isChecked );
//
//
//
//                    startActivity(auto);
//
//
//                }
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
                //  System.out.println(textPosition.getText().toString());
                // samir System.out.println("textPosition.getText().toString()");
            }
        }
    };

}