package project3.Services;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import project3.Map.Graph;
import project3.Map.Node;

import java.util.ArrayList;
import java.util.List;

import static project3.Map.Graph.getDirection;
import static project3.Map.Graph.getNodes;
import static project3.Services.Ambulance.AMBULANCE_CAN_USE_PATH;
import static project3.Services.Ambulance.EXTRACTED_AMBULANCE_LOCATION_TOPIC;
import static project3.Utilities.RemoteControl.VehiclesID1;

public class GPS {

    // Broker set up
    MqttClient client;
    private static final String BROKER_URL = "tcp://10.42.0.1:1883";
    private static final String CLIENT_ID = "GPS";

    // Variables
    private int ambulancePosition;
    private final int crashedPosition = 6;
    private boolean isPathSent = false;

    // Topics
    public static String GPS_DIRECTION = "GPS/U/E/Direction";
    public static String GPS_NODES = "GPS/U/E/Nodes";

    String[] Topics = {
            EXTRACTED_AMBULANCE_LOCATION_TOPIC
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

                List<String> directions = new ArrayList<>();
                ambulancePosition = 2;

                // Ambulance current position
                if(topic.equals(EXTRACTED_AMBULANCE_LOCATION_TOPIC)) {
                    ambulancePosition = Integer.parseInt(payload);
                    System.out.println("ambulancePosition = " + ambulancePosition);

                }
                if(!isPathSent){
                sendPath(directions, ambulancePosition, crashedPosition);
                isPathSent = true;
                }

                if(isPathSent){
                    System.out.println("Path sent, gps is stopped");
                    if(payload.equals("NotUsable") && topic.equals(AMBULANCE_CAN_USE_PATH)){
                        isPathSent = false;
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
        client.connect(options);
        client.subscribe(Topics);
    }

    private void sendPath(List<String> directions, int ambulancePosition, int crashedPosition) throws MqttException {
        List<Integer> shortestPath = graph.findShortestPathBetween(ambulancePosition, crashedPosition);

        // Prints the nodes and the directions
        List<Node> nodes = getNodes(shortestPath);
        for (int i = 0; i < nodes.size() - 1; i++) {
            directions.add(getDirection(nodes.get(i), nodes.get(i + 1)));
        }

        publish(GPS_NODES, shortestPath.toString(), 1,false);
        System.out.println("Shortest path from " + ambulancePosition + " to " + crashedPosition + ": " + shortestPath);

        publish(GPS_DIRECTION, directions.toString(), 1,false);
        System.out.println("Directions: " + directions);
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
            GPS gps = new GPS();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
