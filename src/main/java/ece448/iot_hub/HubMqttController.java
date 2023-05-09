package ece448.iot_hub;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.HashMap;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.MqttClient;
import java.util.TreeMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import lombok.Generated;

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

@Generated
public class HubMqttController {
    private final String broker;
    private final String clientId;
    private final String topicPrefix;

    private final MqttClient client;
    	
    private final HashMap<String, String> states = new HashMap<>();
    private final HashMap<String, String> powers = new HashMap<>();

    public HubMqttController(String broker, String clientId, String topicPrefix) throws Exception {
        // broker should be using ssl:// instead of tcp:// uri.
        this.broker = broker;
        this.clientId = clientId;
        this.topicPrefix = topicPrefix;
        this.client = new MqttClient(broker, clientId, new MemoryPersistence());
    }

    public void start() throws Exception {
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

        opt.setCleanSession(true);
        client.connect(opt);
        
        client.subscribe(topicPrefix+"/update/#", this::handleUpdate);
        logger.info("MqttCtl {}: {} connected", clientId, broker);
    }

    public void close() throws Exception {
        client.disconnect();
        logger.info("MqttCtl {}: disconnected", clientId);
    }

    synchronized public void publishAction(String plugName, String action) {
        String topic = topicPrefix+"/action/"+plugName+"/"+action;
        try
        {
            MqttMessage msg = new MqttMessage();
            logger.info("MqttCtl: publishing action: {} {}", topic, msg.toString());
            client.publish(topic, msg);

            String updateTopic = String.format("%s/update/%s/#", topicPrefix, plugName);
            client.subscribe(updateTopic);
            logger.info("Subscribed to update topic for plug {}", plugName);
        }
        catch (Exception e)
        {
            logger.error("MqttCtl {}: {} fail to publish: Error {}", clientId, topic, e);
        }
    }

    synchronized public String getState(String plugName) {
        return states.get(plugName);
    }

    synchronized public String getPower(String plugName) {
        return powers.get(plugName);
    }

    synchronized public Map<String, String> getStates() {
        return new TreeMap<>(states);
    }

    synchronized public Map<String, String> getPowers() {
        return new TreeMap<>(powers);
    }

    synchronized public List<String> getNames() {
        return new ArrayList<String>(states.keySet());
    }

    synchronized public HashMap<String, String> getPlug(String plugName) {
        HashMap<String, String> ret = new HashMap<>();
        ret.put("name", plugName);
        ret.put("state", states.get(plugName));
        ret.put("power", powers.get(plugName));
        return ret;
    }

    synchronized public List<Plug> getPlugs() {
        List<Plug> plugs = new ArrayList<>();
        List<String> allPlugNames = this.getNames();

        for (int i = 0; i < allPlugNames.size(); i++){
            String name = allPlugNames.get(i);
            String state = this.getState(name);
            String power = this.getPower(name);

            Plug plug = new Plug(name, state, power);
            plugs.add(plug);
        }
        return plugs;
    }

    synchronized protected void handleUpdate(String topic, MqttMessage msg) {
        logger.debug("MqttCtl {}: {} {}", clientId, topic, msg);

        String[] nameUpdate = topic.substring(topicPrefix.length()+1).split("/");
        if ((nameUpdate.length != 3) || !nameUpdate[0].equals("update")) {
            return; // ignore unknown format
        }

        String plugName = nameUpdate[1];
        switch(nameUpdate[2]) {
            case "state":
                String state = msg.toString();
                states.put(plugName, state);
                logger.info("Plug {} updated state to {}", plugName, state);
                break;
            case "power":
                String power = msg.toString();
                powers.put(plugName, power);
                logger.info("Plug {} updated power to {}", plugName, power);
                break;
            default:
                return;
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
    private static final Logger logger = LoggerFactory.getLogger(HubMqttController.class);
}