package project3.Services;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static project3.Services.Crashed.trackRegex;
import static project3.utilities.RemoteControl.VehiclesID1;

public class Ambulance {

    // Broker
    MqttClient client;
    private static final String BROKER_URL = "tcp://10.42.0.1:1883";
    private static final String CLIENT_ID = "Ambulance";

    // Topics
    private static final String EMERGENCY_CURRENT_AMBULANCE_LOCATION = "Emergency/U/E/Location/Ambulance";
    private static String AMBULANCE_TOPIC = "Anki/Vehicles/U/" + VehiclesID1[3]+ "/E/track";
    private static String[] AMBULANCES_TOPICS = {
            "Anki/Vehicles/U/" + VehiclesID1[1]+ "/E/track",
            "Anki/Vehicles/U/" + VehiclesID1[2]+ "/E/track",
            "Anki/Vehicles/U/" + VehiclesID1[3]+ "/E/track",
            "Anki/Vehicles/U/" + VehiclesID1[4]+ "/E/track",
            "Anki/Vehicles/U/" + VehiclesID1[5]+ "/E/track",
            "Anki/Vehicles/U/" + VehiclesID1[6]+ "/E/track",
    };

    private Ambulance() throws MqttException {
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
                String payload = new String(message.getPayload());
                System.out.println(topic + " : " + payload);
                int ambulanceTrackID = extractTrackID(topic, message, trackRegex);
                System.out.println("Ambulance initial position :" + ambulanceTrackID);
                publish(EMERGENCY_CURRENT_AMBULANCE_LOCATION, String.valueOf(ambulanceTrackID), 1, false);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
        client.connect(options);
        client.subscribe(AMBULANCE_TOPIC);
    }

    private int extractTrackID(String topic, MqttMessage message, String regex) throws MqttException {
        int trackID = 0;
        if(topic.contains("track")) {
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
            Ambulance ambulance = new Ambulance();
        } catch (MqttException e){
            e.printStackTrace();
        }
    }
}
