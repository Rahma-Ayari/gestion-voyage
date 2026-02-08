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
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ControllerLogin implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private ServicePersonne servicePersonne = new ServicePersonne();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> rootPane.requestFocus());
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String mdp = passwordField.getText();

        if (email.isEmpty() || mdp.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs vides", "Veuillez saisir votre email et votre mot de passe.");
            return;
        }

        try {
            Personne utilisateurConnecte = servicePersonne.authentifier(email, mdp);
            if (utilisateurConnecte != null) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Bienvenue " + utilisateurConnecte.getPrenom() + " !");
                // Rediriger vers l'accueil ici
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec", "Email ou mot de passe incorrect.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur base de données : " + e.getMessage());
        }
    }

    @FXML
    private void showInscription(ActionEvent event) {
        try {
            // On charge le fichier Inscription.fxml
            Parent root = FXMLLoader.load(getClass().getResource("/Inscription.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page d'inscription.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MotDePasseOublie.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page de récupération.");
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle("-fx-border-color: #FF9800; -fx-border-width: 2;");
        alert.showAndWait();
    }
}