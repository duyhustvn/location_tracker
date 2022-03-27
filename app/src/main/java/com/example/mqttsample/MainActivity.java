package com.example.mqttsample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    Button onBtn;
    Button offBtn;

    final String serverURI = "tcp://test.mosquitto.org";
    final String temperatureTopic = "temperature";
    final String controlLampTopic = "lamp/control";
    String clientId = "android_mqtt";

    MqttAndroidClient mqttAndroidClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onBtn = (Button) findViewById(R.id.main_btn_on);
        offBtn = (Button) findViewById(R.id.main_btn_off);

        onBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage(controlLampTopic, "ON");
            }
        });

        offBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage(controlLampTopic, "OFF");
            }
        });


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
                    case temperatureTopic:
                        Log.e("MESSAGE: ", message.toString());
                        showToast(message.toString());
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
                    showToast("Kết nối mqtt thành công");
                    subscribeTopic(temperatureTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    showToast("Kết nối mqtt thất bại " + exception.getMessage());
                    Log.e("mqtt", exception.getMessage());
                }
            });
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void subscribeTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    showToast("Subscribe to topic " + topic + " success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    showToast("Subscribe to topic " + topic + " failed");
                }
            });
        } catch (MqttException ex) {
            Log.e("subscribeTopic: ", ex.getMessage());
        }
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