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
	// change new tab button to be part of the tabpane and dynamically stay on the
	// right
	// Method to get rid of BrowserTabs when a tab is closed (to stop them
	// persisting)
	// Method to save history object from closed tab to use in browsing history
	// Site couldn't be reached error pane
	// history tab and bookmarks bar
	// Settings: Change homescreen, color, zoom level
	// Progress bar for page loading
	// Add symbols for buttons
	// Context menu on right click
	// Add shadow to buttons on hover
	// new tab on CTRL+T and history on CTRL+H
	// Print, html source

	private Stage primaryStage;
	private Scene scene;
	private Controller control;
	private final TextField addressBar = new TextField();
	private final TabPane tabPane = new TabPane();
	public String homePage = "http://www.google.com";

	private Button back = new Button("back");
	private Button forward = new Button("forward");
	private Button reload = new Button("reload");
	private Button home = new Button("home");
	private Button launch = new Button("launch");
	Tab addTab = new Tab("+");

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

	// Listener to change the engine when we select a tab
	private ChangeListener<Tab> tabChangeListener = new ChangeListener<Tab>() {
		@Override
		public void changed(ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) {
			if (control != null) {
				// Clicking the add new tab button
				if (newTab == addTab) {
					tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2);
					Tab createdTab = createNewTab(homePage);
					tabPane.getTabs().add(tabPane.getTabs().size() - 1, createdTab);
					tabPane.getSelectionModel().select(createdTab);
					
				// On any other tab being selected
				} else if (newTab != addTab) {
					System.out.println("Tab Selection changed");
					control.onTabChange(newTab);
					// If the page has loaded then we can dynamically change the window title and
					// address bar
					if (control.getWebEngine().getLoadWorker().getState() == State.SUCCEEDED) {
						primaryStage.setTitle(control.getWebEngine().getTitle());
						addressBar.setText(control.getWebEngine().getLocation());
					}
				}
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

	private void setupNavigationButtons() {
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

	private Tab createNewTab(String url) {
		Tab newTab = new Tab("New Tab");
		BrowserTab newBrowserTab = new BrowserTab(url, newTab, this);
		control.storeNewTab(newTab, newBrowserTab);
		return newTab;
	}

	public void setWindowTitle(String string) {
		primaryStage.setTitle(string);
	}

	public void setAddressBar(String string) {
		addressBar.setText(string);
	}

	public BrowserTab getControlSelectTab() {
		return control.getSelectTab();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		this.primaryStage = primaryStage;

		setupNavigationButtons();
		navigationBar.getChildren().addAll(back, forward, reload, home, addressBar, launch);

		tabPane.setTabDragPolicy(TabDragPolicy.REORDER);
		tabPane.getSelectionModel().selectedItemProperty().addListener(tabChangeListener);

		// Set handlers for loading URL from text field
		launch.setOnAction(urlLoadingHandler);
		addressBar.setOnAction(urlLoadingHandler);

		// Setup default tab on open and instantiate controller
		addTab.setClosable(false);
		Tab firstTab = new Tab("New Tab");
		BrowserTab firstBrowserTab = new BrowserTab(homePage, firstTab, this);
		tabPane.getTabs().addAll(firstTab, addTab);
		control = new Controller(firstTab, firstBrowserTab);

		VBox root = new VBox();
		root.getChildren().addAll(navigationBar, tabPane);

		VBox.setVgrow(tabPane, Priority.ALWAYS);
		HBox.setHgrow(addressBar, Priority.ALWAYS);

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
