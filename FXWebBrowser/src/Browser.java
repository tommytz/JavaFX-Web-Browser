import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.web.*;
import javafx.stage.Stage;

public class Browser extends Application {
	private Controller control;
	private Stage primaryStage;
	private TabPane tabPane = new TabPane();
	private Tab currentTab;
	
	private TextField addressBar = new TextField();
	public static String homePage = "http://www.google.com";

	// Regular expression pattern to match on valid URL with top level domain
	private Pattern domainPattern = Pattern.compile(
			"https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)?",
			Pattern.CASE_INSENSITIVE);
	private Matcher domainMatcher;
	
	// Event handler for loading a URL from user input, works with the buttons
	private EventHandler<ActionEvent> urlLoadingHandler = new EventHandler<ActionEvent>() {
		public void handle(ActionEvent arg0) {
			// Check if user input matches a valid typed URL
			String address = addressBar.getText();
			domainMatcher = domainPattern.matcher(address);

			if (domainMatcher.matches()){
				control.loadURL(address);
			} else if (toURL("https://" + address) != null) {
				control.loadURL(toURL("https://" + address));
			} else {
				// If not, then run it through a search engine
				control.searchEngineLookup(address);
			}
		}
	};
	
	// Checks if string input is a valid URL. Returns the string if valid, otherwise
	// returns null. This mostly is checking for "https://" at the start. Works with user input.
	private String toURL(String string) {
		try {
			return new URL(string).toExternalForm();
		} catch (MalformedURLException exception) {
			return null;
		}
	}
	
	// These buttons should instead invoke controller methods
	private void setupNavigationButtons(Button back, Button forward, Button reload, Button home) {
		back.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			public void handle(MouseEvent arg0) {
				control.goBack();
			}});
		forward.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			public void handle(MouseEvent arg0) {
				control.goForward();
			}});
		reload.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			public void handle(MouseEvent arg0) {
				control.getWebEngine().reload();
			}});
		home.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			public void handle(MouseEvent arg0) {
				control.getWebEngine().load(homePage);
			}});
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		this.primaryStage = primaryStage;

		Button back = new Button("back");
		Button forward = new Button("forward");
		Button reload = new Button("reload");
		Button home = new Button("home");
		Button launch = new Button("launch");
		setupNavigationButtons(back, forward, reload, home);
		
		HBox navigationBar = new HBox();
		navigationBar.getChildren().addAll(back, forward, reload, home, addressBar, launch);
		
		HBox tabBar = new HBox();
		Button newTab = new Button("+");
		tabBar.getChildren().addAll(tabPane, newTab);
		
		Tab firstTab = new Tab("Home", new WebView());
		currentTab = firstTab;
		tabPane.getTabs().add(firstTab);
		control = new Controller(firstTab);

		// Set handlers for loading URL from text field
		launch.setOnAction(urlLoadingHandler);
		addressBar.setOnAction(urlLoadingHandler);

		VBox root = new VBox();
		root.getChildren().addAll(navigationBar, tabBar);

		VBox.setVgrow(tabPane, Priority.ALWAYS);
		HBox.setHgrow(addressBar, Priority.ALWAYS);

		primaryStage.setScene(new Scene(root));
		primaryStage.sizeToScene();
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch();

	}
	
	public Browser() {
		// TODO Auto-generated constructor stub
	}

}
