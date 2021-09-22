import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Browser extends Application {
	// TO DO:
	// history tab and bookmarks bar
	// Site couldn't be reached error pane
	// Settings: Change homescreen, color, zoom level
	// Context menu on right click
	// new tab on CTRL+T and history on CTRL+H
	// Print, html source

	private Stage primaryStage;
	private Scene scene;
	private Controller control;
	private final HBox navigationBar = new HBox();
	private final HBox bookmarksBar = new HBox();
	private final TextField addressBar = new TextField();
	private final TabPane tabPane = new TabPane();
	public String homePage = "http://www.google.com";

	private final Button back = new Button();
	private final Button forward = new Button();
	private final Button reload = new Button();
	private final Button home = new Button();
	private final Button load = new Button();
	private final Button bookmark = new Button();
	private final MenuButton menu = new MenuButton();
	private final Tab addTab = new Tab();
	
	private ImageView backIcon;
	private ImageView forwardIcon;
	private ImageView reloadIcon;
	private ImageView homeIcon;
	private ImageView loadIcon;
	private ImageView addBookmarkIcon;
	private ImageView bookmarkIcon;
	private ImageView menuIcon;
	private ImageView addIcon;

	// Regular expression patterns to match on valid URL with top level domain
	private final Pattern httpsPattern = Pattern.compile(
			"https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)?",
			Pattern.CASE_INSENSITIVE);
	private final Pattern noHttpsPattern = Pattern.compile(
			"[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)?",
			Pattern.CASE_INSENSITIVE);
	private Matcher httpsMatcher;
	private Matcher noHttpsMatcher;

	private final EventHandler<ActionEvent> urlLoadingHandler = new EventHandler<ActionEvent>() {
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
	private final ChangeListener<Tab> tabChangeListener = new ChangeListener<Tab>() {
		public void changed(ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) {
			if (control != null) {
				// Clicking the add new tab button
				if (newTab == addTab) {
					Tab createdTab = createNewTab(homePage);
					tabPane.getTabs().add(tabPane.getTabs().size() - 1, createdTab); // Add to list one position before
					tabPane.getSelectionModel().select(createdTab);

					// On any other tab being selected
				} else if (newTab != addTab) {
					System.out.println("Tab Selection changed to " + newTab.getText());
					control.onTabChange(newTab);
					// If page has loaded we can dynamically change window title and address bar
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
		
		menu.getItems().addAll(new MenuItem("Test1"), new MenuItem("test2"));
	}
	
	private void importButtonIcons() {
		try {
			backIcon = new ImageView(new Image(new FileInputStream("resources/icons8-back-50.png")));
			forwardIcon = new ImageView(new Image(new FileInputStream("resources/icons8-forward-50.png")));
			reloadIcon = new ImageView(new Image(new FileInputStream("resources/icons8-restart-50.png")));
			homeIcon = new ImageView(new Image(new FileInputStream("resources/icons8-home-50.png")));
			loadIcon = new ImageView(new Image(new FileInputStream("resources/icons8-forward-arrow-50.png")));
			addBookmarkIcon = new ImageView(new Image(new FileInputStream("resources/icons8-add-bookmark-50.png")));
			bookmarkIcon = new ImageView(new Image(new FileInputStream("resources/icons8-bookmark-50.png")));
			menuIcon = new ImageView(new Image(new FileInputStream("resources/icons8-menu-vertical-50.png")));
			addIcon = new ImageView(new Image(new FileInputStream("resources/icons8-plus-48.png")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		backIcon.setFitHeight(20);
		forwardIcon.setFitHeight(20);
		reloadIcon.setFitHeight(20);
		homeIcon.setFitHeight(20);
		loadIcon.setFitHeight(20);
		addBookmarkIcon.setFitHeight(20);
		bookmarkIcon.setFitHeight(20);
		menuIcon.setFitHeight(20);
		addIcon.setFitHeight(20);
		
		backIcon.setPreserveRatio(true);
		forwardIcon.setPreserveRatio(true);
		reloadIcon.setPreserveRatio(true);
		homeIcon.setPreserveRatio(true);
		loadIcon.setPreserveRatio(true);
		addBookmarkIcon.setPreserveRatio(true);
		bookmarkIcon.setPreserveRatio(true);
		menuIcon.setPreserveRatio(true);
		addIcon.setPreserveRatio(true);
		
		back.setGraphic(backIcon);
		forward.setGraphic(forwardIcon);
		reload.setGraphic(reloadIcon);
		home.setGraphic(homeIcon);
		load.setGraphic(loadIcon);
		bookmark.setGraphic(addBookmarkIcon);
		menu.setGraphic(menuIcon);
		addTab.setGraphic(addIcon);
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

	public Controller getControl() {
		return control;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		this.primaryStage = primaryStage;

		// Set handlers for loading URL from text field
		load.setOnAction(urlLoadingHandler);
		addressBar.setOnAction(urlLoadingHandler);
		
		importButtonIcons();
		setupNavigationButtons();
		navigationBar.getChildren().addAll(back, forward, reload, home, addressBar, load, bookmark, menu);

		tabPane.setTabDragPolicy(TabDragPolicy.REORDER);
		tabPane.getSelectionModel().selectedItemProperty().addListener(tabChangeListener);

		// Setup default tab on open and instantiate controller
		addTab.setClosable(false);
		Tab firstTab = new Tab("New Tab");
		BrowserTab firstBrowserTab = new BrowserTab(homePage, firstTab, this);
		tabPane.getTabs().addAll(firstTab, addTab);
		control = new Controller(firstTab, firstBrowserTab);

		// Put all UI elements into root vbox
		VBox root = new VBox();
		root.getChildren().addAll(navigationBar, bookmarksBar, tabPane);

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
