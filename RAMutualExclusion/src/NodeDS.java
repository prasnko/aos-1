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
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;


public class NodeDS {
	private int nodeId;
	private int timeStamp;
//	private int serverTimeStamp;
	private int csEntryCount;
	private int minDelay;
	private int maxDelay;
	private int granted;
	private int finishedNodes;
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	private Map<Integer,String[]> nodes = null;
	boolean requestGranted[];
	private static final Object rqLock = new Object();
	private static final Object srqLock = new Object();
	private static final Object rgLock = new Object();
	private static final Object grqLock = new Object();
	private static final Object rqCsLock = new Object();
	private static final Object exCsLock = new Object();
	private static final Object ndLock = new Object();
	private static final Object tsLock = new Object();
	private static final Object ndsLock = new Object();
	private static final Object mcLock = new Object();
	private static final Object grLock = new Object();
	private final Object grfLock = new Object();
	private static final Object fnLock = new Object();
	private static final Object entCsLock = new Object();
	private static final Object cseCntLock = new Object();
	private int maxClientsCount;
	private Map<Integer,Integer> requestQueue;
	private boolean requestingCS;
	private boolean executingCS;
	private Queue<Integer> grantRequestQueue;
	private boolean enterCS;
	ValueComparator bvc;
    TreeMap<Integer, Integer> sortedRequestQueue;

	public NodeDS()
	{
		timeStamp = 0;
		granted = 0;
		finishedNodes = 0;
		//serverTimeStamp = 0;
		nodes = new HashMap<Integer,String[]>();
		requestQueue = new HashMap<Integer,Integer>();
		readConfig();
		requestGranted = new boolean[getMaxClientsCount()];
		setRequestingCS(false);
		setExecutingCS(false);
		grantRequestQueue = new LinkedList<Integer>();
		bvc =  new ValueComparator(requestQueue);
		sortedRequestQueue = new TreeMap<Integer,Integer>(bvc);
		enterCS = false;
	}
	
	public Object getGRFLock()
	{
		return grfLock;
	}
	public int getGranted()
	{
		synchronized(grLock)
		{
			return granted;
		}		
	}
	
	public void setGranted(int _granted)
	{
		synchronized(grLock)
		{
			granted = _granted;
		}
	}
	
	public int getFinishedNodes()
	{
		synchronized(fnLock)
		{
			return finishedNodes;
		}		
	}
	
	public void setFinishedNodes(int _finishedNodes)
	{
		synchronized(fnLock)
		{
			finishedNodes = _finishedNodes;
		}
	}
	
	public int getCSEntryCount()
	{
		synchronized(cseCntLock)
		{
			return csEntryCount;
		}		
	}
	
	public void setCSEntryCount(int _csEntryCount)
	{
		synchronized(cseCntLock)
		{
			csEntryCount = _csEntryCount;
		}
	}
	
	public Map<Integer,Integer>  getRequestQueue()
	{
		synchronized(rqLock)
		{
			return requestQueue;
		}
	}
	
	public TreeMap<Integer,Integer>  getSortedRequestQueue()
	{
		synchronized(srqLock)
		{
			return sortedRequestQueue;
		}
	}
	
	public Queue<Integer> getGrantRequestQueue()
	{
		synchronized(grqLock)
		{
			return grantRequestQueue;
		}
	}
	
	public boolean getRequestGranted(int index)
	{
		synchronized(rqLock)
		{
			return requestGranted[index];
		}
	}
	
	public void setRequestGranted(int index,boolean value)
	{
		synchronized(rqLock)
		{
			requestGranted[index] = value;
		}
	}
	
	public boolean getEnterCS()
	{
		synchronized(entCsLock)
		{
			return enterCS;
		}
	}
	
	public void setEnterCS(boolean _enterCS)
	{
		synchronized(entCsLock)
		{
			enterCS = _enterCS;
		}
	}
	
	public boolean getRequestingCS()
	{
		synchronized(rqCsLock)
		{
			return requestingCS;
		}
	}
	
	public boolean getExecutingCS()
	{
		synchronized(exCsLock)
		{
			return executingCS;
		}
	}
	
	public void setRequestingCS(boolean _requestingCS)
	{
		synchronized(rqCsLock)
		{
			requestingCS = _requestingCS;
		}
	}
	
	public void setExecutingCS(boolean _executingCS)
	{
		synchronized(exCsLock)
		{
			executingCS = _executingCS;
		}
	}
	public int getNodeId()
	{
		synchronized(ndLock)
		{
			return nodeId;
		}
	}
	
