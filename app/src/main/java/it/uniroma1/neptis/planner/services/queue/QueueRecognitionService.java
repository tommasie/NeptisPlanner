package it.uniroma1.neptis.planner.services.queue;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import it.uniroma1.neptis.planner.LoginActivity;
import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.TrackingActivity;
import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import static android.util.Log.d;

public class QueueRecognitionService extends IntentService {

    private final static String report_URL = "http://"+ LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/report_queue";
    private static final String TAG = "QueueRecognitionService";
    private IBinder binder = new LocalBinder();

    //Sensor variables
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor pedometer;

    private SensorsListener sensorLstr;

    private NotificationManager notificationManager;
    private static final int NOTIFICATION_ID = 123;
    //Needed to update the notification text
    private NotificationCompat.Builder builder;
    private Notification notification;

    //Data structures for signal sampling and analysis
    private List<Double> xList;
    private List<Double> yList;
    private List<Double> zList;
    private int firstWindowCounter = 0;
    private int slidingWindowCounter = 0;
    private SensorsWindowDetection detection;
    private float steps;
    private LinkedList<Boolean> queueRecognition;
    private long serviceStartTime;
    private String attractionId;

    //Partial wake-lock is needed to keep the CPU awake
    // even when the phone is hibernating -> only for data collection
    private PowerManager.WakeLock wakeLock;

    //Logback logging
    //private final Logger gpsLogger = LoggerFactory.getLogger("gps");
    //private final Logger signalsLogger = LoggerFactory.getLogger("signals");

    //Classifiers
    private Instances instances;
    private Classifier perceptron;
    private int windowSize = 256;
    private int halfWindow = windowSize / 2;

