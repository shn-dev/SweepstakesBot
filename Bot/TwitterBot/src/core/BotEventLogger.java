package core;
import java.util.Date;

import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Partial implementation of IBotEventLogger so that inherited classes contain two useful logging methods
 * @author ssepa
 *
 */
public abstract class BotEventLogger implements IBotEventLogger{
	
	protected static String getEntryFailedText(TwitterException e) {
		StringBuilder sb = new StringBuilder("");
		sb.append(new Date(System.currentTimeMillis()) + ": entry failed. " + System.lineSeparator());
		sb.append("The following error " + "occurred when querying/retweeting/following..." + System.lineSeparator());
		sb.append(e.getMessage() + System.lineSeparator());
		sb.append(e.getErrorMessage() + System.lineSeparator());
		return sb.toString();
	}

	protected static String getEntrySuccessText(Status s, int entriesCompleted, int totalNumEntries) {
		StringBuilder sb = new StringBuilder("");
		sb.append(new Date(System.currentTimeMillis()) + ": finished entry. " + System.lineSeparator());
		sb.append("Followed " + s.getUser().getScreenName() + "and retweeted \"" + s.getText() + "\""
				+ System.lineSeparator());
		sb.append(entriesCompleted + "/" + totalNumEntries + " entries completed before next query."
				+ System.lineSeparator());
		sb.append(System.lineSeparator());
		return sb.toString();
	}
}
