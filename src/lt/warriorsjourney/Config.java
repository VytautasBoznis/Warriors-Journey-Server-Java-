package lt.warriorsjourney;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Load and holds all the configuration 
 * present in [WarriorsJourneyServer.properties] file at runtime.
 * 
 * @author Ideo
 */
public class Config
{
	private final static Logger log = Logger.getLogger(Config.class.getName());
	
	// === FILE CONFIGURATION ===
	public static String FOLDER_NAME = "./configs";
	public static String MAIN_CONFIG_FILE_NAME = "./configs/WarriorsJourneyServer.properties";
	
	
	// === MYSQL CONFIGURATION ===
	public static String MYSQL_HOST;
	public static String MYSQL_PORT;
	public static String MYSQL_USERNAME;
	public static String MYSQL_PASSWORD;
	public static String MYSQL_DATABASE;
	public static String MYSQL_TABLE_PREFIX;
	
	// === SERVER CONFIGURATION ===
	public static boolean SHOULD_INSTALL_DB;
	public static int LISTEN_PORT;
	public static int MAX_CONNECTIONS;
	
	public Config()
	{
		WarriorsJourneyServer.initLogger(log);
		loadConfigs();
	}
	
	/**
	 * Starts loading the files data forwards 
	 * request to the appropriate function.
	 */
	
	public void loadConfigs()
	{
		File configs = new File(FOLDER_NAME);
		
		if(!configs.exists())
		{
			log.log(Level.FINE,"Config directory not found creating config folder");
			configs.mkdir();
		}
		
		configs = new File(MAIN_CONFIG_FILE_NAME);
		
		if(!configs.exists())
		{
			log.log(Level.FINE,"No Configuration file found Creating...");
			
			try
			{
				configs.createNewFile();
				
				log.log(Level.FINE,"Configuration file created");
				
				writeConfigs(configs,true);
			} 
			catch (IOException e)
			{
				log.log(Level.SEVERE,"Error creating Configuration File",e.toString());
				e.printStackTrace();
			}
		}
		
		readConfigs(configs);
	}
	
	/**
	 * If the configuration file is not found
	 * this function writes default configuration from screech.
	 * 
	 * @param configs - the file in witch the configs will be written.
	 * @param newWrite - true if used to write the config file from scratch.
	 * 					 false if used to rewrite the config file from present params.
	 */
	
	public void writeConfigs(File configs,boolean newWrite)
	{
		log.log(Level.FINE,"Configuration write Start...");
		
		try
		{
			if(newWrite)
			{
				PrintWriter writer = new PrintWriter(configs);
			
				writer.println("# ===== Warriors Journey Server =====");
				writer.println("# ======== Created by Ideo ==========");
				writer.println("# ============= v0.1 ================");
				writer.println("#									 ");
				writer.println("#   ===== MySQL Configuration =====  ");
				writer.println("#                                    ");
				writer.println(" MysqlHostName = localhost");
				writer.println(" MysqlHostPort = 3306");
				writer.println(" MysqlUsername = root");
				writer.println(" MysqlPassword = ");
				writer.println(" MysqlDatabase = warriorsjourney");
				writer.println(" MysqlTablePrefix = wj_");
				writer.println("#									 ");
				writer.println("#	===== Server Configuration ===== ");
				writer.println("#									 ");
				writer.println("#Should the server try to install used databases ?");
				writer.println("#If true the create if not exists statements will be executed.");
				writer.println("ShouldInstallDatabases = true");
				writer.println("#									 ");
				writer.println("#On what port should the server start listening for connections?");
				writer.println("ListenPort = 12345");
				writer.println("#									");
				writer.println("#The max number of connections running simultaneosly.");
				writer.println("MaxNumberOfConnections = 100");
				writer.println("#									");
				
				writer.close();
			
				log.log(Level.FINE,"Configuration write Done");
			}
			else
			{
				log.log(Level.FINE,"Configuration Save started!");
				
				PrintWriter writer = new PrintWriter(configs);
				
				writer.println("# ===== Warriors Journey Server =====");
				writer.println("# ######### Created by Ideo #########");
				writer.println("# ############# v0.1 ################");
				writer.println("# ###################################");
				writer.println("#   ===== MySQL Configuration =====  ");
				writer.println("#                                    ");
				writer.println(" MysqlHostName = "+MYSQL_HOST);
				writer.println(" MysqlHostPort = "+MYSQL_PORT);
				writer.println(" MysqlUsername = "+MYSQL_USERNAME);
				writer.println(" MysqlPassword = "+MYSQL_PASSWORD);
				writer.println(" MysqlDatabase = "+MYSQL_DATABASE);
				writer.println(" MysqlTablePrefix = "+MYSQL_TABLE_PREFIX);
				writer.println("#									 ");
				writer.println("#	===== Server Configuration ===== ");
				writer.println("#									 ");
				writer.println("#Should the server try to install used databases ?");
				writer.println("#If true the create if not exists statements will be executed.");
				writer.println("ShouldInstallDatabases = "+SHOULD_INSTALL_DB);
				writer.println("#									 ");
				writer.println("#On what port should the server start listening for connections?");
				writer.println("ListenPort = "+LISTEN_PORT);
				writer.println("#									");
				writer.println("#The max number of connections running simultaneosly.");
				writer.println("#FOR NOW used only for authentication + data transfers.");
				writer.println("MaxNumberOfConnections = "+MAX_CONNECTIONS);
				writer.println("#									");
				
				writer.close();
			
				log.log(Level.FINE,"Configuration save Done");
			}
		}
		catch (IOException e)
		{
			log.log(Level.SEVERE,"Error while writing Configuration",e.toString());
			e.printStackTrace();
		}			
	}
	
	/**
	 * If the file is OK and present,
	 * parses data for usage later.
	 * 
	 * @param configs - the file to be parsed.
	 */
	
	public void readConfigs(File configs)
	{
		try
		{
			log.log(Level.FINE,"Configuration reading Started...");
			
			Properties prop = new Properties();
			FileInputStream inputStream = new FileInputStream(configs);
			prop.load(inputStream);
			
			MYSQL_HOST = prop.getProperty("MysqlHostName", "localhost");
			MYSQL_PORT = prop.getProperty("MysqlHostPort", "3306");
			MYSQL_USERNAME = prop.getProperty("MysqlUsername", "root");
			MYSQL_PASSWORD = prop.getProperty("MysqlPassword", "");
			MYSQL_DATABASE = prop.getProperty("MysqlDatabase", "warriorsjourney");
			MYSQL_TABLE_PREFIX = prop.getProperty("MysqlTablePrefix", "wj_");
			SHOULD_INSTALL_DB = Boolean.parseBoolean(prop.getProperty("ShouldInstallDatabases", "true"));
			LISTEN_PORT = Integer.parseInt(prop.getProperty("ListenPort", "12345"));
			MAX_CONNECTIONS = Integer.parseInt(prop.getProperty("MaxNumberOfConnections", "100"));
			
			inputStream.close();
			
			log.log(Level.FINE,"Configuration reading Done");
		}
		catch (IOException e)
		{
			log.log(Level.SEVERE,"Error while trying to read Configuration",e.toString());
			e.printStackTrace();
		}
		
	}	
}
