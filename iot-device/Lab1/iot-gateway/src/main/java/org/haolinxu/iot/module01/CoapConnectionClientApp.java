package org.haolinxu.iot.module01;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.internal.ClientComms;

public class CoapConnectionClientApp {
	
	private static final Logger _Logger = 
			Logger.getLogger(CoapConnectionClientApp.class.getName());

	private static CoapConnectionClientApp _App = null;
	
	public CoapConnectionClientApp() {

		super();

	}

	public static void main(String[] args) {
		
		try {
			
			_App = new CoapConnectionClientApp();
			_App.start();
			
		}catch(Exception e){
			_Logger.log(Level.SEVERE,"Bad stuff happened.", e);
			
			System.exit(0);
			
		}

	}
	
	public void start() {
		
		CoapConnectionClient clientConn = new CoapConnectionClient();
		clientConn.runTests();
		
		
	}

}






