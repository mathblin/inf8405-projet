package com.example.ikram.myapplicationinf8405;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.erz.joysticklibrary.JoyStick;
import com.jackandphantom.joystickview.JoyStickView;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class VideoActivity extends Activity {
    private static final String TAG = "VideoActivity";

    // VideoView && URL
    private VideoView mv;
    //String URL = "http://132.207.186.54:5000";
   // String URL = "http://192.168.55.3:5000";
     // String URL = "http://10.0.0.238:5000";

    String URL = "https://www.youtube.com/watch?v=4Zv0GZUQjZc&ab_channel=Freenove";
    String mode = "0,";
    // Server port and thread

    public static final int SERVERPORT = 5050;
   // public static final String SERVER_IP = "132.207.186.11"; //10.200.26.68
    public static final String SERVER_IP = "132.207.186.54"; //10.200.26.68
    //public static final String SERVER_IP = "10.200.61.168"; //10.200.26.68
  // public static final String SERVER_IP = "10.200.0.163"; //10.200.26.68 192.168.56.1
  //  public static final String SERVER_IP = "192.168.56.1"; //10.200.26.68


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
    public void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            maxAcceleration = extras.getDouble("maxAcceleration");
            minAcceleration = extras.getDouble("minAcceleration");
            posXYZ = extras.getDoubleArray("posXYZ");
            isChecked =  extras.getBoolean("isCheckedCaliber", false);
        }
        swichMode = isChecked;
        String tag = "Angle: " + isChecked +  " esp"+  posXYZ ;
        // Afficher le tag dans la console
        Log.d("JoyStick2", tag);


        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //mv = new VideoView(this);
        setContentView(R.layout.activity_video_view);
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setRight(50);
          webView.loadUrl(URL);
      //  webView.loadUrl("http://"+extras.getString("ip")+":5000");



        //Set up sensors and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //get extras passed from previous activity
        /*if (extras != null && !extras.getString("ip").equals("")) {
            URL = extras.getString("ip");
        } else {
            URL = "http://webcam.aui.ma/axis-cgi/mjpg/video.cgi?resolution=CIF&amp";
        }
*/

        topIntervalX = maxAcceleration/stopInterval;
        bottomIntervalX = minAcceleration/stopInterval;

        // Initialize and start thread
        clientThread = new ClientThread();
        thread = new Thread(clientThread);




        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        thread.start();

        JoyStick joyStick = (JoyStick) findViewById(R.id.joy11);
        Switch switchMode = findViewById(R.id.switchmode);
        if (isChecked){

            joyStick.setVisibility(View.VISIBLE);
            switchMode.setText("Mode JOYSTICK start");
            switchMode.setChecked(isChecked);

        }else {


            joyStick.setVisibility(View.INVISIBLE);
            switchMode.setText("Mode GYROSCOPE start");
            switchMode.setChecked(isChecked);
        }


        joyStick.setListener(new JoyStick.JoyStickListener() {


            @Override
            public void onMove(JoyStick joyStick, double angle, double power, int direction) {
                // Faire quelque chose avec les données d'angle, de puissance et de direction
                String tag = "Angle: " + power;

                // Afficher le tag dans la console
                Log.d("JoyStick", tag);

                String angleText = "Angle: " + angle;

            }

            @Override
            public void onTap() {
            }

            @Override
            public void onDoubleTap() {
            }

            public void onLongPress() {
            }
        });


        switchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked2) {

//                String test2 = "check ischeked avant " + isChecked;

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
        mv.stopPlayback();
        finish();
    }

    //On stop, unregister accelerometer listener
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(accelerometerListener);
    }

    // On destroy, disconnect thread
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != clientThread) {
            clientThread.sendMessage("Disconnect");
            clientThread = null;
        }
    }

    //Accelerometer listener, set the values
    public SensorEventListener accelerometerListener = new SensorEventListener() {




        public void onAccuracyChanged(Sensor sensor, int acc) { }




        public void onSensorChanged(SensorEvent event) {
            if (!swichMode){
            // Get the acceleration from sensors. Raw data
            linear_acceleration[0] = event.values[0] ;
            linear_acceleration[1] = event.values[1] ;
            linear_acceleration[2] = event.values[2] ;


            System.out.println("posXYZ : =========================");
            System.out.println("posXYZ 1: "+ posXYZ[0]);
            System.out.println("posXYZ 2: "+ posXYZ[1]);
            System.out.println("posXYZ 3: "+ posXYZ[2]);

            double m = Math.max(linear_acceleration[0], posXYZ[0]);
            System.out.println("max m : "+ m);

            double stopValue;

            if (m == linear_acceleration[0]){
                stopValue = m - posXYZ[0];
            }
            else{
                stopValue = m - linear_acceleration[0];
            }

            if(Math.abs(linear_acceleration[1]) <= 2.0 && stopValue < topIntervalX && stopValue < bottomIntervalX)
            {
                clientThread.sendMessage("1,x;");
            }

            if (Math.abs(linear_acceleration[1]) > 2.0) // Pritorite pour tourner
            {

                // QEAD

                if (linear_acceleration[1] > posXYZ[1]) // droite
                {
                    double sendValue = linear_acceleration[1] - posXYZ[1];
                    sendValue = Math.floor(sendValue);

                    if (sendValue >= 8.0) {
                        sendValue = 8.0;
                        if (sendValue != lastValueEQ) {
                            clientThread.sendMessage("1,d;");
                        }
                    } else {
                        if (sendValue > lastValueEQ) {
                            clientThread.sendMessage("1,E;");
                            clientThread.sendMessage("1,E;");

                        } else if (sendValue < lastValueEQ) {
                            clientThread.sendMessage("1,e;");
                            clientThread.sendMessage("1,e;");
                        }
                    }
                    lastValueEQ = sendValue;
                } else // gauche
                {
                    double sendValue = posXYZ[1] - linear_acceleration[1];
                    sendValue = Math.floor(sendValue);

                    if (sendValue >= 8.0) {
                        sendValue = 8.0;
                        if (sendValue != lastValueEQ) {
                            clientThread.sendMessage("1,a;");
                        }
                    } else {
                        if (sendValue > lastValueEQ) {
                            clientThread.sendMessage("1,Q;");
                            clientThread.sendMessage("1,Q;");

                        } else if (sendValue < lastValueEQ) {
                            clientThread.sendMessage("1,q;");
                            clientThread.sendMessage("1,q;");
                        }
                    }
                    lastValueEQ = sendValue;
                }
            } else // priorite avancer
            {
                // WS
                if (linear_acceleration[0] > posXYZ[0]) // reculer
                {
                    double sendValue = linear_acceleration[0] - posXYZ[0];
                    sendValue = Math.floor(sendValue);

                    if (sendValue < bottomIntervalX) {
                        clientThread.sendMessage("1,x;");
                    } else {
                        if (sendValue > lastValueWS) {
                            clientThread.sendMessage("1,S;");
                            clientThread.sendMessage("1,S;");
                        } else if (sendValue < lastValueWS) {
                            clientThread.sendMessage("1,s;");
                            clientThread.sendMessage("1,s;");
                        }
                    }

                    lastValueWS = sendValue;
                } else // avancer
                {
                    double sendValue = posXYZ[0] - linear_acceleration[0];
                    sendValue = Math.floor(sendValue);

                    if (sendValue < topIntervalX) {
                        clientThread.sendMessage("1,x;");
                    } else {
                        if (sendValue > lastValueWS) {
                            clientThread.sendMessage("1,W;");
                            clientThread.sendMessage("1,W;");

                        } else if (sendValue < lastValueWS) {
                            clientThread.sendMessage("1,w;");
                            clientThread.sendMessage("1,w;");
                        }
                    }
                    lastValueWS = sendValue;
                }
            }
        }

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