package org.haolinxu.iot.module07;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class TempResourceHandler extends CoapResource{

	private static final Logger _Logger = Logger.getLogger(TempResourceHandler.class.getName());
	
	/**
	 * Default constructor.
	 */
	public TempResourceHandler() {
		super("temp",true); // name of the handler, enable
	}
	
	public TempResourceHandler(String name) {
		super(name);
	}
	
	@Override
	public void handleGET(CoapExchange ce) {
		
		// read 'temp' from file, and return to client
				File file = new File("C:\\DATA\\MyTempFile.txt");
				FileInputStream fis = null;
				
				try {
					fis = new FileInputStream(file);
					int bytes = fis.available();
					byte[] data = new byte[bytes];
					System.out.println(data);
					int count = fis.read(data);
					if(count > 0) {
						ce.respond(ResponseCode.VALID, "GET temp: " + new String(data));
					}

				}catch(Exception e) {
					e.printStackTrace();
				}
		//String resmsg = "Response to: " + super.getName();
		//ce.respond(ResponseCode.VALID,resmsg);
		System.out.println("Received response from the client.");
		
		_Logger.info(ce.getRequestCode().toString() + ": " + ce.getRequestText());
	}
	
	@Override
	public void handlePOST(CoapExchange ce) {
		
		String resmsg = "Response to: " + super.getName();
		ce.respond(ResponseCode.VALID,resmsg);
		
		System.out.println("Received GET response from the client.");
		//ce.accept();
		
		_Logger.info(ce.getRequestCode().toString() + ": " + ce.getRequestText());
	}
	
	@Override
	public void handleDELETE(CoapExchange ce) {
		String resmsg = "Response to: " + super.getName();
		ce.respond(ResponseCode.VALID,resmsg);
		System.out.println("Received DELETE response from the client.");
		delete();
		
		_Logger.info(ce.getRequestCode().toString() + ": " + ce.getRequestText());
	}
	
	@Override
	public void handlePUT(CoapExchange ce) {
		String resmsg = "Response to: " + super.getName();
		ce.respond(ResponseCode.VALID,resmsg);
		System.out.println("Received PUT response from the client.");
		changed();
		
		_Logger.info(ce.getRequestCode().toString() + ": " + ce.getRequestText());
	}
}
