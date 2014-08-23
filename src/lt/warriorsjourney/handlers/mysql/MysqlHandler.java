package lt.warriorsjourney.handlers.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import lt.warriorsjourney.Config;
import lt.warriorsjourney.WarriorsJourneyServer;
import lt.warriorsjourney.handlers.mysql.data.MysqlPlayerData;
import lt.warriorsjourney.handlers.mysql.data.MysqlPlayerScore;

/**
 * Provides all the basic MySQL functions 
 * that are used by the server on runtime.
 * 
 * @author Ideo
 */

public class MysqlHandler
{
	// ===== USED VARS =====
	
	private final static Logger log = Logger.getLogger(MysqlHandler.class.getName());
	
	private Connection mysqlCon;
	
	// ===== USED MYSQL COMMANDS ===== 
	
	private static String CREATE_PLAYER_DATA_DATABASE = "CREATE TABLE IF NOT EXISTS `"+Config.MYSQL_TABLE_PREFIX+"player_data` "
														+ "(`player_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
														+ "`facebook_id` INT NOT NULL DEFAULT 0,"
														+ "`android_id` VARCHAR(20) NOT NULL DEFAULT '',"
														+ "`nick` VARCHAR(20) NOT NULL DEFAULT '',"
														+ "`rank` VARCHAR(20) NOT NULL DEFAULT '',"
														+ "`lvl` INT NOT NULL DEFAULT 1,"
														+ "`exp` INT NOT NULL DEFAULT 0,"
														+ "`balance` INT NOT NULL DEFAULT 0,"
														+ "`str` INT NOT NULL DEFAULT 1,"
														+ "`sta` INT NOT NULL DEFAULT 1,"
														+ "`dex` INT NOT NULL DEFAULT 1,"
														+ "`agi` INT NOT NULL DEFAULT 1,"
														+ "`con` INT NOT NULL DEFAULT 1,"
														+ "`int` INT NOT NULL DEFAULT 1,"
														+ "`bonus_str` INT NOT NULL DEFAULT 0,"
														+ "`bonus_sta` INT NOT NULL DEFAULT 0,"
														+ "`bonus_dex` INT NOT NULL DEFAULT 0,"
														+ "`bonus_agi` INT NOT NULL DEFAULT 0,"
														+ "`bonus_con` INT NOT NULL DEFAULT 0,"
														+ "`bonus_int` INT NOT NULL DEFAULT 0,"
														+ "`unspent_pts` INT NOT NULL DEFAULT 0);";
	
	private static String CREATE_PLAYER_SCORE_DATABASE = "CREATE TABLE IF NOT EXISTS `"+Config.MYSQL_TABLE_PREFIX+"player_score` "
														+ "(`score_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
														+ "`player_id` INT NOT NULL DEFAULT 0,"
														+ "`score` INT NOT NULL DEFAULT 0);";
	
	/**
	 * Creates the basic MySQLHandler object.
	 */
	
	public MysqlHandler()
	{
		WarriorsJourneyServer.initLogger(log);
		
		if(Config.SHOULD_INSTALL_DB)
			createMysqlDataBases();
	}
	
	/**
	 * Returns the main MySQL Connection 
	 * and connects if its not connected.
	 * 
	 * @return mysqlCon the main MySQL Connection used in the system.
	 */
	
	public Connection getMysqlConnection()
	{
		if(mysqlCon != null)
			return mysqlCon;
		else
		{
			log.log(Level.FINE,"Setting up a MySQL Connection");
						
			try
			{
				Class.forName("com.mysql.jdbc.Driver");
				
				String mysqlUrl = ("jdbc:mysql://"+Config.MYSQL_HOST+":"+Config.MYSQL_PORT+"/"+Config.MYSQL_DATABASE);
				
				mysqlCon = DriverManager.getConnection(mysqlUrl, Config.MYSQL_USERNAME, Config.MYSQL_PASSWORD);
				
				log.log(Level.FINE,"MySQL Connection online!");
				return mysqlCon;
			}
			catch(Exception e)
			{
				log.log(Level.SEVERE,"Error while trying to set up a MySQL connection!",e.toString());
				e.printStackTrace();
			}
		}
		
		log.log(Level.FINE,"Error with MySQL Connection please check the problem!");
		return null;
	}
	
	/**
	 * Closes the main MySQL connection.
	 */
	
