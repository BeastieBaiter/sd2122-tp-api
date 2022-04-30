package tp1.server.soap;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.xml.ws.Endpoint;
import tp1.discovery.Discovery;

public class SoapDirectoryServer {

	public static final int PORT = 8080;
	public static final String SERVICE_NAME = "directory";
	public static String SERVER_BASE_URI = "http://%s:%s/soap";
	public static Discovery discovery;

	private static Logger Log = Logger.getLogger(SoapUsersServer.class.getName());

	public static void main(String[] args) throws Exception {
		
		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

		Log.setLevel(Level.INFO);

		String ip = InetAddress.getLocalHost().getHostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

		Endpoint.publish(serverURI.replace(ip, "0.0.0.0"), new SoapUsersWebService());

		Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE_NAME, serverURI));
		
		discovery = new Discovery(SERVICE_NAME,serverURI);
		discovery.start();
	}
}
