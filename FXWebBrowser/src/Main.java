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
	private WebView webView = new WebView();
	private WebEngine webEngine = webView.getEngine();

	private TextField urlField;

	// Regular expression pattern to match on valid url
	private Pattern domainPattern = Pattern.compile(
			"[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)",
			Pattern.CASE_INSENSITIVE);
	private Matcher domainMatcher;

	private EventHandler<ActionEvent> loadURLHandler = new EventHandler<ActionEvent>() {

		@Override
		public void handle(ActionEvent arg0) {
			// Check if user input matches a valid typed URL
			String urlInput = urlField.getText();
			domainMatcher = domainPattern.matcher(urlInput);

			if (domainMatcher.matches()) {
				loadURL(urlInput);
			} else {
				// If not, then run it through a search engine
				searchEngineLookup(urlInput);
			}
		}
	};

	public Main() {
		// TODO Auto-generated constructor stub
	}

	public void loadURL(String url) {
		String urlInput = toURL(url);
		// Turns a valid typed URL into one that can be loaded by adding https:// to it
		if (urlInput == null) {
			urlInput = toURL("https://" + url);
		}
		// Code to do something on page load failing
		/*
		 * NEED TO ASK WHY THIS CODE IS RUNNING MULTIPLE TIMES IF YOU TRY A FAIL MORE THAN ONCE
		 */
		webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
			public void changed(ObservableValue ov, State oldState, State newState) {
				if (newState == State.FAILED) {
					System.out.println("Failed.");
				}
			}
		});
		webEngine.load(urlInput);
	}

	// To use later with any url not containing a top level domain
	public void searchEngineLookup(String string) {
		webView.getEngine().load(String.format("https://www.google.com/search?q=%s", string));
	}

	// Checks if string input is a valid URL. Returns the string if valid, otherwise
	// returns null.
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

		// Set handlers for loading URL from text field
		launchBtn.setOnAction(loadURLHandler);
		urlField.setOnAction(loadURLHandler);

		VBox root = new VBox();
		root.getChildren().addAll(hBox, webView);

		VBox.setVgrow(webView, Priority.ALWAYS);
		HBox.setHgrow(urlField, Priority.ALWAYS);
		
		// Load home page (make this a proper method later)
		webEngine.load("http://www.google.com");

		primaryStage.setScene(new Scene(root));
		primaryStage.sizeToScene();
		primaryStage.show();

	}

	public static void main(String[] args) {
		launch();

	}

}
