package org.haolinxu.iot.module08;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MqttClientConnector implements MqttCallback {
	
	// static
	private static final Logger _Logger = Logger.getLogger(MqttClientConnector.class.getName());
	
	// params
	private String _protocol = "tcp";
	private String _host     = "test.mosquitto.org";
	private int    _port	 = 1883;
	
	private String _userName 	  = null;
	private String _password 	  = null;
	private String _pemFileName   = null;
	private Boolean _isSecureConn = false;
	
	
	
	private String _clientID;
    private String _brokerAddr;
    
    private MqttClient _mqttClient;

    // constructors
    /**
     * Default.
     * 
     */
	public MqttClientConnector() {
		// use defaults
		this(null, false);
	}

	/**
	 * Constructor.
	 * 
	 * @param host       The name of the host.
	 * @param isSecure   Ignored now.
	 */
	public MqttClientConnector(String host, boolean isSecure) {
		super();
		
		// NOTE: 'isSecure' ignored for now
		
		if(host != null && host.trim().length() > 0 ) {
			_host = host;
		}
		
		// NOTE: URL does not have a protocol handler for "tcp",
		// so we need to construct the URL manually
		_clientID = MqttClient.generateClientId();
		
		_Logger.info("Using client ID for broker conn: " + _clientID);
		
		_brokerAddr = _protocol + "://" + _host + ":" + _port;
		
		_Logger.info("Using URL for broker conn: " + _brokerAddr);
		
		MemoryPersistence persistence = new MemoryPersistence();
		
		try {
			_mqttClient = new MqttClient(_brokerAddr, _clientID, persistence);
		} catch(MqttException e) {
			
			_Logger.log(Level.SEVERE, "Failed to create a client. ", e);
			
	    }
	}
	
	// 
	public MqttClientConnector(String host, String userName, String password, String pemFileName) {
		super();
		
		if (host !=null && host.trim().length() >0) {
			_host = host;
		}
		if (userName !=null && userName.trim().length() >0) {
			_userName = userName;
		}
		if (password !=null && password.trim().length() >0) {
			_password = password;
		}
		if (pemFileName !=null && pemFileName.trim().length() >0) {
			_pemFileName = pemFileName;
		}

		if (pemFileName != null) {
			File file = new File(pemFileName);
			if (file.exists()) {
				_protocol   	= "ssl";
				_port			= 8883;
				_pemFileName 	= pemFileName;
				_isSecureConn 	= true;
				
				_Logger.info("PEM file valid. Using secure connection: " + _pemFileName);
			}else {
				_Logger.info("Pem file invalid. Using insecure connection: " + pemFileName);
			}
		}
		
		_clientID 	= MqttClient.generateClientId();
		_brokerAddr = _protocol + "://" + _host + ":" + _port;
		_Logger.info("Client ID: " + _clientID + " . Broker Address: " + _brokerAddr);
		
		_Logger.info("Using URL for broker coon: " + _brokerAddr) ;
		
		MemoryPersistence persistence = new MemoryPersistence();
		
		try {
			_mqttClient = new MqttClient(_brokerAddr, _clientID, persistence);
		} catch(MqttException e) {
			
			_Logger.log(Level.SEVERE, "Failed to create a client. ", e);
			
		}
	}
	
	// public methods
	/**
	 * Connect to the MQTT Broker.
	 */
	public void connect() {
		if (_mqttClient != null) {
			
			try {
				_Logger.info("Connecting to broker: " + _brokerAddr);
				
				//_mqttClient = new MqttClient(_brokerAddr, _clientID, persistence);
				
				MqttConnectOptions connOpts = new MqttConnectOptions();
				
				//Sets whether the client and server should remember state across restarts and reconnects.
				connOpts.setCleanSession(true); 
				
				if (_userName != null) {
					connOpts.setUserName(_userName);
				}
				if (_password !=null) {
					char[] pw = _password.toCharArray();
					connOpts.setPassword(pw);
				}

				// if we are using a secure connection, we need to do:
				if (_isSecureConn) {
					initSecureConnection(connOpts);
				}
				
				_mqttClient.setCallback(this);
				_mqttClient.connect(connOpts);
				
				_Logger.info("Connected to broker: " + _brokerAddr);
			
			}catch(MqttException e){
				
				_Logger.log(Level.SEVERE, "Failed to connect to broker: " + _brokerAddr, e);
				
			}
		}
		
	}
	
	/**
	 * Initialize the connection configuration.
	 * @param connOpts
	 */
	private void initSecureConnection(MqttConnectOptions connOpts) {
		try {
			_Logger.info("Configuring TLS...");

			SSLContext sslContext = SSLContext.getInstance("SSL");
			KeyStore keyStore = readCertificate();
			
			TrustManagerFactory trustManagerFactory = 
					TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			
			trustManagerFactory.init(keyStore);
			sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
			
			connOpts.setSocketFactory(sslContext.getSocketFactory());
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to initialize secure MQTT connection.", e);
		}
	}

	/**
	 * Read a certificate from the system. Store and return the certificate.
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 */
	private KeyStore readCertificate() throws KeyStoreException, 
											  NoSuchAlgorithmException, 
											  CertificateException, 
											  IOException {
		KeyStore 			ks  = KeyStore.getInstance(KeyStore.getDefaultType());
		FileInputStream 	fis = new FileInputStream(_pemFileName);
		BufferedInputStream bis = new BufferedInputStream(fis);
		CertificateFactory  cf  = CertificateFactory.getInstance("X.509");
		ks.load(null);
		while (bis.available() > 0) {
			Certificate cert = cf.generateCertificate(bis);
			ks.setCertificateEntry("adk_store" + bis.available(), cert);
		}
		return ks;
	}


	/**
	 * Disconnect from the broker.
	 */
	public void disconnect() {
		try {
			
			_mqttClient.disconnect();
			_Logger.info("Disconnected form brokder: " + _brokerAddr);
			
		}catch(Exception e) {
			_Logger.log(Level.SEVERE, "Failed to disconnect from broker: " + _brokerAddr, e);
		}
		
	}
	
	/**
	 * Pulishes the given payload to broker directly to topic 'topic'.
	 * 
	 * @param topic
	 * @param qosLevel
	 * @param payload
	 */
	public boolean publishMessage(String topic, int qosLevel, byte[] payload) {
		boolean success = false;
		
		try {
			_Logger.info("Publishing message to topic: " + topic);
			
			MqttMessage message = new MqttMessage(payload);
			
			message.setQos(qosLevel);
			message.setRetained(true);

			
			_mqttClient.publish(topic, payload, qosLevel, true);

			
			_Logger.info("Message " + message.getId() + " has been published.");
			
			success = true;
			
		} catch(MqttException e) {
			_Logger.log(Level.SEVERE, "Failed to publish MQTT message: " + e.getMessage());
		}
		
		return success;
	}
	
	public boolean subscribeToAll() {
		try {
			_mqttClient.subscribe("$SYS/#");
			return true;
		} catch (MqttException e) {
			_Logger.log(Level.WARNING, "Failed to subscribe to all topics.", e);
		}
		return false;
	}

	public boolean subscribeToTopic(String topicName) {
		try {
			_mqttClient.subscribe(topicName);
			return true;
		} catch (MqttException e) {
			_Logger.log(Level.WARNING, "Failed to subscribe to all topics.", e);
		}
		return false;
	}
	
	public void connectionLost(Throwable t) {
		// TODO: now what? --We need to reconnect.
		_Logger.log(Level.WARNING, "Connection to broker lost. Will retry soon.", t);

		if (!_mqttClient.isConnected()) {
			try {
				_mqttClient.connect();
			} catch (MqttSecurityException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO: what else should you do here? --I think just logging the delivery information is ok. 
		try {
			_Logger.info("Delivery complete: " + token.getMessageId() + " - " + token.getResponse() + " - "
					+ token.getMessage());
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to retrieve message from token.", e);
		}
		
	}
	
	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		// TODO: should you analyze the message or just log it? --Analyze it.
		_Logger.info("Message arrived: " + topic + ", " + msg.getId() + ". Message: " + msg.toString());


	}



}

