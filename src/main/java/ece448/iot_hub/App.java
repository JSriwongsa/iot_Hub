package ece448.iot_hub;

import lombok.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@ComponentScan
public class App {

	@Autowired
	public App(Environment env) throws Exception {

	}
	
	// exclude the bean declaration from jacoco
	@Generated
	@Bean(destroyMethod = "close")
	public HubMqttController HubMqttController(Environment env) throws Exception {
		String broker = env.getProperty("mqtt.broker");
		String clientId = env.getProperty("mqtt.clientId");
		String topicPrefix = env.getProperty("mqtt.topicPrefix");
		HubMqttController mqttController = new HubMqttController(broker, clientId, topicPrefix);
		logger.info("MqttController: Started");
		mqttController.start();
		return mqttController;
	}

	private static final Logger logger = LoggerFactory.getLogger(App.class);
}