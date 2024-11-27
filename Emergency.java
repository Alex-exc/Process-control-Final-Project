package project2;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.rmi.Remote;
import java.util.Objects;

import static project2.RemoteControl.*;

public class Emergency {
    private static final String BROKER_URL = "tcp://10.42.0.1:1883";
    private static final String CLIENT_ID = "EmergencyStopService";
    protected static final String EMERGENCY_TOPIC = "Emergency/U/E";
    private static final String REMOTE_TOPIC = "RemoteControl/U/E/vehicles/#";
    protected static final String REMOTE_EMERGENCY_TOPIC = "RemoteControl/U/E/vehicles/emergency";
    private MqttClient client;
    private static boolean emergency = false;

    public Emergency() throws MqttException {
        connectToBroker();
    }

    private void connectToBroker() throws MqttException {
        client = new MqttClient(BROKER_URL, CLIENT_ID, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connexion lost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if(topic.equals(EMERGENCY_TOPIC + "/Message")) {
                    return;
                }
                String payload = new String(message.getPayload());

                client.subscribe(EMERGENCY_TOPIC + "/Message");
                client.subscribe(REMOTE_EMERGENCY_TOPIC);

                if(!emergency) {
                    publish(EMERGENCY_TOPIC + "/" + topic, payload, 1, false);
                    System.out.println(topic + ": " + payload);

                    if(payload.equals("Emergency")) {
                        System.out.println(topic + ": " + payload);
                        emergency = true;
                    }
                }

                if(emergency) {
                    setSpeed("0","2000");
                    System.out.println(topic + ": " + payload);
                    if(payload.equals("!Emergency")) {
                        emergency = false;
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
        client.connect(options);
        client.subscribe(REMOTE_TOPIC);
        client.subscribe(EMERGENCY_TOPIC + "/Message");
    }

    // Publish to any topic
    private void publish(String topic, String message, int qos, boolean retained) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);
        client.publish(topic, mqttMessage);
    }

    private void setSpeed(String velocity, String acceleration) throws MqttException, InterruptedException {
        String payload = String.format("{\"velocity\":\"%s\",\"acceleration\":\"%s\"}", velocity, acceleration);
        publish(EMERGENCY_TOPIC + "/Message", payload, 1, false);
        System.out.println("Changing speed : " + velocity + ", " + acceleration);
    }

    public static void main(String[] args) {
        try {
            System.out.println("Connected to broker URL : " + BROKER_URL);
            Emergency emergency = new Emergency();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}