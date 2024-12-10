package project3.Services;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static project3.Utilities.RemoteControl.VehiclesID1;

public class Emergency {
    private static final String BROKER_URL = "tcp://10.42.0.1:1883";
    private static final String CLIENT_ID = "Emergency";
    public static final String EMERGENCY_TOPIC = "Emergency/U/E";
    static final String REMOTE_TOPIC = "RemoteControl/U/E/vehicles/#";
    public static final String REMOTE_EMERGENCY_TOPIC = "RemoteControl/U/E/vehicles/emergency";
    private MqttClient client;
    public static boolean emergency = false;

    protected static final String EMERGENCY_COORDINATES_TOPIC = "RemoteControl/U/E/vehicles/coordinates";


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
                handleEmergency(VehiclesID1[7
                        ], topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
        client.connect(options);
        client.subscribe(REMOTE_TOPIC);
        for(String id : VehiclesID1) {
            if(id.equals(VehiclesID1[7])) {
                client.subscribe(EMERGENCY_TOPIC + "/Message/" + id);
            }
        }
    }

    private void handleEmergency(String vehicleID, String topic, MqttMessage message) throws MqttException, InterruptedException {
        // Avoid infinite loop
        if(topic.equals(EMERGENCY_TOPIC + "/Message/" + vehicleID)) {
            return;
        }
        System.out.println(vehicleID);
        String payload = new String(message.getPayload());

        client.subscribe(EMERGENCY_TOPIC + "/Message/" + vehicleID);
        client.subscribe(REMOTE_EMERGENCY_TOPIC + "/" + vehicleID);

        if(!emergency) {
            publish(EMERGENCY_TOPIC + "/" + topic, payload, 1, false);
            System.out.println(topic + ": " + payload);
            setLights(vehicleID,"frontRed","off", 0, 0, 0);
            setLights(vehicleID,"engineRed","off", 0, 0, 0);

            if(payload.equals("Emergency" + vehicleID)) {
                System.out.println(topic + ": " + payload);
                emergency = true;
            }
        }

        if(emergency) {
            setSpeed(vehicleID, "0","2000");
            setLights(vehicleID, "frontRed", "flash", 0, 15, 255);
            setLights(vehicleID, "engineRed", "flash", 0, 15, 255);
            System.out.println(topic + ": " + payload);
            if(payload.equals("!Emergency" + vehicleID)) {
                emergency = false;
            }
        }
    }
    // Publish to any topic
    private void publish(String topic, String message, int qos, boolean retained) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);
        client.publish(topic, mqttMessage);
    }

    private void setSpeed(String vehicleID, String velocity, String acceleration) throws MqttException, InterruptedException {
        String payload = String.format("{\"velocity\":\"%s\",\"acceleration\":\"%s\"}", velocity, acceleration);
        publish(EMERGENCY_TOPIC + "/Message/speed/" + vehicleID, payload, 1, false);
        System.out.println("Changing speed to : " + vehicleID + ". New parameters : " + velocity + ", " + acceleration);
    }
    private void setLights(String vehicleID, String lightType, String effect, int start, int end, int frequency) throws MqttException, InterruptedException {
        String payload = String.format("{\"%s\":{\"effect\":\"%s\",\"start\":%d,\"end\":%d,\"frequency\":%d}}",
                lightType, effect, start, end, frequency
        );
        publish(EMERGENCY_TOPIC + "/Message/lights/" + vehicleID, payload, 1, false);
        System.out.println("Changing light to : " + vehicleID + ". New parameters : " + lightType + ", " + effect + ", " + start+ ", " + end + ", " + frequency);

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