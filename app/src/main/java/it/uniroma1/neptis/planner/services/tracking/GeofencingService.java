/*
 * Copyright (c) 2017. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.services.tracking;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.ArrayList;

import it.uniroma1.neptis.planner.Home;
import it.uniroma1.neptis.planner.QueueChecker;
import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.model.city.CityAttraction;
import it.uniroma1.neptis.planner.services.queue.ReportAsyncTask;

public class GeofencingService extends IntentService {

    private static final String NAME = GeofencingService.class.getName();
    private IBinder binder = new LocalBinder();
    private LocationManager locManager;
    private LocationListener destListener;
    private double destinationLat;
    private double destinationLng;
    private double destinationRadius;
    private String currentPlan;
    private String destinationId;
    private String destinationName;

    private ArrayList<CityAttraction> attractions;
    private CityAttraction currentAttraction;
    private int index;

    private long fenceEnterTime;
    private long fenceExitTime;
    private boolean fenceEntered;

    private NotificationManager notificationManager;
    private static final int NOTIFICATION_ID = 123;
    //Needed to update the notification text
    private NotificationCompat.Builder builder;
    private Notification notification;

    private PowerManager.WakeLock lock;

    private FirebaseUser user;

    public GeofencingService() {
        super("GeofencingService");
        destinationLat = 0.0;
        destinationLng = 0.0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        attractions = (ArrayList<CityAttraction>)intent.getSerializableExtra("attractions");
        index = intent.getIntExtra("index",0);
        if(index == -1)
            index = 0;
        currentAttraction = attractions.get(index);
        currentPlan = intent.getStringExtra("current_plan");
        destinationId = intent.getStringExtra("id");
        destinationName = intent.getStringExtra("name");

        destinationLat = Double.parseDouble(currentAttraction.getLatitude());
        destinationLng = Double.parseDouble(currentAttraction.getLongitude());
        destinationRadius = currentAttraction.getRadius();
        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            destListener = new DestListener();
            int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                //It should be granted since it gets asked at the first possible Activity (Welcome)
                //Cal updates every 10 seconds
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 1000, 0, destListener);;

        }
        Log.d("GeoFencing",""+destinationLat);
        Log.d("GeoFencing",""+destinationLng);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        lock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "GeofencingWakeLock");
        lock.acquire();

        fenceEntered = false;
        user = FirebaseAuth.getInstance().getCurrentUser();
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
        Intent i = new Intent(this,Home.class);
        i.putExtra("computed_plan_file", currentPlan);
        i.putExtra("index", index);
        PendingIntent pi = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon((R.drawable.ic_main_notification))
                .setContentTitle("Neptis Planner")
                .setContentText("")
                .setContentIntent(pi);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void launchNotification2(String message) {
        Intent i = new Intent(this, Home.class);
        i.putExtra("computed_plan_file", currentPlan);
        i.putExtra("index", index);
        PendingIntent pi = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon((R.drawable.ic_main_notification))
                .setContentTitle("Neptis Planner")
                .setContentText(message)
                .setContentIntent(pi);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
        Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }

    @Override
    public void onDestroy() {
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
            //TODO set diameter of the attraction
            if(distance <= destinationRadius) {
                if(!fenceEntered) {
                    fenceEntered = true;
                    launchNotification2("Arrivato");
                    fenceEnterTime = System.currentTimeMillis();
                }
            }
             else {
                if(fenceEntered) {
                    fenceEntered = false;
                    fenceExitTime = System.currentTimeMillis();
                    final int minutes = (int) Math.ceil((fenceExitTime - fenceEnterTime) / 60000);
                    user.getIdToken(true)
                            .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                    if (task.isSuccessful()) {
                                        String idToken = task.getResult().getToken();
                                        new ReportAsyncTask(getApplicationContext()).execute("visit","city",currentAttraction.getId(),String.valueOf(minutes), idToken);
                                    } else {
                                        // Handle error -> task.getException();
                                    }
                                }
                            });
                    index++;
                    launchNotification2("La visita è durata " + minutes + " minuti");
                    if(index == attractions.size())
                        stopSelf();
                    currentAttraction = attractions.get(index);
                    destinationLat = Double.parseDouble(currentAttraction.getLatitude());
                    destinationLng = Double.parseDouble(currentAttraction.getLongitude());

                }
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

        private double getDistance(double lat, double lng) {
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