import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebHistory.Entry;
import javafx.stage.Stage;

public class Browser extends Application {

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
	private final MenuItem browsingHistory = new MenuItem("Browsing History");
	private final MenuItem viewPageSource = new MenuItem("View page source");
	private final MenuItem setHomeScreen = new MenuItem("Set home screen");
	private final MenuItem zoomIn = new MenuItem("Zoom in");
	private final MenuItem zoomOut = new MenuItem("Zoom out");
	private final MenuItem defaultZoom = new MenuItem("Default zoom");

	private final Tab addTab = new Tab();
	private Tab pageSourceTab;
	private Tab browsingHistoryTab;

	private ImageView backIcon;
	private ImageView forwardIcon;
	private ImageView reloadIcon;
	private ImageView homeIcon;
	private ImageView loadIcon;
	private ImageView addBookmarkIcon;
	private ImageView bookmarkIcon;
	private ImageView menuIcon;
	private ImageView addIcon;
	
	private final Comparator<WebHistory.Entry> dateOrder = new Comparator<WebHistory.Entry>() {

		@Override
		public int compare(Entry o1, Entry o2) {
			// TODO Auto-generated method stub
			return o2.getLastVisitedDate().compareTo(o1.getLastVisitedDate());
		}};

	// Regular expression patterns to match on valid URL with top level domain
	// Adapted from https://regexr.com/37i6s
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
				} else if (newTab != addTab && newTab != pageSourceTab && newTab != browsingHistoryTab) {
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

	private final EventHandler<ActionEvent> viewPageSourceHandler = new EventHandler<ActionEvent>() {
		public void handle(ActionEvent arg0) {
			// Adapted from https://zetcode.com/java/readwebpage/
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(control.getWebEngine().getLocation())).GET()
					.build();
			HttpResponse<String> response;
			try {
				response = client.send(request, HttpResponse.BodyHandlers.ofString());
				TextArea htmlText = new TextArea(response.body());
				htmlText.setWrapText(true);

				pageSourceTab = new Tab((String.format("%s Page Source", control.getWebEngine().getTitle())), htmlText);
				tabPane.getTabs().add(tabPane.getTabs().size() - 1, pageSourceTab); // Add to list one position before
				tabPane.getSelectionModel().select(pageSourceTab);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	private final EventHandler<ActionEvent> generateHistoryPageHandler = new EventHandler<ActionEvent>() {

		@Override
		public void handle(ActionEvent arg0) {
			BorderPane historyPage = new BorderPane();
			Text historyTitle = new Text("Browsing History");
			historyTitle.setFont(new Font("Arial Bold", 24));
			VBox browsingHistoryDisplay = new VBox();
			browsingHistoryDisplay.setPadding(new Insets(10));
			historyPage.setTop(historyTitle);
			historyPage.setCenter(browsingHistoryDisplay);
			historyPage.setPadding(new Insets(10));
			
			List<WebHistory.Entry> entries = new ArrayList<WebHistory.Entry>();

			for (WebHistory history : control.getBrowsingHistory().values()) {
				entries.addAll(history.getEntries());
			}
			entries.sort(dateOrder); // Had problems with sorting due to sometimes entries having null dates
			
			// Create hyperlinks in the history page
			for (WebHistory.Entry entry : entries) {
				Hyperlink link = new Hyperlink(entry.getUrl());
				link.setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent arg0) {
						Tab createdTab = createNewTab(entry.getUrl());
						tabPane.getTabs().add(tabPane.getTabs().size() - 1, createdTab); // Add to list one position
																							// before
						tabPane.getSelectionModel().select(createdTab);
					}
				});
				browsingHistoryDisplay.getChildren().add(link);
			}
			browsingHistoryTab = new Tab("History", historyPage);
			tabPane.getTabs().add(tabPane.getTabs().size() - 1, browsingHistoryTab); // Add to list one position before
			tabPane.getSelectionModel().select(browsingHistoryTab);
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
		back.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent arg0) {
				control.goBack();
			}
		});
		forward.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent arg0) {
				control.goForward();
			}
		});
		reload.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent arg0) {
				control.getWebEngine().reload();
			}
		});
		home.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent arg0) {
				control.getWebEngine().load(homePage);
			}
		});
		bookmark.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent arg0) {
				createNewBookmark(control.getWebEngine().getLocation(), control.getWebEngine().getTitle());
			}
		});
	}
	
	private void setupMenuItems() {
		viewPageSource.setOnAction(viewPageSourceHandler);
		browsingHistory.setOnAction(generateHistoryPageHandler);
		setHomeScreen.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				homePage = control.getWebEngine().getLocation();
				
			}});
		zoomIn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				control.getWebView().setZoom(1.25);
				
			}});
		zoomOut.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				control.getWebView().setZoom(0.75);
				
			}});
		defaultZoom.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				control.getWebView().setZoom(1.0);
				
			}});
	}

	private void importButtonIcons() {
		try {
			backIcon = new ImageView(new Image(new FileInputStream("img/icons8-back-50.png")));
			forwardIcon = new ImageView(new Image(new FileInputStream("img/icons8-forward-50.png")));
			reloadIcon = new ImageView(new Image(new FileInputStream("img/icons8-restart-50.png")));
			homeIcon = new ImageView(new Image(new FileInputStream("img/icons8-home-50.png")));
			loadIcon = new ImageView(new Image(new FileInputStream("img/icons8-forward-arrow-50.png")));
			addBookmarkIcon = new ImageView(new Image(new FileInputStream("img/icons8-add-bookmark-50.png")));
			bookmarkIcon = new ImageView(new Image(new FileInputStream("img/icons8-bookmark-50.png")));
			menuIcon = new ImageView(new Image(new FileInputStream("img/icons8-menu-vertical-50.png")));
			addIcon = new ImageView(new Image(new FileInputStream("img/icons8-plus-48.png")));
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

	public Tab createNewTab(String url) {
		Tab newTab = new Tab("New Tab");
		BrowserTab newBrowserTab = new BrowserTab(url, newTab, this);
		control.storeNewTab(newTab, newBrowserTab);
		return newTab;
	}
	
	public void createNewBookmark(String location, String title) {
		Bookmark newBookmark = new Bookmark(location, title, Browser.this);
		bookmarksBar.getChildren().add(newBookmark.getButton());
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

	public TabPane getTabPane() {
		return tabPane;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		this.primaryStage = primaryStage;
		
		bookmarksBar.setSpacing(2);
		bookmarksBar.setPadding(new Insets(2));
		createNewBookmark(homePage, "Google");

		// Set handlers for loading URL from text field
		load.setOnAction(urlLoadingHandler);
		addressBar.setOnAction(urlLoadingHandler);

		importButtonIcons();
		setupNavigationButtons();
		navigationBar.getChildren().addAll(back, forward, reload, home, addressBar, load, bookmark, menu);
		navigationBar.setSpacing(2);
		navigationBar.setPadding(new Insets(2));

		// Setting up side menu
		setupMenuItems();
		menu.getItems().addAll(viewPageSource, browsingHistory, setHomeScreen, zoomIn, zoomOut, defaultZoom);

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
