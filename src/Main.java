import Controller.MainCoordinator;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.MalformedURLException;

public class Main extends Application {

    MainCoordinator coordinator;

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/View/mainLayout.fxml"));

        Scene scene = new Scene(root,800, 700);
        primaryStage.setTitle("Gapminder Visualization Tool");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    @FXML
    private void Init() throws MalformedURLException {
        coordinator = new MainCoordinator();
        coordinator.initialize();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
