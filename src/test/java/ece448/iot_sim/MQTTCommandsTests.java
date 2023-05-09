package ece448.iot_sim;
import static org.junit.Assert.*;

//import org.junit.Test;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTCommandsTests {

    List<PlugSim> plugList = Arrays.asList(new PlugSim("xx"), new PlugSim("yy"), new PlugSim("zz.666"));
    String topicPrefix;

    @Test
    public void testMQTTCommandsConstructor() {
        // setup our constructor, make sure we give it the plugList of all of our plugNames.
        MQTTCommands mqttCmds = new MQTTCommands(plugList, topicPrefix);
        
        String constructor_name = mqttCmds.getClass().getName();
        assertEquals(constructor_name, mqttCmds.getClass().getName());
        assertEquals(plugList.get(0).getName(), "xx");
        assertEquals(plugList.get(1).getName(), "yy");
        assertEquals(plugList.get(2).getName(), "zz.666");
    }
    
    @Test
    public void testhandleMessage() {

        MQTTCommands mqttCmds = new MQTTCommands(plugList, topicPrefix);
            
        String topic_action_toggle = "1677446445897/grade_p3/iot_ece448/action/zz.666/toggle";
        String topic_action_on = "1677446445897/grade_p3/iot_ece448/action/zz.666/on";
        String topic_action_off = "1677446445897/grade_p3/iot_ece448/action/zz.666/off";

        // action.equals("on")
        String paths[] = topic_action_on.split("/");
        String action = paths[paths.length - 1];
        MqttMessage msg = new MqttMessage(action.getBytes());
        mqttCmds.handleMessage(topic_action_on, msg);

        // action.equals("off")
        String paths2[] = topic_action_off.split("/");
        String action2 = paths2[paths2.length - 1];
        MqttMessage msg2 = new MqttMessage(action2.getBytes());
        mqttCmds.handleMessage(topic_action_off, msg2);

        // action.equals("toggle")
        String paths3[] = topic_action_toggle.split("/");
        String action3 = paths3[paths3.length - 1];
        MqttMessage msg3 = new MqttMessage(action3.getBytes());
        mqttCmds.handleMessage(topic_action_toggle, msg3);

        // Throwable
        MqttMessage msg4 = new MqttMessage("bad action".getBytes());
        mqttCmds.handleMessage("non existant topic", msg4);

    }
        
    @Test
    public void testgetTopic() {
        MQTTCommands mqttCmds = new MQTTCommands(plugList, topicPrefix);
        String topicWithAction = topicPrefix+"/action/#";
        assertEquals(mqttCmds.getTopic(), topicWithAction);
    }
}
