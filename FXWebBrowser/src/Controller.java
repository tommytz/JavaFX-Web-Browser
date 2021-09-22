import javafx.scene.control.Tab;
import javafx.scene.web.*;
import java.util.*;

// Mostly for dealing with whichever tab we are focusing on
public class Controller {
	private final Map<Tab, BrowserTab> allTabs = new HashMap<Tab, BrowserTab>();
	private final Set<WebHistory> closedTabHistory = new HashSet<WebHistory>();
	private BrowserTab selectTab;
	
	// Constructor takes the default first tab that opens with the browser
	public Controller(Tab defaultTab, BrowserTab defaultBrowserTab) {
		this.selectTab = defaultBrowserTab;
		storeNewTab(defaultTab, defaultBrowserTab);
	}
	
	// Code to change WebEngine whenever we change tab
	public void onTabChange(Tab newTab) {
		selectTab = allTabs.get(newTab);
	}
	
	public void storeNewTab(Tab tab, BrowserTab browserTab) {
		allTabs.put(tab, browserTab);	
	}
	
	public void onTabClose(Tab tab, BrowserTab browserTab) {
		closedTabHistory.add(browserTab.getHistory());
		allTabs.remove(tab, browserTab);
	}
	
	public void loadURL(String url) {		
		selectTab.getEngine().load(url);
	}
	
	// If an address cannot be loaded it will instead be passed to a search engine
	public void searchEngineLookup(String string) {
		selectTab.getEngine().load("https://www.google.com/search?q=" + string);
	}
	
	public void goBack() {
		try{
			selectTab.getHistory().go(-1);
		} catch(IndexOutOfBoundsException e){
			System.out.println("First page in history, can't go back further");
		}
	}
	
	public void goForward() {
		try{
			selectTab.getHistory().go(1);
		} catch(IndexOutOfBoundsException e){
			System.out.println("Latest page in history, can't go forwards");
		}
	}
	
	public BrowserTab getSelectTab() {
		return selectTab;
	}

	public WebEngine getWebEngine() {
		return selectTab.getEngine();
	}

	public Map<Tab, BrowserTab> getAllTabs() {
		return allTabs;
	}

	public Set<WebHistory> getClosedTabHistory() {
		return closedTabHistory;
	}
}
