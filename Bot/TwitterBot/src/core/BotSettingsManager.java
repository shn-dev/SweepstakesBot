package core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

public class BotSettingsManager implements IBotSettings, java.io.Serializable {

	/**
	 * randomly generated
	 */
	private static final long serialVersionUID = 8730440046448316650L;

	@Override
	public String getAPI_KEY() {
		return API_KEY;
	}
	
	@Override
	public void setAPI_KEY(String aPI_KEY) {
		API_KEY = aPI_KEY;
	}

	@Override
	public String getAPI_SECRET() {
		return API_SECRET;
	}

	@Override
	public void setAPI_SECRET(String aPI_SECRET) {
		API_SECRET = aPI_SECRET;
	}

	@Override
	public String getACCESS_TOKEN() {
		return ACCESS_TOKEN;
	}

	@Override
	public void setACCESS_TOKEN(String aCCESS_TOKEN) {
		ACCESS_TOKEN = aCCESS_TOKEN;
	}

	@Override
	public String getACCESS_TOKEN_SECRET() {
		return ACCESS_TOKEN_SECRET;
	}

	@Override
	public void setACCESS_TOKEN_SECRET(String aCCESS_TOKEN_SECRET) {
		ACCESS_TOKEN_SECRET = aCCESS_TOKEN_SECRET;
	}

	@Override
	public int getTWEET_DELAY() {
		return TWEET_DELAY;
	}

	@Override
	public void setTWEET_DELAY(int tWEET_DELAY) {
		TWEET_DELAY = tWEET_DELAY;
	}

	public static int tweetsPerDayToMS(int tweetsPerDay) {
		return (24 * 60 * 60 * 1000) / tweetsPerDay;
	}

	public static int msToTweetsPerDay(int ms) {
		return (1000 * 60 * 60 * 24) / ms;
	}

	@Override
	public List<String> getQUERIES() {
		return QUERIES;
	}

	@Override
	public void setQUERIES(List<String> qUERIES) {
		QUERIES = qUERIES;
	}

	private String API_KEY;
	private String API_SECRET;
	private String ACCESS_TOKEN;
	private String ACCESS_TOKEN_SECRET;

	private int TWEET_DELAY;
	private List<String> QUERIES = Arrays.asList(new String[] { "RETWEET TO WIN", "RT TO WIN", "ENTER TO WIN" });
	private static BotSettingsManager bs;

	private BotSettingsManager() {
	}

	synchronized public static BotSettingsManager getInstance() {
		if (bs == null)
			bs = new BotSettingsManager();
		return bs;
	}

	@FunctionalInterface
	public interface BotSettingsIOFailure {
		public void onException(Exception ex);
	}

	public static BotSettingsManager tryLoadInstance(BotSettingsIOFailure f, String filename) {
		BotSettingsManager bsm;
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			bsm = (BotSettingsManager) ois.readObject();
			fis.close();
			ois.close();
			return bsm;
		} catch (IOException | ClassNotFoundException ex) {
			f.onException(ex);
			return BotSettingsManager.getInstance();
		}
	}

	public static void saveBotSettings(BotSettingsIOFailure f, String outputFileName) {
		try {
			FileOutputStream fos = new FileOutputStream(outputFileName);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(BotSettingsManager.getInstance());
			fos.close();
			oos.close();
		} catch (Exception ex) {
			f.onException(ex);
		}
	}
}
