package project3.utilities;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static project3.Services.Emergency.REMOTE_EMERGENCY_TOPIC;

public class RemoteControl {

    private static final String BROKER_URL = "tcp://10.42.0.1:1883";
    private static final String CLIENT_ID = "RemoteControl";

    public static final String[] VehiclesID1 = {
            "825D320F", // White car
            "1C39200D", // White car with white sticker
            "64CB5600", // White car
            "13705309", // Dodge car
            "DDF65009", // Blue car
            "847FF007", // Dark Green - Orange car
            "B75C320F", // White car
            "93EC5112"  // White car with blue sticker
    };

    protected static final String[] VehiclesID2 = {
            "3F960001", // Yellow car
            "94D75600", // White car with sticker db443e1e4
            "A9450008", // Black-red car
            "8EF0310F", // Dodge car
            "595D320F", // White car with orange sticker
            "69CAA20E", // Pick-up Hummer
            "045F302F", // White car
            "ECE95112"  // White car
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

    // Alternative topics
    protected static final String ALTERNATIVE_TOPIC = "Alternative/U/E/vehicles/";


    // Every Subscription
    protected static final String speedSubscription = "{\"type\": \"speedSubscription\", \"payload\": {\"topic\":" + " \"Emergency/U/E/RemoteControl/+/E/vehicles/speed/#\", \"subscribe\": true}}";
    protected static final String emergencySubscription = "{\"type\": \"speedSubscription\", \"payload\": {\"topic\":" + " \"Emergency/U/E/Message\", \"subscribe\": true}}";
    protected static final String discoverSubscription = "{\"type\": \"discoverSubscription\", \"payload\":" + " {\"topic\": \"RemoteControl/U/E/hosts/discover\", \"subscribe\": true}}";
    protected static final String connectSubscription = "{\"type\": \"connectSubscription\", \"payload\":" + " {\"topic\": \"RemoteControl/U/E/vehicles/connect/#\", \"subscribe\": true}}";
    protected static final String lightsSubscription = "{\"type\": \"lightsSubscription\", \"payload\": {\"topic\": \"Emergency/U/E/RemoteControl/+/E/vehicles/lights/#\", \"subscribe\": true}}";
    protected static final String laneSubscription = "{\"type\": \"laneSubscription\", \"payload\": {\"topic\": \"Emergency/U/E/RemoteControl/+/E/vehicles/laneChange/#\", \"subscribe\": true}}";

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
        Thread.sleep(3000);
        publish(CONNECT_SUBSCRIPTION_TOPIC, t, 0, false);
        System.out.println("I'm subscribing to RemoteControl/+/E/vehicles/connect/#");
    }

    public void setEmergencyTo(String vehicleID) throws MqttException, InterruptedException {
        publish(REMOTE_EMERGENCY_TOPIC + "/" + vehicleID, "Emergency" + vehicleID, 1, false);
    }

    public void undoEmergencyTo(String vehicleID) throws MqttException, InterruptedException {
        publish(REMOTE_EMERGENCY_TOPIC + "/" + vehicleID, "!Emergency" + vehicleID, 1, false);
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
        String oneEmergencySubscription = "{\"type\": \"speedSubscription\", \"payload\": {\"topic\": \"Emergency/U/E/Message/speed/"
                + vehicleID + "\", \"subscribe\": true}}";
        String oneLightEmergencySubscription = "{\"type\": \"lightsSubscription\", \"payload\": {\"topic\": \"Emergency/U/E/Message/lights/"
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
        Thread.sleep(2000);
        publish("Anki/Vehicles/U/" + vehicleID + "/I", oneLightEmergencySubscription, 1, false);
        System.out.println(vehicleID + " : " + oneLightEmergencySubscription);
        System.out.println("Remote set up done");
    }

    public void subscribeCoordinatesOf(String vehicleID) throws MqttException, InterruptedException {

    }

    public void connectID(String vehicleID) throws MqttException, InterruptedException {
        String specificConnectTopic = "RemoteControl/U/E/vehicles/connect/" + vehicleID;
        String oneConnectSubscription = "{\"type\": \"connectSubscription\", \"payload\": {\"topic\": \"RemoteControl/U/E/vehicles/connect/"
                + vehicleID + "\", \"subscribe\": true}}";
        publish("Anki/Vehicles/U/" + vehicleID + "/I", oneConnectSubscription, 0, false);
        Thread.sleep(3000);
        publish(specificConnectTopic, t, 1, false);
        System.out.println(vehicleID + " is connected");
    }

    public void setSpeedTo(String vehicleID, String velocity, String acceleration) throws MqttException, InterruptedException {
        String specificSpeedTopic = "RemoteControl/U/E/vehicles/speed/" + vehicleID;
        String payload = String.format("{\"velocity\":\"%s\",\"acceleration\":\"%s\"}",
                velocity, acceleration);
        publish(specificSpeedTopic, payload, 1, false);
    }

    public void setLightsTo(String vehicleID, String lightType, String effect, int start, int end, int frequency) throws MqttException, InterruptedException {
        String specificLightsTopic = "RemoteControl/U/E/vehicles/lights/" + vehicleID;
        String payload = String.format("{\"%s\":{\"effect\":\"%s\",\"start\":%d,\"end\":%d,\"frequency\":%d}}",
                lightType, effect, start, end, frequency
        );
        publish(specificLightsTopic, payload, 1, false);
    }

    public void setLaneTo(String vehicleID, int velocity, int acceleration, double offset, double offsetFromCenter) throws MqttException, InterruptedException {

        // Specific topic for lane change control of the vehicle
        String specificLaneTopic = "RemoteControl/U/E/vehicles/laneChange/" + vehicleID;
        // Lane change payload
        String payload = String.format("{\"velocity\":%d,\"acceleration\":%d,\"offset\":%.1f,\"offsetFromCenter\":%.1f}",
                velocity, acceleration, offset, offsetFromCenter
        );

        // Publish the lane change payload to the specific topic
        publish(specificLaneTopic, payload, 1, false);
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
            remote.connectAll();
            Thread.sleep(2000);
            remote.subscribeVehicle(VehiclesID1[3]);
            remote.subscribeVehicle(VehiclesID1[7]);
            System.out.println("Set up done");
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
