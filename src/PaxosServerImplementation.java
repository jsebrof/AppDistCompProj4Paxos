import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class PaxosServerImplementation extends java.rmi.server.UnicastRemoteObject implements PaxosServerInterface {
	private static final long serialVersionUID = -3503888069129478963L;
	private HashMap<String, String> store;
	private long timestart;
	private String[] otherServers;
	private Queue<String[]> updates;

	// Constructor
	public PaxosServerImplementation(HashMap<String, String> the_store, long the_time, String[] the_other_servers) throws RemoteException
	{
		super();
		store = the_store;
		timestart = the_time;
		otherServers = the_other_servers;
		updates = new LinkedList<String[]>();
	}

	public synchronized String Put(String key, String value) throws RemoteException // synchronized means thread safety
	{
		String return_string;
		if (store.containsKey(key))
		{
			return_string = "Replaced Value \"" + store.containsKey(key) + "\" for Value \"" + value + "\" for Key \"" + key + "\"";
		}
		else
		{
			return_string = "Placed Key \"" + key + "\" and Value \"" + value + "\" into the Key/Value store";
		}
		store.put(key, value); // place key/value into the Map

		// Create references to the remote objects through the rmiregistries
		try
		{
			PaxosServerInterface[] serverInterfaces = {
					(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[0] + "/ThreadsService"),
					(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[1] + "/ThreadsService"),
					(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[2] + "/ThreadsService"),
					(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[3] + "/ThreadsService")};

			// send update to each other replicated server and print acknowledgment
			for(int i = 0; i < serverInterfaces.length; i++)
			{
				System.out.println("PUT Update " + serverInterfaces[i].UpdatePut(key, value) + " by server " + otherServers[i] + " at " + (System.currentTimeMillis()-timestart) + " milliseconds");
			}

			// send commit to each other replicated server and print acknowledgment
			for(int i = 0; i < serverInterfaces.length; i++)
			{
				System.out.println("PUT Update " + serverInterfaces[i].CommitUpdate() + " by server " + otherServers[i] + " at " + (System.currentTimeMillis()-timestart) + " milliseconds");
			}
		}
		catch (MalformedURLException | NotBoundException e)
		{
			e.printStackTrace();
			return "Replicated server error, see accessed server error printout";
		}

		System.out.println(return_string + " across all servers at " + (System.currentTimeMillis()-timestart) + " milliseconds");
		return return_string;
	}

	public synchronized String Get(String key) throws RemoteException
	{
		String return_string = "Value for Key \"" + key + "\" not found in Key/Value store";
		if (store.containsKey(key))
		{
			return_string = "Value for Key \"" + key + "\" is \"" + store.get(key) + "\"";
			System.out.println(return_string + " retrieved at " + (System.currentTimeMillis()-timestart) + " milliseconds");
		}
		else
		{
			System.out.println(return_string + " at " + (System.currentTimeMillis()-timestart) + " milliseconds");
		}
		return return_string;
	}

	public synchronized String Delete(String key) throws RemoteException
	{
		String return_string;
		if (store.containsKey(key))
		{
			return_string = "Key \"" + key + "\" Value \"" + store.get(key) + "\" deleted";
			store.remove(key); // delete key/value from the Map

			// Create references to the remote objects through the rmiregistries
			try
			{
				PaxosServerInterface[] serverInterfaces = {
						(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[0] + "/ThreadsService"),
						(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[1] + "/ThreadsService"),
						(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[2] + "/ThreadsService"),
						(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[3] + "/ThreadsService")};

				// send update to each other replicated server and print acknowledgment
				for(int i = 0; i < serverInterfaces.length; i++)
				{
					System.out.println("DELETE Update " + serverInterfaces[i].UpdateDelete(key) + " by server " + otherServers[i] + " at " + (System.currentTimeMillis()-timestart) + " milliseconds");
				}

				// send commit to each other replicated server and print acknowledgment
				for(int i = 0; i < serverInterfaces.length; i++)
				{
					System.out.println("DELETE Update " + serverInterfaces[i].CommitUpdate() + " by server " + otherServers[i] + " at " + (System.currentTimeMillis()-timestart) + " milliseconds");
				}
			}
			catch (MalformedURLException | NotBoundException e)
			{
				e.printStackTrace();
				return "Replicated server error, see accessed server error printout";
			}
		}
		else
		{
			return_string = "No value found in store for Key \"" + key + "\"";
			System.out.println(return_string + " at " + (System.currentTimeMillis()-timestart) + " milliseconds");
		}
		System.out.println(return_string + " across all servers at " + (System.currentTimeMillis()-timestart) + " milliseconds");
		return return_string;
	}

	public synchronized String UpdatePut(String key, String value) throws RemoteException
	{
		String[] PUTupdate = {"put",key,value};
		updates.add(PUTupdate); // place the update into this queue for storage until told to commit the update
		return "acknowledged";
	}

	public synchronized String UpdateDelete(String key) throws RemoteException
	{
		String[] DELETEupdate = {"delete",key};
		updates.add(DELETEupdate); // place the update into this queue for storage until told to commit the update
		return "acknowledged";
	}

	public synchronized String CommitUpdate() throws RemoteException
	{
		String[] update = updates.remove();
		if (update[0] == "put")
		{
			store.put(update[1], update[2]); // place key/value into the Map
		}
		else if (update[0] == "delete")
		{
			store.remove(update[1]); // delete key/value from the Map
		}
		return "committed";
	}
}





































