package project3;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static project3.Emergency.EMERGENCY_TOPIC;
import static project3.Emergency.REMOTE_EMERGENCY_TOPIC;

public class RemoteControl {

    private static final String BROKER_URL = "tcp://10.42.0.1:1883";
    private static final String CLIENT_ID = "RemoteControl";

    protected static final String[] VehiclesID = {
            "825D320F", // White car
            "1C39200D", // White car with white sticker
            "64CB5600", // White car
            "13705309", // Dodge car
            "DDF65009", // Blue car
            "847FF007", // Dark Green - Orange car
            "B75C320F", // White car
            "93EC5112"  // White car with blue sticker
    };

    // Discover and connect
    private static final String FIRST_TOPIC = "Anki/Hosts/U/hyperdrive/I";
        private static final String DISCOVER_SUBSCRIPTION_TOPIC = "RemoteControl/U/E/hosts/discover";

    protected static final String SECOND_TOPIC = "Anki/Vehicles/U/I";
        private static final String CONNECT_SUBSCRIPTION_TOPIC = "RemoteControl/U/E/vehicles/connect/all";


        // payload for connexion
        private static final String f = "value : false";
        private static final String t = "value : true";

    // Vehicles parameters in RemoteControl
    protected static final String SPEED_TOPIC = "RemoteControl/U/E/vehicles/speed/E";
    protected static final String LIGHTS_TOPIC = "RemoteControl/U/E/vehicles/lights/E";
    protected static final String LANE_TOPIC = "RemoteControl/U/E/vehicles/laneChange/E";


    // Every Subscription
    protected static final String speedSubscription = "{\"type\": \"speedSubscription\", \"payload\": {\"topic\":" + " \"Emergency/U/E/RemoteControl/+/E/vehicles/speed/#\", \"subscribe\": true}}";
    protected static final String emergencySubscription = "{\"type\": \"speedSubscription\", \"payload\": {\"topic\":" + " \"Emergency/U/E/Message\", \"subscribe\": true}}";
    protected static final String discoverSubscription = "{\"type\": \"discoverSubscription\", \"payload\":" + " {\"topic\": \"RemoteControl/U/E/hosts/discover\", \"subscribe\": true}}";
    protected static final String connectSubscription = "{\"type\": \"connectSubscription\", \"payload\":" + " {\"topic\": \"RemoteControl/U/E/vehicles/connect/#\", \"subscribe\": true}}";
    private static final String lightsSubscription = "{\"type\": \"lightsSubscription\", \"payload\": {\"topic\": \"Emergency/U/E/RemoteControl/+/E/vehicles/lights/#\", \"subscribe\": true}}";
    private static final String laneSubscription = "{\"type\": \"laneSubscription\", \"payload\": {\"topic\": \"Emergency/U/E/RemoteControl/+/E/vehicles/laneChange/#\", \"subscribe\": true}}";

    // RemoteControl set up
    private MqttClient client;

