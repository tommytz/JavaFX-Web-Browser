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
import javafx.scene.web.*;
import javafx.stage.Stage;

public class Main extends Application {
	private Stage primaryStage;
	private WebView browser = new WebView();
	private WebEngine engine = browser.getEngine();
	private WebHistory history; // To be used later in going forwards and backwards + browsing history

	private TextField urlField;

	// Regular expression pattern to match on valid URL with top level domain
	private Pattern domainPattern = Pattern.compile(
			"https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)?",
			Pattern.CASE_INSENSITIVE);
	private Matcher domainMatcher;

	// Event handler for loading a URL from user input
	private EventHandler<ActionEvent> loadURLHandler = new EventHandler<ActionEvent>() {
		public void handle(ActionEvent arg0) {
			// Check if user input matches a valid typed URL
			String urlInput = urlField.getText();
			domainMatcher = domainPattern.matcher(urlInput);

			if (domainMatcher.matches() || (toURL("https://" + urlInput) != null)) { // Accounts for URL using both http/https and URL not using it
				loadURL(urlInput);
			} else {
				// If not, then run it through a search engine
				searchEngineLookup(urlInput);
			}
		}
	};
	
	// Code to do something on page load failing or succeeding
	private ChangeListener<State> loadListener = new ChangeListener<State>() {
		public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
			if (newState == State.FAILED) {
				// TO DO: Make some kind of page for failing to load message
				System.out.println("Failed to load website.");
			}
			if (newState == State.SUCCEEDED) {
				// Updates text field and window title on page load
				urlField.setText(engine.getLocation());
				primaryStage.setTitle(engine.getTitle());
				System.out.println("Succesfully loaded " + engine.getLocation());
			}
		}
	};

	// Makes the website load!
	public void loadURL(String url) {
		String urlInput = toURL(url);
		// Turns a valid typed URL into one that can be loaded by adding https:// to it
		if (urlInput == null) {
			urlInput = toURL("https://" + url);
		}		
		engine.load(urlInput);

	}

	// To use with any URL that does not contain a top level domain. Will look it up
	// in a search engine instead of trying to load the URL.
	public void searchEngineLookup(String string) {
		engine.load(String.format("https://www.google.com/search?q=%s", string));
	}

	// Checks if string input is a valid URL. Returns the string if valid, otherwise
	// returns null. This mostly is checking for "https://" at the start.
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
		this.primaryStage = primaryStage;

		Button back = new Button("back");
		Button forward = new Button("forward");
		Button reload = new Button("reload");
		Button home = new Button("home");
		urlField = new TextField();
		Button launch = new Button("launch");

		HBox navigationBar = new HBox();
		navigationBar.getChildren().addAll(back, forward, reload, home, urlField, launch);

		// Set handlers for loading URL from text field
		launch.setOnAction(loadURLHandler);
		urlField.setOnAction(loadURLHandler);

		VBox root = new VBox();
		root.getChildren().addAll(navigationBar, browser);

		VBox.setVgrow(browser, Priority.ALWAYS);
		HBox.setHgrow(urlField, Priority.ALWAYS);
		
		// Add listener to do things on page load failing or succeeding
		engine.getLoadWorker().stateProperty().addListener(loadListener);

		// Load home page (make this a proper method later)
		engine.load("http://www.google.com");

		primaryStage.setScene(new Scene(root));
		primaryStage.sizeToScene();
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch();

	}
	
	public Main() {
		// TODO Auto-generated constructor stub
	}

}
