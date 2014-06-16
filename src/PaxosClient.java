import java.rmi.Naming;			// Import rmi naming - so you can lookup remote objects
import java.rmi.RemoteException;	// Import exceptions
import java.net.MalformedURLException;	
import java.rmi.NotBoundException;	

public class PaxosClient
{
	public static void main(String[] args)
	{
		long timestart = System.currentTimeMillis();

		try
		{
			System.out.println("Client Start at " + (System.currentTimeMillis()-timestart) + " milliseconds");

			String serverIP = "";
			int port = 0;
			String operation = "";
			String key = "";

			if (args.length >= 4) // If there are the minimum # of command line arguments
			{
				serverIP = args[0];
				port = Integer.parseInt(args[1]);
				operation = args[2];
				key = args[3];
			}
			else
			{
				System.out.println("Insufficient command line arguments at " + (System.currentTimeMillis()-timestart) + " milliseconds");
				System.exit(0);
			}

			String value = "";
			if (args.length > 4) // A fifth command line argument in case of a put operation
			{
				value = args[4];
			}

			// Create the reference to the remote object through the rmiregistry
			System.out.println("rmi://" + serverIP + ":" + port + "/ThreadsService");
			PaxosServerInterface server = (PaxosServerInterface)Naming.lookup("rmi://" + serverIP + ":" + port + "/ThreadsService");
			String returnedString;
			
			switch (operation.toLowerCase())
			{
			case "put":
				returnedString = server.Put(key, value);
				System.out.println("Received from server: \"" + returnedString + "\" at " + (System.currentTimeMillis()-timestart) + " milliseconds");
				break;
			case "get":
				returnedString = server.Get(key);
				System.out.println("Received from server: \"" + returnedString + "\" at " + (System.currentTimeMillis()-timestart) + " milliseconds");
				break;
			case "delete":
				returnedString = server.Delete(key);
				System.out.println("Received from server: \"" + returnedString + "\" at " + (System.currentTimeMillis()-timestart) + " milliseconds");
				break;
			default:
				System.out.println("Unknown command from client: \"" + operation + " " + key + " " + value + "\" at " + (System.currentTimeMillis()-timestart) + " milliseconds");
				break;
			}
			System.out.println("Client Close at " + (System.currentTimeMillis()-timestart) + " milliseconds");
		}

		// Catch the exceptions - rubbish URL, Remote exception or Not bound exception.
		catch (MalformedURLException murle)
		{
			System.out.println("MalformedURLException"+murle);
		}
		catch (RemoteException re)
		{
			System.out.println("RemoteException"+re);
		}
		catch (NotBoundException nbe)
		{
			System.out.println("NotBoundException"+nbe);
		}
	}
}