    public RemoteControl() throws MqttException {
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
                System.out.println(topic + " : " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        client.connect(options);
       // client.subscribe(Emergency.EMERGENCY_TOPIC + "/Message");
    }

    private void discover() throws MqttException, InterruptedException {
        publish(FIRST_TOPIC, discoverSubscription, 0, false); // I want to subscribe to RemoteControl/+/E/hosts/discover using Anki/Hosts/U/I
        System.out.println("I'm subscribing to RemoteControl/+/E/hosts/discover");
        Thread.sleep(3000);
        publish(DISCOVER_SUBSCRIPTION_TOPIC, t, 0, false);
        Thread.sleep(3000);
    }

    private void connectAll() throws MqttException, InterruptedException {
        publish(SECOND_TOPIC, connectSubscription, 0, false); // I want to subscribe to RemoteControl/+/E/vehicles/connect/# using Anki/Vehicles/U/I
        System.out.println(connectSubscription);
        Thread.sleep(3000);
        publish(CONNECT_SUBSCRIPTION_TOPIC, t, 0, false);
        System.out.println("I'm subscribing to RemoteControl/+/E/vehicles/connect/#");
    }

    public void setEmergencyTo(String vehicleID) throws MqttException, InterruptedException {
        publish(REMOTE_EMERGENCY_TOPIC + "/" + vehicleID, "Emergency", 1, false);
    }

    public void undoEmergencyTo(String vehicleID) throws MqttException, InterruptedException {
        publish(REMOTE_EMERGENCY_TOPIC + "/" + vehicleID, "!Emergency", 1, false);
    }

    public void subscribeVehicle(String vehicleID) throws MqttException, InterruptedException {

        String specificSpeedTopic = "RemoteControl/U/E/vehicles/speed/" + vehicleID;
        String specificLaneTopic = "RemoteControl/U/E/vehicles/laneChange/" + vehicleID;
        String specificLightTopic = "RemoteControl/U/E/vehicles/lights/" + vehicleID;
        String specificEmergencyTopic = "RemoteControl/U/E/vehicles/emergency/Message/" + vehicleID;

        String oneSpeedSubscription = "{\"type\": \"speedSubscription\", \"payload\": {\"topic\": \"Emergency/U/E/RemoteControl/+/E/vehicles/speed/"
                + vehicleID + "\", \"subscribe\": true}}";
        String oneLaneSubscription = "{\"type\": \"laneSubscription\", \"payload\": {\"topic\": \"Emergency/U/E/RemoteControl/+/E/vehicles/laneChange/"
                + vehicleID + "\", \"subscribe\": true}}";
        String oneLightSubscription = "{\"type\": \"lightsSubscription\", \"payload\": {\"topic\": \"Emergency/U/E/RemoteControl/+/E/vehicles/lights/"
                + vehicleID + "\", \"subscribe\": true}}";
        String oneEmergencySubscription = "{\"type\": \"speedSubscription\", \"payload\": {\"topic\": \"Emergency/U/E/Message/"
                + vehicleID + "\", \"subscribe\": true}}";

        publish("Anki/Vehicles/U/" + vehicleID + "/I", oneSpeedSubscription, 0, false);
        System.out.println(vehicleID + " : " + oneSpeedSubscription);
        Thread.sleep(2000);
        publish("Anki/Vehicles/U/" + vehicleID + "/I", oneLaneSubscription, 2, false);
        System.out.println(vehicleID + " : " + oneLaneSubscription);
        Thread.sleep(2000);
        publish("Anki/Vehicles/U/" + vehicleID + "/I",oneLightSubscription, 2, false);
        System.out.println(vehicleID + " : " + oneLightSubscription);
        Thread.sleep(2000);
        publish("Anki/Vehicles/U/" + vehicleID + "/I", oneEmergencySubscription, 1, false);
        System.out.println(vehicleID + " : " + oneEmergencySubscription);
    }

    public void connectID(String vehicleID) throws MqttException, InterruptedException {
        // Specific topic for each vehicle
        String specificConnectTopic = "RemoteControl/U/E/vehicles/connect/" + vehicleID;
        System.out.println("Connect the vehicle " + vehicleID);

        String oneConnectSubscription = "{\"type\": \"connectSubscription\", \"payload\": {\"topic\": \"RemoteControl/U/E/vehicles/connect/"
                + vehicleID + "\", \"subscribe\": true}}";

        // Subscribe the vehicle to the remote
        publish("Anki/Vehicles/U/" + vehicleID + "/I", oneConnectSubscription, 0, false);
        Thread.sleep(3000);
        // Confirm the connection
        publish(specificConnectTopic, t, 1, false);

    }

    public void setSpeedTo(String vehicleID, String velocity, String acceleration) throws MqttException, InterruptedException {

        // Specific topic for speed control of the vehicle
        String specificSpeedTopic = "RemoteControl/U/E/vehicles/speed/" + vehicleID;
        System.out.println("Setting speed for vehicle " + vehicleID);

        // Speed control payload
        String payload = String.format("{\"velocity\":\"%s\",\"acceleration\":\"%s\"}", velocity, acceleration);

        // Publish the speed control payload to the specific topic
        publish(specificSpeedTopic, payload, 1, false);
        System.out.println("Changing speed for vehicle " + vehicleID + ": velocity = " + velocity + ", acceleration = " + acceleration);
        System.out.println(payload);
    }

    public void setLightsTo(String vehicleID, String lightType, String effect, int start, int end, int frequency) throws MqttException, InterruptedException {

        // Specific topic for lights control of the vehicle
        String specificLightsTopic = "RemoteControl/U/E/vehicles/lights/" + vehicleID;
        System.out.println("Setting lights for vehicle " + vehicleID);

        // Lights control payload
        String payload = String.format(
                "{\"%s\":{\"effect\":\"%s\",\"start\":%d,\"end\":%d,\"frequency\":%d}}",
                lightType, effect, start, end, frequency
        );

        // Publish the lights control payload to the specific topic
        publish(specificLightsTopic, payload, 1, false);
        System.out.println(lightType + " light for vehicle " + vehicleID + ": effect = " + effect + ", start = " + start + ", end = " + end + ", frequency = " + frequency);
        System.out.println(payload);
    }

    public void setLaneTo(String vehicleID, int velocity, int acceleration, double offset, double offsetFromCenter) throws MqttException, InterruptedException {

        // Specific topic for lane change control of the vehicle
        String specificLaneTopic = "RemoteControl/U/E/vehicles/laneChange/" + vehicleID;
        System.out.println("Changing lane for vehicle " + vehicleID);

        // Lane change payload
        String payload = String.format("{\"velocity\":%d,\"acceleration\":%d,\"offset\":%.1f,\"offsetFromCenter\":%.1f}",
                velocity, acceleration, offset, offsetFromCenter
        );

        // Publish the lane change payload to the specific topic
        publish(specificLaneTopic, payload, 1, false);
        System.out.println("Changing lane for vehicle " + vehicleID + ": velocity = " + velocity +
                ", acceleration = " + acceleration +
                ", offset = " + offset +
                ", offsetFromCenter = " + offsetFromCenter);
        System.out.println(payload);
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
            RemoteControl remote = new RemoteControl();
            remote.discover();
            remote.connectID(VehiclesID[3]);
            remote.subscribeVehicle(VehiclesID[3]);
            remote.connectID(VehiclesID[4]);
            remote.subscribeVehicle(VehiclesID[4]);
            System.out.println("Set up done");
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
