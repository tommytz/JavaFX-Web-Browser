import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class Bookmark {
	private String location;
	private String title;
	private Hyperlink link;
	private Browser browser;
	
	private final EventHandler<ActionEvent> onBookmarkClick = new EventHandler<ActionEvent>() {
		public void handle(ActionEvent arg0) {
			TabPane tabPane = browser.getTabPane();
			Tab createdTab = browser.createNewTab(location);
			tabPane.getTabs().add(tabPane.getTabs().size() - 1, createdTab); // Add to list one position before
			tabPane.getSelectionModel().select(createdTab);		
		}
	};
	
	public Bookmark(String location, String title, Browser browser) {
		this.location = location;
		this.title = title;
		this.browser = browser;
		this.link = new Hyperlink(location);
		link.setOnAction(onBookmarkClick);
	}

	public String getTitle() {
		return title;
	}

	public Hyperlink getLink() {
		return link;
	}
}
