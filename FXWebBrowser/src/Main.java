import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Main extends Application {
	private TextField urlField;
	private WebView webView;
	
	// Regex pattern to match on valid url
	private Pattern domainPattern = Pattern.compile(
			"[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)",
			Pattern.CASE_INSENSITIVE);
	
	private EventHandler<ActionEvent> loadURLHandler = new EventHandler<ActionEvent>() {

		@Override
		public void handle(ActionEvent arg0) {
			// TODO Auto-generated method stub
			loadURL(urlField.getText());

//			Matcher m = domainPattern.matcher((urlField.getText()));
			// Matcher domainMatcher = domainPattern.matcher(urlField.getText());
			// System.out.println(m.group(0));
//			System.out.println(m.matches());
			// www.
		}
	};

	public Main() {
		// TODO Auto-generated constructor stub
	}

	public void loadURL(String url) {
		String inputURL = toURL(url);
		if (inputURL == null) {
			inputURL = toURL("https://" + url);
//			System.out.printf("Input missing \"https://\", replaced with: https://%s%n", url);
		}
		// Code to do something on page load failing
		/*WebEngine webEngine = webView.getEngine();

		webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
			public void changed(ObservableValue ov, State oldState, State newState) {
				if (newState == State.FAILED) {
					System.out.println("Failed.");
				}
			}
		});
		webEngine.load(inputURL);*/
		webView.getEngine().load(inputURL);
	}

	// To use later with any url not containing a top level domain
	public void searchInput(String string) {
		webView.getEngine().load(String.format("https://www.google.com/search?q=%s", string));
	}

	/*
	 * Checks if string input is a valid URL. Returns the string if valid, otherwise
	 * returns null.
	 */
	private String toURL(String string) {
		try {
			return new URL(string).toExternalForm();
		} catch (MalformedURLException exception) {
			return null;
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		Button launchBtn = new Button("Launch");
		urlField = new TextField();

		HBox hBox = new HBox();
		hBox.getChildren().addAll(launchBtn, urlField);

		webView = new WebView();
		webView.getEngine().load("http://www.google.com");

		// Set handlers for loading URL from text field
		launchBtn.setOnAction(loadURLHandler);
		urlField.setOnAction(loadURLHandler);

		VBox root = new VBox();
		root.getChildren().addAll(hBox, webView);

		VBox.setVgrow(webView, Priority.ALWAYS);
		HBox.setHgrow(urlField, Priority.ALWAYS);

		primaryStage.setScene(new Scene(root));
		primaryStage.sizeToScene();
		primaryStage.show();

	}

	public static void main(String[] args) {
		launch();

	}

}
