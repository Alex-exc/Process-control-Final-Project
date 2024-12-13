package project3.Services;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import project3.Map.Graph;
import project3.Map.Node;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static project3.Map.Graph.*;
import static project3.Services.Crashed.trackRegex;
import static project3.Services.GPS.GPS_DIRECTION;
import static project3.Utilities.RemoteControl.VehiclesID1;

public class Ambulance {

    private Set<Integer> visitedTrackIDs = new LinkedHashSet<>();
    private static List<String> ambulanceDirections = new ArrayList<>();
    private List<String> gpsDirections = new ArrayList<>();
    private int ambulanceTrackID;
    private boolean isSameWay;
    private boolean isLoopDone = false;

    // Broker
    MqttClient client;
    private static final String BROKER_URL = "tcp://10.42.0.1:1883";
    private static final String CLIENT_ID = "Ambulance";

    // Topics
    public static final String EXTRACTED_AMBULANCE_LOCATION_TOPIC = "Ambulance/U/E/TrackID";
    public static String GET_AMBULANCE_LOCATION_TOPIC = "Anki/Vehicles/U/" + VehiclesID1[7] + "/E/track";
    protected static final String AMBULANCE_CAN_USE_PATH = "Ambulance/U/E/CanUse";

    private final String[] TOPICS = {
            GET_AMBULANCE_LOCATION_TOPIC,  // Listens here to get the trackID
            GPS_DIRECTION,

    };
    static Graph graph = new Graph();

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

                if(topic.equals(GET_AMBULANCE_LOCATION_TOPIC)) {
                    ambulanceTrackID = extractTrackID(topic, message, trackRegex); // Intercept the trackID from Anki
                    System.out.println("Ambulance at ID : " + ambulanceTrackID);
                    publish(EXTRACTED_AMBULANCE_LOCATION_TOPIC, String.valueOf(ambulanceTrackID), 1, false); // Publishes it on Ambulance
                }

                visitedTrackIDs.add(ambulanceTrackID);
                System.out.println("Visited nodes : " + visitedTrackIDs);
                List<Node> nodes = getNodes(visitedTrackIDs);

                for (int i = 0; i < nodes.size() - 1; i++) {
                        Node currentNode = nodes.get(i);
                        Node nextNode = nodes.get(i + 1);
                        ambulanceDirections.add(getDirection(currentNode, nextNode));
                }

                System.out.println("Ambulance directions : " + ambulanceDirections);

                if (topic.equals(TOPICS[1])) {
                    payload = payload.replaceAll("[\\[\\]\\s]", "");
                    String[] directions = payload.split(",");

                    gpsDirections.clear();
                    Collections.addAll(gpsDirections, directions);
                    System.out.println("GPS directions : " + gpsDirections);
                }

                if(isLoopDone) {
                    isSameWay = isUsablePath(ambulanceDirections, gpsDirections);
                    System.out.println(isSameWay);
                    if (!isSameWay) {
                        publish(AMBULANCE_CAN_USE_PATH, String.valueOf(isSameWay), 1, false);
                        System.out.println("isSameWay : " + isSameWay);
                    }
                    if(isSameWay){
                        publish(AMBULANCE_CAN_USE_PATH, String.valueOf(isSameWay), 1, false);
                        System.out.println("isSameWay : " + isSameWay);
                    }
                }
                isLoopDone = true;
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        client.connect(options);
        client.subscribe(TOPICS);
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

    public String extractDirection(String topic, MqttMessage message, String regex) throws MqttException {
        String direction = "";
        if(topic.equals(GET_AMBULANCE_LOCATION_TOPIC)) {
            String payload = new String(message.getPayload());
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(payload);
            if (matcher.find()) {
                direction = matcher.group(1);
                System.out.println(direction);
            }
            return direction;
        }
        return direction;
    }

    // Publish to any topic
    private void publish(String topic, String message, int qos, boolean retained) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);
        client.publish(topic, mqttMessage);
    }

    public static List<Node> getNodes(Set<Integer> path) {
        List<Node> nodeList = new ArrayList<>();
        for (int id : path) {
            Node node = nodes.get(id); // Get the node id
            if (node != null) {
                nodeList.add(node); // Add the node in the list
            } else {
                System.out.println("Node with ID " + id + " does not exist.");
            }
        }
        return nodeList;
    }

    private boolean isUsablePath(List<String> ambulanceDirections, List<String> gpsDirections) {
        String currentDirection = ambulanceDirections.get(ambulanceDirections.size()-1);
        String nextDirection = gpsDirections.get(0);

            if(
                    (
                            currentDirection.equals("N") && (
                                    nextDirection.equals("S") || nextDirection.equals("SW") || nextDirection.equals("SE")
                            )
                    ) || (
                            currentDirection.equals("S") && (
                                    nextDirection.equals("N") || nextDirection.equals("NW") || nextDirection.equals("NE")
                            )
                    ) || (
                            currentDirection.equals("E") && (
                                    nextDirection.equals("W") || nextDirection.equals("NW") || nextDirection.equals("SW")
                            )
                    ) || (
                            currentDirection.equals("W") && (
                                    nextDirection.equals("E") || nextDirection.equals("NE") || nextDirection.equals("SE")
                            )
                    ) || (
                            currentDirection.equals("SW") && (
                                    nextDirection.equals("E") || nextDirection.equals("NE") || nextDirection.equals("N") ||
                                            nextDirection.equals("NW") || nextDirection.equals("SE")
                            )
                    ) || (
                            currentDirection.equals("SE") && (
                                    nextDirection.equals("W") || nextDirection.equals("NW") || nextDirection.equals("N") ||
                                            nextDirection.equals("NE") || nextDirection.equals("SW")
                            )
                    ) || (
                            currentDirection.equals("NE") && (
                                    nextDirection.equals("S") || nextDirection.equals("SW") || nextDirection.equals("W") ||
                                            nextDirection.equals("NW") || nextDirection.equals("SE")
                            )
                    ) || (
                            currentDirection.equals("NW") && (
                                    nextDirection.equals("S") || nextDirection.equals("SE") || nextDirection.equals("E") ||
                                            nextDirection.equals("NE") || nextDirection.equals("SW")
                            )
                    )
            ) {
                return false;
            }
        return true;
    }

    public static void main(String[] args) {
        try {
            System.out.println("Connected to broker URL : " + BROKER_URL);
            Ambulance ambulance = new Ambulance();
            graph.initializeGraph();
            ambulanceDirections.add("E");
        } catch (MqttException e){
            e.printStackTrace();
        }
    }
}
