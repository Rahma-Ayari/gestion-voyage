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

    // ─── fx:id correspondant exactement à votre Inscription.fxml ─────────────
    @FXML private StackPane     rootPane;
    @FXML private VBox          mainContainer;
    @FXML private TextField     nomField;
    @FXML private TextField     prenomField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;

    private ServicePersonne servicePersonne = new ServicePersonne();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> mainContainer.requestFocus());
    }

    // ─────────────────────────────────────────────────────────────────
    //  BOUTON "S'INSCRIRE"  →  onAction="#handleInscription"
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void handleInscription(ActionEvent event) {
        String nom    = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email  = emailField.getText().trim();
        String mdp    = passwordField.getText();

        // Validation champs vides
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants",
                    "Veuillez remplir tous les champs !");
            return;
        }

        // Validation format email
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showAlert(Alert.AlertType.WARNING, "Email invalide",
                    "Veuillez saisir une adresse e-mail valide.");
            return;
        }

        // Validation longueur mot de passe
        if (mdp.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Mot de passe trop court",
                    "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        try {
            // Email déjà utilisé ?
            if (servicePersonne.verifierEmailExiste(email)) {
                showAlert(Alert.AlertType.ERROR, "Email déjà utilisé",
                        "Un compte avec cet email existe déjà.\nVeuillez en utiliser un autre.");
                return;
            }

            // Construction de la Personne
            Personne nouvellePersonne = new Personne();
            nouvellePersonne.setNom(nom);
            nouvellePersonne.setPrenom(prenom);
            nouvellePersonne.setEmail(email);
            nouvellePersonne.setMotDePasse(mdp);
            nouvellePersonne.setDateInscription(new Date());

            // Envoi de la demande → statut = EN_ATTENTE
            if (servicePersonne.ajouter(nouvellePersonne)) {
                showAlert(Alert.AlertType.INFORMATION, "Demande envoyée !",
                        "✔  Votre demande d'inscription a bien été envoyée à TripEase.\n\n"
                                + "Un administrateur va examiner votre dossier.\n"
                                + "Vous pourrez vous connecter dès validation de votre compte.");
                // Redirection vers Login après l'alerte
                navigateToLogin(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec",
                        "L'inscription a échoué. Veuillez réessayer.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL",
                    "Problème de base de données : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  HYPERLINK "Se connecter ici"  →  onAction="#showLogin"
    //  ⚠ Nom exact utilisé dans votre Inscription.fxml
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void showLogin(ActionEvent event) {
        navigateToLogin(event);
    }

    // ─────────────────────────────────────────────────────────────────
    //  Navigation partagée → Login.fxml
    // ─────────────────────────────────────────────────────────────────
    private void navigateToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage  = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur redirection vers Login.fxml : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Alerte stylisée orange
    // ─────────────────────────────────────────────────────────────────
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #FF9800;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );
        alert.showAndWait();
    }
}