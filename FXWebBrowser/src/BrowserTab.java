import java.net.URL;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.web.*;

public class BrowserTab {
	private Tab tab;
	private Browser browser;
	private final WebView webView = new WebView();
	private final WebEngine engine = webView.getEngine();
	private URL html404 = this.getClass().getResource("resources/index.html");

	private ChangeListener<State> loadListener = new ChangeListener<State>() {
		public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
			if (newState == State.FAILED) {
				// TO DO: Make some kind of page for failing to load message
				System.out.println("Failed to load website.");
				if (html404 != null) {
					engine.load(html404.toExternalForm());
				} else {
					System.out.println(getClass());
				}
			}
			if (newState == State.SUCCEEDED) {
				tab.setText(engine.getTitle());
				if (browser.getControl().getSelectTab().equals(BrowserTab.this)) {
					browser.setAddressBar(engine.getLocation());
					browser.setWindowTitle(engine.getTitle());
				}
				// Update browsing history for this tab when a new page is loaded
				browser.getControl().getBrowsingHistory().remove(BrowserTab.this);
				browser.getControl().getBrowsingHistory().put(BrowserTab.this, getHistory());
				System.out.println("Succesfully loaded " + engine.getLocation());
			}
		}
	};

	private EventHandler<Event> onTabCloseHandler = new EventHandler<Event>() {
		public void handle(Event arg0) {
			browser.getControl().onTabClose(tab, BrowserTab.this);
		}
	};

	public BrowserTab(String url, Tab tab, Browser browser) {
		this.tab = tab;
		this.browser = browser;
		tab.setOnClosed(onTabCloseHandler);
		tab.setContent(webView);
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
