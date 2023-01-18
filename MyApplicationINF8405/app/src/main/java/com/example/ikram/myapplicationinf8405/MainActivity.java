package com.example.ikram.myapplicationinf8405;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    //Button connexion;
    private Button calibrateButton;
    private EditText adresseIPText;
    private String adresseIP ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adresseIPText = this.findViewById(R.id.adresseIPText);
        adresseIP = adresseIPText.getText().toString();

        calibrateButton = this.findViewById(R.id.buttonCalibrate);
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

