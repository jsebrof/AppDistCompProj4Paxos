public interface PaxosServerInterface extends java.rmi.Remote 
{	
	public String Put(String key, String value) throws java.rmi.RemoteException;
	public String Get(String key) throws java.rmi.RemoteException;
	public String Delete(String key) throws java.rmi.RemoteException;
	public String Prop2All(String[] proposal) throws java.rmi.RemoteException;
	public String Accept(String[] proposal) throws java.rmi.RemoteException;
	public String Learn(String[] proposal) throws java.rmi.RemoteException;
}

