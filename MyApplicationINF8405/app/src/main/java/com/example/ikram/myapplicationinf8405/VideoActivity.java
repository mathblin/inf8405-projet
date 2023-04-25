package com.example.ikram.myapplicationinf8405;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.erz.joysticklibrary.JoyStick;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;

public class VideoActivity extends Activity {
    double JOYSTICK_SCALE = 0.5;
    private static final String TAG = "VideoActivity";

    // VideoView && URL
    private VideoView mv;
    String mode = "0,";
    // Server port and thread
    public static final String SERVER_IP_POLY = "10.0.0.62";

    // Nouveau Tests pour un IP et port
    public static final int VIDEOPORT = 8000;
    public static final int SERVERPORT = 5050;
    //
    ClientThread clientThread;
    Thread thread;
    //Declare
    SensorManager sensorManager;
    Sensor sensorAccelerometer;

    //Assign initial values to acceleration, first time sensor change, initial device position
    double[] linear_acceleration = {0, 0, 0};
    double[] posXYZ = {0, 0, 0};
    boolean isChecked;

    // Max acceleration and Min acceleration
    double maxAcceleration;
    double minAcceleration;

    // Top and bottom interval to send X to the server to stop
    double topIntervalX;
    double bottomIntervalX;

    // number to divide max and min to set the X range to stop
    double stopInterval = 4.0;

    double lastValueWS = 0.0;
    double lastValueEQ = 0.0;
    boolean swichMode;

    RobotController robotController;
    private BroadcastReceiver networkReceiver;

    String video_ip;
    String video_url;

