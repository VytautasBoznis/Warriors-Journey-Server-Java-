package lt.warriorsjourney.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import lt.warriorsjourney.WarriorsJourneyServer;
import lt.warriorsjourney.handlers.mysql.MysqlHandler;
import lt.warriorsjourney.handlers.mysql.data.MysqlPlayerData;
import lt.warriorsjourney.handlers.mysql.data.MysqlPlayerScore;

public class WarriorsJourneyClient extends Thread
{
	public static final Logger log = Logger.getLogger(WarriorsJourneyClient.class.getName());

	int threadId;
	
	Socket socket;
	BufferedReader input;
	BufferedWriter output;
	boolean closing = false;
	boolean logedin = false;
	
	MysqlPlayerData playerData;
	WarriorsJourneyServer serverInstance;

	//Used to store task that should be enforced to the client, used in the run() while loop.
	//Ids: 1 - force nick creation.
	int task = 0;
	
	/**
	 * Creates a client thread for Client-Server communication
	 * 
	 * @param cSocket Client socket used for this client
	 */

	public WarriorsJourneyClient(Socket cSocket,WarriorsJourneyServer server,int id)
	{
		WarriorsJourneyServer.initLogger(log);
		
		serverInstance = server;
		threadId = id;
		socket = cSocket;
		
		try
		{
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			
			log.log(Level.FINER,"Client input/output streams created");
			log.log(Level.FINER,"Starting Authentification");
			
			forceClientLogin();
		}
		catch (IOException e)
		{
			log.log(Level.SEVERE,"Client input stream error ",e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Runs communication
	 * 
	 * Reads all the data that the client sends and forwards it
	 * for data manipulation.
	 */
	
	@Override
	public void run()
	{
		try
		{
			int faills = 0;
			while(!closing)
			{
				//if there are task pending perform them.
				if(task != 0)
				{
					if(task == 1)
					{
						getNickFromClient();
						task = 0;
					}
				}
				
				//else just read normal input.
				String line;
				if((line = input.readLine()) != null)
				{
					faills = 0;
					useData(line);
				}
				else
					faills++;
				
				if(faills >= 10)
					closing = true;
			}
			
			log.log(Level.FINE,"Started Client connection kill " + threadId);
			
			input.close();
			output.close();
			
			socket.close();
			
			serverInstance.nullifyThread(threadId);
		
		}	
		catch (IOException | NullPointerException e)
		{
			//there will always be an null pointer exception on thread kill
			//and it will not cause any problems, because once the array reaches it's end
			//the null will be rewritten.
			
			if(e instanceof IOException)
			{	
				log.log(Level.SEVERE,"Error in client thread!");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Sends a message to the client as a line of text.
	 * Should be used as the only way to communicate.
	 * 
	 * @param message the message that should be sent.
	 */
	
	public void sendMessage(String message)
	{
		try
		{
			output.write(message + "\n");
			output.flush();
		}
		catch (IOException e)
		{
			log.log(Level.SEVERE,"Failed to send message to client "+threadId,e.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles requests and data processing	
	 * 
	 * @param line the text received from client
	 */
	
	private void useData(String line)
	{
		
		int data[] = convertData(line);
		
		if(data[0] == 1 && !logedin)
		{
			login(data);
		}
		
		if(data[0] == 2)
		{
			//gets the best scores.
			List<MysqlPlayerScore> scores = getBestScore(data[1],true);
			
			//formats a message to send.
			String message = "/pBestS;";
			if(scores == null) // the scores could be null and that should be checked!
				message += "0;";
			else
			{
				message += scores.size()+";";
				
				//adds the scores to the message
				for(int i = 0;i<scores.size();i++)
					message += scores.get(i).getScore()+";";
			}
	
			//sends the one message with all the data.
			sendMessage(message);
		}
		
		if(data[0] == 3)
		{
			//gets the best scores.
			List<MysqlPlayerScore> scores = getBestScore(data[1],false);
			
			//formats a message to send.
			String message = "/gBestS;";
			
			if(scores == null) // the scores could be null and that should be checked!
				message += "0;";
			else
			{
				message += scores.size()+";";
				
				//adds the scores to the message
				for(int i = 0;i<scores.size();i++)
					message += scores.get(i).getScore()+";";
			}
		
			//sends the one message with all the data.
			sendMessage(message);
		}
		
		if(data[0] == 4)
		{
			addScore(data[1]);
			notifyScoreChange();
		}
		
		/*if(data[0] == 5) cia veliau kai bus onlinas arba rank sistema globali.
		{
			//uzdeda nauja nika is line pasiemes ji
			savePlayerToMysql();
		}*/
	}
	
	/**
	 * Concerts from data stream strings to
	 * server-side used data.
	 * 
	 * Converts in a couple of steps:
	 * 1.looks for known commands ex.: (/aID;(authentication ID); ,/addS;(score);, 
	 * 	/getS;(amount of best scores to get probably 10);). Commands have a syntax of :
	 * 	"/" for command start indication, then the command, and the value separated by semicolons ex: (;10;)
	 * 
	 * 2.adds the command id to the data[0] array.
	 * 3.adds the values to the next data arrays spaces.
	 * 
	 * @param line of text that contains convertible parameters
	 * @return an array of data with values from the inputed line.
	 * 		   The first [0] value represents what action was specified.
	 * 		   0 - Unknown request.
	 * 		   1 - Authentication.
	 * 		   2 - Score request (/getSp) for best player scores.
	 * 		   3 - Score request (/getSg) for best global scores.
	 * 		   4 - Score submission.
	 * 		   5 - new nickname.
	 * 
	 * NOTE! If data[-1] that means the input should be a string not an integer.
	 * 		 And from that point the next task should use the line forwarded in the first place
	 * 		 (the useData) function.
	 */
	
	private int[] convertData(String line)
	{
		int[] data = new int[10];
		String dataString = "";
		
		int lastChar = 0;
		
		while(lastChar < line.length())
		{
			dataString += line.charAt(lastChar);
			
			if(dataString.equalsIgnoreCase("/aID;"))
			{
				lastChar++;
				
				String Id = "";
												
				while(line.charAt(lastChar) != ';')
				{
					Id += line.charAt(lastChar);
					lastChar++;
				}
				
				data[0] = 1;
				data[1] = Integer.parseInt(Id);
				
				return data;
			}
			
			if(dataString.equalsIgnoreCase("/getSp;"))
			{
				lastChar++;
				
				String Id = "";
												
				while(line.charAt(lastChar) != ';')
				{
					Id += line.charAt(lastChar);
					lastChar++;
				}
				
				data[0] = 2;
				data[1] = Integer.parseInt(Id);
				
				return data;
			}
			
			if(dataString.equalsIgnoreCase("/getSg;"))
			{
				lastChar++;
				
				String Id = "";
												
				while(line.charAt(lastChar) != ';')
				{
					Id += line.charAt(lastChar);
					lastChar++;
				}
				
				data[0] = 3;
				data[1] = Integer.parseInt(Id);
				
				return data;
			}
			
			if(dataString.equalsIgnoreCase("/addS;"))
			{
				lastChar++;
				
				String Id = "";
												
				while(line.charAt(lastChar) != ';')
				{
					Id += line.charAt(lastChar);
					lastChar++;
				}
				
				data[0] = 4;
				data[1] = Integer.parseInt(Id);
				
				return data;
			}
			
			if(dataString.equalsIgnoreCase("/nNick;"))
			{
				lastChar++;
															
				data[0] = 5;
				data[1] = -1;
				
				return data;
			}
			
						
			lastChar++;
		}
		
		log.log(Level.FINE,"Received unknown request: "+line+" from client: "+threadId);
		log.log(Level.FINE,"oh and that thread id is SOOO usefull...");
		return data;
	}

	/**
	 * Performs authentication:
	 * 
	 * Sets up the appropriate MySQL user data or creates it form scratch
	 * if the MySQL handler does'nt have it for this id.
	 * 
	 * NOTE! Should be encoded with MD5 or SHA-256.
	 * 
	 * @param data the array of data converted to integers.
	 */
	
	public void login(int[] data)
	{
		int facebook_id = data[1];
		
		MysqlHandler mysql = serverInstance.getMysqlHandler();
		
		playerData = mysql.getMysqlPlayerDataByFacebookId(facebook_id);
		
		if(playerData == null)
		{
			String nick = "";
			mysql.createNewPlayer(facebook_id, nick);
		}
		
		logedin = true;
		
		sendMessage("/authOK;");
		
		log.log(Level.FINER,"New player loged In!");
	}
	
	/**
	 * Returns the number of best scores found number from best to worst.
	 * 
	 * @param number - the amount of scores to return.
	 * @param personal - true for best personal scores, false for best global scores.
	 * @return - The MysqlPlayerScore object array containing all the scores from best to worst.
	 * 			 A smaller array if there are not enough scores ex. request 5 there are 2.
	 * 			 Null if there are no scores.
	 */
	
	public List<MysqlPlayerScore> getBestScore(int number,boolean personal)
	{
		List<MysqlPlayerScore> scores = new ArrayList<MysqlPlayerScore>();
		
		//the received list is already ordered in descending order.
		if(personal)
			scores = serverInstance.getMysqlHandler().getScoresById(playerData.getPlayerId());
		else
			scores = serverInstance.getMysqlHandler().getScoresGlobal(number);
		
		if(number > scores.size())
			number = scores.size();
		
		return scores;
	}
	
	/**
	 * Adds the given score to MySQL and saves it if it's in the top 10 scores of the player.
	 * NOTE! Should only have MAX 10 best scores of a player at any given time!
	 * 
	 * @param score - the score to be checked and saved if it's in the top 10 scores.
	 */
	
	public void addScore(int score)
	{
		List<MysqlPlayerScore> scores = getBestScore(10,true);
		
		MysqlPlayerScore toRemove = null;
		
		if(scores.size() >= 10)
		{

			for(int i = 0;i<scores.size();i++)
			{
				if(score > scores.get(i).getScore())
					toRemove = scores.get(i);
			}
			
			if(toRemove != null)
				serverInstance.getMysqlHandler().removeScore(toRemove);
			else
				return;//to low score to save (not in the best 10). 
					   //There could be a message that tells this to the player
		}
		
		notifyScoreChange();
		serverInstance.getMysqlHandler().saveScore(playerData.getPlayerId(), score);
	}
	
	/**
	 * Sends a message to the client that forces it to login.
	 */
	
	public void forceClientLogin()
	{
		if(!logedin)
			sendMessage("/login;");		
	}
	
	public void notifyScoreChange()
	{
		sendMessage("/newScores;");
	}
	
	/**
	 * Sends a message to a client that forces nick creation.
	 * Should only be used when a new player logins.
	 */
	
	public void getNickFromClient()
	{
		sendMessage("/nNick;");
	}

	/**
	 * Saves the new nickname in the MysqlPlayerData object.
	 * 
	 * @param nick the nick that should be saved.
	 */
	
	public void setNewNick(String nick)
	{
		log.log(Level.FINE,playerData.getNickName()+" Changed Nickname to: "+nick);
		
		playerData.setNickName(nick);
	}
	
	/**
	 * Saves the player data to MySQL.
	 */
	
	public void savePlayerToMysql()
	{
		serverInstance.getMysqlHandler().savePlayerData(playerData);
	}
	
}
