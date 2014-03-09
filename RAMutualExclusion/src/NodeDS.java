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
	private int clientTimeStamp;
	private int finishedNodes;
	private int totalMessages;
	private int messagesPerRound[] = new int[40];
	private double timePerRound[] = new double[40];
	private double startTime;
	private double endTime;
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	private Map<Integer,String[]> nodes = null;
	boolean requestGranted[];
	private static final Object tmLock = new Object();
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
	private static final Object tprLock = new Object();
	private static final Object mprLock = new Object();
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
		totalMessages = 0;
		//serverTimeStamp = 0;
		clientTimeStamp = 0;
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
	
	public int getTotalMessages()
	{
		synchronized(tmLock)
		{
			return totalMessages;
		}
	}
	
	public void setTotalMessages(int _totalMessages)
	{
		synchronized(tmLock)
		{
			totalMessages = _totalMessages;
		}
	}
	
	public Object getGRFLock()
	{
		return grfLock;
	}
	
	public int[] getMessagesPerRound()
	{
		synchronized(mprLock)
		{
			return messagesPerRound;
		}
	}
	
	public void setMessagesPerRound(int index,int value)
	{
		synchronized(mprLock)
		{
			messagesPerRound[index] = value;
		}
	}
	
	public double[] getTimePerRound()
	{
		synchronized(tprLock)
		{
			return timePerRound;
		}
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
	
	public int max(int x, int y)
	{
		if(x>y)
			return x;
		else
			return y;
	}
	
	public void increaseTimeStamp()
	{
		clientTimeStamp = this.getTimeStamp();
		clientTimeStamp++;
		this.setTimeStamp(clientTimeStamp);
	}
	
	public void enterCriticalSection()
	{
		
		increaseTimeStamp();
	
		PrintWriter writer = null;
		try {
			if(this.getNodeId()%2==0)
			 {
				clientSocket = new Socket(this.getNodes().get(2)[0],Integer.parseInt(this.getNodes().get(2)[1]));
				System.out.println("Connecting to Server on host: "+this.getNodes().get(2)[0]+" port: "+Integer.parseInt(this.getNodes().get(2)[1]));
			 }
			else
			{
				clientSocket = new Socket(this.getNodes().get(1)[0],Integer.parseInt(this.getNodes().get(1)[1]));
				System.out.println("Connecting to Server on host: "+this.getNodes().get(1)[0]+" port: "+Integer.parseInt(this.getNodes().get(1)[1]));
			}
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
		
		try {
			writer = new PrintWriter(clientSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		increaseTimeStamp();
		System.out.println("Client timestamp: Message "+clientTimeStamp+":"+this.getNodeId()+":enterCS");
		writer.println(clientTimeStamp+":"+this.getNodeId()+":enterCS");
		
		writer.close();
		
		increaseTimeStamp();
		int cseCount = this.getCSEntryCount();	
		cseCount++;
		this.setCSEntryCount(cseCount);
		System.out.println("Client timestamp: cse count:"+cseCount);
		
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
   public void grantRequests(int round)
   {
	   Integer requestTime = null;
	   Integer nodeTime = null;
	   System.out.println("Request queue size: "+this.getRequestQueue().size());
	   if(this.getRequestQueue().size()>0)
	   {
			for(Integer key:this.getRequestQueue().keySet())
			{	
				if(key == this.getNodeId())
				{
					increaseTimeStamp();
					System.out.println("Client timestamp: "+this.getTimeStamp()+" This Node Time: "+nodeTime);
					nodeTime = this.getRequestQueue().get(key);
					
				}
			}
			if(nodeTime!=null)
			{
				for(Integer key:this.getRequestQueue().keySet())
				{	
					System.out.println("Request Node: "+key+" Time: "+this.getRequestQueue().get(key));
					if(nodeTime>this.getRequestQueue().get(key))
					{
						increaseTimeStamp();
						System.out.println("Client timestamp: "+getTimeStamp()+" Adding"+key+"to grant request queue");
						this.getGrantRequestQueue().add(key);
					}
					else if(nodeTime==this.getRequestQueue().get(key))
					{
						if(key<this.getNodeId())
						{
							increaseTimeStamp();
							System.out.println("Client timestamp: "+getTimeStamp()+" Adding"+key+"to grant request queue");
							this.getGrantRequestQueue().add(key);
						}
					}
				}
			}
			else
			{
				for(Integer key:this.getRequestQueue().keySet())
				{
					increaseTimeStamp();
					System.out.println("Client timestamp: "+getTimeStamp()+" Adding"+key+"to grant request queue");
					this.getGrantRequestQueue().add(key);
				}
			}
			if(this.getGrantRequestQueue().size() > 0)
			{
				System.out.println("Client timestamp: "+getTimeStamp()+" Granted Request queue size: "+this.getGrantRequestQueue().size());
				while(this.getGrantRequestQueue().size() > 0)
				{
					increaseTimeStamp();
					int grantedRequest = (int) this.getGrantRequestQueue().remove();
					
					increaseTimeStamp();
					this.getRequestQueue().remove(grantedRequest);
					
					increaseTimeStamp();
					try {
						 clientSocket = new Socket((this.getNodes().get(grantedRequest)[0]),Integer.parseInt(this.getNodes().get(grantedRequest)[1]));
					} catch (NumberFormatException | IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//node.setRequestGranted(grantedRequest,true);
					//sock = serverSock.accept();
					//System.out.println("Send Reply.");
					System.out.println("Client timestamp: "+getTimeStamp()+" Request granted to server: "+grantedRequest);
					PrintWriter writer = null;
					try {
						writer = new PrintWriter(clientSocket.getOutputStream());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					increaseTimeStamp();
					writer.println(getTimeStamp()+":"+this.getNodeId()+":granted");
					int mpr = this.getMessagesPerRound()[round];
					mpr++;
					this.setMessagesPerRound(round,mpr);
					writer.close();					
				}
			}
	   }
   }
	
	public void startClient()
	{
		int clientTimeStampJ;
		increaseTimeStamp();
		System.out.println("Client timestamp: "+clientTimeStamp + "Client Started");
		//int thisClientTs = clientTimeStamp;
		int maxTs = -1;
		int clientTs[] = new int[getMaxClientsCount()];
		for(int j = 0;j < getMaxClientsCount(); j++)
			setRequestGranted(j,false);
		for(int i = 0;i< 40;i++)
		{
			for(int j = 0;j < getMaxClientsCount(); j++)
			{
				clientTs[j] = (this.getTimeStamp());
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
			 boolean csEntered = false;
			for(int j = 1;j < getMaxClientsCount(); j++)
			{	
				clientTimeStampJ = clientTs[j]; 
				int index= (j+(this.getNodeId()))%getMaxClientsCount();
				if(!this.getRequestGranted(index))
				{	
					String message;
					try
					{
						//Create a client socket and connect to server at 127.0.0.1 port 5000
						Socket clientSocket = new Socket(this.getNodes().get(index)[0],Integer.parseInt(this.getNodes().get(index)[1]));
						System.out.println("Connecting to Server on host: "+this.getNodes().get(index)[0]+" port: "+Integer.parseInt(this.getNodes().get(index)[1]));
						
						PrintWriter writer = null;
						try {
							writer = new PrintWriter(clientSocket.getOutputStream());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//
						clientTimeStampJ++;
						if(this.getTimeStamp()<clientTimeStampJ)
						 this.setTimeStamp(clientTimeStampJ);
						
						if(!getRequestQueue().containsKey(this.getNodeId()))
						{
							getRequestQueue().put(this.getNodeId(),clientTimeStampJ);
							System.out.println("Client timestamp: "+clientTimeStampJ+" Placing Node "+this.getNodeId()+" in it's own request queue");
						}
						
						clientTimeStampJ++;
						System.out.println("Client timestamp: "+clientTimeStampJ+" Node "+this.getNodeId()+" sending request to node "+index);
						if(this.getTimeStamp()<clientTimeStampJ)
						this.setTimeStamp(clientTimeStampJ);
						//System.out.println("Client timestamp: cse count:"+cseCount);
						startTime = System.nanoTime();
						writer.println(clientTimeStampJ+":"+this.getNodeId()+":request:"+i);
						int mpr = this.getMessagesPerRound()[i];
						mpr++;
						this.setMessagesPerRound(i,mpr);
						clientTimeStampJ++;
						if(this.getTimeStamp()<clientTimeStampJ)
							this.setTimeStamp(clientTimeStampJ);
						System.out.println("Client timestamp: "+clientTimeStampJ+" Node "+this.getNodeId()+" setRequestingCS: true");
						this.setRequestingCS(true);
						writer.close();
					}
					catch(IOException ex)
					{
						ex.printStackTrace();
					}
					
				}
				else
				{
					//this.setRequestGranted(j,true);
			    	//int clientTimeStamp = this.getTimeStamp();
			    	clientTimeStampJ ++;
			    	if(this.getTimeStamp()<clientTimeStampJ)
				     this.setTimeStamp(clientTimeStampJ );
					int granted = this.getGranted();
					granted+=1;
					this.setGranted(granted);
				    System.out.println("Client timestamp: "+clientTimeStampJ+" Nodes granting CS: "+this.getGranted()+" Max client count: "+(this.getMaxClientsCount()-1));
				    
				}
				clientTs[j] = (clientTimeStampJ);
				
			}
			while(true)
			{
				if(this.getGranted()==this.getMaxClientsCount()-1)
				{
					increaseTimeStamp();
				    System.out.println("Client timestamp: "+this.getTimeStamp() +" setExecutingCS: true");
					this.setExecutingCS(true);
					
					increaseTimeStamp();
				    System.out.println("Client timestamp: "+this.getTimeStamp() +" Entering critical section");
					this.enterCriticalSection();
					endTime = System.nanoTime();
					int mpr = this.getMessagesPerRound()[i];
					mpr++;
					this.setMessagesPerRound(i,mpr);
					increaseTimeStamp();
				    System.out.println("Client timestamp: "+this.getTimeStamp() +" setExecutingCS: false");
					this.setExecutingCS(false);
					
					increaseTimeStamp();
					System.out.println("Client timestamp: "+this.getTimeStamp()+" setGranted: 0");
				    this.setGranted(0);
				    
				    increaseTimeStamp();
				    System.out.println("Server timestamp: "+this.getTimeStamp()+" Request queue before removal: "+this.getRequestQueue());
				    this.getRequestQueue().remove(this.getNodeId());
				    System.out.println("Element to be removed: "+this.getNodeId());
					System.out.println("Client timestamp: "+this.getTimeStamp()+" Request queue after removal: "+this.getRequestQueue());
					
					increaseTimeStamp();
				    System.out.println("Client timestamp: "+this.getTimeStamp()+" Granting deffered requests");
				    synchronized(this.getGRFLock())
					{
				    	this.grantRequests(i);
					}
				    break;
				    //csEntered = true;}
			}
		 }
			this.getTimePerRound()[i]= ((endTime-startTime)/1e9);
		}
		
		System.out.println("Connecting to Server on host: "+this.getNodes().get(0)[0]+" port: "+Integer.parseInt(this.getNodes().get(0)[1]));
		increaseTimeStamp();
		System.out.println("Client timestamp: "+this.getTimeStamp()+" Socket connected by client: "+this.getNodeId());
		
		Socket clientSocket = null;
		try {
			clientSocket = new Socket(this.getNodes().get(0)[0],Integer.parseInt(this.getNodes().get(0)[1]));
		} catch (NumberFormatException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(clientSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		System.out.println("Client timestamp: "+this.getTimeStamp()+" Node "+this.getNodeId()+" has finished computation");
		increaseTimeStamp();
		int sum = 0;
		int min= Integer.MAX_VALUE;
		int max= Integer.MIN_VALUE;
		for(int i=0;i<40;i++)
		{
			sum += getMessagesPerRound()[i];
			if(min>getMessagesPerRound()[i])
				min = getMessagesPerRound()[i];
			if(max<getMessagesPerRound()[i])
				max = getMessagesPerRound()[i];
		}
		
		setTotalMessages(sum+1);
		writer.println(this.getTimeStamp()+":"+this.getNodeId()+":finishComputation:"+getTotalMessages());
		writer.close();
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter("Statistics"+this.getNodeId()+".txt", true)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.println("Max messages: "+max+"and min messages: "+min+"\n");
		for(int i=0;i<40;i++)
		{
			out.println("Round "+i+" Total Messages exchanged: "+getMessagesPerRound()[i]+"\n");
			out.println("Round "+i+" Total Time taken: "+getTimePerRound()[i]+"\n");
		}
	    out.close();
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

