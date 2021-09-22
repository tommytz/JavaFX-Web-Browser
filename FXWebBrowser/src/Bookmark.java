import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;

public class Bookmark {
	private final Button button = new Button();
	private String location;
	private String title;
	private Browser browser;
	
	private final EventHandler<ActionEvent> onBookmarkClick = new EventHandler<ActionEvent>() {

		@Override
		public void handle(ActionEvent arg0) {
			Tab createdTab = browser.createNewTab(location);
			browser.getTabPane().getTabs().add(browser.getTabPane().getTabs().size() - 1, createdTab); // Add to list one position
																				// before
			browser.getTabPane().getSelectionModel().select(createdTab);
		}
	};
	
	
	public Bookmark(String location, String title, Browser browser) {
		this.location = location;
		this.title = title;
		this.browser = browser;
		button.setText(title);
		button.setOnAction(onBookmarkClick);
	}

	public Button getButton() {
		return button;
	}
}
