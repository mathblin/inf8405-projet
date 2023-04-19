package com.example.ikram.myapplicationinf8405;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
public class MainActivity extends AppCompatActivity {

    private Button calibrateButton;
    private EditText adresseIPText;
    private String adresseIP;
    String URL = "http://webcam.aui.ma/axis-cgi/mjpg/video.cgi?resolution=CIF&amp";
    String youtubeUrl = "https://www.youtube.com/watch?v=4Zv0GZUQjZc&ab_channel=Freenove";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        TextView textViewTeam = findViewById(R.id.textView2);
        textViewTeam.setText(R.string.team);
        textViewTeam.setTypeface(null, Typeface.BOLD);
        textViewTeam.setTextColor(Color.WHITE);
        textViewTeam.setTextSize(20);

        adresseIPText = this.findViewById(R.id.adresseIPText);
        adresseIP = adresseIPText.getText().toString();

        calibrateButton = findViewById(R.id.buttonCalibrate);
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent robotActivity = new Intent(MainActivity.this, CalibrationActivity.class);
                robotActivity.putExtra("ip", adresseIP);
                startActivity(robotActivity);
            }
        });
    }
}
