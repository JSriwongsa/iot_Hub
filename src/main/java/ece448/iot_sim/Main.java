package ece448.iot_sim;

import java.io.File;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ece448.iot_sim.http_server.JHTTP;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

public class Main implements AutoCloseable {
	public static void main(String[] args) throws Exception {
		// load configuration file
		String configFile = args.length > 0 ? args[0] : "simConfig.json";
		SimConfig config = mapper.readValue(new File(configFile), SimConfig.class);
		logger.info("{}: {}", configFile, mapper.writeValueAsString(config));
		
		try (Main m = new Main(config))
		{
			// loop forever
			for (;;)
			{
				Thread.sleep(60000);
			}
		}
	}
	
	public Main(SimConfig config) throws Exception {
		// create plugs
		ArrayList<PlugSim> plugs = new ArrayList<>();
		for (String plugName: config.getPlugNames()) {
			plugs.add(new PlugSim(plugName));
		}
		
		// start power measurements
		MeasurePower measurePower = new MeasurePower(plugs);
		measurePower.start();
		
		// start HTTP commands
		this.http = new JHTTP(config.getHttpPort(), new HTTPCommands(plugs));
		this.http.start();
		
		// start MQTT commands:
		MQTTCommands mqttCmd = new MQTTCommands(plugs, config.getMqttTopicPrefix());
		
		// guard clause:
		// setup MQTT client and connect to the broker using json config file values.
		// dont execute this code if mqttClientId is null. 
		// gradle grade_p2 fails after we added this, so make this aware of that so those tests will still pass:
		String mqttBroker = config.getMqttBroker();
		String mqttClientId = config.getMqttClientId();
		if (mqttBroker == null || mqttClientId == null) {
			logger.info("MQTT Not loaded. Continuing without.");
			this.mqtt = null;
		} else {
			this.mqtt = new MqttClient(mqttBroker, mqttClientId, new MemoryPersistence());       
			
			MqttConnectOptions opt = new MqttConnectOptions();
			
			// SSL/TLS options:
			String caFilePath = "/home/ubuntu/iot_ece448/ca/ca.crt";
			String clientCrtFilePath = "/home/ubuntu/iot_ece448/ca/client.pem";
			String clientKeyFilePath = "/home/ubuntu/iot_ece448/ca/client.key";
			
			SSLSocketFactory socketFactory = getSocketFactory(caFilePath,
			clientCrtFilePath, clientKeyFilePath, "asdf");
			
			// self-signed certificate being used:
			// disable host name verification
			opt.setSocketFactory(socketFactory);
			opt.setHttpsHostnameVerificationEnabled(false);
			
			this.mqtt.connect(opt);
			
			// sub: /action
			// sub: use lambda with handleMessage(). IoC implementation
			logger.info("MQTT subscribe to {}", mqttCmd.getTopic());
			this.mqtt.subscribe(mqttCmd.getTopic(), (topic, msg)-> {
				mqttCmd.handleMessage(topic, msg);
			});
			
			// start MQTT updates. get the topic from our json config value:
			MQTTUpdates mqttUpd = new MQTTUpdates(config.getMqttTopicPrefix());
			
			// pub: /updates
			// pub: use lambda with getTopic() and getMessage(). IoC implementation
			for (PlugSim plug: plugs) {
				plug.addObserver((name, key, value) -> {
					try {
						logger.info("MQTT publish to {} {} {}", name, key, value);
						mqtt.publish(mqttUpd.getTopic(name, key), mqttUpd.getMessage(value));
					} catch (Exception e) {
						logger.error("fail to publish {} {} {}", name, key, value, e);
					}
				});
			}
		}
	}
	
	private static SSLSocketFactory getSocketFactory(final String caCrtFile,
	final String crtFile, final String keyFile, final String password)
	throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		
		// load CA certificate
		X509Certificate caCert = null;
		
		FileInputStream fis = new FileInputStream(caCrtFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		
		while (bis.available() > 0) {
			caCert = (X509Certificate) cf.generateCertificate(bis);
			// System.out.println(caCert.toString());
		}
		
		// load client certificate
		bis = new BufferedInputStream(new FileInputStream(crtFile));
		X509Certificate cert = null;
		while (bis.available() > 0) {
			cert = (X509Certificate) cf.generateCertificate(bis);
			// System.out.println(caCert.toString());
		}
		
		// load client private key
		PEMParser pemParser = new PEMParser(new FileReader(keyFile));
		Object object = pemParser.readObject();
		PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
		.build(password.toCharArray());
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
		.setProvider("BC");
		KeyPair key;
		if (object instanceof PEMEncryptedKeyPair) {
			System.out.println("Encrypted key - we will use provided password");
			key = converter.getKeyPair(((PEMEncryptedKeyPair) object)
			.decryptKeyPair(decProv));
		} else {
			System.out.println("Unencrypted key - no password needed");
			key = converter.getKeyPair((PEMKeyPair) object);
		}
		pemParser.close();
		
		// CA certificate is used to authenticate server
		KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
		caKs.load(null, null);
		caKs.setCertificateEntry("ca-certificate", caCert);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
		tmf.init(caKs);
		
		// client key and certificates are sent to server so it can authenticate
		// us
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ks.setCertificateEntry("certificate", cert);
		ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
		new java.security.cert.Certificate[] { cert });
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
		.getDefaultAlgorithm());
		kmf.init(ks, password.toCharArray());
		
		// finally, create SSL socket factory
		SSLContext context = SSLContext.getInstance("TLSv1.2");
		context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		
		return context.getSocketFactory();
	}
	
	@Override
	public void close() throws Exception {
		http.close();
		// guard against no mqtt being provided. otherwise disconnect. 
		// grade_p2 inits SimConfig without any mqtt info.
		// this prevents error.
		if (this.mqtt == null) {
		} else {
			mqtt.disconnect();
		}
	}
	private final JHTTP http;
	private final MqttClient mqtt;
	
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
}
