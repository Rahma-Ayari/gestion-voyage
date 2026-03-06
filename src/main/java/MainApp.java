
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import Service.ServiceVol;

import java.io.IOException;
import java.sql.SQLException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Chargement du fichier FXML
            // Note : Adaptez le chemin si votre FXML est dans un sous-dossier (ex: "/View/Destination.fxml")
             ServiceVol serviceVol = new ServiceVol();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/VolView.fxml"));
            Parent root = loader.load();

            // Création de la scène
            Scene scene = new Scene(root);
            primaryStage.setMaximized(true); // plein écran
            // Configuration du Stage (Fenêtre)
            primaryStage.setTitle("TripEase - Gestion des Destinations");


            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du FXML : " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
