package Modelo;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		URL fxmlUrl = getClass().getResource("/Vista/EventosView.fxml");

		Parent root = FXMLLoader.load(fxmlUrl);

		primaryStage.setTitle("Gestor de Eventos");
		Scene scene = new Scene(root, 1000, 700);

		// Cargar la hoja de estilos
		URL cssUrl = getClass().getResource("/Modelo/application.css");
		if (cssUrl != null) {
			scene.getStylesheets().add(cssUrl.toExternalForm());
		}

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);

	}
}