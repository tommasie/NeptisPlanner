package it.uniroma1.neptis.planner.services.tracking;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import it.uniroma1.neptis.planner.LoginActivity;
import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.TrackingActivity;

public class FINDService extends Service {

    private static final String LEARN = "learn";
    private static final String TRACK = "track";
    private static final String report_URL = "http://"+ LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/report_queue";
    private IBinder binder = new LocalBinder();
    private String action;
    private Timer timer;
    private WifiManager wifiManager;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    private String room;
    private String museum;
    private String currentRoom;
    private List<String> roomList;
    private Map<String,Integer> roomCounter;
    private long startTimeRoom;
    private long startTimeMuseum;

    private NotificationManager notificationManager;
    private static final int NOTIFICATION_ID = 123;
    //Needed to update the notification text
    private NotificationCompat.Builder builder;
    private Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"Find");
        wakeLock.acquire();
        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY,"FindWifiLock");
        wifiLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service","started");
        action = intent.getAction();
        Log.d("service action",action);
        //room = intent.getStringExtra("room");
        museum = intent.getStringExtra("attraction_id");
        currentRoom = null;
        roomList = new ArrayList<>();
        roomCounter = new HashMap<>();
        startTimeMuseum = System.currentTimeMillis();
        setUpNotification();
        startForeground(NOTIFICATION_ID,notification);
        startScanThread();
        return START_STICKY;
    }

    private void startScanThread() {
        timer = new Timer();
        WiFiScan task = new WiFiScan(new WiFiScanHandler());
        timer.schedule(task, 0, 10 * 1000);  // interval 10 seconds
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public FINDService getService() {
            return FINDService.this;
        }
    }

    @Override
    public void onDestroy() {
        Log.d("service","stopped");
        stopForeground(true);
        notificationManager.cancel(NOTIFICATION_ID);
        wifiLock.release();
        wakeLock.release();
        timer.cancel();
        timer.purge();
        super.onDestroy();
    }

    private void setUpNotification() {
        Intent i = new Intent(this,TrackingActivity.class);
        i.putExtra("running",true);
        PendingIntent pi = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_main_notification)
                .setContentTitle("Neptis: room recognition")
                .setContentText("Current room:")
                .setContentIntent(pi);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("WifiReceiver","message received");
            List<ScanResult> scanResults = wifiManager.getScanResults();
            if(action.equals(LEARN))
                new LearnTask(getApplicationContext(),scanResults).execute(room,museum);
            else if(action.equals(TRACK))
                new TrackTask(getApplicationContext(),scanResults,roomList).execute(room,museum);
        }
    }

    private class WiFiScan extends TimerTask {
        private Handler handler;

        public WiFiScan(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.sendEmptyMessage(1);
        }
    }

    private class WiFiScanHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 1:
                    Log.d("SERVICE","scan request");
                    boolean n = wifiManager.startScan();
                    Log.d("SERVICE", "wifi.startScan " + n);
                    getResults();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

        private void getResults() {
            //TODO get museum id/name from the queue service class (and somehow retrieve rooms ids)
            List<ScanResult> scanResults = wifiManager.getScanResults();
            if(action.equals(LEARN))
                new LearnTask(getApplicationContext(),scanResults).execute(room,museum);
            else if(action.equals(TRACK))
                new TrackTask(getApplicationContext(),scanResults, roomList).execute(room,museum);

            if(roomList.size() == 5) {
                roomCounter.clear();
                for(String r : roomList) {
                    Integer count = roomCounter.get(r);
                    roomCounter.put(r,count == null ? 1 : ++count);
                }
                for(String r : roomCounter.keySet()) {
                    int v = roomCounter.get(r);
                   if(v > 3) {
                       if(currentRoom == null) {
                           startTimeRoom = System.currentTimeMillis();
                           currentRoom = r;
                       } else {
                           //TODO the room has changed, therefore we need to send data to the server
                           int minutes = (int)(((System.currentTimeMillis() - startTimeRoom)/1000)/60);
                           new TrackingAsyncTask().execute(report_URL,String.valueOf(minutes));
                           currentRoom = r;
                           startTimeRoom = System.currentTimeMillis();
                       }
                   }
                }
            }
        }
    }

    private class TrackingAsyncTask extends AsyncTask<String,Void,Integer> {

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
            //Log.d("LOG",urlURL);

            // HTTP post
            try {
                URL url = new URL(urlURL);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                // set like post request
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Accept-Charset", charset);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                // Object json to send
                JSONObject json = new JSONObject();
                json.put("type", "Visit");
                json.put("category", "City");
                json.put("nameId", ""); //parameter not used server-side
                json.put("attractionId", museum);
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


        protected void onPostExecute(Integer result) {

            if (result== 204)
                Toast.makeText(getApplicationContext(), "Report succesful! \nThank you!", Toast.LENGTH_LONG).show();
            else Toast.makeText(getApplicationContext(), result+"Error on reporting..", Toast.LENGTH_LONG).show();
//            progress.dismiss();
            stopSelf();

        }


    }
}
