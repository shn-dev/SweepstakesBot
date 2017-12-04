package core;

public class BotTimer extends Thread {

	@FunctionalInterface
	public interface TimerUpdate {
		public void postUpdate(int timeRemaining);
	}

	private int mTimeRemaining;
	private TimerUpdate mUpdate;
	private volatile boolean mRunning;

	public BotTimer(int startTime, TimerUpdate update) {
		mTimeRemaining = startTime;
		mUpdate = update;
		mRunning = true;
	}

	public void terminate() {
		mRunning = false;
		mTimeRemaining = 0;
	}

	@Override
	public void run() {
		try {
			while (mTimeRemaining > 0 && mRunning) {
				Thread.sleep(1000);
				mTimeRemaining -= 1000;
				postTime(mTimeRemaining);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void postTime(int timeRemaining) {
		if (timeRemaining < 0)
			mTimeRemaining = 0;
		mUpdate.postUpdate(mTimeRemaining);
	}
}