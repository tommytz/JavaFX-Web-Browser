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
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Browser extends Application {
	// TO DO:
	// change new tab button to be part of the tabpane and dynamically stay on the right
	// Site couldn't be reached error pane
	// history tab and bookmarks bar
	// Settings: Change homescreen, color, zoom level
	// Progress bar for page loading
	// Add symbols for buttons
	// Context menu on right click
	// Print, html source
	
	
	private Stage primaryStage;
	private Scene scene;
	private Controller control;
	private final TextField addressBar = new TextField();
	private final TabPane tabPane = new TabPane();
	public String homePage = "http://www.google.com";
	
	private final HBox navigationBar = new HBox();
	private final HBox tabBar = new HBox(); // To be removed when new tab button is in tab pane

	// Regular expression patterns to match on valid URL with top level domain
	private final Pattern httpsPattern = Pattern.compile(
			"https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)?",
			Pattern.CASE_INSENSITIVE);
	private final Pattern noHttpsPattern = Pattern.compile(
			"[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)?",
			Pattern.CASE_INSENSITIVE);
	private Matcher httpsMatcher;
	private Matcher noHttpsMatcher;

	private EventHandler<ActionEvent> urlLoadingHandler = new EventHandler<ActionEvent>() {
		public void handle(ActionEvent arg0) {
			// Check if user input matches a valid typed URL
			String address = addressBar.getText();
			httpsMatcher = httpsPattern.matcher(address);
			noHttpsMatcher = noHttpsPattern.matcher(address);

			if (httpsMatcher.matches()) {
				control.loadURL(address);
			} else if (noHttpsMatcher.matches()) {
				control.loadURL(toURL("https://" + address));
			} else {
				// If not, then run it through a search engine
				control.searchEngineLookup(address);
			}
		}
	};

	private EventHandler<ActionEvent> newTabHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent arg0) {
			addNewTab(homePage);
		}
	};

	// Listener to change the engine when we select a tab
	private ChangeListener<Tab> tabChangeListener = new ChangeListener<Tab>() {
		@Override
		public void changed(ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) {
			System.out.println("Tab Selection changed");
			control.onTabChange(newTab);
			// If the page has loaded then we can dynamically change the window title and address bar
			if (control.getWebEngine().getLoadWorker().getState() == State.SUCCEEDED) {
				primaryStage.setTitle(control.getWebEngine().getTitle());
				addressBar.setText(control.getWebEngine().getLocation());
			}
		}
	};

	private String toURL(String string) {
		try {
			return new URL(string).toExternalForm();
		} catch (MalformedURLException exception) {
			return null;
		}
	}

	private void setupNavigationButtons(Button back, Button forward, Button reload, Button home) {
		back.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			public void handle(MouseEvent arg0) {
				control.goBack();
			}
		});
		forward.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			public void handle(MouseEvent arg0) {
				control.goForward();
			}
		});
		reload.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			public void handle(MouseEvent arg0) {
				control.getWebEngine().reload();
			}
		});
		home.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			public void handle(MouseEvent arg0) {
				control.getWebEngine().load(homePage);
			}
		});
	}

	private void addNewTab(String url) {
		Tab newTab = new Tab("New Tab");
		BrowserTab newBrowserTab = new BrowserTab(url, newTab, this);
		control.storeNewTab(newTab, newBrowserTab);
		tabPane.getTabs().add(newTab);
		tabPane.getSelectionModel().select(newTab);
	}

	public void setWindowTitle(String string) {
		primaryStage.setTitle(string);
	}

	public void setAddressBar(String string) {
		addressBar.setText(string);
	}

	public BrowserTab getControlFocusTab() {
		return control.getFocusTab();
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
		navigationBar.getChildren().addAll(back, forward, reload, home, addressBar, launch);

		Button newTab = new Button("+"); // This button is what is causing the distortion of the vbox on the right
		newTab.setOnAction(newTabHandler);
		tabPane.setTabDragPolicy(TabDragPolicy.REORDER);
		tabPane.getSelectionModel().selectedItemProperty().addListener(tabChangeListener);
		tabBar.getChildren().addAll(tabPane, newTab);
		
		// Set handlers for loading URL from text field
		launch.setOnAction(urlLoadingHandler);
		addressBar.setOnAction(urlLoadingHandler);
		
		// Setup default tab on open and instantiate controller
		Tab firstTab = new Tab("New Tab");
		BrowserTab firstBrowserTab = new BrowserTab(homePage, firstTab, this);
		tabPane.getTabs().add(firstTab);
		control = new Controller(firstTab, firstBrowserTab);

		VBox root = new VBox();
		root.getChildren().addAll(navigationBar, tabBar); // Will change tabBar to tabPane

		VBox.setVgrow(tabBar, Priority.ALWAYS);
		HBox.setHgrow(addressBar, Priority.ALWAYS);
		HBox.setHgrow(tabPane, Priority.ALWAYS);

		scene = new Scene(root);
		primaryStage.setScene(scene);
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
