package com.example.ikram.myapplicationinf8405;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.webkit.WebView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    //Button buttonMin;
    //Button buttonMax;
    Button buttonStart;
    Button buttonCalibrate;

    // The min and max acceleration for the x axis.
    double max_acceleration = 0.000;
    double min_acceleration = 0.000;

    boolean calibrate_was_pressed = false;
    // TextViews
    TextView textMin, textMax, textPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Bundle extras = getIntent().getExtras();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        // PUT the app on fullscreen.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Buttons
        buttonStart = this.findViewById(R.id.buttonStart);
        buttonCalibrate = this.findViewById(R.id.buttonCalibrate);
        buttonStart.setEnabled(false);

        //Set up sensors and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // IP selection (Spinner)
        List<TagValuePair> spinnerItems = new ArrayList<>();
        // Loading default values and stored values.
        spinnerItems.add(new TagValuePair("Poly", "132.207.186.54"));
        spinnerItems.add(new TagValuePair("O.O", "192.168.2.243"));
        spinnerItems.add(new TagValuePair("SMOL", "192.168.4.3"));
        Spinner ip_spinner = findViewById(R.id.ip_selection);

        ArrayAdapter<TagValuePair> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ip_spinner.setAdapter(spinnerAdapter);
        ip_spinner.bringToFront();

        // TODO : Add a way to add new values to the spinner and save them in memory for next start up.
        // Started Add Ip Button:
        Button button_addIP = findViewById(R.id.buttonAdd);
        button_addIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText tagEditText = findViewById(R.id.editTextTag);
                EditText valueEditText = findViewById(R.id.editTextValue);
                String tag = tagEditText.getText().toString();
                if(Objects.equals(tag, "Poly")) {
                    Toast.makeText(getApplicationContext(), "Poly ne peut être modifié", Toast.LENGTH_SHORT).show();
                    return;
                }// Can't add the Poly tag. It's protected.
                String value = valueEditText.getText().toString();
                // Check if the tag already exists in the spinnerItems list
                for (TagValuePair item : spinnerItems) {
                    if (item.getTag().equals(tag)) {
                        // If the tag exists, return without adding a new item
                        Toast.makeText(getApplicationContext(), tag+" déja dans la liste", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                TagValuePair tagValuePair = new TagValuePair(tag, value);
                spinnerItems.add(tagValuePair);
                ArrayAdapter<TagValuePair> adapter = new ArrayAdapter<>(CalibrationActivity.this, android.R.layout.simple_spinner_item, spinnerItems);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Spinner spinner = findViewById(R.id.ip_selection);
                spinner.setAdapter(adapter);
                Toast.makeText(getApplicationContext(), tag+" a été ajouté à la liste.", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }
        });
        //End add ip button logic.

        // Start of remove button logic.
        Button ButtonRemoveIP= findViewById(R.id.buttonRemove);
        ButtonRemoveIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText tag_to_remove = findViewById(R.id.editTextTag);
                if(Objects.equals(tag_to_remove.getText().toString(), "Poly")){
                    Toast.makeText(getApplicationContext(), "Poly ne peut être supprimer", Toast.LENGTH_SHORT).show();
                    return; // Poly tag can't be removed. It's protected.
                }
                Spinner spinner = findViewById(R.id.ip_selection);
                ArrayAdapter<TagValuePair> adapter = (ArrayAdapter<TagValuePair>) spinner.getAdapter();
                for (int i = 0; i < adapter.getCount(); i++) {
                    TagValuePair item = adapter.getItem(i);
                    if (item.getTag().equals(tag_to_remove.getText().toString())) {
                        adapter.remove(adapter.getItem(i));
                        Toast.makeText(getApplicationContext(), tag_to_remove.getText().toString()
                                +" a été enlevé de la liste.", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
        // End of Remove button logic

        // Switch to select the joystick or the gyro.
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



        buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle extras = getIntent().getExtras();
                max_acceleration = max_acceleration * -1; // To remove the negative

                TagValuePair selectedTagValuePair = (TagValuePair) ip_spinner.getSelectedItem();
                // get the tag and its ip value
                String tag = selectedTagValuePair.getTag();
                String ip_value = selectedTagValuePair.getValue();


                Intent auto = new Intent(CalibrationActivity.this, VideoActivity.class);
                Intent man = new Intent(CalibrationActivity.this, VideoActivityMan.class);

                String adresseIP = ip_value;

                auto.putExtra("tag",tag);
                auto.putExtra("ip", adresseIP);
                auto.putExtra("maxAcceleration", max_acceleration);
                auto.putExtra("minAcceleration", min_acceleration);
                auto.putExtra("posXYZ", posXYZ);

                man.putExtra("tag",tag);
                man.putExtra("ip", adresseIP);
                man.putExtra("maxAcceleration", max_acceleration);
                man.putExtra("minAcceleration", min_acceleration);
                man.putExtra("posXYZ", posXYZ);
                auto.putExtra("isCheckedCaliber", isChecked);
                startActivity(auto);
            }
        });

        buttonCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calibrate_was_pressed = true;
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

        int i = 0;
        public void onSensorChanged(SensorEvent event) {

            // Get the acceleration from sensors. Raw data
            posXYZ[0] = posXYZ[0] + 0.1 * (event.values[0] - posXYZ[0]);
            posXYZ[1] = posXYZ[1] + 0.1 * (event.values[1] - posXYZ[1]);
            posXYZ[2] = posXYZ[2] + 0.1 * (event.values[2] - posXYZ[2]);
            // Take a sample of a 100 data points for calibration
            if (calibrate_was_pressed){
                // Disable start button during calibration
                buttonStart.setEnabled(false);
                // while start is not pressed get average values
                /*for (int i = 0; i < 1000; i++) {
                    posXYZ[0] = addToAverage(i, posXYZ[0], event.values[0]); // initial X
                    posXYZ[1] = addToAverage(i, posXYZ[1], event.values[1]);  // initial Y
                    posXYZ[2] = addToAverage(i, posXYZ[2], event.values[2]);  // initial Z
                }*/
                Toast.makeText(getApplicationContext(), "Calibration terminée!", Toast.LENGTH_SHORT).show();
                buttonStart.setEnabled(true);
                calibrate_was_pressed = false;
                i=0;
            }

            //Loop acceleration in XYZ
            for (int i = 0; i < linear_acceleration.length; i++){

                // Find the max value between the new position and the initial device position
                // Calculate the difference between them
                double m = Math.max(event.values[i], posXYZ[i]);
                double sendValue;

                if (m == event.values[i]){
                    sendValue = m - posXYZ[i];
                }
                else{
                    sendValue = m - event.values[i];
                }

                // display the acceleration in negative value or in positive value depending of the position
                if(event.values[i] < posXYZ[i]){
                    //Log.d("direction", "pos " + i + " : " + (int)(sendValue * -1) + " m/s^2");
                    sendValue = sendValue * -1;
                }
                else{
                    //Log.d("direction", "pos " + i + " : " + (int)sendValue + " m/s^2");
                    sendValue = sendValue * 1;
                }
                relative_linear_acceleration[i] = sendValue;

                /*if(relative_linear_acceleration[0] > 0)
                {
                    buttonMin.setEnabled(true);
                    buttonMax.setEnabled(false);
                }
                else
                {
                    buttonMin.setEnabled(false);
                    buttonMax.setEnabled(true);
                }*/
                //textPosition.setText(String.valueOf(relative_linear_acceleration[0]));
            }
        }
    };

}