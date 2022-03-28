package com.example.mqttsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationActivity extends AppCompatActivity {
    // Location
    LocationManager locationManager;
    Location location;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    private static final int GPS_TIME_INTERVAL = 1000 * 60; // get gps location every 10 second
    private static final int GPS_DISTANCE = 0; // set the distance value in meter
    private static final int HANDLER_DELAY = 1000 * 10;
    private static final int START_HANDLER_DELAY = 0;

    // MQTT
    final String serverURI = "tcp://test.mosquitto.org";
    String clientId = "android_mqtt";
    String locationTopic = "org.location";
    TextView username;
    TextView licensePlate;
    TextView lat;
    TextView lng;
    TextView speed;

    Bundle extras;


    MqttAndroidClient mqttAndroidClient;

    final static String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    final static int PERMISSION_ALL = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        }

        username = findViewById(R.id.activity_location_username);
        licensePlate = findViewById(R.id.activity_location_license_plate);
        lat = findViewById(R.id.activity_location_lat);
        lng = findViewById(R.id.activity_location_lng);
        speed = findViewById(R.id.activity_location_speed);

        extras = getIntent().getExtras();
        if (extras != null) {
            username.setText(extras.getString("username"));
            licensePlate.setText(extras.getString("licensePlate"));
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                requestLocation();
                handler.postDelayed(this, HANDLER_DELAY);
            }
        }, START_HANDLER_DELAY);

        clientId = clientId + System.currentTimeMillis();
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverURI, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                switch (topic) {
//                    case temperatureTopic:
//                        Log.e("MESSAGE: ", message.toString());
//                        showToast(message.toString());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });


        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);


        try {
            mqttAndroidClient.connect(mqttConnectOptions, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e("MQTT: ", "Kết nối MQTT thành công");
                    showToast("Kết nối mqtt thành công", Toast.LENGTH_SHORT);
                    // subscribeTopic(temperatureTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    showToast("Kết nối mqtt thất bại " + exception.getMessage(), Toast.LENGTH_SHORT);
                    Log.e("mqtt", exception.getMessage());
                }
            });
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    LocationListener locationListener = new LocationListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.e("onLocationChange ", "Got new location");
            try {
                JSONObject locationMessageData = new JSONObject();
                locationMessageData.put("lat", location.getLatitude());
                locationMessageData.put("lng", location.getLongitude());
                locationMessageData.put("speed", location.getSpeedAccuracyMetersPerSecond());

                lat.setText(String.valueOf(location.getLatitude()));
                lng.setText(String.valueOf(location.getLongitude()));
                speed.setText(String.valueOf(location.getSpeedAccuracyMetersPerSecond()));

                publishMessage(locationTopic, locationMessageData.toString());
                showToast("Got Coordinates: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT);
                locationManager.removeUpdates(this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    };

    private void requestLocation() {
        Log.e("requestLocation ", "locationManager: " + locationManager);
        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Log.e("requestLocation ", "locationManager: " + locationManager);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.e("requestLocation ", "isGpsEnabled: " + isGPSEnabled);
        Log.e("requestLocation ", "isNetworkEnabled: " + isNetworkEnabled);
        if (isGPSEnabled) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (location == null) {
                    Log.e("requestLocation ", "requestLocationUpdates");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            GPS_TIME_INTERVAL, GPS_DISTANCE, locationListener);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    requestLocation();
                    handler.postDelayed(this, HANDLER_DELAY);
                }
            }, START_HANDLER_DELAY);
        } else {
            finish();
        }
    }

    public void showToast(String msg, int duration) {
        Toast.makeText(this, msg, duration).show();
    }

    public void publishMessage(String topic, String message) {
        try {
            MqttMessage pMessage = new MqttMessage();
            pMessage.setPayload(message.getBytes());
            mqttAndroidClient.publish(topic, pMessage);
        } catch (MqttException ex) {
            Log.e("Publish: ", ex.getMessage());
        }
    }
}
