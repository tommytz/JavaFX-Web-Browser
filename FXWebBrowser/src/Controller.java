import javafx.scene.control.Tab;
import javafx.scene.web.*;
import java.util.*;

// Mostly for dealing with whichever tab we are focusing on
public class Controller {
	private Map<Tab, BrowserTab> allTabs = new HashMap<Tab, BrowserTab>();
	private BrowserTab focusTab;
	
	// Constructor takes the default first tab that opens with the browser
	public Controller(Tab defaultTab, BrowserTab defaultBrowserTab) {
		this.focusTab = defaultBrowserTab;
		storeNewTab(defaultTab, defaultBrowserTab);
	}
	
	// Code to change WebEngine whenever we change tab
	public void onTabChange(Tab newFocusTab) {
		focusTab = allTabs.get(newFocusTab);
	}
	
	public void storeNewTab(Tab tab, BrowserTab browserTab) {
		allTabs.put(tab, browserTab);	
	}
	
	// Makes the URL address load! Works with the engine.
	public void loadURL(String url) {		
		focusTab.getEngine().load(url);
	}
	
	// To use with any URL that does not contain a top level domain. Will look it up
	// in a search engine instead of trying to load the URL. Works with the engine.
	public void searchEngineLookup(String string) {
		focusTab.getEngine().load(String.format("https://www.google.com/search?q=%s", string));
	}
	
	public void goBack() {
		try{
			focusTab.getHistory().go(-1);
		} catch(IndexOutOfBoundsException e){
			System.out.println("First page in history, can't go back further");
		}
	}
	
	public void goForward() {
		try{
			focusTab.getHistory().go(1);
		} catch(IndexOutOfBoundsException e){
			System.out.println("Latest page in history, can't go forwards");
		}
	}
	
	public WebEngine getWebEngine() {
		return focusTab.getEngine();
	}

}
