package core;

import java.util.List;
import java.util.Random;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class Bot extends Thread {

	private BotEventLogger bel;
	private BotSettingsManager bsm;
	private volatile boolean mRunning;
	private volatile BotTimer mTimer;

	public Bot() {
		bsm = BotSettingsManager.getInstance();
		mRunning = true;
	}

	public void terminate() {
		mRunning = false;
		if(mTimer != null)
			mTimer.terminate();
		bel.onTerminated();
	}
	
	public void setTimer(BotTimer timer) {
		mTimer = timer;
	}
	
	public void setAndStartTimer(BotTimer timer) {
		mTimer = timer;
		mTimer.start();
	}
	
	public BotTimer getTimer() throws NullPointerException{
		if(mTimer == null) {
			throw new NullPointerException("Timer is null. Make sure to set the timer first.");
		}
		return mTimer;
	}

	public void setEventLogger(BotEventLogger bel) {
		this.bel = bel;
	}

	public BotEventLogger getEventLogger() {
		return bel;
	}

	private Twitter runConfig() {

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(bsm.getAPI_KEY()).setOAuthConsumerSecret(bsm.getAPI_SECRET())
				.setOAuthAccessToken(bsm.getACCESS_TOKEN()).setOAuthAccessTokenSecret(bsm.getACCESS_TOKEN_SECRET());
		TwitterFactory tf = new TwitterFactory(cb.build());
		return tf.getInstance();
	}

	/**
	 * Skew time +/-5% to make entries look less automated
	 * 
	 * @param t
	 *            The time variable to obfuscate.
	 * @return The obfuscated time in milliseconds.
	 */
	private static int obfuscateTweetDelayTime(int t) {
		Random r = new Random();
		int i = t / 100 * r.nextInt(6); // gets between 0 and 5% of seed delay time
		if (r.nextBoolean())
			i *= -1; // determine whether to add or subtract from seed delay time
		return t + i;
	}

	@Override
	public void run() {
		Twitter twitter = runConfig();
		while (mRunning) {
			try {
				// The factory instance is re-useable and thread safe.
				List<String> queries = bsm.getQUERIES();
				Query query = new Query(queries.get(new Random().nextInt(queries.size())));
				query.count(100);
				QueryResult result = twitter.search(query);

				int entries = 0;
				for (Status s : result.getTweets()) {
					if (mRunning) {
						long idToFollow = s.getUser().getId();
						long idToRetweet = s.getId();
						try {
							twitter.retweetStatus(idToRetweet);
							twitter.createFriendship(idToFollow);
							twitter.createFavorite(idToRetweet); //twitter favorites are analogous to Facebook likes
							entries++;
							int delay = obfuscateTweetDelayTime(bsm.getTWEET_DELAY());
							setAndStartTimer(new BotTimer(delay, bel.onTimerUpdate()));
							bel.onEntrySuccess(s, entries, result.getTweets().size());
							Thread.sleep(delay);

						} catch (TwitterException ex) {// catches exceptions from retweeting status/following
							// This is most likely thrown from having already previously retweeted.
							bel.onEntryFailed(ex);
							continue; // continue to next queried status
						}
					}
					else {
						break;
					}
				}

			} catch (TwitterException e) { // catches exceptions from querying
				bel.onQueryingFailed(e);
				terminate();
				break;
			} catch (Exception e) { // catches all other exceptions...
				bel.onUnknownFailure(e);
				terminate();
				break;
			}
		}
		bel.onTerminated();
	}
}

