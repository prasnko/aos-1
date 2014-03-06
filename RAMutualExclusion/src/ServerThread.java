import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class ServerThread implements Runnable {

  private NodeDS node = null;
  private int serverTimeStamp = -1;
  private ServerSocket serverSock; 
  private Socket clientSocket = null;
   public ServerThread(Object parameter) {
       // store parameter for later user
	   node = (NodeDS)parameter;
		try {
			serverSock = new ServerSocket(Integer.parseInt(node.getNodes().get(node.getNodeId())[1]));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
   }
   
   public int max(int x, int y)
	{
		if(x>y)
			return x;
		else
			return y;
	}
   
   /*public void grantRequests()
   {
	   Integer requestTime = null;
	   Integer nodeTime = null;
		//requestTime = Integer.parseInt(messageParts[0]);
	   if(node.getSortedRequestQueue().size()>0)
	   {
			for(Integer key:node.getSortedRequestQueue().keySet())
			{	
				if(key == node.getNodeId())
				 nodeTime = node.getSortedRequestQueue().get(key);
			}
			if(nodeTime!=null)
			{
				for(Integer key:node.getSortedRequestQueue().keySet())
				{	
					if(nodeTime>node.getSortedRequestQueue().get(key))
					{
						node.getGrantRequestQueue().add(key);
						node.getSortedRequestQueue().remove(key);
						node.getRequestQueue().remove(key);
					}
					else if(nodeTime==node.getSortedRequestQueue().get(key))
					{
						if(key<node.getNodeId())
						{
							 node.getGrantRequestQueue().add(key);
							 node.getSortedRequestQueue().remove(key);
							 node.getRequestQueue().remove(key);
						}
					}
				}
			}
			else
			{
				for(Integer key:node.getSortedRequestQueue().keySet())
				{
					node.getGrantRequestQueue().add(key);
					node.getSortedRequestQueue().remove(key);
					node.getRequestQueue().remove(key);
				}
			}
			if(node.getGrantRequestQueue().size() > 0)
			{
				System.out.println("Server timestamp: "+serverTimeStamp+" Queue size: "+node.getGrantRequestQueue().size());
				while(node.getGrantRequestQueue().size() > 0)
				{
					int grantedRequest = (int) node.getGrantRequestQueue().remove();
					try {
						clientSocket = new Socket((node.getNodes().get(grantedRequest)[0]),Integer.parseInt(node.getNodes().get(grantedRequest)[1]));
					} catch (NumberFormatException | IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					node.setRequestGranted(grantedRequest,true);
					//sock = serverSock.accept();
					//System.out.println("Send Reply.");
					serverTimeStamp = node.getTimeStamp();
					serverTimeStamp++;
					node.setTimeStamp(serverTimeStamp);
					System.out.println("Server timestamp: "+serverTimeStamp+" Request granted to server: "+grantedRequest);
					PrintWriter writer = null;
					try {
						writer = new PrintWriter(clientSocket.getOutputStream());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					writer.println(serverTimeStamp+":"+node.getNodeId()+":granted");
					writer.close();
					serverTimeStamp = node.getTimeStamp();
					serverTimeStamp++;
					node.setTimeStamp(serverTimeStamp);
					System.out.println("Server timestamp: "+serverTimeStamp);
					serverTimeStamp = node.getTimeStamp();
					serverTimeStamp++;
					node.setTimeStamp(serverTimeStamp);
					System.out.println("Server timestamp: "+serverTimeStamp);
					
				}
			}
	   }
   }*/
   
   
   public void startServer()
	{
		String message = null;
		try
		{
			//Create a server socket at port 5000
			System.out.println("Server started on: "+Integer.parseInt(node.getNodes().get(node.getNodeId())[1]));
			serverTimeStamp = node.getTimeStamp();
			serverTimeStamp++;
			node.setTimeStamp(serverTimeStamp);
			System.out.println("Server timestamp: "+serverTimeStamp+" at node: "+node.getNodeId());
			//Server goes into a permanent loop accepting connections from clients			
			while(true)
			{
				if(node.getFinishedNodes()==(node.getMaxClientsCount()-1))
				{
					System.out.println("All nodes finished computation. Hahaha... :D");
					break;
				}
				//Listens for a connection to be made to this socket and accepts it
				//The method blocks until a connection is made
				Socket sock = serverSock.accept();
				serverTimeStamp = node.getTimeStamp();
				serverTimeStamp++;
				node.setTimeStamp(serverTimeStamp);
				System.out.println("Server timestamp: "+serverTimeStamp+" Node "+node.getNodeId()+" accepted connection");
				BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				message = reader.readLine();
				serverTimeStamp = node.getTimeStamp();
				serverTimeStamp++;
				node.setTimeStamp(serverTimeStamp);
				System.out.println("Server timestamp: "+serverTimeStamp+" Node "+node.getNodeId()+" read message from client");
				String messageParts[] = message.split(":");
				serverTimeStamp = node.getTimeStamp();
				serverTimeStamp = max(serverTimeStamp,Integer.parseInt(messageParts[0]));
				node.setTimeStamp(serverTimeStamp);
				System.out.println("Server timestamp: "+serverTimeStamp+" Client says: " + message);
				System.out.println("Server timestamp: "+serverTimeStamp+" max of message from client "+messageParts[1]+" and server "+node.getNodeId()+" timestamp");
				//boolean sendReply = false;
				//System.out.println("Message parts 2: "+messageParts[2]);
				if(messageParts[2].equals("request"))
				{
					node.setRequestGranted(Integer.parseInt(messageParts[1]),false);
					serverTimeStamp = node.getTimeStamp();
					serverTimeStamp++;
					node.setTimeStamp(serverTimeStamp);
					System.out.println("Server timestamp: "+serverTimeStamp+" Requesting CS: "+node.getRequestingCS());
					System.out.println("Server timestamp: "+serverTimeStamp+" Executing CS: "+node.getExecutingCS());
					node.getRequestQueue().put(Integer.parseInt(messageParts[1]),Integer.parseInt(messageParts[0]));
					/*synchronized(node)
					{
						node.getSortedRequestQueue().clear();
						node.getSortedRequestQueue().putAll(node.getRequestQueue());
					}*/
					serverTimeStamp = node.getTimeStamp();
					serverTimeStamp++;
					node.setTimeStamp(serverTimeStamp);
					System.out.println("Server timestamp: "+serverTimeStamp);
					if(!node.getRequestingCS() && !node.getExecutingCS())
					{
						node.grantRequests();
					}
					else if(!node.getExecutingCS())
					{
						
						//nodeTime = serverTimeStamp;
						/*if(requestTime<nodeTime)
							node.getGrantRequestQueue().add((messageParts[1]));
						else if(requestTime==nodeTime)
						{
							
								sendReply = true;
						}*/
						node.grantRequests();
					}
					//if(node.getGrantRequestQueue().size()==0)
					//System.out.println("Send Reply: "+sendReply);
					
				}
				else if(messageParts[2].equals("granted"))
				{
					int granted = node.getGranted();
					granted+=1;
					node.setGranted(granted);
					node.setRequestGranted(Integer.parseInt(messageParts[1]),true);
			    	serverTimeStamp = node.getTimeStamp();
				    serverTimeStamp++;
				    node.setTimeStamp(serverTimeStamp);
				    System.out.println("Server timestamp: "+serverTimeStamp+" Nodes granting CS: "+node.getGranted()+" Max client count: "+(node.getMaxClientsCount()-1));
					/*if(node.getGranted()==node.getMaxClientsCount()-1)
					{
						node.setExecutingCS(true);
						node.setEnterCS(true);
						//node.enterCriticalSection();
						while(true)
						{
							if(!node.getEnterCS())
							{
								break;
							}
						}
						node.setExecutingCS(false);
						serverTimeStamp = node.getTimeStamp();
					    serverTimeStamp++;
					    System.out.println("Server timestamp: "+serverTimeStamp);
					    node.setTimeStamp(serverTimeStamp);
						node.setGranted(0);
						serverTimeStamp = node.getTimeStamp();
					    serverTimeStamp++;
					    node.setTimeStamp(serverTimeStamp);
					    System.out.println("Server timestamp: "+serverTimeStamp);
					    System.out.println("Server timestamp: "+serverTimeStamp+" Request queue before removal: "+node.getRequestQueue());
					    node.getRequestQueue().remove(node.getNodeId());
					    System.out.println("Element to be removed: "+node.getNodeId());
						System.out.println("Server timestamp: "+serverTimeStamp+" Request queue after removal: "+node.getRequestQueue());
						serverTimeStamp = node.getTimeStamp();
					    serverTimeStamp++;
					    node.setTimeStamp(serverTimeStamp);
					    System.out.println("Server timestamp: "+serverTimeStamp+" Granting deffered requests");
						node.grantRequests();
					}*/
				}
				else if(messageParts[2].equals("enterCS"))
				{	
					try {
					    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("dataCollect.txt", true)));
					    out.println(messageParts[1]+"\t Entering \n");
					    serverTimeStamp = node.getTimeStamp();
					    serverTimeStamp++;
					    node.setTimeStamp(serverTimeStamp);
					    System.out.println("Server timestamp: "+serverTimeStamp+" Node: "+Integer.parseInt(messageParts[1])+" entered critical section.");
					    out.println(messageParts[1]+"\t Leaving \n");
					    out.close();
					} catch (IOException e) {
					    //exception handling left as an exercise for the reader
					}
				}
				else if(messageParts[2].equals("finishComputation"))
				{
					int finishComputation = node.getFinishedNodes();
					finishComputation+=1;
					node.setFinishedNodes(finishComputation);
				}
				sock.close();
			}
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}

   public void run() {
	   startServer();
   }
}
