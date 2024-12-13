package project3.Utilities;

import org.eclipse.paho.client.mqttv3.MqttException;

import javax.swing.*;
import java.awt.*;

public class Interface {

    private static RemoteControl remoteControl;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                remoteControl = new RemoteControl();

                String[] vehicleOptions = {
                        "825D320F", // White car
                        "1C39200D", // White car with white sticker
                        "64CB5600", // White car
                        "13705309", // Dodge car
                        "DDF65009", // Blue car
                        "847FF007", // Dark Green - Orange car
                        "B75C320F", // White car
                        "93EC5112"  // White car with blue sticker
                };

                JFrame frame = new JFrame("Remote Control Interface");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(500, 600);
                frame.setLayout(new GridLayout(6, 1, 10, 10));
                frame.getContentPane().setBackground(Color.PINK);

                // Vehicle selection
                JPanel vehicleSelection = createVehicleSelection(vehicleOptions);

                // Speed control section
                JPanel speedControl = createSpeedControl();

                // Light control section
                JPanel lightControl = createLightControl();

                // Lane control section
                JPanel laneControl = createLaneControl();

                // Emergency control section
                JPanel emergencyControl = createEmergencyControl();

                JPanel notEmergencyControl = createNotEmergency();

                frame.add(vehicleSelection);
                frame.add(speedControl);
                frame.add(lightControl);
                frame.add(laneControl);
                frame.add(emergencyControl);
                frame.add(notEmergencyControl);
                frame.setVisible(true);

            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
    }

    private static String selectedVehicleID = null;

    private static JPanel createVehicleSelection(String[] vehicleOptions) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Select Vehicle"));
        panel.setBackground(new Color(255, 230, 240));

        JLabel vehicleLabel = new JLabel("Vehicle ID:");
        JComboBox<String> vehicleComboBox = new JComboBox<>(vehicleOptions);

        vehicleComboBox.addActionListener(e -> {
            selectedVehicleID = (String) vehicleComboBox.getSelectedItem();
            System.out.println("Selected Vehicle: " + selectedVehicleID);
        });

        panel.add(vehicleLabel);
        panel.add(vehicleComboBox);

        return panel;
    }

    private static JPanel createSpeedControl() {
        String[] speedOptions = {"0", "100", "200", "250", "300", "400", "500"};

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Speed Control"));
        panel.setBackground(new Color(255, 230, 240));

        JLabel velocityLabel = new JLabel("Velocity:");
        JComboBox<String> velocityComboBox = new JComboBox<>(speedOptions);
        velocityComboBox.setSelectedItem("250"); // Set default value

        JLabel accelerationLabel = new JLabel("Acceleration:");
        JComboBox<String> accelerationComboBox = new JComboBox<>(speedOptions);
        accelerationComboBox.setSelectedItem("500"); // Set default value

        JButton setSpeedButton = new JButton("Set Speed");
        setSpeedButton.setBackground(new Color(255, 102, 178));
        setSpeedButton.setForeground(Color.BLACK);

        setSpeedButton.addActionListener(e -> {
            if (selectedVehicleID == null) {
                JOptionPane.showMessageDialog(null, "Please select a vehicle first!");
                return;
            }
            String velocity = (String) velocityComboBox.getSelectedItem();
            String acceleration = (String) accelerationComboBox.getSelectedItem();
            try {
                System.out.println("Setting Speed: VehicleID = " + selectedVehicleID +
                        ", Velocity = " + velocity + ", Acceleration = " + acceleration);
                remoteControl.setSpeedTo(selectedVehicleID, velocity, acceleration);
            } catch (MqttException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });

        panel.add(velocityLabel);
        panel.add(velocityComboBox);
        panel.add(accelerationLabel);
        panel.add(accelerationComboBox);
        panel.add(new JLabel());
        panel.add(setSpeedButton);

        return panel;
    }


    private static JPanel createLightControl() {
        String[] lightOptions = {"frontGreen", "frontRed", "frontBlue", "tail"};
        String[] effectOptions = {"pulse", "flash"};

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Lights Control"));
        panel.setBackground(new Color(255, 230, 240));

        JLabel lightTypeLabel = new JLabel("Light Type:");
        JComboBox<String> lightTypeComboBox = new JComboBox<>(lightOptions);

        JLabel effectLabel = new JLabel("Effect:");
        JComboBox<String> effectComboBox = new JComboBox<>(effectOptions);

        JButton setLightButton = new JButton("Set Lights");
        setLightButton.setBackground(new Color(255, 102, 178));
        setLightButton.setForeground(Color.BLACK);

        setLightButton.addActionListener(e -> {
            if (selectedVehicleID == null) {
                JOptionPane.showMessageDialog(null, "Please select a vehicle first!");
                return;
            }
            String lightType = (String) lightTypeComboBox.getSelectedItem();
            String effect = (String) effectComboBox.getSelectedItem();
            try {
                remoteControl.setLightsTo(selectedVehicleID, lightType, effect, 0, 100, 10);
            } catch (MqttException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });

        panel.add(lightTypeLabel);
        panel.add(lightTypeComboBox);
        panel.add(effectLabel);
        panel.add(effectComboBox);
        panel.add(new JLabel());
        panel.add(setLightButton);

        return panel;
    }

    private static JPanel createLaneControl() {
        String[] speedOptions = {"0", "100", "200", "250", "300", "400", "500"};
        String[] offsetOptions = {"-90", "-80", "-70", "-60", "-50", "-40", "-30", "-20", "-10", "10", "20", "30", "40", "50", "60", "70", "80", "90"};
        String[] offsetFromCenterOptions = {"-50", "-40", "-30", "-20", "-10", "0", "10", "20", "30", "40", "50"};

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Lane Control"));
        panel.setBackground(new Color(255, 230, 240));

        JLabel velocityLabel = new JLabel("Velocity:");
        JComboBox<String> velocityComboBox = new JComboBox<>(speedOptions);
        velocityComboBox.setSelectedItem("250"); // Set default value

        JLabel accelerationLabel = new JLabel("Acceleration:");
        JComboBox<String> accelerationComboBox = new JComboBox<>(speedOptions);
        accelerationComboBox.setSelectedItem("500"); // Set default value

        JLabel offsetLabel = new JLabel("Offset:");
        JComboBox<String> offsetComboBox = new JComboBox<>(offsetOptions);

        JLabel offsetCenterLabel = new JLabel("Offset From Center:");
        JComboBox<String> offsetCenterComboBox = new JComboBox<>(offsetFromCenterOptions);
        offsetCenterComboBox.setSelectedItem("0"); // Set default value

        JButton setLaneButton = new JButton("Set Lane");
        setLaneButton.setBackground(new Color(255, 102, 178));
        setLaneButton.setForeground(Color.BLACK);

        setLaneButton.addActionListener(e -> {
            if (selectedVehicleID == null) {
                JOptionPane.showMessageDialog(null, "Please select a vehicle first!");
                return;
            }
            String velocity = (String) velocityComboBox.getSelectedItem();
            String acceleration = (String) accelerationComboBox.getSelectedItem();
            String offset = (String) offsetComboBox.getSelectedItem();
            String offsetFromCenter = (String) offsetCenterComboBox.getSelectedItem();
            try {
                remoteControl.setLaneTo(selectedVehicleID,
                        Integer.parseInt(velocity),
                        Integer.parseInt(acceleration),
                        Double.parseDouble(offset),
                        Double.parseDouble(offsetFromCenter));
                System.out.println(velocity + " " + acceleration + " " + offset + " " + offsetFromCenter);
            } catch (MqttException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });

        panel.add(velocityLabel);
        panel.add(velocityComboBox);
        panel.add(accelerationLabel);
        panel.add(accelerationComboBox);
        panel.add(offsetLabel);
        panel.add(offsetComboBox);
        panel.add(offsetCenterLabel);
        panel.add(offsetCenterComboBox);
        panel.add(new JLabel());
        panel.add(setLaneButton);

        return panel;
    }


    private static JPanel createEmergencyControl() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Emergency Stop"));
        panel.setBackground(new Color(255, 230, 240));

        JButton emergencyButton = new JButton("Activate Emergency Stop");
        emergencyButton.setBackground(new Color(255, 102, 178));
        emergencyButton.setForeground(Color.RED);

        emergencyButton.addActionListener(e -> {
            if (selectedVehicleID == null) {
                JOptionPane.showMessageDialog(null, "Please select a vehicle first!");
                return;
            }
            try {
                remoteControl.setEmergencyTo(selectedVehicleID);
            } catch (MqttException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });

        panel.add(emergencyButton);

        return panel;
    }

    private static JPanel createNotEmergency() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Deactivate Emergency Stop"));
        panel.setBackground(new Color(255, 230, 240));

        JButton notEmergencyButton = new JButton("Deactivate Emergency Stop");
        notEmergencyButton.setBackground(new Color(255, 102, 178));
        notEmergencyButton.setForeground(Color.GREEN);

        notEmergencyButton.addActionListener(e -> {
            if (selectedVehicleID == null) {
                JOptionPane.showMessageDialog(null, "Please select a vehicle first!");
                return;
            }
            try {
                remoteControl.undoEmergencyTo(selectedVehicleID);
            } catch (MqttException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });

        panel.add(notEmergencyButton);

        return panel;
    }
}
