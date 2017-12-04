package application;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;

import core.Bot;
import core.BotEventLogger;
import core.BotSettingsManager;
import core.BotTimer.TimerUpdate;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import twitter4j.Status;
import twitter4j.TwitterException;

public class Main extends Application {
	Bot bot;
	Tab settingsTab;
	Tab logTab;
	TabPane tabPane;

	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setTitle("Twitter Bot");

			tabPane = new TabPane();
			settingsTab = new Tab();
			logTab = new Tab();

			Scene scene = new Scene(tabPane, 600, 600);
			setupSettingsTab(settingsTab, tabPane);
			setupLogTab(logTab, tabPane);

			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * If string isn't null, set the textfield's text property.
	 * 
	 * @param tf
	 *            The textfield.
	 * @param str
	 *            The text to bind to the textfield.
	 */
	private void setTextConditionally(TextField tf, String str) {
		if (str != null)
			tf.setText(str);
	}

	private void setupSettingsTab(Tab settingsTab, TabPane tabPane) {

		settingsTab.setText("Settings");
		VBox vboxSettings = new VBox();
		vboxSettings.setSpacing(10);
		vboxSettings.setPadding(new Insets(50, 50, 50, 50));

		// Instantiate new textfield instances...
		TextField apiKeyTF = new TextField();
		TextField apiSecretTF = new TextField();
		TextField accessTokenTF = new TextField();
		TextField accessTokenSecretTF = new TextField();
		TextField delayTF = new TextField();

		// Instantiate new label instances...
		Label apiKeyLabel = new Label("API Key:");
		Label apiSecretLabel = new Label("API Secret: ");
		Label accessTokenLabel = new Label("Access Token: ");
		Label accessTokenSecretLabel = new Label("Access Token Secret: ");
		Label delayLabel = new Label("Set number of entries/day (+/- 5%)");

		Button begin = new Button("Start");
		Button stop = new Button("Stop");

		BotSettingsManager bsm;

		// try to load previous settings from save file
		bsm = BotSettingsManager.tryLoadInstance((Exception ex) -> {
			if(ex != null && !(ex instanceof FileNotFoundException)) 
				//If the file isn't found it probably just means
				//the user is using the bot for the first time, so ignore it. On other errors, report it.
				new MsgBox(ex.toString());
		}, "bot.ser");

		// Set up the labels and text fields
		apiKeyTF.setPromptText("API Key");
		apiKeyTF.setPrefColumnCount(30);
		setTextConditionally(apiKeyTF, bsm.getAPI_KEY());

		apiSecretTF.setPromptText("API Secret");
		setTextConditionally(apiSecretTF, bsm.getAPI_SECRET());

		accessTokenTF.setPromptText("Access Token");
		setTextConditionally(accessTokenTF, bsm.getACCESS_TOKEN());

		accessTokenSecretTF.setPromptText("Token Secret");
		setTextConditionally(accessTokenSecretTF, bsm.getACCESS_TOKEN_SECRET());

		delayTF.setPromptText("# sweepstakes entries per day.");
		int delay = bsm.getTWEET_DELAY() == 0 ? 199 : BotSettingsManager.msToTweetsPerDay(bsm.getTWEET_DELAY());
		setTextConditionally(delayTF, String.valueOf(delay));

		// Set up buttons and button group
		HBox buttonGroup = new HBox();
		buttonGroup.setAlignment(Pos.CENTER);
		buttonGroup.setPadding(new Insets(50, 20, 10, 20));
		buttonGroup.setSpacing(10);

		stop.setDisable(true);
		stop.setPadding(new Insets(30, 50, 30, 50));
		stop.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				bot.interrupt();
				stop.setDisable(true);
				begin.setDisable(false);
			}
		});

		// Set up start button
		begin.setPadding(new Insets(30, 50, 30, 50));
		begin.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				// Create new thread and attach bot reference to it.
				bot = new Bot();

				// Disable the begin button so you cant start a bunch of bots.
				begin.setDisable(true);
				stop.setDisable(false);
				tabPane.getSelectionModel().select(logTab);
				
				
				BotSettingsManager bsm = BotSettingsManager.getInstance();
				bsm.setAPI_KEY(apiKeyTF.getText());
				bsm.setAPI_SECRET(apiSecretTF.getText());
				bsm.setACCESS_TOKEN(accessTokenTF.getText());
				bsm.setACCESS_TOKEN_SECRET(accessTokenSecretTF.getText());
				bsm.setTWEET_DELAY(BotSettingsManager.tweetsPerDayToMS(Integer.parseInt(delayTF.getText())));

				// Try and save the settings to a .tmp file for later use.
				BotSettingsManager.saveBotSettings((Exception ex) -> {
					if(ex!=null)new MsgBox(ex.toString());
				}, "bot.ser");

				bot.setEventLogger(new BotEventLogger() {

					@Override
					public void onTerminated() {
						begin.setDisable(false);
					}

					@Override
					public void onUnknownFailure(Exception e) {
						StringBuilder msg = new StringBuilder("");
						msg.append(new Date(System.currentTimeMillis()).toString() + ": An error occurred."
								+ System.lineSeparator());
						msg.append(e.getMessage());
						msg.append(System.lineSeparator());

						Platform.runLater(() -> {
							logEntries.add(0, (String) msg.toString());
						});
					}

					@Override
					public void onQueryingFailed(Exception e) {
						bot.interrupt();
						StringBuilder msg = new StringBuilder("");
						if (e instanceof TwitterException) {
							msg.append(new Date(System.currentTimeMillis()).toString()
									+ ": An error occurred querying for tweets..." + System.lineSeparator());
							msg.append(BotEventLogger.getEntryFailedText((TwitterException) e));
							msg.append(System.lineSeparator());
						} else {
							msg.append(new Date(System.currentTimeMillis()).toString()
									+ ": An error occurred querying for tweets... " + System.lineSeparator());
							msg.append(e.getMessage());
							msg.append(System.lineSeparator());
						}
						Platform.runLater(() -> {
							logEntries.add(0, (String) msg.toString());
						});
					}

					@Override
					public void onEntryFailed(Exception e) {
						StringBuilder msg = new StringBuilder("");
						if (e instanceof TwitterException) {
							msg.append(new Date(System.currentTimeMillis()).toString()
									+ ": An error occurred when retweeting..." + System.lineSeparator());
							msg.append(BotEventLogger.getEntryFailedText((TwitterException) e));
							msg.append(System.lineSeparator());
						} else {
							msg.append(new Date(System.currentTimeMillis()).toString() + " An error occurred."
									+ System.lineSeparator());
							msg.append(e.getMessage());
							msg.append(System.lineSeparator());
						}
						Platform.runLater(() -> {
							logEntries.add(0, (String) msg.toString());
						});
					}

					@Override
					public void onQuerySuccess(List<Status> statusList) {
						StringBuilder msg = new StringBuilder("");
						msg.append(new Date(System.currentTimeMillis()).toString() + ": Obtained " + statusList.size()
								+ " tweets.");
						Platform.runLater(() -> {
							logEntries.add(0, (String) msg.toString());
						});
					}

					@Override
					public void onEntrySuccess(Status s, int entries, int entryListSize) {
						StringBuilder msg = new StringBuilder("");
						msg.append(new Date(System.currentTimeMillis()).toString() + ": Successful sweepstakes entry!");
						msg.append(System.lineSeparator());
						msg.append("Tweet text: " + s.getText() + System.lineSeparator());
						msg.append(String.valueOf(entries) + "/" + String.valueOf(entryListSize)
								+ " tweets completed from current query.");
						msg.append(System.lineSeparator());
						Platform.runLater(() -> {
							logEntries.add(0, (String) msg.toString());
						});
					}

					@Override
					public TimerUpdate onTimerUpdate() {
						return x -> {
							Platform.runLater(() -> {
								msNextEntryLabel.setText("Time til next entry: " + String.valueOf(x / 1000) + "s");
							});
						};
					}
				});
				bot.start();
			}
		});
		buttonGroup.getChildren().addAll(begin, stop);

		vboxSettings.getChildren().addAll(apiKeyLabel, apiKeyTF, apiSecretLabel, apiSecretTF, accessTokenLabel,
				accessTokenTF, accessTokenSecretLabel, accessTokenSecretTF, delayLabel, delayTF, buttonGroup);
		settingsTab.setContent(vboxSettings);
		tabPane.getTabs().add(settingsTab);
	}

	static final ObservableList<String> logEntries = FXCollections.observableArrayList();
	final ListView<String> lv = new ListView<>(logEntries); // outside of method scope to make accessible in
	final Label msNextEntryLabel = new Label("Time til next entry: N/A");

	private void setupLogTab(Tab logTab, TabPane tabPane) {

		VBox vbox = new VBox();
		vbox.setPadding(new Insets(50, 50, 50, 50));
		vbox.setAlignment(Pos.CENTER);

		Label logBoxLabel = new Label("Sweepstakes entry log:");
		logBoxLabel.setPadding(new Insets(0, 0, 10, 0));

		lv.setEditable(true);

		vbox.getChildren().addAll(logBoxLabel, msNextEntryLabel, lv);
		logTab.setText("Entries");
		logTab.setContent(vbox);
		tabPane.getTabs().add(logTab);
	}

	public static void main(String[] args) {
		launch(args);
	}

}
