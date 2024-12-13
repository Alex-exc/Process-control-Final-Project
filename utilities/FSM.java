package project3.Utilities;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static project3.Services.Ambulance.EXTRACTED_AMBULANCE_LOCATION_TOPIC;
import static project3.Services.GPS.GPS_NODES;
import static project3.Utilities.RemoteControl.VehiclesID1;
import static project3.Utilities.RemoteControl.VehiclesID2;

public class FSM {

    MqttClient client;
    public static String BROKER_URL = "tcp://10.42.0.1:1883";
    private static final String CLIENT_ID = "FSM";

    // Topics
    protected String GPS_DIRECTION = "GPS/U/E/Direction";

    // Variables
    String gpsPayload = null;
    String gpsNodes = null;
    int acceleration = 300;
    int velocity = 300;
    double offsetFromCenter = 0;
    List<Integer> visitedTrackIDs = new ArrayList<>();
    private final int crashedPosition = 6;
    boolean isNewInstruction;
    List<String> gpsInstructions = new ArrayList<>();


    String[] TOPICS =  {
            GPS_DIRECTION,
            EXTRACTED_AMBULANCE_LOCATION_TOPIC,
            GPS_NODES
    };

    public enum State {
        S,
        E,
        N,
        W,
        NW,
        NE,
        SW,
        SE,
        X; // Default state
    }

    private State currentState = State.X;

