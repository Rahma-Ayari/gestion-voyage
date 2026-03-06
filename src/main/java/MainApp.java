
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Chargement du fichier FXML
            // Note : Adaptez le chemin si votre FXML est dans un sous-dossier (ex: "/View/Destination.fxml")
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ActiviteView.fxml"));
            Parent root = loader.load();

            // Création de la scène
            Scene scene = new Scene(root, 1200, 800);

            // Configuration du Stage (Fenêtre)
            primaryStage.setTitle("TripEase - Gestion des Destinations");

            // Ajout d'une icône à la fenêtre (optionnel)
            // primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/tripease_logo2.png")));

            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du FXML : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}