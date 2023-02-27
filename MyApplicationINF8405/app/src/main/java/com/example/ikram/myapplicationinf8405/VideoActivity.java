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
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
    String URL = "http://192.168.55.3:5000";
     // String URL = "http://10.0.0.238:5000";

   // String URL = "http://webcam.aui.ma/axis-cgi/mjpg/video.cgi?resolution=CIF&amp0";
    // Server port and thread

    public static final int SERVERPORT = 5050;
   // public static final String SERVER_IP = "132.207.186.11"; //10.200.26.68
    //public static final String SERVER_IP = "10.0.0.62"; //10.200.26.68
  //  public static final String SERVER_IP = "10.200.61.168"; //10.200.26.68
    public static final String SERVER_IP = "10.200.0.163"; //10.200.26.68


    ClientThread clientThread;
    Thread thread;

    //Declare
    SensorManager sensorManager;
    Sensor sensorAccelerometer;

    //Assign initial values to acceleration, first time sensor change, initial device position
    double[] linear_acceleration = {0, 0, 0};
    double[] posXYZ = {0, 0, 0};

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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        System.out.println("ligne000");
        Bundle extras = getIntent().getExtras();
        System.out.println(extras.getString("ip"));
        System.out.println("ligne111");


        //mv = new VideoView(this);
        setContentView(R.layout.activity_video_view);
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setRight(50);
        System.out.println("ligne000");
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
        if(extras != null){
            maxAcceleration = extras.getDouble("maxAcceleration");
            minAcceleration = extras.getDouble("minAcceleration");
            posXYZ = extras.getDoubleArray("posXYZ");
        }

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
            System.out.println("testsamir123456");

            clientThread = null;
        }
    }

    //Accelerometer listener, set the values
    public SensorEventListener accelerometerListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) { }

        public void onSensorChanged(SensorEvent event) {
            System.out.println("0000");
            // Get the acceleration from sensors. Raw data
            linear_acceleration[0] = event.values[0] ;
            linear_acceleration[1] = event.values[1] ;
            linear_acceleration[2] = event.values[2] ;

            double m = Math.max(linear_acceleration[0], posXYZ[0]);
            double stopValue;

            if (m == linear_acceleration[0]){
                stopValue = m - posXYZ[0];
                System.out.println("00001");
            }
            else{
                stopValue = m - linear_acceleration[0];
                System.out.println("00002");
            }

            if(Math.abs(linear_acceleration[1]) <= 2.0 && stopValue < topIntervalX && stopValue < bottomIntervalX)
            {
                clientThread.sendMessage("x");
                System.out.println("00003");
            }

            if (Math.abs(linear_acceleration[1]) > 2.0) // Pritorite pour tourner
            {

                // QEAD

                if (linear_acceleration[1] > posXYZ[1]) // droite
                {
                    System.out.println("0000droite");
                    double sendValue = linear_acceleration[1] - posXYZ[1];
                    sendValue = Math.floor(sendValue);

                    if (sendValue >= 8.0) {
                        sendValue = 8.0;
                        if (sendValue != lastValueEQ) {
                            clientThread.sendMessage("d");
                            System.out.println("0000d");
                        }
                    } else {
                        if (sendValue > lastValueEQ) {
                            clientThread.sendMessage("E");
                            clientThread.sendMessage("E");
                            System.out.println("0000e");

                        } else if (sendValue < lastValueEQ) {
                            clientThread.sendMessage("e");
                            clientThread.sendMessage("e");
                            System.out.println("0000Esmal");
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
                            clientThread.sendMessage("a");
                            System.out.println("0000a");
                        }
                    } else {
                        if (sendValue > lastValueEQ) {
                            clientThread.sendMessage("Q");
                            clientThread.sendMessage("Q");
                            System.out.println("0000q");

                        } else if (sendValue < lastValueEQ) {
                            clientThread.sendMessage("q");
                            clientThread.sendMessage("q");
                            System.out.println("0000qsmal");
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
                        clientThread.sendMessage("x");
                        System.out.println("0000xligne257");
                    } else {
                        if (sendValue > lastValueWS) {
                            clientThread.sendMessage("S");
                            clientThread.sendMessage("S");
                            System.out.println("0000s262");
                        } else if (sendValue < lastValueWS) {
                            clientThread.sendMessage("s");
                            clientThread.sendMessage("s");
                            System.out.println("0000266");
                        }
                    }

                    lastValueWS = sendValue;
                } else // avancer
                {
                    double sendValue = posXYZ[0] - linear_acceleration[0];
                    sendValue = Math.floor(sendValue);

                    if (sendValue < topIntervalX) {
                        clientThread.sendMessage("x");
                    } else {
                        if (sendValue > lastValueWS) {
                            clientThread.sendMessage("W");
                            clientThread.sendMessage("W");
                            System.out.println("0000pppppppppppppppppppppppppWWW");

                        } else if (sendValue < lastValueWS) {
                            clientThread.sendMessage("w");
                            clientThread.sendMessage("w");
                        }
                    }
                    lastValueWS = sendValue;
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

            System.out.println("tests000000000000000000amirk0"+SERVERPORT);
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);
                System.out.println("teseeeeeeeeeeeeeeeetsamirk1"+SERVERPORT);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
                System.out.println("catcfrrrrrrrrrrrrrrrrrrrrrrrrrrrrh1");
            } catch (IOException e1) {
                e1.printStackTrace();
                System.out.println("catffffffffffffffffffffffffffch2");
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