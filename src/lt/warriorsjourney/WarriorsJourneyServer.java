/**
 * Warriors Journey dedicated server.
 * Built to maintain data from facebook/player progress/scores
 * 
 * Upcoming features:
 * 
 * Data/Score saving to MySQL. --
 * Data/Score fetching to Apps. --
 * Online play support. --
 * 
 * @author Ideo
 * @version 0.1
 */

package lt.warriorsjourney;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import lt.warriorsjourney.client.WarriorsJourneyClient;
import lt.warriorsjourney.handlers.mysql.MysqlHandler;

public class WarriorsJourneyServer 
{
	private final static Logger log = Logger.getLogger(WarriorsJourneyServer.class.getName());
	
	private static MysqlHandler mysqlData;
	private static Config config;
	
	@SuppressWarnings("unused")
	private static WarriorsJourneyServer _instance;
	
	WarriorsJourneyClient[] clientThreads; 
	
	ServerSocket sSocket = null; 
	
	public static void main(String[] args)
	{
		initLogger(log);
		_instance = new WarriorsJourneyServer();
	}
	
	public WarriorsJourneyServer()
	{
		log.log(Level.FINE,"=== Warriors Journey Server Loading... ===");
				
		config = new Config();
		
		clientThreads = new WarriorsJourneyClient[Config.MAX_CONNECTIONS];
						
		log.log(Level.FINE,"=== Loading Handlers ===");
				
		mysqlData = new MysqlHandler();
				
		log.log(Level.FINE,"=== Handlers ONLINE ===");
		
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
				
		log.log(Level.FINE,"=== Warriors Journey Server Loading DONE ===");
		
		log.log(Level.FINE,"=== Starting Listening on Port:"+Config.LISTEN_PORT+" ===");
		
		try
		{
			sSocket = new ServerSocket(Config.LISTEN_PORT);
			log.log(Level.FINER,"Listening start");
			
			while(true)
			{
				for(int i = 0; i < Config.MAX_CONNECTIONS;i++)
				{
					if(clientThreads[i] == null)
					{
						Socket socket = sSocket.accept();
						
						clientThreads[i] = new WarriorsJourneyClient(socket, this,i);
						clientThreads[i].start();
						
						log.log(Level.FINE, "New Connection! At thread: "+i);
						break;
					}
					if(i == Config.MAX_CONNECTIONS)
						log.log(Level.SEVERE,"Server FULL check max connection number!");
				}
			}			
		}
		catch (IOException e)
		{
			log.log(Level.SEVERE, "Unable to start socket Listening please check the problem!",e.toString());
			e.printStackTrace();
		}
		
	}
	
	public void nullifyThread(int id) throws NullPointerException
	{
		clientThreads[id] = null;
	}
	
	class ShutdownHook extends Thread
	{
	    public void run()
	    {
	    	log.log(Level.FINE,"=== Warriors Journey Server ShuttingDown STARTED ! ===");
	    	
	    	File configFile = new File(Config.MAIN_CONFIG_FILE_NAME);
	    	
	    	//save first!
	    	//close ports
	    	config.writeConfigs(configFile, false);
	    	//save dataMYSQL
	        mysqlData.closeConnection();
	    	log.log(Level.FINE,"=== Warriors Journey Server ShutDown Good Night ! ===");
	    }
	}
		
	@SuppressWarnings("deprecation")
	public static void initLogger(Logger logg)
	{
		FileHandler fh = null;
		ConsoleHandler handler = new ConsoleHandler();
		String directory = "./logs";
		
		Date date = new Date();
		File log = new File(directory);
		
		if(!log.exists())
			log.mkdir();
		
		try 
		{
			fh = new FileHandler(directory+"/WarriorsJourneyServer-"+date.getDay()+".log", false);
		}
		catch (SecurityException | IOException e) 
		{
			e.printStackTrace();
		}
		  
		fh.setFormatter(new SimpleFormatter());
		handler.setFormatter(new SimpleFormatter());
		
		fh.setLevel(Level.FINE);
		logg.addHandler(fh);
		
		handler.setLevel(Level.FINE);
		logg.addHandler(handler);
		
		logg.setLevel(Level.FINE);
	}
	
	/**
	 * Used by the client threads to get a reference to the
	 * MySQL Handler used by the main server.
	 * 
	 * @return used MySQL handler
	 */
	
	public MysqlHandler getMysqlHandler()
	{
		return mysqlData;
	}
	
}
