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
  int totalNodesMessages[];
  private ServerSocket serverSock; 
  private Socket clientSocket = null;
   public ServerThread(Object parameter) {
       // store parameter for later user
	   node = (NodeDS)parameter;
	   totalNodesMessages = new int[node.getMaxClientsCount()];
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
      
   public void increaseTimeStamp()
   {
	   serverTimeStamp = node.getTimeStamp();
	   serverTimeStamp++;
	   node.setTimeStamp(serverTimeStamp);
   }
   
   public void startServer()
	{
		String message = null;
		try
		{
			//Create a server socket at port 5000
			System.out.println("Server started on: "+Integer.parseInt(node.getNodes().get(node.getNodeId())[1]));
			
			increaseTimeStamp();
			System.out.println("Server timestamp: "+node.getTimeStamp()+" at node: "+node.getNodeId());
			//Server goes into a permanent loop accepting connections from clients			
			while(true)
			{
				if(node.getFinishedNodes()==(node.getMaxClientsCount()-1))
				{
					System.out.println("All nodes finished computation.");
					int sum = 0;
					for(int i=0;i<node.getMaxClientsCount();i++)
						sum += totalNodesMessages[i];
					System.out.println("Total Messages exchanged: " +sum);
					break;
				}
				//Listens for a connection to be made to this socket and accepts it
				//The method blocks until a connection is made
				Socket sock = serverSock.accept();
				
				increaseTimeStamp();
				System.out.println("Server timestamp: "+node.getTimeStamp()+" Node "+node.getNodeId()+" accepted connection");
				BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				message = reader.readLine();
				
				increaseTimeStamp();
				System.out.println("Server timestamp: "+node.getTimeStamp()+" Node "+node.getNodeId()+" read message from client");
				String messageParts[] = message.split(":");
				//serverTimeStamp = node.getTimeStamp();
				serverTimeStamp = max(node.getTimeStamp(),Integer.parseInt(messageParts[0]));
				node.setTimeStamp(serverTimeStamp);
				System.out.println("Server timestamp: "+node.getTimeStamp()+" Client says: " + message);
				System.out.println("Server timestamp: "+node.getTimeStamp()+" max of message from client "+messageParts[1]+" and server "+node.getNodeId()+" timestamp");
				//boolean sendReply = false;
				//System.out.println("Message parts 2: "+messageParts[2]);
				if(messageParts[2].equals("request"))
				{
					increaseTimeStamp();
					node.setRequestGranted(Integer.parseInt(messageParts[1]),false);
					System.out.println("Server timestamp: "+node.getTimeStamp()+" Requesting CS: "+node.getRequestingCS());
					System.out.println("Server timestamp: "+node.getTimeStamp()+" Executing CS: "+node.getExecutingCS());
					node.getRequestQueue().put(Integer.parseInt(messageParts[1]),Integer.parseInt(messageParts[0]));
					/*synchronized(node)
					{
						node.getSortedRequestQueue().clear();
						node.getSortedRequestQueue().putAll(node.getRequestQueue());
					}*/
					increaseTimeStamp();
					System.out.println("Server timestamp: "+node.getTimeStamp());
					if(!node.getRequestingCS() && !node.getExecutingCS())
					{
						synchronized(node.getGRFLock())
						{
							node.grantRequests(Integer.parseInt(messageParts[3]));
						}
					}
					else if(!node.getExecutingCS())
					{
						synchronized(node.getGRFLock())
						{
							node.grantRequests(Integer.parseInt(messageParts[3]));
						}
					}
					
				}
				else if(messageParts[2].equals("granted"))
				{
					int granted = node.getGranted();
					granted+=1;
					node.setGranted(granted);
					node.setRequestGranted(Integer.parseInt(messageParts[1]),true);
					increaseTimeStamp();
				    System.out.println("Server timestamp: "+node.getTimeStamp()+" Nodes granting CS: "+node.getGranted()+" Max client count: "+(node.getMaxClientsCount()-1));
				
				}
				else if(messageParts[2].equals("enterCS"))
				{	
					PrintWriter out;
					try {
						if(node.getNodeId()%2==0)
					     out = new PrintWriter(new BufferedWriter(new FileWriter("dataCollected.txt", true)));
						else
						 out = new PrintWriter(new BufferedWriter(new FileWriter("dataCollected2.txt", true)));	
					    out.println(messageParts[1]+"\t Entering \n");
					    serverTimeStamp = node.getTimeStamp();
					    serverTimeStamp++;
					    node.setTimeStamp(serverTimeStamp);
					    System.out.println("Server timestamp: "+node.getTimeStamp()+" Node: "+Integer.parseInt(messageParts[1])+" entered critical section.");
					    out.println(messageParts[1]+"\t Leaving \n");
					    out.close();
					} catch (IOException e) {
					    //exception handling left as an exercise for the reader
					}
				}
				else if(messageParts[2].equals("finishComputation"))
				{
					int finishComputation = node.getFinishedNodes();
					totalNodesMessages[finishComputation] = Integer.parseInt(messageParts[3]);
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