	public void closeConnection()
	{
		log.log(Level.FINE,"MySQL connection closing.");

		if(mysqlCon != null)
			try
			{
				mysqlCon.close();
				mysqlCon = null;

				log.log(Level.FINE,"Succesfully closed MySQL Connection!");
			} 
			catch (SQLException e)
			{
				log.log(Level.FINE,"Error while closing Mysql connections!");
				e.printStackTrace();
			}
		else
			log.log(Level.WARNING, "MySQL connection was never opened!");
	}
	
	/**
	 * Creates a new MySQL player data save in MySQL and return a new MysqPlayerData object to the client.
	 * 
	 * @param facebook_id - the facebook id.
	 * @param nick - the nick the player chooses.
	 * @return a new MysqlPlayerData object or null on error.
	 */
	
	public MysqlPlayerData createNewPlayer(int facebook_id,String nick)
	{
		Connection con = getMysqlConnection();
		int player_id;

		try
		{
			log.log(Level.FINER,"Creating new player! "+facebook_id);
			
			Statement stmt = con.createStatement();
			stmt.executeUpdate(getPlayerCreateSTM(facebook_id,"",nick,"Starter",1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,0));
			ResultSet rs = stmt.executeQuery(getPlayerSearchSTM(facebook_id));
			
			rs.first();
			player_id = rs.getInt(1);
			
			rs.close();
			stmt.close();
			
			return new MysqlPlayerData(player_id,facebook_id,"",nick,"Starter",1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,0);
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE,"Error while creating new player in MySQL id: "+facebook_id,e.toString());
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Used by the client thread to get a reference of a players data
	 * present in MySQL.
	 * 
	 * If there is no data in MySQL returns a new data object and creates
	 * a new account in MySQL.
	 * 
	 * NOTE: Facebook id should never be used or stored on runtime from this time forward!
	 * 		 Always use the player id! (Even on the friend list searching)
	 * 
	 * @param facebook_id - the id that should be searched. 
	 * @return the MysqlPlayerData object associated with the given id, or a new one.
	 * 			or NULL if the player is not present in the Database he/she is created later in createNewPlayer
	 */
	
	public MysqlPlayerData getMysqlPlayerDataByFacebookId(int facebook_id)
	{
		Connection con = getMysqlConnection();
		
		try
		{
			log.log(Level.FINER,"Gathering data for player: "+facebook_id);
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(getPlayerSearchSTM(facebook_id));
			
			if(!rs.first())
			{
				log.log(Level.FINER,"New Player! "+ facebook_id);
				
				rs.close();
				stmt.close();

				return null;
			}
			else
			{
				int player_id = rs.getInt(1);
				String android_id = rs.getString(3);
				String nick = rs.getString(4);
				String rank = rs.getString(5);
				int lvl = rs.getInt(6);
				int exp = rs.getInt(7);
				int balance = rs.getInt(8);
				
				int STR = rs.getInt(9);
				int STA = rs.getInt(10);
				int DEX = rs.getInt(11);
				int AGI = rs.getInt(12);
				int CON = rs.getInt(13);
				int INT = rs.getInt(14);
				
				int BONUS_STR = rs.getInt(15);
				int BONUS_STA = rs.getInt(16);
				int BONUS_DEX = rs.getInt(17);
				int BONUS_AGI = rs.getInt(18);
				int BONUS_CON = rs.getInt(19);
				int BONUS_INT = rs.getInt(20);
				
				int unspent_pts = rs.getInt(21);
				
				rs.close();
				stmt.close();
				
				return new MysqlPlayerData(player_id,facebook_id,android_id,nick,rank,lvl,exp,balance,STA,STR,DEX,AGI,CON,INT,
											BONUS_STR,BONUS_STA,BONUS_DEX,BONUS_AGI,BONUS_CON,BONUS_INT,unspent_pts);
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE,"Error fetching player data: "+facebook_id,e.toString());
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Gets the best global scores the amount of them specified by number.
	 * 
	 * @param number - the number of scores to return.
	 * @return the MysqlPlayerScore object list with all the scores in it.
	 */
	
	public List<MysqlPlayerScore> getScoresGlobal(int number)
	{
		Connection con = getMysqlConnection();
		List<MysqlPlayerScore> scores = new ArrayList<MysqlPlayerScore>();
		
		try
		{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(getGlobalPlayerScoreSTM());
			
			int n = 0;
			
			while(rs.next() && n < number)
			{
				scores.add(new MysqlPlayerScore(rs.getInt("score_id"),rs.getInt("player_id"),rs.getInt("score")));
				n++;
			}
			
			rs.close();
			stmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE,"Failed to get global best player scores",e.toString());
			e.printStackTrace();
		}		
		
		return scores;
	}
	
	/**
	 * Returns all the scores in MySQL in PlayerScore object array.
	 * 
	 * @param player_id - Player id to whom the scores should belong.
	 * @return the MysqlPlayerScore object list with all the scores in it.
	 */
	
	public List<MysqlPlayerScore> getScoresById(int player_id)
	{
		Connection con = getMysqlConnection();
		List<MysqlPlayerScore> scores = new ArrayList<MysqlPlayerScore>();
		
		try
		{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(getPlayerScoreByIdSTM(player_id));
			
			while(rs.next())
				scores.add(new MysqlPlayerScore(rs.getInt("score_id"),rs.getInt("player_id"),rs.getInt("score")));
					
			rs.close();
			stmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE,"Failed to get player scores id: "+player_id,e.toString());
			e.printStackTrace();
		}
		return scores;
	}
	
	public void removeScore(MysqlPlayerScore score)
	{
		Connection con = getMysqlConnection();
		
		try
		{
			Statement stmt = con.createStatement();
			stmt.executeUpdate(removePlayerScoreSTM(score));
			
			stmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE,"Failed to remove player score",e.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves the given score to MySQL.
	 * @param player_id - the players id to whom the score belongs.
	 * @param score - the score to save.
	 */
	
	public void saveScore(int player_id,int score)
	{
		Connection con = getMysqlConnection();
		
		try
		{
			Statement stmt = con.createStatement();
			stmt.executeUpdate(savePlayerScoreSTM(player_id,score));
			stmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE,"Failed to save player score",e.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates all databases used in game, 
	 * should be called on first Server load [can be disabled in cofig].
	 */
	
	public void createMysqlDataBases()
	{
		Connection con = getMysqlConnection();
		
		try
		{
			log.log(Level.FINE,"Starting MySQL Database creation.");
			
			Statement stmt = con.createStatement();
			stmt.executeUpdate(CREATE_PLAYER_DATA_DATABASE);
			stmt.executeUpdate(CREATE_PLAYER_SCORE_DATABASE);
			
			log.log(Level.FINE,"MySQL databsae creation DONE!");
			
			stmt.close();
		} 
		catch (SQLException e)
		{
			log.log(Level.SEVERE,"ERROR creating MySQL databases check the problem please!",e.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves MysqlPlayerData to MySQL for longer saving.
	 *  
	 * @param playerData - the MysqlPlayerData that should be saved.
	 */
	
	public void savePlayerData(MysqlPlayerData playerData)
	{
		try
		{
			Connection con = getMysqlConnection();
			Statement stmt = con.createStatement();
			
			stmt.executeUpdate(getPlayerSaveSTM(playerData));
			stmt.close();
		}
		catch(SQLException e)
		{
			log.log(Level.SEVERE,"Failed to save player data to MySQL",e.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates an MySQL statement to be used for score removing from MySQL.
	 * 
	 * @param score - the MysqlPlayerScore object that represents the score. 
	 * @return the statement to be used for score removing.
	 */
	
	public String removePlayerScoreSTM(MysqlPlayerScore score)
	{
		return  "DELETE FROM `"+Config.MYSQL_TABLE_PREFIX+"player_score` WHERE"
				+ " score_id = '"+score.getScoreId()+"';";
	}
	
	/**
	 * Creates an MySQL statement to be used for MysqlPlayerScore object saving.
	 * 
	 * @param player_id - the players id to whom the score belongs.
	 * @param score - the players score to be save. 
	 * @return the statement to be used for score saving.
	 */
	
	public String savePlayerScoreSTM(int player_id,int score)
	{
		return  "INSERT INTO `"+Config.MYSQL_TABLE_PREFIX+"player_score` (`player_id`,`score`)"
				+ "VALUES ('"+player_id+"','"+score+"');";
	}
	
	/**
	 * Creates an MySQL statement to be used for global best score search.
	 * 
	 * @return the statement to be used to get the scores.
	 */
	
	public String getGlobalPlayerScoreSTM()
	{
		return "SELECT * FROM `"+Config.MYSQL_TABLE_PREFIX+"player_score` ORDER BY score DESC;";
	}
	
	/**
	 * Creates an MySQL statement to be used to search for players scores from player_score table.
	 * 
	 * @param player_id
	 * @return the statement to be used to get the score
	 */
	
	public String getPlayerScoreByIdSTM(int player_id)
	{
		return  "SELECT * FROM `"+Config.MYSQL_TABLE_PREFIX+"player_score` WHERE "
				+ "player_id = '"+player_id+"' ORDER BY score DESC;";
	}
	
	/**
	 * Creates an update statement from the given MysqlPlayerData object to save player data to MySQL
	 * 
	 * @param playerData - the MysqlPlayerData object from witch the data should be taken.
	 * @return A statement to be used for Player Data saving.
	 */
	
	public String getPlayerSaveSTM(MysqlPlayerData playerData)
	{
		return  "UPTATE `"+Config.MYSQL_TABLE_PREFIX+"player_data` SET "
				+ "`nick` = '"+playerData.getNickName()+"',"
				+ "`rank` = '"+playerData.getRank()+"',"
				+ "`lvl` = '"+playerData.getLvl()+"',"
				+ "`exp` = '"+playerData.getExp()+"',"
				+ "`balance` = '"+playerData.getBalance()+"',"
				+ "`str` = '"+playerData.getSTR()+"',"
				+ "`sta` = '"+playerData.getSTA()+"',"
				+ "`dex` = '"+playerData.getDEX()+"',"
				+ "`agi` = '"+playerData.getAGI()+"',"
				+ "`con` = '"+playerData.getCON()+"',"
				+ "`int` = '"+playerData.getINT()+"',"
				+ "`bonus_str` = '"+playerData.getBONUS_STR()+"',"
				+ "`bonus_sta` = '"+playerData.getBONUS_STA()+"',"
				+ "`bonus_dex` = '"+playerData.getBONUS_DEX()+"',"
				+ "`bonus_agi` = '"+playerData.getBONUS_AGI()+"',"
				+ "`bonus_con` = '`"+playerData.getBONUS_CON()+"',"
				+ "`bonus_int` = '"+playerData.getBONUS_INT()+"',"
				+ "`unspent_pts` = '"+playerData.getUnspentPts()+"'"
				+ " WHERE `player_id` = '"+playerData.getPlayerId()+"';";
				
	}
		
	/**
	 * Creates the MySQL statement for player data gathering from player_data table
	 * 
	 * @param facebook_id - the id that should be searched for
	 * @return - MySQL statement that should be used.
	 */
	
	public String getPlayerSearchSTM(int facebook_id)
	{
		return  "SELECT * FROM `"+Config.MYSQL_TABLE_PREFIX+"player_data` "
				+ "WHERE facebook_id = '"+facebook_id+"';";
	}
	
	/**
	 * Creates the MySQL statement used to create a new player data save. 
	 * 
	 * @param - player data from the MySQLPlayerData object.
	 * @return - MySQL statement used to create a new player data save.
	 */
	
	public String getPlayerCreateSTM(int facebook_id,String android_id,String nick,String rank,int lvl,int exp,int balance,int str,
									 int sta,int dex,int agi,int con,int intel,int bonus_str,int bonus_sta,int bonus_dex,int bonus_agi,int bonus_con,
									 int bonus_int, int unspent_pts)
	{
		return "INSERT INTO "+Config.MYSQL_TABLE_PREFIX+"player_data "
			   + "(`facebook_id`,`android_id`,`nick`,`rank`,`lvl`,`exp`,"
			   + "`balance`,`str`,`sta`,`dex`,`agi`,`con`,`int`,`bonus_str`,`bonus_sta`,"
			   + "`bonus_dex`,`bonus_agi`,`bonus_con`,`bonus_int`,`unspent_pts`) VALUES"
			   + "('"+facebook_id+"','"+android_id+"','"+nick+"','"+rank+"',"
			   + "'"+lvl+"','"+exp+"','"+balance+"','"+str+"','"+sta+"','"+dex+"','"+agi+"','"+con+"',"
			   + "'"+intel+"','"+bonus_str+"','"+bonus_sta+"','"+bonus_dex+"','"+bonus_agi+"',"
			   + "'"+bonus_con+"','"+bonus_int+"','"+unspent_pts+"');";
	}
	
	/**
	 * Creates the MySQL statement used to save players score.
	 * @param score data from the MysqlPlayerScore object.
	 * @return MySQL statement used to save player data.
	 */
	
	public String getCreatePlayerScoreSTM(int score_id,int player_id,int score)
	{
		return "INSERT INTO `"+Config.MYSQL_TABLE_PREFIX+"player_score` "
			   + "(`score_id`,`player_id`,`score`) VALUES ('"+score_id+"',"
			   + "'"+player_id+"','"+score+"');";
	}
}
