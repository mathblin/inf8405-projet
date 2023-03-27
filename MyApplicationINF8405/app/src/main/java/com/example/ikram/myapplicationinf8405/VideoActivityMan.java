package com.example.ikram.myapplicationinf8405;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;


import com.erz.joysticklibrary.JoyStick;
import com.jackandphantom.joystickview.JoyStickView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;



public class VideoActivityMan extends Activity {
    private static final String TAG = "VideoActivity";
    private Handler mHandler;
    private VideoView mv;
    String URL = "http://webcam.aui.ma/axis-cgi/mjpg/video.cgi?resolution=CIF&amp0";
    private boolean isLongPressDetected = false;
    private Runnable mLongPressRunnable;

    public static final int SERVERPORT = 5050;

    public static final String SERVER_IP = "10.0.0.62"; //10.200.26.68

    VideoActivityMan.ClientThread clientThread;
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
    private static final int LONG_PRESS_TIME = 500;


    private View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            // Ajoutez le code que vous souhaitez exécuter lorsque le long press est détecté
            return true;
        }
    };
    private void initLongPressRunnable() {
        mLongPressRunnable = new Runnable() {
            @Override
            public void run() {
                // Ajoutez le code que vous souhaitez exécuter lorsque le long press est détecté
            }
        };
    }
    private boolean mJoystickLongPressed = false;
    private Handler mJoystickHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1 && mJoystickLongPressed) {
                // Code to be executed after long press
                System.out.println("Button relea555sed");
                return true;
            }
            return false;
        }
    });

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private TextView messageTextView;

    // initialiser les vues et les flux
    //private TextView messageTextView;

   // private TextView textView;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle extras = getIntent().getExtras();
        //mv = new VideoView(this);
        setContentView(R.layout.activity_video_man);
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);


        webView.setRight(50);
        //   //System.out.println("ligne000");
        webView.loadUrl(URL);

        JoyStick joy1 = (JoyStick) findViewById(R.id.joy1);
        // joy1.setListener((JoyStick.JoyStickListener) this);
//        joy1.setPadColor(Color.GREEN);
        joy1.setButtonColor(Color.BLUE);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (extras != null) {
            maxAcceleration = extras.getDouble("maxAcceleration");
            minAcceleration = extras.getDouble("minAcceleration");
            posXYZ = extras.getDoubleArray("posXYZ");
        }

        topIntervalX = maxAcceleration / stopInterval;
        bottomIntervalX = minAcceleration / stopInterval;

        // Initialize and start thread
        clientThread = new ClientThread();
        thread = new Thread(clientThread);

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        thread.start();






        messageTextView = findViewById(R.id.text_view);

// se connecter au serveur
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // créer un socket pour se connecter au serveur
                    socket = new Socket(SERVER_IP, 5050);

                    // initialiser les flux d'entrée et de sortie
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                    // envoyer un message au serveur
                    writer.println("Hello, server!");
                    writer.flush();
                    System.out.println("Button 88888888888");
                    // attendre la réponse du serveur
                    String message = reader.readLine();
                    System.out.println("Button 9999999999999");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                System.out.println("Button 1111111111111");
                                 messageTextView.setText(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });





                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();








        JoyStick joyStick = (JoyStick) findViewById(R.id.joy1);


      // Button btn2 =  findViewById(R.id.button2);

        joyStick.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                System.out.println("Button releaslooooooooooooned");
                // Votre code pour l'événement long clic ici
                return true; // Indique que l'événement a été géré avec succès
            }
        });




