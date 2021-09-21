import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.web.*;

// Moved the code that deals with listening to the engine to here.
public class BrowserTab {
	private WebView webView = new WebView();
	private WebEngine engine = webView.getEngine();
	
	// Code to do something on page load failing or succeeding. Works with the engine.
	private ChangeListener<State> loadListener = new ChangeListener<State>() {
		public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
			if (newState == State.FAILED) {
				// TO DO: Make some kind of page for failing to load message
				System.out.println("Failed to load website.");
			}
			if (newState == State.SUCCEEDED) {
				// Updates text field and window title on page load
//				urlField.setText(engine.getLocation());
//				primaryStage.setTitle(engine.getTitle());
				System.out.println("Succesfully loaded " + engine.getLocation());
			}
		}
	};

	public BrowserTab(String url) {
		engine.getLoadWorker().stateProperty().addListener(loadListener);
		engine.load(url);
	}

	public WebView getWebView() {
		return webView;
	}

	public WebEngine getEngine() {
		return engine;
	}

	public WebHistory getHistory() {
		return engine.getHistory();
	}
}
