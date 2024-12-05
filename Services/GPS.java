package project3.Services;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import project3.Map.Graph;

public class GPS {

    // Broker
    MqttClient client;
    private static final String BROKER_URL = "tcp://10.42.0.1:1883";
    private static final String CLIENT_ID = "GPS";

    // Position

    private static int ambulancePosition = 0;
    private static int crashedPosition = 0;

    // Topics
    protected String EMERGENCY_CURRENT_AMBULANCE_LOCATION = "Emergency/U/E/Location/Ambulance";
    protected String EMERGENCY_CURRENT_CRASHED_LOCATION = "Emergency/U/E/Location/Crashed";

    String[] locationsArrayTopics = {
            EMERGENCY_CURRENT_AMBULANCE_LOCATION,
            EMERGENCY_CURRENT_CRASHED_LOCATION
    };


    Graph graph = new Graph();

    private GPS() throws MqttException {
        connectToBroker();
    }
    private void connectToBroker() throws MqttException {
        client = new MqttClient(BROKER_URL, CLIENT_ID, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        // Initialize graph that represents the map
        graph.initializeGraph();

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connexion lost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                String payload = new String(message.getPayload());
                System.out.println(topic + " : " + payload);

                // Ambulance current position
                if(topic.equals(EMERGENCY_CURRENT_AMBULANCE_LOCATION)) {
                    ambulancePosition = Integer.parseInt(payload);
                    System.out.println("ambulancePosition = " + ambulancePosition);
                }

                if (topic.equals(EMERGENCY_CURRENT_CRASHED_LOCATION)) {
                    crashedPosition = Integer.parseInt(payload);
                    System.out.println("crashedPosition = " + crashedPosition);
                }
                if (ambulancePosition == 0 || crashedPosition == 0) {
                    System.out.println("Error, one of the nodes does not exist");
                } else {
                    graph.findAllPaths(ambulancePosition, crashedPosition);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
        client.connect(options);
        client.subscribe(locationsArrayTopics);
    }

    public static void main(String[] args) {
        try {
            System.out.println("Connected to broker URL : " + BROKER_URL);
            GPS gps = new GPS();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
