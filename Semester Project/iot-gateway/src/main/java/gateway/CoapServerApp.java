package gateway;

import java.util.logging.Logger;

public class CoapServerApp
{
	
	private static final Logger _Logger = Logger.getLogger(CoapServerApp.class.getName());
	private static CoapServerApp _App;
	
    public static void main( String[] args )
    {
        _App = new CoapServerApp();
        try {
        	_Logger.info("Coap Server starts.");
        	_App.start();
        }catch(Exception e) {
        	e.printStackTrace();
        }
    }
 
    
    // constructors
    /**
     * 
     */
    public CoapServerApp() {
    	super();
    }
    
    // public methods
    /**
     * 
     */
    public void start() {
    	CoapServerConnector serverConn = new CoapServerConnector();
    	serverConn.start();

    }
}