package lt.warriorsjourney.handlers.mysql.data;

/**
 * Holds data used in game,
 * also this data is saved to MySQL on shutdown.
 * 
 * @author Ideo
 */

public class MysqlPlayerData
{
	// ===== PLAYER UNIQUE IDS =====
	private int player_id;
	private int facebook_id;
	private String android_id;
	private String nick_name;
	
	// ===== PLAYER GAME STATS =====
	private String rank;
	private int lvl;
	private int exp;
	private int balance;
	
	// ===== PLAYER CHARACTER STATS =====
	private int STR;
	private int STA;
	private int DEX;
	private int AGI;
	private int CON;
	private int INT;
	
	//Bonus stats are used in combination with items
	//to add additional costumaisation 
	private int BONUS_STR;
	private int BONUS_STA;
	private int BONUS_DEX;
	private int BONUS_AGI;
	private int BONUS_CON;
	private int BONUS_INT;

	//unspent_pts the points that are left and still can 
	//be used to increase other stats
	private int unspent_pts;

	
	public MysqlPlayerData(int id,int fb_id,String android_id,String nickName,String rank,int lvl,int exp,int balance, int str,int sta,int dex,int agi,int con,int intelegence,
						   int bonusStr,int bonusSta,int bonusDex,int bonusAgi,int bonusCon,int bonusInt,int unspentPts)
	{
		setPlayerId(id);
		setFacebookId(fb_id);
		setAndroidId(android_id);
		setNickName(nickName);
		setRank(rank);
		setLvl(lvl);
		setExp(exp);
		setBalance(balance);
		setSTR(str);
		setSTA(sta);
		setDEX(dex);
		setAGI(agi);
		setCON(con);
		setINT(intelegence);
		setBONUS_STR(bonusStr);
		setBONUS_STA(bonusSta);
		setBONUS_DEX(bonusDex);
		setBONUS_AGI(bonusAgi);
		setBONUS_CON(bonusCon);
		setBONUS_INT(bonusInt);
		setUnspentPts(unspentPts);
	}
	
	public int getPlayerId()
	{
		return player_id;
	}

	public void setPlayerId(int playerId)
	{
		player_id = playerId;
	}

	public int getFacebookId()
	{
		return facebook_id;
	}

	public void setFacebookId(int facebookId)
	{
		facebook_id = facebookId;
	}

	public String getAndroidId()
	{
		return android_id;
	}

	public void setAndroidId(String androidId)
	{
		android_id = androidId;
	}

	public String getNickName()
	{
		return nick_name;
	}

	public void setNickName(String nickName)
	{
		nick_name = nickName;
	}

	public String getRank()
	{
		return rank;
	}

	public void setRank(String rank)
	{
		this.rank = rank;
	}

	public int getLvl()
	{
		return lvl;
	}

	public void setLvl(int lvl)
	{
		this.lvl = lvl;
	}

	public int getExp()
	{
		return exp;
	}

	public void setExp(int exp)
	{
		this.exp = exp;
	}

	public int getBalance()
	{
		return balance;
	}

	public void setBalance(int balance)
	{
		this.balance = balance;
	}

	public int getSTR()
	{
		return STR;
	}

	public void setSTR(int str)
	{
		STR = str;
	}

	public int getSTA()
	{
		return STA;
	}

	public void setSTA(int sta)
	{
		STA = sta;
	}

	public int getDEX()
	{
		return DEX;
	}

	public void setDEX(int dex)
	{
		DEX = dex;
	}

	public int getAGI()
	{
		return AGI;
	}

	public void setAGI(int agi)
	{
		AGI = agi;
	}

	public int getCON()
	{
		return CON;
	}

	public void setCON(int con)
	{
		CON = con;
	}

	public int getINT()
	{
		return INT;
	}

	public void setINT(int value)
	{
		INT = value;
	}

	public int getBONUS_STR()
	{
		return BONUS_STR;
	}

	public void setBONUS_STR(int bonusStr)
	{
		BONUS_STR = bonusStr;
	}

	public int getBONUS_STA()
	{
		return BONUS_STA;
	}

	public void setBONUS_STA(int bonusSta)
	{
		BONUS_STA = bonusSta;
	}

	public int getBONUS_DEX()
	{
		return BONUS_DEX;
	}

	public void setBONUS_DEX(int bonusDex)
	{
		BONUS_DEX = bonusDex;
	}

	public int getBONUS_AGI()
	{
		return BONUS_AGI;
	}

	public void setBONUS_AGI(int bonusAgi)
	{
		BONUS_AGI = bonusAgi;
	}

	public int getBONUS_CON()
	{
		return BONUS_CON;
	}

	public void setBONUS_CON(int bonusCon)
	{
		BONUS_CON = bonusCon;
	}

	public int getBONUS_INT()
	{
		return BONUS_INT;
	}

	public void setBONUS_INT(int bonusInt)
	{
		BONUS_INT = bonusInt;
	}

	public int getUnspentPts()
	{
		return unspent_pts;
	}

	public void setUnspentPts(int unspentPts)
	{
		unspent_pts = unspentPts;
	}
	
	public void saveData()
	{
		
	}
}
