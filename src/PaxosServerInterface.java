public interface PaxosServerInterface extends java.rmi.Remote 
{	
	public String Put(String key, String value) throws java.rmi.RemoteException;
	public String Get(String key) throws java.rmi.RemoteException;
	public String Delete(String key) throws java.rmi.RemoteException;
	public String UpdatePut(String key, String value) throws java.rmi.RemoteException;
	public String UpdateDelete(String key) throws java.rmi.RemoteException;
	public String CommitUpdate() throws java.rmi.RemoteException;
}

