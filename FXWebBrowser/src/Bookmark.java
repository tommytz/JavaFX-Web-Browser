import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public class Bookmark {
	private final Button button = new Button();
	private String location;
	private Browser browser;
	
	private final EventHandler<ActionEvent> onBookmarkClick = new EventHandler<ActionEvent>() {
		public void handle(ActionEvent arg0) {
			browser.getControl().getWebEngine().load(location);
		}
	};
	
	public Bookmark(String location, String title, Browser browser) {
		this.location = location;
		this.browser = browser;
		button.setText(title);
		button.setOnAction(onBookmarkClick);
	}

	public Button getButton() {
		return button;
	}
}