	public int getMaxClientsCount()
	{
		synchronized(ndLock)
		{
			return maxClientsCount;
		}
	}
	
	public void setMaxClientsCount(int _maxClientsCount)
	{
		synchronized(mcLock)
		{
			maxClientsCount = _maxClientsCount;
		}
	}

	public int getTimeStamp()
	{
		synchronized(tsLock)
		{
			return timeStamp;
		}
	}
	
	public void setTimeStamp(int _timeStamp)
	{
		synchronized(tsLock)
		{
			timeStamp = _timeStamp;
		}
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
		synchronized(ndLock)
		{
			nodeId = _nodeId;
		}
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
		synchronized(ndsLock)
		{
			return nodes.get(id);
		}
	}
	
	public Map<Integer,String[]> getNodes()
	{
		synchronized(ndsLock)
		{
			return nodes;
		}
	}
	

	public void readConfig()
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("config.txt"));
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
			System.out.println(line);
				line = line.trim();
				line = line.split("#")[0];
				//int[] newInstance = new int[attributes];			
				String[] words = line.split("\\s+");
				if(words.length>2)
				{
					String[] value = {words[1],words[2]};
					nodes.put(Integer.parseInt(words[0]),value);				
				}
				else
				{
					setMaxClientsCount(Integer.parseInt(words[0]));
					System.out.println("max client count: "+getMaxClientsCount());
				}
				try {
					line = br.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
	}
	
