import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class PaxosServerImplementation extends java.rmi.server.UnicastRemoteObject implements PaxosServerInterface {
	private static final long serialVersionUID = -3503888069129478963L;
	private HashMap<String, String> store;
	private long timestart;
	private String[] otherServers;
	private ArrayList<String[]> proposals;
	// private String node;
	private boolean isLeader;

	// Constructor
	public PaxosServerImplementation(HashMap<String, String> the_store, long the_time, String[] the_other_servers, /*String node,*/ int isLeader) throws RemoteException
	{
		super();
		store = the_store;
		timestart = the_time;
		otherServers = the_other_servers;
		proposals = new ArrayList<String[]>();
		// this.node = node;
		if(isLeader == 1) this.isLeader = true;
		else this.isLeader = false;
	}

	public String Put(String key, String value) throws RemoteException // synchronized means thread safety
	{
		String return_string = "";
		String[] proposal = {"put", key, value};

		// Create references to the remote objects through the RMI registry
		try
		{
			//PaxosServerInterface myInterface = (PaxosServerInterface)Naming.lookup("rmi://" + this.node + "/ThreadsService");
			PaxosServerInterface[] serverInterfaces = {
					(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[0] + "/ThreadsService"),
					(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[1] + "/ThreadsService"),
					(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[2] + "/ThreadsService"),
					(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[3] + "/ThreadsService")};

			// send proposal to each other server and for itself
			//myInterface.Prop2All(proposal);
			Prop2All(proposal);
			for(int i = 0; i < serverInterfaces.length; i++)
			{
				serverInterfaces[i].Prop2All(proposal);
			}
			synchronized(store)
			{
				return_string = "Put received \"" + store.containsKey(key) + "\" for Value \"" + value + "\" for Key \"" + key + "\"";
			}
			System.out.println(return_string + " at " + (System.currentTimeMillis()-timestart) + " milliseconds");
		}
		catch (MalformedURLException | NotBoundException e)
		{
			e.printStackTrace();
			return "Replicated server error, see accessed server error printout";
		}
		return return_string;
	}

	public String Get(String key) throws RemoteException
	{
		String return_string = "Value for Key \"" + key + "\" not found in Key/Value store";
		synchronized(store)
		{
			if (store.containsKey(key))
			{
				return_string = "Value for Key \"" + key + "\" is \"" + store.get(key) + "\"";
				System.out.println(return_string + " retrieved at " + (System.currentTimeMillis()-timestart) + " milliseconds");
			}
			else
			{
				System.out.println(return_string + " at " + (System.currentTimeMillis()-timestart) + " milliseconds");
			}
		}
		return return_string;
	}

	public String Delete(String key) throws RemoteException
	{
		String return_string;
		String[] proposal = {"delete",key};
		// Create references to the remote objects through the RMI registry
		try
		{
			//PaxosServerInterface myInterface = (PaxosServerInterface)Naming.lookup("rmi://" + this.node + "/ThreadsService");
			PaxosServerInterface[] serverInterfaces = {
					(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[0] + "/ThreadsService"),
					(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[1] + "/ThreadsService"),
					(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[2] + "/ThreadsService"),
					(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[3] + "/ThreadsService")};

			// send proposal to each other server and for itself
			//myInterface.Prop2All(proposal);
			Prop2All(proposal);
			for(int i = 0; i < serverInterfaces.length; i++)
			{
				serverInterfaces[i].Prop2All(proposal);
			}
			synchronized(store)
			{
				return_string = "Delete received \"" + store.containsKey(key) + "\" for Key \"" + key + "\"";
			}
			System.out.println(return_string + " at " + (System.currentTimeMillis()-timestart) + " milliseconds");
		}
		catch (MalformedURLException | NotBoundException e)
		{
			e.printStackTrace();
			return "Replicated server error, see accessed server error printout";
		}
		return return_string;
	}

	public String Prop2All(String[] proposal) throws RemoteException {
		proposals.add(proposal);
		if(isLeader){
			try
			{
				// PaxosServerInterface myInterface = (PaxosServerInterface)Naming.lookup("rmi://" + this.node + "/ThreadsService");
				PaxosServerInterface[] serverInterfaces = {
						(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[0] + "/ThreadsService"),
						(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[1] + "/ThreadsService"),
						(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[2] + "/ThreadsService"),
						(PaxosServerInterface)Naming.lookup("rmi://" + otherServers[3] + "/ThreadsService")};

				int accepts = 0;
				String result;
				// result = myInterface.Accept(proposal);
				result = Accept(proposal);
				if(result.equals("accepted")) accepts++;

				for (int i = 0; i < serverInterfaces.length; i++) {
					result = serverInterfaces[i].Accept(proposal);
					System.out.println("Proposal " + result + " by server " + otherServers[i] + " at " + (System.currentTimeMillis()-timestart) + " milliseconds");

					if(result.equalsIgnoreCase("accepted")) accepts++;
				}

				if(accepts > (serverInterfaces.length + 1) / 2) {
					// myInterface.Learn(proposal);
					Learn(proposal);
					for (int i = 0; i < serverInterfaces.length; i++) {
						result = serverInterfaces[i].Learn(proposal);
					}
				}
			}
			catch (MalformedURLException | NotBoundException e)
			{
				e.printStackTrace();
				return "Problem looking up for other RMI server ThreadsService";
			}
		}
		return null;
	}

	public String Accept(String[] proposal) throws RemoteException {
		/*
		Random rand = new Random();
		int random_number = rand.nextInt(10) + 1; // generate a random # from 1-10
		if (random_number <= 3) { // 30% chance of failure
			System.out.println("Randomized System Failure at " + (System.currentTimeMillis()-timestart) + " milliseconds");
			try {
			    //thread to sleep for the specified number of milliseconds
			    Thread.sleep(3000);
			} catch ( java.lang.InterruptedException ie) {
			    System.out.println("Interrupted Exception " + ie);
			}
			return null;
		}
		 */
		String return_string = "rejected";
		String operation = proposal[0];
		String key = proposal[1];
		String value = "";
		if(proposal.length == 3){
			value = proposal[2];
		}
		synchronized(store)
		{
			if (operation.equals("put") || store.containsKey(key))
			{
				return_string = "accepted";
			}
		}
		System.out.println(return_string.toUpperCase() + " (" + key + ", " + value + ")" + " at " + (System.currentTimeMillis()-timestart) + " milliseconds");
		return return_string;
	}

	public String Learn(String[] proposal) throws RemoteException {
		boolean success = false;
		System.out.println(proposal[0] + " " + proposal[1] + " " + proposal[2]);
		String operation = proposal[0];
		String key = proposal[1];
		String value = "";
		if(proposal.length == 3){
			value = proposal[2];
		}
		synchronized(store)
		{
			if (operation == "put")
			{
				System.out.println(key + " " + value);
				store.put(key, value); // place key/value into the Map
				success = (store.containsKey(key) && store.get(key) == value);
				System.out.println(store.containsKey(key));
			}
			else if (operation == "delete")
			{
				store.remove(key); // delete key/value from the Map
				success = !store.containsKey(key);
			}
		}
		for (int i = 0; i < proposals.size(); i++) {
			if(proposals.get(i).equals(proposal)){
				proposals.remove(i);
				break;
			}
		}
		System.out.println("Learned " + operation + "(" + key + ", " + value + ") " + success + " at " + (System.currentTimeMillis()-timestart) + " milliseconds");
		return "learned";
	}
}





































