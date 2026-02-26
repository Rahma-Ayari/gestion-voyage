package Controller;

import Entite.Personne;
import Service.ServicePersonne;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ControllerLogin implements Initializable {

    // @FXML signifie : "Cet objet est lié à un élément de mon interface visuelle"
    @FXML private TextField     emailField; // Le champ où l'utilisateur tape son email
    @FXML private PasswordField passwordField; // Le champ où les caractères sont cachés (****)

    // On crée un lien vers le service qui gère la base de données
    private final ServicePersonne servicePersonne = new ServicePersonne();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cette méthode s'exécute automatiquement à l'ouverture de la page.
        // Ici, on ne fait rien de spécial au départ.
    }

    // ─────────────────────────────────────────────────────────────────
    //  BOUTON "SE CONNECTER"  →  onAction="#handleLogin"  (Login.fxml)
    // ─────────────────────────────────────────────────────────────────
    @FXML
    // La fonction "Se Connecter" (handleLogin)
    // C'est le cœur du code. Elle s'active quand on clique sur le bouton de connexion.
    private void handleLogin(ActionEvent event) {
        // 1. On récupère ce que l'utilisateur a tapé
        String email = emailField.getText().trim(); // .trim() enlève les espaces inutiles au début/fin
        String mdp   = passwordField.getText();

        // 2. Vérification simple : est-ce que c'est vide ?
        if (email.isEmpty() || mdp.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants",
                    "Veuillez remplir tous les champs.");
            return; // On arrête tout ici si c'est vide
        }

        try {
            // 3. On demande au Service de vérifier si l'utilisateur existe en base de données
            Personne personne = servicePersonne.authentifier(email, mdp);

            if (personne != null) {
                // Si on a trouvé quelqu'un -> Direction la page d'accueil
                showAlert(Alert.AlertType.INFORMATION, "Bienvenue !",
                        "Bonjour " + personne.getPrenom() + " " + personne.getNom() + " !\n"
                                + "Bienvenue sur TripEase. Bon voyage ! ✈");
                navigateTo("/Home.fxml", event);

            } else {
                // Si personne n'est trouvé
                showAlert(Alert.AlertType.ERROR, "Identifiants incorrects",
                        "Email ou mot de passe invalide.\nVeuillez vérifier vos informations.");
            }

        } catch (SQLException e) {
            // 4. Gestion des cas particuliers (Statuts du compte)
            String msg = e.getMessage();

            if ("STATUT:EN_ATTENTE".equals(msg)) {
                showAlert(Alert.AlertType.WARNING, "Compte en attente de validation",
                        "⏳  Votre demande d'inscription est en cours d'examen.\n\n"
                                + "Un administrateur TripEase doit valider votre compte.\n"
                                + "Merci de patienter et de réessayer ultérieurement.");

            } else if ("STATUT:REFUSE".equals(msg)) {
                showAlert(Alert.AlertType.ERROR, "Accès refusé",
                        "✖  Votre demande d'inscription a été refusée par l'administrateur.\n\n"
                                + "Pour plus d'informations, contactez le support TripEase.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur technique",
                        "Un problème est survenu : " + msg);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  HYPERLINK "Mot de passe oublié ?"  →  onAction="#handleForgotPassword"
    //  → Redirige vers MotDePasseOublie.fxml
    //    géré par ControllerMotDePasseOublie
    //    qui envoie un code par email, puis redirige vers ResetPassword.fxml
    //    géré par ControllerResetPassword
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        navigateTo("/MotDePasseOublie.fxml", event);
    }

    // ─────────────────────────────────────────────────────────────────
    //  HYPERLINK "Créer un compte"  →  onAction="#showInscription"
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void showInscription(ActionEvent event) {
        navigateTo("/Inscription.fxml", event);
    }


    // ─────────────────────────────────────────────────────────────────
    //  Navigation générique
    // ─────────────────────────────────────────────────────────────────
    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            // Charge le nouveau fichier visuel (.fxml)
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Trouve la fenêtre actuelle (le Stage)
            Stage stage  = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Change le contenu de la fenêtre
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur redirection vers " + fxmlPath + " : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Alerte stylisée orange
    // ─────────────────────────────────────────────────────────────────
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); // Crée une petite fenêtre pop-up
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
        alert.showAndWait(); // Affiche et attend que l'utilisateur clique sur OK
    }
}
