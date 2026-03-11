package Controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import Controller.ConfigurerVoyage.ConfigVoyageController; // ← IMPORT AJOUTÉ
import Entite.Administrateur;
import Entite.Personne;
import Entite.Utilisateur;                                 // ← IMPORT AJOUTÉ
import Service.ServiceAdministrateur;
import Service.ServicePersonne;
import Utils.SessionManager;
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
import javafx.stage.Stage;

public class ControllerLogin implements Initializable {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;

    private final ServicePersonne       servicePersonne       = new ServicePersonne();
    private final ServiceAdministrateur serviceAdministrateur = new ServiceAdministrateur();

    // ← On garde la personne connectée ici pour pouvoir la passer à l'écran suivant
    private Personne personneConnectee;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Rien à faire au démarrage
    }

    // ─────────────────────────────────────────────────────────────────
    //  BOUTON "SE CONNECTER"
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String mdp   = passwordField.getText();

        if (email.isEmpty() || mdp.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants",
                    "Veuillez remplir tous les champs.");
            return;
        }

        try {
            // ── Vérification ADMIN ─────────────────────────────────
            Administrateur admin = serviceAdministrateur.authentifier(email, mdp);
            if (admin != null) {
                SessionManager.setCurrentAdmin(admin);
                HomeAdminController.setNomAdmin(admin.getEmail());
                showAlert(Alert.AlertType.INFORMATION, "Bienvenue (Admin)",
                        "Connexion réussie en tant qu'administrateur.\n"
                                + "Vous allez être redirigé vers le panneau d'administration.");
                navigateToAdmin(event);
                return;
            }

            // ── Vérification UTILISATEUR ───────────────────────────
            Personne personne = servicePersonne.authentifier(email, mdp);

            if (personne != null) {
                // On mémorise la personne connectée
                this.personneConnectee = personne;

                SessionManager.setCurrentUser(personne);
                showAlert(Alert.AlertType.INFORMATION, "Bienvenue !",
                        "Bonjour " + personne.getPrenom() + " " + personne.getNom() + " !\n"
                                + "Bienvenue sur TripEase. Bon voyage ! ✈");

                navigateToUser(event);   // ← passe l'utilisateur à l'écran suivant

            } else {
                showAlert(Alert.AlertType.ERROR, "Identifiants incorrects",
                        "Email ou mot de passe invalide.\nVeuillez vérifier vos informations.");
            }

        } catch (SQLException e) {
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
    //  REDIRECTION ADMIN (inchangée)
    // ─────────────────────────────────────────────────────────────────
    private void navigateToAdmin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/HomeAdmin.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase - Panneau d'administration");
            stage.show();
            System.out.println("✅ Redirection vers HomeAdmin réussie");
        } catch (IOException e) {
            System.err.println("❌ Erreur redirection vers HomeAdmin.fxml : " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible d'accéder au panneau d'administration.");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  REDIRECTION USER ← VERSION CORRIGÉE
    //  On passe l'utilisateur connecté à ConfigVoyageController
    // ─────────────────────────────────────────────────────────────────
    private void navigateToUser(ActionEvent event) {
        try {
            // ÉTAPE 1 : créer le loader (sans .load() immédiatement)
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ConfigurerVoyage/ConfigVoyageUser.fxml"));

            // ÉTAPE 2 : charger l'écran
            Parent root = loader.load();

            // ÉTAPE 3 : récupérer le contrôleur de cet écran
            ConfigVoyageController ctrl = loader.getController();

            // ÉTAPE 4 : lui passer l'utilisateur connecté ← LA LIGNE CLÉ
            // Personne hérite de Utilisateur (ou l'inverse),
            // donc on caste vers Utilisateur si nécessaire.
            // Si Personne ET Utilisateur sont des classes séparées,
            // créez un Utilisateur avec les infos de personne :
            Utilisateur u = new Utilisateur(
                    personneConnectee.getIdUtilisateur(),   // ← id
                    personneConnectee.getEmail(),           // ← email
                    personneConnectee.getMotDePasse(),      // ← mot de passe
                    personneConnectee.getDateInscription()  // ← date inscription
            );
            ctrl.setUtilisateur(u);  // ← on transmet l'utilisateur

            // ÉTAPE 5 : afficher l'écran
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase - Configuration du voyage");
            stage.show();

            System.out.println("✅ Redirection vers ConfigVoyageUser réussie");

        } catch (IOException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible d'accéder au formulaire de configuration du voyage.");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Liens "Mot de passe oublié" et "Créer un compte" (inchangés)
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        navigateTo("/MotDePasseOublie.fxml", event);
    }

    @FXML
    private void showInscription(ActionEvent event) {
        navigateTo("/Inscription.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage  = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Erreur redirection vers " + fxmlPath + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Alerte stylisée (inchangée)
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