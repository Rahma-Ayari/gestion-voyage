import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Assurez-vous que le FXML est bien à src/main/resources/Destination.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationView.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Gestion Voyage");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
