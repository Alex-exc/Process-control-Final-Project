package project3.Services;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static project3.Utilities.RemoteControl.VehiclesID1;

public class Crashed {

    // Broker
    MqttClient client;
    public static String BROKER_URL = "tcp://10.42.0.1:1883";
    private static final String CLIENT_ID = "Map";

    // Topics
    public static String TRACK_CRASHED_TOPIC = "Anki/Vehicles/U/" + VehiclesID1[7]+ "/E/track";
    public String EMERGENCY_CURRENT_CRASHED_LOCATION = "Emergency/U/E/Location/Crashed";

    // Regex
    protected static String trackRegex = "\"trackID\"\\s*:\\s*(\\d+)";
    public static String directionRegex = "\"direction\"\\s*:\\s*\"(\\w+)\"";

    private Crashed() throws MqttException {
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

                int crashedCarTrackID = extractTrackID(topic, message, trackRegex);
                System.out.println("Crashed car at ID : " + crashedCarTrackID);
                publish(EMERGENCY_CURRENT_CRASHED_LOCATION, String.valueOf(crashedCarTrackID), 1, false);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        client.connect(options);
        client.subscribe(TRACK_CRASHED_TOPIC);
    }

    private int extractTrackID(String topic, MqttMessage message, String regex) throws MqttException {
        int trackID = 0;
        if(topic.equals(TRACK_CRASHED_TOPIC)) {
            String payload = new String(message.getPayload());
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(payload);
            if (matcher.find()) {
                trackID = Integer.parseInt(matcher.group(1));
            }
            return trackID;
        }
        return trackID;
    }


    // Publish to any topic
    private void publish(String topic, String message, int qos, boolean retained) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);
        client.publish(topic, mqttMessage);
    }

    public static void main(String[] args) {
        try {
            System.out.println("Connected to broker URL : " + BROKER_URL);
            Crashed map = new Crashed();
        } catch (MqttException e){
            e.printStackTrace();
        }
    }
}
