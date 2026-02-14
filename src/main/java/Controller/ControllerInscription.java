package Controller;

import Entite.Personne;
import Service.ServicePersonne;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.ResourceBundle;

public class ControllerInscription implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private VBox mainContainer;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private ServicePersonne servicePersonne = new ServicePersonne();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // On force le focus sur le conteneur pour afficher les promptTexts
        Platform.runLater(() -> mainContainer.requestFocus());
    }

    @FXML
    private void handleInscription(ActionEvent event) {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String mdp = passwordField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs !");
            return;
        }

        try {
            Personne nouvellePersonne = new Personne();
            nouvellePersonne.setNom(nom);
            nouvellePersonne.setPrenom(prenom);
            nouvellePersonne.setEmail(email);
            nouvellePersonne.setMotDePasse(mdp);
            nouvellePersonne.setDateInscription(new Date());

            if (servicePersonne.ajouter(nouvellePersonne)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Compte créé avec succès ! Bienvenue chez TripEase.");
                showLogin(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "L'inscription a échoué.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Problème de base de données : " + e.getMessage());
        }
    }

    @FXML
    private void showLogin(ActionEvent event) {
        try {
            // Changement de scène vers Login.fxml
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur redirection vers Login : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #FF9800; -fx-border-width: 2;");
        alert.showAndWait();
    }
}