package it.uniroma1.neptis.planner.services.tracking;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import it.uniroma1.neptis.planner.QueueChecker;
import it.uniroma1.neptis.planner.R;

public class GeofencingService extends IntentService {

    private static final String NAME = GeofencingService.class.getName();
    private IBinder binder = new LocalBinder();
    private LocationManager locManager;
    private LocationListener destListener;
    private double destinationLat;
    private double destinationLng;
    private String currentPlan;
    private String destinationId;
    private String destinationName;

    private NotificationManager notificationManager;
    private static final int NOTIFICATION_ID = 123;
    //Needed to update the notification text
    private NotificationCompat.Builder builder;
    private Notification notification;

    private PowerManager.WakeLock lock;

    public GeofencingService() {
        super("GeofencingService");
        destinationLat = 0.0;
        destinationLng = 0.0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String coordinates = intent.getStringExtra("coordinates");
        currentPlan = intent.getStringExtra("current_plan");
        destinationLat = Double.parseDouble(coordinates.split(",")[0]);
        destinationLng = Double.parseDouble(coordinates.split(",")[1]);
        destinationId = intent.getStringExtra("id");
        destinationName = intent.getStringExtra("name");

        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            destListener = new DestListener();
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15 * 1000, 0, destListener);
        }
        Log.i("Service",""+destinationLat);
        Log.i("Service",""+destinationLng);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        lock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "GeofencingWakeLock");
        lock.acquire();
        launchNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {}

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public GeofencingService getService() {
            return GeofencingService.this;
        }
    }

    public void launchNotification() {
        //Intent i = new Intent(this,Selected_Plan.class);
        //i.putExtra(MyPlans.EXTRA_MESSAGE,currentPlan);
        Intent i = new Intent(this,QueueChecker.class);
        i.putExtra("destination_name",destinationName);
        i.putExtra("destination_id",destinationId);
        PendingIntent pi = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon((R.drawable.ic_main_notification))
                .setContentTitle("Neptis: destination reached")
                .setContentText(getString(R.string.notification_text))
                .setContentIntent(pi)
                .setAutoCancel(true);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
        Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        locManager.removeUpdates(destListener);
        v.vibrate(500);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.d(NAME,"onDestroy()");
        lock.release();
        super.onDestroy();
    }

    private class DestListener implements LocationListener {

        private double lat = 0.0;
        private double lng = 0.0;

        @Override
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            double distance = getDistance(destinationLat,destinationLng);
            if(distance <= 10.0) {
                launchNotification();
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }

        public double getDistance(double lat, double lng) {
            double earthRadius = 6371000; //meters
            double dLat = Math.toRadians(this.lat - lat);
            double dLng = Math.toRadians(this.lng - lng);
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(this.lat)) *
                            Math.sin(dLng/2) * Math.sin(dLng/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            double dist = earthRadius * c;
            return dist;
        }
    }
}