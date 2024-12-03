package project3;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static project3.Emergency.EMERGENCY_TOPIC;
import static project3.Emergency.REMOTE_TOPIC;

public class Alternative {

    private static final String BROKER_URL = "tcp://10.42.0.1:1883";
    private static final String CLIENT_ID = "Alternative";
    private static boolean alternative = false;
    private MqttClient client;

    public Alternative() throws MqttException {


        MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connexion lost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if(topic.contains("Emergency")) {
                    String payload = new String(message.getPayload());
                    if(payload.equals("Emergency")){
                        alternative = true;
                    }
                }
                if(alternative) {
                    findAlternative();
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
        client.connect(options);
    }
    private void findAlternative() throws MqttException {
        
    }

    // Publish to any topic
    private void publish(String topic, String message, int qos, boolean retained) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);
        client.publish(topic, mqttMessage);
    }
}
