package com.example.ikram.myapplicationinf8405;

import static java.sql.DriverManager.println;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.widget.TextView;
public class MainActivity extends AppCompatActivity {

    private Button calibrateButton;
    private EditText adresseIPText;
    private String adresseIP;
    String URL = "http://webcam.aui.ma/axis-cgi/mjpg/video.cgi?resolution=CIF&amp";
    String youtubeUrl = "https://www.youtube.com/watch?v=4Zv0GZUQjZc&ab_channel=Freenove";

    private int value = 0;
    private boolean buttonPressed = false;
    private final Handler handler = new Handler();
    private final Runnable repeatAction = new Runnable() {
        @Override
        public void run() {
            if (buttonPressed) {
                value++;
                handler.postDelayed(this, 20);
                System.out.println("command test");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        WebView webView = (WebView) findViewById(R.id.view);
        webView.setWebViewClient(new WebViewClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setRight(50);
        webView.loadUrl(youtubeUrl);
        // webView.loadUrl(URL);


        TextView textView = findViewById(R.id.textView);
        textView.setText(R.string.welcome);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(Color.BLUE);
        textView.setTextSize(20);

        TextView textViewTeam = findViewById(R.id.textView2);
        textViewTeam.setText(R.string.team);
        textViewTeam.setTypeface(null, Typeface.BOLD);
        textViewTeam.setTextColor(Color.RED);
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

//        button.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                switch (motionEvent.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        buttonPressed = true;
//                        handler.post(repeatAction);
//                        return true;
//                    case MotionEvent.ACTION_UP:
//                    case MotionEvent.ACTION_CANCEL:
//                        buttonPressed = false;
//                        handler.removeCallbacks(repeatAction);
//                        return true;
//                }
//                return false;
//            }
//        });
    }


//        Button button = findViewById(R.id.longPressButton);
//        button.setOnTouchListener(new View.OnTouchListener() {
//            private Handler handler = new Handler();
//            private boolean longPressDetected = false;
//            int value = 0 ;
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        longPressDetected = false;
//                        handler.postDelayed(longPressRunnable, 500); // 500ms pour considérer un long press
//                        System.out.println("longbreak");
//                        break;
//                    case MotionEvent.ACTION_UP:
//                    case MotionEvent.ACTION_CANCEL:
//                        handler.removeCallbacks(longPressRunnable);
//                        if (!longPressDetected) {
//                            // Action pour un clic simple
//                            System.out.println("shortbreak");
//                        }
//                        break;
//                }
//                return true;
//            }
//
//            private Runnable longPressRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    longPressDetected = true;
//
//                    // Action pour un long press
//                }
//            };
//        });

//        Button longPressButton = findViewById(R.id.longPressButton); // Récupère l'élément Button depuis le layout XML
//          // Valeur initiale de la variable
//
//        longPressButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                value += 1;
//                System.out.println("test");
//                System.out.println(value);
//                // Mettre à jour la valeur de l'élément UI approprié ici
//            }
//        });


}