    String SERVER_IP;
    public void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            maxAcceleration = extras.getDouble("maxAcceleration");
            minAcceleration = extras.getDouble("minAcceleration");
            posXYZ = extras.getDoubleArray("posXYZ");
            isChecked =  extras.getBoolean("isCheckedCaliber", false);
            video_ip = extras.getString("ip");
        }
        swichMode = isChecked;
        String tag = "Angle: " + isChecked +  " esp"+ Arrays.toString(posXYZ);
        // Afficher le tag dans la console
        Log.d("JoyStick2", tag);
        video_url = "http://"+video_ip+":"+VIDEOPORT;

        // Handles different ips
        if(Objects.equals(extras.getString("tag"), "Poly"))
            SERVER_IP= SERVER_IP_POLY;
        else // Autre IP que Poly
            SERVER_IP = extras.getString("ip");
        // End

        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video_view);
        WebView webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setRight(50);
        webView.loadUrl(video_url);

        // PUT the app on fullscreen.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);




        // TextView for connection
        TextView connection_lost = findViewById(R.id.connection_lost);
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Create a BroadcastReceiver to listen for network connectivity changes
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get the network info
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                // Check if the network is connected
                if (networkInfo != null && networkInfo.isConnected()) {
                    // Network is connected
                    connection_lost.setVisibility(View.INVISIBLE);
                    System.out.println("networkInfo = " + networkInfo);
                } else {
                    // Network is disconnected
                    connection_lost.bringToFront();
                    connection_lost.setVisibility(View.VISIBLE);
                    System.out.println("networkInfo = " + networkInfo);
                }
            }
        };

        // Register the BroadcastReceiver to listen for network connectivity changes
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);

        //Set up sensors and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        topIntervalX = maxAcceleration/stopInterval;
        bottomIntervalX = minAcceleration/stopInterval;

        // Initialize and start thread
        clientThread = new ClientThread();
        thread = new Thread(clientThread);

        // Initialize the Robot Controller
        robotController = new RobotController(clientThread);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        thread.start();

        // Display Size:
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Joystick Logic
        JoyStick joyStick = (JoyStick) findViewById(R.id.joy111);
        joyStick.setButtonColor(Color.rgb(214, 185, 113));

        // TODO : Ajouter une maniere dans l'app de changer le scale du joystick.
        int joyStickSize = (int) (Math.min(screenWidth, screenHeight) * JOYSTICK_SCALE);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(joyStickSize, joyStickSize);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        joyStick.setLayoutParams(layoutParams);
        joyStick.setPadColor(android.R.color.darker_gray);
        joyStick.setAlpha(0.5F);

        joyStick.bringToFront();
        // End Joystick Logic

        // SwitchMode logic
        Switch switchMode = findViewById(R.id.switchmode);
        switchMode.bringToFront();
        // End SwitchMode logic

        // Hello Button logic
        Button hello_button = findViewById(R.id.hello_button);
        hello_button.bringToFront();
        hello_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle button click here
                Toast.makeText(getApplicationContext(), "Bonjour!", Toast.LENGTH_SHORT).show();
                clientThread.sendMessage("2,1;");
            }
        });
        // End of Hello button logic

        if (isChecked){
            joyStick.setVisibility(View.VISIBLE);
            switchMode.setText("Mode JOYSTICK start");
            switchMode.setChecked(isChecked);
        }else {
            joyStick.setVisibility(View.INVISIBLE);
            switchMode.setText("Mode GYROSCOPE start");
            switchMode.setChecked(isChecked);
        }
        switchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked2) {

                // String test2 = "check ischeked avant " + isChecked;

                Log.d("mode2",  "ischecked :"+isChecked );
                Log.d("mode2",  "ischecked2 : " + isChecked2);

                    if (isChecked2) {
                        switchMode.setText("Mode JOYSTICK3 ");
                        joyStick.setVisibility(View.VISIBLE);
                        swichMode = true;
                    } else {
                        switchMode.setText("Mode gyro ");
                        joyStick.setVisibility(View.INVISIBLE);
                        swichMode = false;
                }
            }
        });

        joyStick.setListener(new JoyStick.JoyStickListener() {
            @Override
            public void onMove(JoyStick joyStick, double angle, double power, int direction) {
                double angleDegrees = Math.toDegrees(angle);
                if (angleDegrees <0){
                    double angleFinal =  (180 + Math.abs(angleDegrees));
                    angleDegrees = angleFinal;
                } else if (angleDegrees == 0)  {
                    double  angleFinal = (int) 0;
                    angleDegrees = angleFinal;
                } else {
                    double  angleFinal = (int) (180 -angleDegrees);
                    angleDegrees = angleFinal;
                }

                int test1 = (int) Math.floor(power);
                int test2 = (int) Math.floor(angleDegrees);
                String command = mode + test2 +  ","+ test1 +";" ;
                Log.d("JoyStick", command);
               // clientThread.sendMessage(command);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        clientThread.sendMessage(command);
                    }
                }).start();

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onTap() {
                // Gérer le tap sur le joystick
            }
            @Override
            public void onDoubleTap() {
                // Gérer le double tap sur le joystick
            }
            public void onLongPress() {
                // Gérer le double tap sur le joystick
            }
        });

        // Execute URL for video
        //new DoRead().execute(URL);
    }

    //On resume, register accelerometer listener
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(accelerometerListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        super.onPause();
        //mv.stopPlayback();
        clientThread.sendMessage("1,x;");
        finish();
    }

    //On stop, unregister accelerometer listener
    public void onStop() {
        super.onStop();
        clientThread.sendMessage("1,x;");
        sensorManager.unregisterListener(accelerometerListener);
        unregisterReceiver(networkReceiver);
    }

    // On destroy, disconnect thread
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != clientThread) {
            // TODO : Send Stop command to robot.
            clientThread.sendMessage("1,x;");
            clientThread.sendMessage("disconnect");
            //clientThread = null;
        }
    }

    private double addToAverage(int n, double old_average, double new_value ){
        return ( n * old_average + new_value ) / (n + 1);
    }

    //Accelerometer listener, set the values
    public SensorEventListener accelerometerListener = new SensorEventListener() {

        public void onAccuracyChanged(Sensor sensor, int acc) { }

        private static final double ALPHA = 0.1; // Low-pass filter coefficient
        private double[] filteredValues = {0, 0, 0};
        // TODO : Filtrer les angles du gyrospcope.
        // TODO : Réduire la latence des inputs.
        // Reduire la quantite de donnee envoyer au serveur
        public void onSensorChanged(SensorEvent event) {
            if (swichMode){ return; }

            filteredValues[0] = filteredValues[0] + ALPHA * (event.values[0] - filteredValues[0]);
            filteredValues[1] = filteredValues[1] + ALPHA * (event.values[1] - filteredValues[1]);
            filteredValues[2] = filteredValues[2] + ALPHA * (event.values[2] - filteredValues[2]);

            Log.d("Filtered Values:", filteredValues[0] +" , "+filteredValues[1] +" , "+filteredValues[2] );
            bottomIntervalX = posXYZ[0]+1.0;
            topIntervalX = posXYZ[0]-1.0;
            robotController.handleRobotMovement(filteredValues, posXYZ, topIntervalX, bottomIntervalX);
        }
    };

    // Class for async task of video stream
    public class DoRead extends AsyncTask<String, Void, VideoStreamActivity> {
        protected VideoStreamActivity doInBackground(String... url) {
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            Log.d(TAG, "1. Sending http request");
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                Log.d(TAG, "2. Request finished, status = "
                        + res.getStatusLine().getStatusCode());
                if (res.getStatusLine().getStatusCode() == 401) {
                    return null;
                }
                return new VideoStreamActivity(res.getEntity().getContent());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-ClientProtocolException", e);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-IOException", e);
            }

            return null;
        }

        protected void onPostExecute(VideoStreamActivity result) {
            mv.setSource(result);
            mv.setDisplayMode(VideoView.SIZE_BEST_FIT);
            mv.showFps(true);
        }
    }
    // source : https://stackoverflow.com/questions/25093546/android-os-networkonmainthreadexception-at-android-os-strictmodeandroidblockgua
    // source : http://www.coderzheaven.com/2017/05/01/client-server-programming-in-android-send-message-to-the-client-and-back/
    // Class for client thread with socket to send message
    class ClientThread implements Runnable {
        private Socket socket;

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        void sendMessage(String message) {
            try {
                if (null != socket) {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    out.write(message);
                    out.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}