//        btn2.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        mJoystickHandler.postDelayed(mLongPressRunnable, LONG_PRESS_TIME);
//                        System.out.println("Button redddleased");
//                        return true;
//                    case MotionEvent.ACTION_UP:
//                        System.out.println("Button reldddeased");
//                        mJoystickHandler.removeCallbacks(mLongPressRunnable);
//                        return true;
//                }
//                return false;
//            }
//        });







        joyStick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                return false;
            }

            public void onLongPress(MotionEvent e) {
                // Ajouter le code à exécuter lorsqu'un appui long est détecté

            }

        });







        joyStick.setListener(new JoyStick.JoyStickListener() {
            @Override
            public void onMove(JoyStick joyStick, double angle, double power, int direction) {
             //   System.out.println("Button released" + angle + power);
                // Faire quelque chose avec les données d'angle, de puissance et de direction
                String tag = "Angle: " + power  ;

                // Afficher le tag dans la console
                Log.d("JoyStick", tag);
                String angleText = "Angle: " + angle;
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





        webView.loadUrl("http://" + extras.getString("ip") + ":5000");
        JoyStickView joyStick0 = findViewById(R.id.circleView);
        joyStick0.setOnMoveListener(new JoyStickView.OnMoveListener() {

            @Override
            public void onMove(double angle, float strength) {

                float speed = (float) (strength * 1.75 + 80);


            }

            public void onButtonReleased() {
                // Button is released
                //  //System.out.println("Button released");
            }

        });


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
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (null != clientThread) {
//            clientThread.sendMessage("Disconnect");
//            //System.out.println("testsamir123456");
//
//            clientThread = null;
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            // fermer les flux d'entrée et de sortie et le socket lorsqu'on quitte l'activité
            reader.close();
            writer.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Accelerometer listener, set the values
    public SensorEventListener accelerometerListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }

        public void onSensorChanged(SensorEvent event) {
            //System.out.println("0000");
            // Get the acceleration from sensors. Raw data
            linear_acceleration[0] = event.values[0];
            linear_acceleration[1] = event.values[1];
            linear_acceleration[2] = event.values[2];

            double m = Math.max(linear_acceleration[0], posXYZ[0]);
            double stopValue;

            if (m == linear_acceleration[0]) {
                stopValue = m - posXYZ[0];
                //System.out.println("00001");
            } else {
                stopValue = m - linear_acceleration[0];
                //System.out.println("00002");
            }

            if (Math.abs(linear_acceleration[1]) <= 2.0 && stopValue < topIntervalX && stopValue < bottomIntervalX) {
                clientThread.sendMessage("x");
                //System.out.println("00003");
            }

            if (Math.abs(linear_acceleration[1]) > 2.0) // Pritorite pour tourner
            {

                // QEAD

                if (linear_acceleration[1] > posXYZ[1]) // droite
                {
                    //System.out.println("0000droite");
                    double sendValue = linear_acceleration[1] - posXYZ[1];
                    sendValue = Math.floor(sendValue);

                    if (sendValue >= 8.0) {
                        sendValue = 8.0;
                        if (sendValue != lastValueEQ) {
                            clientThread.sendMessage("d");
                            //System.out.println("0000d");
                        }
                    } else {
                        if (sendValue > lastValueEQ) {
                            clientThread.sendMessage("E");
                            clientThread.sendMessage("E");
                            //System.out.println("0000e");

                        } else if (sendValue < lastValueEQ) {
                            clientThread.sendMessage("e");
                            clientThread.sendMessage("e");
                            //System.out.println("0000Esmal");
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
                            //System.out.println("0000a");
                        }
                    } else {
                        if (sendValue > lastValueEQ) {
                            clientThread.sendMessage("Q");
                            clientThread.sendMessage("Q");
                            //System.out.println("0000q");

                        } else if (sendValue < lastValueEQ) {
                            clientThread.sendMessage("q");
                            clientThread.sendMessage("q");
                            //System.out.println("0000qsmal");
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
                        //System.out.println("0000xligne257");
                    } else {
                        if (sendValue > lastValueWS) {
                            clientThread.sendMessage("S");
                            clientThread.sendMessage("S");
                            //System.out.println("0000s262");
                        } else if (sendValue < lastValueWS) {
                            clientThread.sendMessage("s");
                            clientThread.sendMessage("s");
                            //System.out.println("0000266");
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
                            //System.out.println("0000pppppppppppppppppppppppppWWW");

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

