package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * Show a simple form with a message, convenient for displaying error messages.
 * @author ssepa
 *
 */
public class MsgBox{

	private String mMsg;
	
	public MsgBox(String msg) {
		mMsg = msg;
		create();
	}
	
	private void create() {
		try {
			Stage primaryStage = new Stage();
			FlowPane root = new FlowPane();
			Scene scene = new Scene(root);
			
			Label msgLabel = new Label(mMsg);
			msgLabel.setPadding(new Insets(50,50,50,50));
			root.getChildren().add(msgLabel);

			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setAlwaysOnTop(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