    public QueueRecognitionService() {
        super("QueueRecognitionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        pedometer = null;
        if(Build.VERSION.SDK_INT >= 19) {
            pedometer = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (pedometer != null)
                Log.i(TAG, "pedometer available, using this sensor");
            steps = 0.0F;
        }
        xList = new ArrayList<>();
        yList = new ArrayList<>();
        zList = new ArrayList<>();

        detection = new SensorsWindowDetection();
        queueRecognition = new LinkedList<>();
        serviceStartTime = System.currentTimeMillis();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        attractionId = intent.getStringExtra("destination_id");
        setUpNotification();
        startForeground(NOTIFICATION_ID, notification);
        setUpClassifiers();
        startDataCapture();
        //Keep the service as alive as possible
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {}

    public class LocalBinder extends Binder {
        public QueueRecognitionService getService() {
            return QueueRecognitionService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {

        sensorManager.unregisterListener(sensorLstr);
        stopForeground(true);
        notificationManager.cancel(NOTIFICATION_ID);
        wakeLock.release();
        super.onDestroy();
    }


    public void startDataCapture() {
        Log.i(TAG, "Start writing data");
        sensorLstr = new SensorsListener();
        sensorManager.registerListener(sensorLstr, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        if(pedometer != null)
            sensorManager.registerListener(sensorLstr, pedometer, SensorManager.SENSOR_DELAY_GAME);
    }


    public void receiveAccelerometerData(double x, double y, double z) {
        //Sliding window implementation
        if (firstWindowCounter == windowSize) {
            if (slidingWindowCounter % halfWindow == 0) {
                slidingWindowCounter = 1;
                Instance instance = new DenseInstance(instances.numAttributes());
                instance.setDataset(instances);
                detection.setX(xList);
                detection.setY(yList);
                detection.setZ(zList);
                double[] features = detection.getFeatures();
                for (int d = 0; d < 9; d++)
                    instance.setValue(d, features[d]);
                try {
                    double neural_class;
                    Intent i = new Intent("ML_data");
                    neural_class = perceptron.classifyInstance(instance);
                    i.putExtra("neural", neural_class);
                    boolean walking;
                    if (neural_class == 1.0)
                        walking = false;
                    else walking = true;
                    System.out.println(walking);
                    queueRecognition.add(walking);
                    if(queueRecognition.size() == 5) {
                        int values = 0;
                        for(boolean b : queueRecognition)
                            if(b)
                                values++;
                        if(values > queueRecognition.size() / 2) {
                            Log.d("QUEUE","start report");
                            long queueTime = System.currentTimeMillis() - serviceStartTime;
                            int minutes = (int)((queueTime / 1000)/60);
                            new ReportAsyncTask().execute(report_URL,String.valueOf(minutes));
                            sensorManager.unregisterListener(sensorLstr);
                        }
                        queueRecognition.removeFirst();
                    }
                    i.putExtra("walking", walking);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                slidingWindowCounter++;
            }
            xList.add(x);
            xList.remove(0);
            yList.add(y);
            yList.remove(0);
            zList.add(z);
            zList.remove(0);
        }
        else {
            firstWindowCounter++;
            xList.add(x);
            yList.add(y);
            zList.add(z);
        }
    }

    public void receiveStepCounterData(float s) {
        steps = s;
        Intent intent = new Intent("STEPS");
        intent.putExtra("steps",steps);
        //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    //Set up the notification, useful to check wether the service is still running
    //with or without the activity being on the foreground
    private void setUpNotification() {
        Intent i = new Intent(this,TrackingActivity.class);
        i.putExtra("attraction_id",attractionId);
        PendingIntent pi = PendingIntent.getActivity(this,0,i,0);
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon((R.drawable.ic_main_notification))
                .setContentTitle("Neptis: queue detection")
                .setContentText("")
                .setContentIntent(pi);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void setUpClassifiers() {
        try {
            InputStream is = getResources().getAssets().open("walk_semifinal2.arff");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            instances = new Instances(br);
            instances.setClassIndex(instances.numAttributes() - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        perceptron = new MultilayerPerceptron();
        try {
            perceptron.buildClassifier(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SensorsListener implements android.hardware.SensorEventListener {
        private static final String TAG = "SensorEventListener";
        private double[] gravity;
        final double alpha = 0.8;

        public SensorsListener() {
            this.gravity = new double[3];
            Log.i(TAG, "Listener started");
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                //For each axis of the accelerometer, ignore the gravity's forces noise
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                double x = event.values[0] - gravity[0];
                double y = event.values[1] - gravity[1];
                double z = event.values[2] - gravity[2];

                receiveAccelerometerData(x,y,z);

                return;
            }
            else if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                Log.i(TAG,"Step counted");
                //TODO: compute n of steps in a time frame a detect a walk
                    receiveStepCounterData(event.values[0]);
                return;
            }
            else return;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }

    private class ReportAsyncTask extends AsyncTask<String, String, Integer> {

//        protected void onPreExecute() {
//            progress.show();
//        }

        @Override
        protected Integer doInBackground(String... params) {
            InputStream in = null;
            int code = -1;
            String charset = "UTF-8";
            String urlURL = params[0]; // URL to call
            String sminutes = params[1];
            try {
                URL url = new URL(urlURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                // set like post request
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Accept-Charset", charset);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                // JSON object to send
                JSONObject json = new JSONObject();
                json.put("type", "Queue");
                json.put("category", "City");
                json.put("nameId", ""); //parameter not used server-side
                json.put("attractionId", attractionId);
                json.put("minutes", sminutes);

                // get current date
                Calendar calendar = Calendar.getInstance();
                java.sql.Timestamp ourJavaTimestampObject = new java.sql.Timestamp(calendar.getTime().getTime());
                String ts = ourJavaTimestampObject.toString().replace(' ','T');
                Log.d("timestamp: ",ts);
                json.put("data",ts);
                DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
                String s = json.toString();
                byte[] data = s.getBytes("UTF-8");
                printout.write(data);
                printout.flush();
                printout.close();
                in = new BufferedInputStream(urlConnection.getInputStream());
                code = urlConnection.getResponseCode();
                Log.d("code report",code+"");
                //urlConnection.disconnect();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return -1;
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result== 204)
                Toast.makeText(getApplicationContext(), "Report succesful! \nThank you!", Toast.LENGTH_LONG).show();
            else Toast.makeText(getApplicationContext(), result+"Error on reporting..", Toast.LENGTH_LONG).show();
//            progress.dismiss();
            stopSelf();
        }
    }
}
