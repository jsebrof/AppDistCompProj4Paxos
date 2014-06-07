import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;		//Import naming classes to bind to rmiregistry
import java.rmi.RemoteException;
import java.util.HashMap;

public class PaxosServer
{
	public static void main(String args[]) throws RemoteException, UnknownHostException
	{
		long timestart = System.currentTimeMillis();
		String[] otherServers = {"","","",""};
		int port;
		String node = java.net.InetAddress.getLocalHost().getHostName();
		int isLeader = 0;
		if (args.length >= 5) // If there are the minimum # of command line arguments
		{
			otherServers[0] = args[0];
			otherServers[1] = args[1];
			otherServers[2] = args[2];
			otherServers[3] = args[3];
			isLeader = Integer.parseInt(args[4]);
		}
		else
		{
			System.out.println("Insufficient command line arguments at " + (System.currentTimeMillis()-timestart) + " milliseconds");
			System.exit(0); // don't continue if we don't have the necessary server addresses
		}
		if (args.length >= 6) // If there is a port selection command line argument
		{
			port = Integer.parseInt(args[5]); // Port # to listen for messages at
		}
		else
		{
			System.out.println("Server will use default port 1099 at " + (System.currentTimeMillis()-timestart) + " milliseconds");
			port = 1099; // default port for the server
		}
		HashMap<String, String> store = new HashMap<String, String>(); // Map for storing key/value pairs
		System.out.println("Server Start at " + (System.currentTimeMillis()-timestart) + " milliseconds");
		LocateRegistry.createRegistry(port); // start the rmiregistry at the specificed port
		try
		{
			PaxosServerInterface c = new PaxosServerImplementation(store, timestart, otherServers, node, isLeader); // use a new thread to provide each service
       		Naming.rebind("rmi://localhost:" + port + "/ThreadsService", c); // bind the service to the machine the server program is being run on
		} 
		catch (Exception e)
		{
			System.out.println("Server Error: " + e);
			e.printStackTrace();
		}
	}
}
