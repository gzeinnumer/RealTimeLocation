package com.gzeinnumer.realtimelocation.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.gzeinnumer.realtimelocation.R;
import com.gzeinnumer.realtimelocation.data.LocationPref;
import com.gzeinnumer.realtimelocation.utils.UtilsLocation;

public class LocationUpdateService extends Service {

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final String PACKAGE_NAME = "";
    private static final String TAG = LocationUpdateService.class.getSimpleName();
    private static final String CHANNEL_ID = "channel_01";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME + ".started_from_notification";
    private final IBinder mBinder = new LocalBinder();
    private static final int NOTIFICATION_ID = 1234567812;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private String battery = "0";
    private boolean mChangingConfiguration = false;
    private NotificationManager mNotificationManager;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Location mLocation;
    private LocationPref locationPref;

    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            battery = String.valueOf(level);
        }
    };

    public LocationUpdateService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        startConfigService();
    }

    private void startConfigService() {
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };
        locationPref = new LocationPref(this);

        createLocationRequest();
        getLastLocation();

        new HandlerThread(TAG).start();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "GPS";
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.setSound(null, null);
            mChannel.enableVibration(false);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.i(getClass().getSimpleName(), "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.i(getClass().getSimpleName(), "Lost location permission." + unlikely);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean startFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION, false);
        if (startFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }
        return START_NOT_STICKY;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (!mChangingConfiguration) {
            Log.i(getClass().getSimpleName(), "Starting foreground service");
            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true;
    }

    public void requestLocationUpdates() {
        Log.i(getClass().getSimpleName(), "Requesting location updates");
        UtilsLocation.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), LocationUpdateService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            UtilsLocation.setRequestingLocationUpdates(this, false);
            Log.i(getClass().getSimpleName(), "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    public void removeLocationUpdates() {
        Log.i(getClass().getSimpleName(), "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            UtilsLocation.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            UtilsLocation.setRequestingLocationUpdates(this, true);
            Log.i(getClass().getSimpleName(), "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, LocationUpdateService.class);

        CharSequence text = UtilsLocation.getLocationText(mLocation);

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText("APP sedang aktif")
                .setOngoing(false)
                .setSound(null)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(null)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    public class LocalBinder extends Binder {
        public LocationUpdateService getService() {
            return LocationUpdateService.this;
        }
    }

    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(this.mBatInfoReceiver);
        super.onDestroy();
    }

    private void onNewLocation(Location location) {
        Log.i(getClass().getSimpleName(), "onNewLocation: " + location);

        //if you wanto to active notification active this
        startForeground(NOTIFICATION_ID, getNotification());

        locationPref.saveString(LocationPref.ADDRESS, UtilsLocation.getAddress(location, getApplicationContext()));
        locationPref.saveString(LocationPref.LG, Double.toString(location.getLongitude()));
        locationPref.saveString(LocationPref.LA, Double.toString(location.getLatitude()));

        Log.i(getClass().getSimpleName(), "onNewLocation: Distance " + UtilsLocation.getDistanceInM(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()), locationPref.getValue(LocationPref.LA), locationPref.getValue(LocationPref.LG)));
        Log.i(getClass().getSimpleName(), "onNewLocation: " + battery);
        Log.i(getClass().getSimpleName(), "onNewLocation: " + locationPref.getValue(LocationPref.ADDRESS));
        Log.i(getClass().getSimpleName(), "onNewLocation: " + locationPref.getValue(LocationPref.LA));
        Log.i(getClass().getSimpleName(), "onNewLocation: " + locationPref.getValue(LocationPref.LG));

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
            locationPref.saveString(LocationPref.ADDRESS, UtilsLocation.getAddress(location, getApplicationContext()));
            locationPref.saveString(LocationPref.LG, Double.toString(location.getLongitude()));
            locationPref.saveString(LocationPref.LA, Double.toString(location.getLatitude()));
        }
    }
}
