package ece448.iot_sim;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQTTCommands {
    // same structure as HTTPCommands.java
    private final TreeMap<String, PlugSim> plugs = new TreeMap<>();
    private final String topicPrefix;

    public MQTTCommands(List<PlugSim> plugs, String topicPrefix) {
		for (PlugSim plug: plugs)
		{
			this.plugs.put(plug.getName(), plug);
        }
        this.topicPrefix = topicPrefix;
    }
    
    // use # wildcard to match anything nested after action/
    public String getTopic() {
        return topicPrefix+"/action/#";
    }

    // MqttMessage: 
    public void handleMessage(String topic, MqttMessage msg) {
        try {
            logger.info("MQTTCommand {}", topic);
            // switch on/off toggle here:
            String paths[] = topic.split("/");
            String action = paths[paths.length - 1];
            String plugName = paths[paths.length - 2 ];

            logger.info("MQTT PlugName: {}", plugName);
            logger.info("MQTT Action: {}", action);

            // this should handle the PlugSim logic
            PlugSim plug = plugs.get(plugName);

            if (action.equals("on")) {
                plug.switchOn();
            } else if (action.equals("off")) {
                plug.switchOff();
            } else if (action.equals("toggle")) {
                plug.toggle();
            } else {};
                // no such action
        }
        catch (Throwable th) {
            // Otherwise, MQTT Client will disconnect w/o error information
            //logger.error("MQTTCommand {}: {}", topic, th.getMessage(), th);
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(MQTTCommands.class);

}