/*	public static void startServer()
	{
		String message = null;
		try
		{
			//Create a server socket at port 5000
			System.out.println("Server started on: "+Integer.parseInt(this.getNodes().get(this.getNodeId())[1]));
			ServerSocket serverSock = new ServerSocket(Integer.parseInt(this.getNodes().get(this.getNodeId())[1]));
			serverTimeStamp++;
			System.out.println("Server timestamp: "+serverTimeStamp);
			//Server goes into a permanent loop accepting connections from clients			
			while(true)
			{
				//Listens for a connection to be made to this socket and accepts it
				//The method blocks until a connection is made
				Socket sock = serverSock.accept();
				serverTimeStamp++;
				System.out.println("Server timestamp: "+serverTimeStamp);
				BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				message = reader.readLine();
				serverTimeStamp++;
				System.out.println("Server timestamp: "+serverTimeStamp);
				String messageParts[] = message.split(":");
				serverTimeStamp = max(serverTimeStamp,Integer.parseInt(messageParts[0]));
				System.out.println("Client says: " + message);
				System.out.println("Server timestamp: "+serverTimeStamp);
				boolean sendReply = false;
				if(messageParts[2]=="request")
				{
					if(!getRequestingCS() && !getExecutingCS())
					{
						sendReply = true;
					}
					else if(!getExecutingCS())
					{
						Integer requestTime = null;
						Integer nodeTime = null;
						getRequestQueue().put(Integer.parseInt(messageParts[1]),Integer.parseInt(messageParts[0]));
						serverTimeStamp++;
						System.out.println("Server timestamp: "+serverTimeStamp);
						requestTime = Integer.parseInt(messageParts[0]);
						for(Integer key:getRequestQueue().keySet())
						{	
							if(key == this.nodeId)
							 nodeTime = getRequestQueue().get(key);
						}
						if(requestTime<nodeTime)
							sendReply = true;
						else if(requestTime==nodeTime)
						{
							if(Integer.parseInt(messageParts[1])<this.nodeId)
								sendReply = true;
						}
					}
					if(sendReply)
					{
						serverTimeStamp++;
						System.out.println("Server timestamp: "+serverTimeStamp);
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
					    System.out.println("Server timestamp: "+serverTimeStamp);
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
*/	
	
	
	public int max(int x, int y)
	{
		if(x>y)
			return x;
		else
			return y;
	}
	
	public void enterCriticalSection()
	{
		int clientTimeStamp;
		clientTimeStamp = this.getTimeStamp();
		clientTimeStamp++;
		this.setTimeStamp(clientTimeStamp);
		System.out.println("Client timestamp: "+clientTimeStamp);
		PrintWriter writer = null;
		try {
			clientSocket = new Socket(this.getNodes().get(0)[0],Integer.parseInt(this.getNodes().get(0)[1]));
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
		System.out.println("Connecting to Server on host: "+this.getNodes().get(0)[0]+" port: "+Integer.parseInt(this.getNodes().get(0)[1]));
		try {
			writer = new PrintWriter(clientSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.println(clientTimeStamp+":"+this.getNodeId()+":enterCS");
		clientTimeStamp = this.getTimeStamp();
		clientTimeStamp++;
		this.setTimeStamp(clientTimeStamp);
		System.out.println("Client timestamp: "+clientTimeStamp);
		writer.close();
		int cseCount = this.getCSEntryCount();
		cseCount++;
		this.setCSEntryCount(cseCount);
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clientTimeStamp = this.getTimeStamp();
		clientTimeStamp++;
		this.setTimeStamp(clientTimeStamp);
		System.out.println("Client timestamp: "+clientTimeStamp);
	}
   public void grantRequests()
   {
	   int serverTimeStamp = this.getTimeStamp();
	   Integer requestTime = null;
	   Integer nodeTime = null;
		//requestTime = Integer.parseInt(messageParts[0]);
	   System.out.println("Request queue size: "+this.getRequestQueue().size());
	   if(this.getRequestQueue().size()>0)
	   {
			for(Integer key:this.getRequestQueue().keySet())
			{	
				if(key == this.getNodeId())
				{
					nodeTime = this.getRequestQueue().get(key);
					serverTimeStamp = this.getTimeStamp();
					serverTimeStamp++;
					this.setTimeStamp(serverTimeStamp);
					System.out.println("Server timestamp: "+serverTimeStamp+" This Node Time: "+nodeTime);
				}
			}
			if(nodeTime!=null)
			{
				for(Integer key:this.getRequestQueue().keySet())
				{	
					System.out.println("Request Node: "+key+" Time: "+this.getRequestQueue().get(key));
					if(nodeTime>this.getRequestQueue().get(key))
					{
						this.getGrantRequestQueue().add(key);
						//this.getRequestQueue().remove(key);
						serverTimeStamp = this.getTimeStamp();
						serverTimeStamp++;
						this.setTimeStamp(serverTimeStamp);
						System.out.println("Server timestamp: "+serverTimeStamp);
					}
					else if(nodeTime==this.getRequestQueue().get(key))
					{
						if(key<this.getNodeId())
						{
							this.getGrantRequestQueue().add(key);
							//this.getRequestQueue().remove(key);
							 serverTimeStamp = this.getTimeStamp();
						     serverTimeStamp++;
						     this.setTimeStamp(serverTimeStamp);
							 System.out.println("Server timestamp: "+serverTimeStamp);
						}
					}
				}
			}
			else
			{
				for(Integer key:this.getRequestQueue().keySet())
				{
					this.getGrantRequestQueue().add(key);
					//this.getRequestQueue().remove(key);
					serverTimeStamp = this.getTimeStamp();
				    serverTimeStamp++;
				    this.setTimeStamp(serverTimeStamp);
					System.out.println("Server timestamp: "+serverTimeStamp);
				}
			}
			if(this.getGrantRequestQueue().size() > 0)
			{
				System.out.println("Server timestamp: "+serverTimeStamp+" Granted Request queue size: "+this.getGrantRequestQueue().size());
				while(this.getGrantRequestQueue().size() > 0)
				{
					int grantedRequest = (int) this.getGrantRequestQueue().remove();
					this.getRequestQueue().remove(grantedRequest);
					try {
						clientSocket = new Socket((this.getNodes().get(grantedRequest)[0]),Integer.parseInt(this.getNodes().get(grantedRequest)[1]));
					} catch (NumberFormatException | IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//node.setRequestGranted(grantedRequest,true);
					//sock = serverSock.accept();
					//System.out.println("Send Reply.");
					serverTimeStamp = this.getTimeStamp();
					serverTimeStamp++;
					this.setTimeStamp(serverTimeStamp);
					System.out.println("Server timestamp: "+serverTimeStamp+" Request granted to server: "+grantedRequest);
					PrintWriter writer = null;
					try {
						writer = new PrintWriter(clientSocket.getOutputStream());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					writer.println(serverTimeStamp+":"+this.getNodeId()+":granted");
					writer.close();
					serverTimeStamp = this.getTimeStamp();
					serverTimeStamp++;
					this.setTimeStamp(serverTimeStamp);
					System.out.println("Server timestamp: "+serverTimeStamp);
					serverTimeStamp = this.getTimeStamp();
					serverTimeStamp++;
					this.setTimeStamp(serverTimeStamp);
					System.out.println("Server timestamp: "+serverTimeStamp);
					
				}
			}
	   }
   }
	
	public void startClient()
	{
		int clientTimeStamp = this.getTimeStamp();
		clientTimeStamp++;
		this.setTimeStamp(clientTimeStamp);
		System.out.println("Client timestamp: "+clientTimeStamp);
		//int thisClientTs = clientTimeStamp;
		int maxTs = -1;
		int clientTs[] = new int[getMaxClientsCount()];
		for(int j = 0;j < getMaxClientsCount(); j++)
			setRequestGranted(j,false);
		for(int i = 0;i< 40;i++)
		{
			for(int j = 0;j < getMaxClientsCount(); j++)
			{
				clientTs[j] = (clientTimeStamp);
			}
			if(getCSEntryCount() > 20 && this.getNodeId() % 2==0 )
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
			
			//clientTimeStamp++;
			//int reqTs = this.getTimeStamp();
			/*synchronized(this)
			{
				getSortedRequestQueue().clear();
				getSortedRequestQueue().putAll(getRequestQueue());
			}*/
			 boolean csEntered = false;
			for(int j = 1;j < getMaxClientsCount(); j++)
			{	
				clientTimeStamp = clientTs[j]; 
				int index= (j+(this.getNodeId()))%getMaxClientsCount();
				if(!this.getRequestGranted(index))
				{	
					String message;
					try
					{
						//Create a client socket and connect to server at 127.0.0.1 port 5000
						Socket clientSocket = new Socket(this.getNodes().get(index)[0],Integer.parseInt(this.getNodes().get(index)[1]));
						System.out.println("Connecting to Server on host: "+this.getNodes().get(index)[0]+" port: "+Integer.parseInt(this.getNodes().get(index)[1]));
						clientTimeStamp++;
						if(this.getTimeStamp()<clientTimeStamp)
							this.setTimeStamp(clientTimeStamp);
						System.out.println("Client timestamp: "+clientTimeStamp+" Socket connected by client: "+this.getNodeId());
						PrintWriter writer = null;
						try {
							writer = new PrintWriter(clientSocket.getOutputStream());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//
						clientTimeStamp++;
						if(this.getTimeStamp()<clientTimeStamp)
						 this.setTimeStamp(clientTimeStamp);
						//System.out.println("Client timestamp: "+clientTimeStamp+" Node "+index+" put in request queue of node "+this.getNodeId());
						if(!getRequestQueue().containsKey(this.getNodeId()))
							 getRequestQueue().put(this.getNodeId(),clientTimeStamp);
						writer.println(clientTimeStamp+":"+this.getNodeId()+":request");
						this.setRequestingCS(true);
						clientTimeStamp++;
						if(this.getTimeStamp()<clientTimeStamp)
							this.setTimeStamp(clientTimeStamp);
						System.out.println("Client timestamp: "+clientTimeStamp+" Request sent from node "+this.getNodeId()+" to node "+index);
						writer.close();
					}
					catch(IOException ex)
					{
						ex.printStackTrace();
					}
					
				}
				else
				{
					int granted = this.getGranted();
					granted+=1;
					this.setGranted(granted);
					//this.setRequestGranted(j,true);
			    	//int clientTimeStamp = this.getTimeStamp();
			    	clientTimeStamp ++;
			    	if(this.getTimeStamp()<clientTimeStamp)
				     this.setTimeStamp(clientTimeStamp );
				    System.out.println("Client timestamp: "+clientTimeStamp+" Nodes granting CS: "+this.getGranted()+" Max client count: "+(this.getMaxClientsCount()-1));
				    
				}
				clientTs[j] = (clientTimeStamp);
			}
			while(true)
			{
				if(this.getGranted()==this.getMaxClientsCount()-1)
				{
					this.setExecutingCS(true);
					this.enterCriticalSection();
					this.setExecutingCS(false);
					clientTimeStamp = this.getTimeStamp();
					clientTimeStamp++;
				    System.out.println("Server timestamp: "+clientTimeStamp);
				    if(this.getTimeStamp()<clientTimeStamp)
				     this.setTimeStamp(clientTimeStamp);
				    this.setGranted(0);
				    clientTimeStamp = this.getTimeStamp();
				    clientTimeStamp++;
				    if(this.getTimeStamp()<clientTimeStamp)
				     this.setTimeStamp(clientTimeStamp);
				    System.out.println("Server timestamp: "+clientTimeStamp);
				    System.out.println("Server timestamp: "+clientTimeStamp+" Request queue before removal: "+this.getRequestQueue());
				    this.getRequestQueue().remove(this.getNodeId());
				    System.out.println("Element to be removed: "+this.getNodeId());
					System.out.println("Server timestamp: "+clientTimeStamp+" Request queue after removal: "+this.getRequestQueue());
					clientTimeStamp = this.getTimeStamp();
					clientTimeStamp++;
					if(this.getTimeStamp()<clientTimeStamp)
				     this.setTimeStamp(clientTimeStamp);
				    System.out.println("Server timestamp: "+clientTimeStamp+" Granting deffered requests");
				    synchronized(this.getGRFLock())
					{
				    	this.grantRequests();
					}
				    break;
				    //csEntered = true;
				}
			}
			/*if(!csEntered)
			{
				while(true)
				{
					if(this.getEnterCS())
					{
						break;
					}
				}
				this.enterCriticalSection();
				this.setEnterCS(false);
			}*/
			/*for(int j = 1;j < getMaxClientsCount(); j++)
			{	
				clientTimeStamp = this.getTimeStamp(); 
				int index= (j+(this.getNodeId()))%getMaxClientsCount();
				if(!this.getRequestGranted(index))
				{	
					String message;
					try
					{
						clientSocket = new Socket(this.getNodes().get(index)[0],Integer.parseInt(this.getNodes().get(index)[1]));
						//Read messages from server. Input stream are in bytes. They are converted to characters by InputStreamReader
						//Characters from the InputStreamReader are converted to buffered characters by BufferedReader
						BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						//The method readLine is blocked until a message is received 
						message = reader.readLine();
						clientTimeStamp++;
						//if(this.getTimeStamp()<clientTimeStamp)
						this.setTimeStamp(clientTimeStamp);
						System.out.println("Client timestamp: "+clientTimeStamp+" Message read from server "+index+" to client "+this.getNodeId());
						String messageParts[] = message.split(":");
						System.out.println("Client timestamp: "+clientTimeStamp+" Server "+messageParts[1]+" says: " + message);
						if(messageParts[2].equals("granted"))
						{
							granted++;
							setRequestGranted(j,true);
						}
						clientTimeStamp = max(Integer.parseInt(messageParts[0]),clientTimeStamp);
						//if(this.getTimeStamp()<clientTimeStamp)
						this.setTimeStamp(clientTimeStamp);
						System.out.println("Client timestamp: "+clientTimeStamp);
						reader.close();
						clientSocket.close();
						//this.setRequestingCS(false);
					
					}
					catch(IOException ex)
					{
						ex.printStackTrace();
					}
					//clientTs[j] = (clientTimeStamp);
				}
			}*/
			/*for(int j = 0;j < getMaxClientsCount(); j++)
			{
				if(maxTs<clientTs[j])
					maxTs = clientTs[j];
			}
			clientTimeStamp = maxTs;
			if(this.getTimeStamp()<clientTimeStamp)*/
			//this.setTimeStamp(clientTimeStamp);
			/*if(granted==maxClientsCount-1)
			{
				this.setExecutingCS(true);
				enterCriticalSection();
				this.setExecutingCS(false);
			}
			clientTimeStamp++;
			this.setTimeStamp(clientTimeStamp);*/
			//System.out.println("Client timestamp: "+clientTimeStamp);
		}
		//this.setTimeStamp(clientTimeStamp);
		Socket clientSocket = null;
		try {
			clientSocket = new Socket(this.getNodes().get(0)[0],Integer.parseInt(this.getNodes().get(0)[1]));
		} catch (NumberFormatException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Connecting to Server on host: "+this.getNodes().get(0)[0]+" port: "+Integer.parseInt(this.getNodes().get(0)[1]));
		clientTimeStamp = this.getTimeStamp();
		clientTimeStamp+=1;
		this.setTimeStamp(clientTimeStamp);
		System.out.println("Client timestamp: "+clientTimeStamp+" Socket connected by client: "+this.getNodeId());
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(clientSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		System.out.println("Client timestamp: "+clientTimeStamp+" Node "+this.getNodeId()+" has finished computation");
		writer.println(clientTimeStamp+":"+this.getNodeId()+":finishComputation");
		clientTimeStamp++;
		this.setTimeStamp(clientTimeStamp);
		writer.close();
	}
	
	public static void main(String args[])
	{
		NodeDS node = new NodeDS();
		InetAddress inet = null;
		try {
			inet = InetAddress.getLocalHost();
			System.out.println("Host Address: "+inet.getCanonicalHostName());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Map.Entry<Integer, String[]> entry : node.getNodes().entrySet()) {
			System.out.println("Map value: "+entry.getValue()[0]);
		   if((entry.getValue()[0]).equals(String.valueOf(inet.getCanonicalHostName())))
		   {
			   node.setNodeId(entry.getKey());
			  System.out.println("nodeId: "+node.getNodeId());
		   }
		}
		System.out.println("Starting server at node: "+node.getNodeId());
		Runnable r = new ServerThread(node);
		new Thread(r).start();
		try {
			Thread.sleep(45000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Starting client at node: "+node.getNodeId());
		node.startClient();
	}
}

