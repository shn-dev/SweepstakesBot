package core;
import java.util.List;

import twitter4j.Status;

public interface IBotEventLogger {

	public void onUnknownFailure(Exception e);

	public void onQueryingFailed(Exception e);

	public void onEntryFailed(Exception e);

	public void onQuerySuccess(List<Status> statusList);

	public void onEntrySuccess(Status s, int entries, int entryListSize);

	void onTerminated();
	
	BotTimer.TimerUpdate onTimerUpdate();
}
