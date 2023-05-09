package ece448.iot_sim;
import static org.junit.Assert.*;

//import org.junit.Test;
import org.junit.jupiter.api.Test;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTUpdatesTests {
    @Test
    public void testMQTTUpdatesConstructor() {
        String topicPrefix = "1677446445897/grade_p3/iot_ece448/";
        MQTTUpdates mqttUpdates = new MQTTUpdates(topicPrefix);
        String constructor_name = mqttUpdates.getClass().getName();
        assertEquals(constructor_name, mqttUpdates.getClass().getName());
    }

    @Test
    public void testgetTopic() {
        String topicPrefix = "1677446445897/grade_p3/iot_ece448/";
        MQTTUpdates mqttUpdates = new MQTTUpdates(topicPrefix);
        String expectedString = topicPrefix+"/update/state/on";

        String stateOnTopicString = mqttUpdates.getTopic("state", "on");
        assertEquals(stateOnTopicString, expectedString);
    }

    @Test
    public void testgetMessage() {
        String topicPrefix = "1677446445897/grade_p3/iot_ece448/";
        MQTTUpdates mqttUpdates = new MQTTUpdates(topicPrefix);

        MqttMessage expected_msg = new MqttMessage("My Message".getBytes());
        expected_msg.setRetained(true);
        assertEquals(expected_msg.toString(), mqttUpdates.getMessage("My Message").toString());
    }


    
}
