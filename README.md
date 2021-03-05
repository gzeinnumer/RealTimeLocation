# RealTimeLocation

- `manifests.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest >

    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.INTERNET" />

    <application>

        ...

        <service
            android:name=".service.LocationUpdateService"
            android:enabled="true"
            android:exported="true" />
    </application>

</manifest>
```

- [LocationUpdateService.java](https://github.com/gzeinnumer/RealTimeLocation/blob/master/app/src/main/AndroidManifest.xml)
```java
public class LocationUpdateService extends Service {

    ...

    private void onNewLocation(Location location) {
        Log.i(getClass().getSimpleName(), "onNewLocation: " + location);

        //if you wanto to active notification active this
        startForeground(NOTIFICATION_ID, getNotification());

        locationPref.saveString(LocationPref.ADDRESS, Utils.getAddress(location, getApplicationContext()));
        locationPref.saveString(LocationPref.LG, Double.toString(location.getLongitude()));
        locationPref.saveString(LocationPref.LA, Double.toString(location.getLatitude()));

        Log.i(getClass().getSimpleName(), "onNewLocation: Distance " + Utils.getDistanceInKM(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()), locationPref.getValue(LocationPref.LA), locationPref.getValue(LocationPref.LG)));
        Log.i(getClass().getSimpleName(), "onNewLocation: " + battery);
        Log.i(getClass().getSimpleName(), "onNewLocation: " + locationPref.getValue(LocationPref.ADDRESS));
        Log.i(getClass().getSimpleName(), "onNewLocation: " + locationPref.getValue(LocationPref.LA));
        Log.i(getClass().getSimpleName(), "onNewLocation: " + locationPref.getValue(LocationPref.LG));

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
            locationPref.saveString(LocationPref.ADDRESS, Utils.getAddress(location, getApplicationContext()));
            locationPref.saveString(LocationPref.LG, Double.toString(location.getLongitude()));
            locationPref.saveString(LocationPref.LA, Double.toString(location.getLatitude()));
        }
    }
}
```

---

```
Copyright 2021 M. Fadli Zein
```