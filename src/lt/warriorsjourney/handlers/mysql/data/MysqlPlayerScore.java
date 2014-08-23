package lt.warriorsjourney.handlers.mysql.data;

/**
 * Holds one of many scores data.
 * Is saved in MySQL later.
 * 
 * @author Ideo
 */

public class MysqlPlayerScore
{
	private int player_id;
	private int score_id;
	private int score;
	
	public MysqlPlayerScore(int score_id,int player_id,int score)
	{
		setPlayerId(player_id);
		setScoreId(score_id);
		setScore(score);
	}

	public int getPlayerId()
	{
		return player_id;
	}

	public void setPlayerId(int playerId)
	{
		player_id = playerId;
	}

	public int getScoreId()
	{
		return score_id;
	}

	public void setScoreId(int scoreId)
	{
		score_id = scoreId;
	}

	public int getScore()
	{
		return score;
	}

	public void setScore(int score)
	{
		this.score = score;
	}
}