    public FSM() throws MqttException {
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

                if (topic.equals(TOPICS[0])) { // GPS_DIRECTION
                    gpsPayload = payload;
                    isNewInstruction = true;
                }

                if (topic.equals(TOPICS[1])|| topic.equals(TOPICS[2])) { // EXTRACTED_AMBULANCE_LOCATION_TOPIC

                    if (gpsPayload == null || gpsPayload.isEmpty()) {
                        System.out.println("No GPS payload available");
                        return;
                    }

                    System.out.println(gpsInstructions);
                    if(isNewInstruction) {
                        gpsInstructions = convertToList(gpsPayload); // i.e [W, NW, N, NE, NE, N, NW, W]
                        System.out.println("New gps instructions : " + gpsInstructions); // Comes from TOPICS[0]
                        isNewInstruction = false;
                    }

                    if (visitedTrackIDs.contains(crashedPosition)) { // If there are no more instructions, stop
                        setSpeed("0", "2000", VehiclesID1[7]);
                    }

                    // Problem : Data is overwritten, we don't have the time to see the changes.
                    // Goal : Use the first instruction of the gps, change the state of the vehicle
                    // Remove the first instruction once it is done.
                    // How to know if it is done ?
                    // => If we receive a new position from the ambulance
                    // => Meaning there are no loops possible

                    int ambulancePosition = Integer.parseInt(payload);
                    visitedTrackIDs.add(ambulancePosition);
                    // Here, switch states each time the ambulance changes its position
                    // It changes its states according to the new instructions
                    // e.g. if the new instruction is north, then it changes its state to north
                    // How to wait for the new position?
                    // => No need as we call changeState once we receive the new position thanks to messageArrived()

                    changeState(gpsInstructions.get(0),gpsInstructions.get(1), gpsInstructions);

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        client.connect(options);
        client.subscribe(TOPICS);
    }

    public void changeState(String currentDirection, String nextDirection, List<String> gpsInstructions) throws MqttException, InterruptedException {
        if (currentState == State.X) {
            System.out.println("Current State : " + currentState);
            switch (currentDirection) {
                case "N":
                    currentState = State.N;
                    break;
                case "E":
                    currentState = State.E;
                    break;
                case "S":
                    currentState = State.S;
                    break;
                case "W":
                    currentState = State.W;
                    break;
                case "NW":
                    currentState = State.NW;
                    break;
                case "NE":
                    currentState = State.NE;
                    break;
                case "SW":
                    currentState = State.SW;
                    break;
                case "SE":
                    currentState = State.SE;
                    break;
            }
            System.out.println("Current State : " + currentState);
        } else {
            setLane(400, 400, calculateOffset(currentDirection, nextDirection), offsetFromCenter, VehiclesID1[7]);
            gpsInstructions.remove(0);
            System.out.println("GPS Instructions : " + gpsInstructions);
            System.out.println("Current direction : " + currentDirection + ", next direction : " + nextDirection);
            currentState = State.X;
        }
    }

    private double calculateOffset(String currentDirection, String nextDirection) {
        double offset = 0;

        // Transition Start
        if(currentDirection.equals("X") && nextDirection.equals("N")) {
            offset = 0;
        }
        if(currentDirection.equals("X") && nextDirection.equals("W")) {
            offset = 0;
        }
        if(currentDirection.equals("X") && nextDirection.equals("E")) {
            offset = 0;
        }
        if (currentDirection.equals("X") && nextDirection.equals("W")) {
            offset = 0;
        }
        if (currentDirection.equals("X") && nextDirection.equals("NW")) {
            offset = 0;
        }
        if (currentDirection.equals("X") && nextDirection.equals("NE")) {
            offset = 0;
        }
        if (currentDirection.equals("X") && nextDirection.equals("SW")) {
            offset = 0;
        }
        if (currentDirection.equals("X") && nextDirection.equals("SE")) {
            offset = 0;
        }

        // Transition South
        if (currentDirection.equals("S") && nextDirection.equals("S")) {
            offset = 0;
            System.out.println("S->S");
        } else if (currentDirection.equals("S") && nextDirection.equals("SE")) {
            offset = 30;
            System.out.println("S->SE");
        } else if (currentDirection.equals("S") && nextDirection.equals("SW")) {
            offset = -30;
            System.out.println("S->SW");
        } else if (currentDirection.equals("S") && nextDirection.equals("E")) {
            offset = 0;
            System.out.println("S->E");
        } else if (currentDirection.equals("S") && nextDirection.equals("W")) {
            offset = 0;
            System.out.println("S->W");

            // Transition North
        } else if (currentDirection.equals("N") && nextDirection.equals("N")) {
            offset = 0;
            System.out.println("N->N");
        } else if (currentDirection.equals("N") && nextDirection.equals("NE")) {
            offset = 0;
            System.out.println("N->NE");
        } else if (currentDirection.equals("N") && nextDirection.equals("NW")) {
            offset = 0;
            System.out.println("N->NW");
        } else if (currentDirection.equals("N") && nextDirection.equals("E")) {
            offset = 0;
            System.out.println("N->E");
        } else if (currentDirection.equals("N") && nextDirection.equals("W")) {
            offset = 0;
            System.out.println("N->W");

            // Transition East
        } else if (currentDirection.equals("E") && nextDirection.equals("E")) {
            offset = 0;
            System.out.println("E->E");
        } else if (currentDirection.equals("E") && nextDirection.equals("NE")) {
            offset = 30;
            System.out.println("E->NE");
        } else if (currentDirection.equals("E") && nextDirection.equals("SE")) {
            offset = -30;
            System.out.println("E->SE");
        } else if (currentDirection.equals("E") && nextDirection.equals("S")) {
            offset = 0;
            System.out.println("E->S");
        } else if (currentDirection.equals("E") && nextDirection.equals("N")) {
            offset = -30;
            System.out.println("E->N");

            // Transition West
        } else if (currentDirection.equals("W") && nextDirection.equals("W")) {
            offset = 0;
            System.out.println("W->W");
        } else if (currentDirection.equals("W") && nextDirection.equals("SW")) {
            offset = 30;
            System.out.println("W->SW");
        } else if (currentDirection.equals("W") && nextDirection.equals("NW")) {
            offset = -30;
            System.out.println("W->NW");
        } else if (currentDirection.equals("W") && nextDirection.equals("S")) {
            offset = 0;
            System.out.println("W->S");
        } else if (currentDirection.equals("W") && nextDirection.equals("N")) {
            offset = 0;
            System.out.println("W->N");
            // Transition North West
        } else if (currentDirection.equals("NW") && nextDirection.equals("NW")) {
            offset = 60;
            System.out.println("NW->NW");
        } else if (currentDirection.equals("NW") && nextDirection.equals("W")) {
            offset = 30;
            System.out.println("NW->W");
        } else if (currentDirection.equals("NW") && nextDirection.equals("N")) {
            offset = 30;
            System.out.println("NW->N");
            // Transition South West
        } else if (currentDirection.equals("SW") && nextDirection.equals("SW")) {
            offset = 60;
            System.out.println("SW->SW");
        } else if (currentDirection.equals("SW") && nextDirection.equals("S")) {
            offset = -30;
            System.out.println("SW->S");
        } else if (currentDirection.equals("SW") && nextDirection.equals("W")) {
            offset = -30;
            System.out.println("SW->W");

            // Transition South East
        } else if (currentDirection.equals("SE") && nextDirection.equals("SE")) {
            offset = -60;
            System.out.println("SE->SE");
        } else if (currentDirection.equals("SE") && nextDirection.equals("S")) {
            offset = 30;
            System.out.println("SE->S");
        } else if (currentDirection.equals("SE") && nextDirection.equals("E")) {
            offset = 30;
            System.out.println("SE->E");

            // Transition North East
        } else if (currentDirection.equals("NE") && nextDirection.equals("NE")) {
            offset = 30;
            System.out.println("NE->NE");
        } else if (currentDirection.equals("NE") && nextDirection.equals("E")) {
            offset = -30;
            System.out.println("NE->E");
        } else if (currentDirection.equals("NE") && nextDirection.equals("N")) {
            offset = 0;
            System.out.println("NE->N");
        }
        System.out.println("offset : " + offset);
        return offset;
    }


    public State getCurrentState() {
        return currentState;
    }

    private void resetToInitialState() {
        System.out.println("Resetting to initial state.");
        currentState = State.X;
    }

    public List<String> convertToList(String gpsPath) {

        Pattern pattern = Pattern.compile("\\[([^\\]]*)\\]");
        Matcher matcher = pattern.matcher(gpsPath);

        if (matcher.find()) {
            String content = matcher.group(1);
            String[] directionsArray = content.split(",");
            List<String> directionsList = new ArrayList<>();
            for (String direction : directionsArray) {
                directionsList.add(direction.trim()); // Supprimer les espaces éventuels
            }
            return directionsList;
        }
        return null;
    }

    private void setLane(int velocity, int acceleration, double offset, double offsetFromCenter, String vehicleID) throws MqttException {
        String specificLaneTopic = "GPS/U/I/laneChange/" + vehicleID;
        System.out.println(specificLaneTopic);
        String payload = String.format(
                "{\"velocity\":%d,\"acceleration\":%d,\"offset\":%.1f,\"offsetFromCenter\":%.1f}",
                velocity, acceleration, offset, offsetFromCenter
        );

        publish(specificLaneTopic, payload, 2, false);
    }

    private void setSpeed(String velocity, String acceleration, String vehicleID) throws MqttException, InterruptedException {
        String specificSpeedTopic = "GPS/U/I/speed/" + vehicleID;
        String payload = String.format(
                "{\"velocity\":\"%s\",\"acceleration\":\"%s\"}", velocity, acceleration
        );
        publish(specificSpeedTopic, payload, 2, false);
        System.out.println("Changing speed : " + velocity + ", " + acceleration);
    }

    private void setLights(String vehicleID, String lightType, String effect, int start, int end, int frequency) throws MqttException, InterruptedException {
        String specificLightsTopic = "GPS/U/I/lights" + vehicleID;
        String payload = String.format(
                "{\"%s\":{\"effect\":\"%s\",\"start\":%d,\"end\":%d,\"frequency\":%d}}",
                lightType, effect, start, end, frequency
        );
        publish(specificLightsTopic, payload, 2, false);
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
            FSM fsm = new FSM();
        } catch(MqttException e) {
            e.printStackTrace();
        }
    }
}