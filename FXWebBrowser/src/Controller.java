import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.control.Tab;
import javafx.scene.web.*;
import java.util.*;

public class Controller {
	private Map<Tab, WebView> allTabs = new HashMap<Tab, WebView>();
	private WebView webView;
	
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
				System.out.println("Succesfully loaded " + webView.getEngine().getLocation());
			}
		}
	};
	
	// Constructor takes the default first tab that opens with the browser
	public Controller(Tab firstTab) {
		this.webView = (WebView) firstTab.getContent();
		storeNewTab(firstTab);
	}
	
	// Code to change WebEngine whenever we change tab
	public void onTabChange(Tab newTab) {
		webView = allTabs.get(newTab);
		webView.getEngine().getLoadWorker().stateProperty().addListener(loadListener);
	}
	
	public void storeNewTab(Tab tab) {
		WebView newWebView = (WebView) tab.getContent();
		allTabs.put(tab, newWebView);
		newWebView.getEngine().getLoadWorker().stateProperty().addListener(loadListener);
		newWebView.getEngine().load(Browser.homePage);
		
	}
	
	// Makes the URL address load! Works with the engine.
	public void loadURL(String url) {		
		webView.getEngine().load(url);
	}
	
	// To use with any URL that does not contain a top level domain. Will look it up
	// in a search engine instead of trying to load the URL. Works with the engine.
	public void searchEngineLookup(String string) {
		webView.getEngine().load(String.format("https://www.google.com/search?q=%s", string));
	}
	
	public void goBack() {
		try{
			webView.getEngine().getHistory().go(-1);
		} catch(IndexOutOfBoundsException e){
			System.out.println("First page in history, can't go back further");
		}
	}
	
	public void goForward() {
		try{
			webView.getEngine().getHistory().go(1);
		} catch(IndexOutOfBoundsException e){
			System.out.println("Latest page in history, can't go forwards");
		}
	}
	
	public WebEngine getWebEngine() {
		return webView.getEngine();
	}

}
