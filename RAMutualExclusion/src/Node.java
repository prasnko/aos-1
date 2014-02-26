import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Node {
	private int nodeId;
	private int clientTimeStamp;
	private int serverTimeStamp;
	private int csEntryCount;
	private int minDelay;
	private int maxDelay;
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	private Map<Integer,String[]> nodes = null;
	private static final Object rqLock = new Object();
	private static final int maxClientsCount = 10;
	private Map<Integer,Integer> requestQueue;
		
	public Node()
	{
		clientTimeStamp = 0;
		serverTimeStamp = 0;
		nodes = new HashMap<Integer,String[]>();
		requestQueue = new HashMap<Integer,Integer>();
		readConfigFile();
	}
	
	public Map<Integer,Integer>  getRequestQueue()
	{
		synchronized(rqLock)
		{
			return requestQueue;
		}
	}
	
	public int getNodeId()
	{
		return nodeId;
	}
	
	public int getMinDelay()
	{
		return minDelay;
	}
	
	public int getMaxDelay()
	{
		return maxDelay;
	}
	
	public void setMinDelay(int _minDelay)
	{
		minDelay = _minDelay;
	}
	
	public void setMaxDelay(int _maxDelay)
	{
		maxDelay = _maxDelay;
	}
	
	public void setNodeId(int _nodeId)
	{
		nodeId = _nodeId;
	}
	
	/*public int getTimeStamp()
	{
		return timeStamp;
	}*/
	
	public ServerSocket getServerSocket()
	{
		return serverSocket;
	}
	
	public Socket getClientSocket()
	{
		return clientSocket;		
	}
	
	
	public String[] getNodeById(int id)
	{
		return nodes.get(id);
	}
	
	public Map<Integer,String[]> getNodes()
	{
		return nodes;
	}
	

	public void readConfigFile()
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("config.ini"));
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		//	this.initialize(attributes, numberOfInstances);		
		String line = null;
		try {
			line = br.readLine();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int i = 0;
		while(line != null){
				
				line = line.trim();
				line = line.split("#")[0];
				//int[] newInstance = new int[attributes];			
				String[] words = line.split("\\s+");
				String[] value = {words[1],words[2]};
				nodes.put(Integer.parseInt(words[0]),value);				
				try {
					line = br.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
	}
	
	public void startServer()
	{
		String message = null;
		try
		{
			//Create a server socket at port 5000
			ServerSocket serverSock = new ServerSocket(Integer.parseInt(this.getNodes().get(this.getNodeId())[1]));
			serverTimeStamp++;
			//Server goes into a permanent loop accepting connections from clients			
			while(true)
			{
				//Listens for a connection to be made to this socket and accepts it
				//The method blocks until a connection is made
				Socket sock = serverSock.accept();
				serverTimeStamp++;
				BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				message = reader.readLine();
				serverTimeStamp++;
				String messageParts[] = message.split(":");
				System.out.println("Client says:" + message);
				boolean sendReply = false;
				if(messageParts[2]=="request")
				{
					Integer requestTime = null;
					Integer nodeTime = null;
					getRequestQueue().put(Integer.parseInt(messageParts[1]),Integer.parseInt(messageParts[0]));
					for(Integer key:getRequestQueue().keySet())
					{
						if(key == Integer.parseInt(messageParts[1]))
						 requestTime = getRequestQueue().get(key);
						else if(key == this.nodeId)
						 nodeTime = getRequestQueue().get(key);
					}
					if(requestTime<nodeTime)
						sendReply = true;
					else if(requestTime==nodeTime)
					{
						if(Integer.parseInt(messageParts[1])<this.nodeId)
							sendReply = true;
					}
					if(sendReply)
					{
						serverTimeStamp++;
						PrintWriter writer = null;
						try {
							writer = new PrintWriter(sock.getOutputStream());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						writer.println(serverTimeStamp+":"+this.getNodeId()+":granted");
						writer.close();
					}
				}
				else if(messageParts[2]=="enterCS")
				{	
					try {
					    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("dataCollect.txt", true)));
					    out.println(message+"\t Entering");
					    out.println(message+"\t Leaving");
					    out.close();
					    serverTimeStamp++;
					} catch (IOException e) {
					    //exception handling left as an exercise for the reader
					}
				}
				sock.close();
			}
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void enterCriticalSection()
	{
		csEntryCount++;
		try {
			Socket clientSocket = new Socket(this.getNodes().get(0)[0],Integer.parseInt(this.getNodes().get(0)[1]));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clientTimeStamp++;
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(clientSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.println(clientTimeStamp+":"+this.getNodeId()+":enterCS");
		writer.close();
		try {
			clientSocket.close();
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
	
	public void startClient()
	{
		clientTimeStamp++;
		for(int i = 0;i< 40;i++)
		{
			if(csEntryCount >= 20 && i % 2==0 )
    		{
    			setMinDelay(200);
    			setMaxDelay(500);
    		}
    		else
    		{
    			setMinDelay(10);
    			setMaxDelay(100);
    		}
			 double delay = minDelay + (int)(Math.random() * ((maxDelay - minDelay) + 1));
			 try {
				Thread.sleep((long) delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int granted = 0;
			for(int j = 0;j < maxClientsCount; j++)
			{	
				int index= (j+(this.getNodeId()))%maxClientsCount;
				String message;
				try
				{
					//Create a client socket and connect to server at 127.0.0.1 port 5000
					Socket clientSocket = new Socket(this.getNodes().get(index)[0],Integer.parseInt(this.getNodes().get(index)[1]));
					clientTimeStamp++;
					PrintWriter writer = null;
					try {
						writer = new PrintWriter(clientSocket.getOutputStream());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					getRequestQueue().put(this.nodeId,clientTimeStamp);
					writer.println(clientTimeStamp+":"+this.getNodeId()+":request");
					writer.close();
					try {
						clientSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					clientTimeStamp++;
					//Read messages from server. Input stream are in bytes. They are converted to characters by InputStreamReader
					//Characters from the InputStreamReader are converted to buffered characters by BufferedReader
					BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					//The method readLine is blocked until a message is received 
					message = reader.readLine();
					clientTimeStamp++;
					String messageParts[] = message.split(":");
					System.out.println("Server says:" + message);
					if(messageParts[2]=="granted")
						granted++;
					clientTimeStamp = max(Integer.parseInt(messageParts[1]),clientTimeStamp);
					reader.close();
				}
				catch(IOException ex)
				{
					ex.printStackTrace();
				}
			}
			if(granted==maxClientsCount-1)
			{
				enterCriticalSection();
			}
			clientTimeStamp++;
		}
	}
	
	public static void main(String args[])
	{
		Node node = new Node();
		InetAddress inet = null;
		try {
			inet = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Map.Entry<Integer, String[]> entry : node.getNodes().entrySet()) {
		   if(entry.getValue()[1]==inet.getHostAddress())
		   {
			   node.setNodeId(entry.getKey());
		   }
		}
		
	}
}

