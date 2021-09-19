import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Main extends Application {

	public Main() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		Button launchBtn = new Button("Launch");
		TextField urlField = new TextField();
		
		HBox hBox = new HBox();
		hBox.getChildren().addAll(launchBtn, urlField);
		
		WebView webView = new WebView();
		webView.getEngine().load("http://www.google.com");
		
		VBox root = new VBox();
		root.getChildren().addAll(hBox, webView);
		
		VBox.setVgrow(webView, Priority.ALWAYS);
		HBox.setHgrow(urlField, Priority.ALWAYS);
		
		primaryStage.setScene(new Scene(root));
		primaryStage.sizeToScene();
		primaryStage.show();

	}

	public static void main(String[] args) {
		launch();

	}

}
