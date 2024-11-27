package project2;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static project2.Emergency.EMERGENCY_TOPIC;
import static project2.Emergency.REMOTE_EMERGENCY_TOPIC;

public class RemoteControl {

    private static final String BROKER_URL = "tcp://10.42.0.1:1883";
    private static final String CLIENT_ID = "RemoteControl";

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
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        client.connect(options);
       // client.subscribe(Emergency.EMERGENCY_TOPIC + "/Message");
    }

    private void discoverAndConnect() throws MqttException, InterruptedException {
        publish(FIRST_TOPIC, discoverSubscription, 0, false); // I want to subscribe to RemoteControl/+/E/hosts/discover using Anki/Hosts/U/I
        System.out.println("I'm subscribing to RemoteControl/+/E/hosts/discover");
        Thread.sleep(3000);
        publish(DISCOVER_SUBSCRIPTION_TOPIC, t, 0, false);
        Thread.sleep(3000);
        publish(SECOND_TOPIC, connectSubscription, 0, false); // I want to subscribe to RemoteControl/+/E/vehicles/connect/# using Anki/Vehicles/U/I
        Thread.sleep(3000);
        publish(CONNECT_SUBSCRIPTION_TOPIC, t, 0, false);
        System.out.println("I'm subscribing to RemoteControl/+/E/vehicles/connect/#");
    }

    protected void setSpeed(String velocity, String acceleration) throws MqttException, InterruptedException {
        publish(SECOND_TOPIC, speedSubscription, 0, false);
        // publish(SECOND_TOPIC, emergencySubscription, 0, false);
        System.out.println("I'm subscribing to " + EMERGENCY_TOPIC + "/" + SPEED_TOPIC + "and" + EMERGENCY_TOPIC + "/Message" );
        String payload = String.format("{\"velocity\":\"%s\",\"acceleration\":\"%s\"}", velocity, acceleration);
        publish(SPEED_TOPIC, payload, 1, false);
        System.out.println("Changing speed : " + velocity + ", " + acceleration);
    }

    public void setLights(String lightType, String effect, int start, int end, int frequency) throws MqttException {
        publish(SECOND_TOPIC, lightsSubscription, 0, false);
        String payload = String.format(
                "{\"%s\":{\"effect\":\"%s\",\"start\":%d,\"end\":%d,\"frequency\":%d}}",
                lightType, effect, start, end, frequency
        );
        publish(LIGHTS_TOPIC, payload, 1, false);
        System.out.println(lightType + " light: effect : " + effect + ", start : " + start + ", end : " + end + ", frequency : " + frequency);
    }

    public void setLane(int velocity, int acceleration, double offset, double offsetFromCenter) throws MqttException {
        publish(SECOND_TOPIC, laneSubscription, 0, false);
        String payload = String.format("{\"velocity\":%d,\"acceleration\":%d,\"offset\":%.1f,\"offsetFromCenter\":%.1f}",
                velocity, acceleration, offset, offsetFromCenter
        );
        publish(LANE_TOPIC, payload, 1, false);
        System.out.println("Changing lane with velocity : " + velocity +
                ", acceleration : " + acceleration +
                ", offset : " + offset +
                ", offsetFromCenter : " + offsetFromCenter);
    }


    public void setEmergency() throws MqttException, InterruptedException {
        publish(SECOND_TOPIC, emergencySubscription, 1, false);
        publish(REMOTE_EMERGENCY_TOPIC, "Emergency", 1, false);
        System.out.println("Emergency : true");
    }
    public void undoEmergency() throws MqttException, InterruptedException {
        publish(SECOND_TOPIC, emergencySubscription, 1, false);
        publish(REMOTE_EMERGENCY_TOPIC, "!Emergency", 1, false);
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
            remote.discoverAndConnect();

        } catch (MqttException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
