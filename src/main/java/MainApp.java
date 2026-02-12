
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

 @Override
public void start(Stage stage) throws Exception {

    Parent root = FXMLLoader.load(
            getClass().getResource("/VolView.fxml")
    );

    Scene scene = new Scene(root);

    stage.setScene(scene);
    stage.show();
}

}
