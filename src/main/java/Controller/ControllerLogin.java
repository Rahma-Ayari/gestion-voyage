package Controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import Entite.Administrateur;
import Entite.Personne;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

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
            // 3.a Vérifier ADMINISTRATEUR
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

            // 3.b Vérifier UTILISATEUR (Personne)
            Personne personne = servicePersonne.authentifier(email, mdp);
            if (personne != null) {
                SessionManager.setCurrentUser(personne);
                showAlert(Alert.AlertType.INFORMATION, "Bienvenue !",
                        "Bonjour " + personne.getPrenom() + " "
                                + personne.getNom() + " !\n"
                                + "Bienvenue sur TripEase. Bon voyage ! ✈");

                // ← Redirige vers DashboardUser en passant la Personne
                navigateToUser(event, personne);

            } else {
                showAlert(Alert.AlertType.ERROR, "Identifiants incorrects",
                        "Email ou mot de passe invalide.\n"
                                + "Veuillez vérifier vos informations.");
            }

        } catch (SQLException e) {
            String msg = e.getMessage();
            if ("STATUT:EN_ATTENTE".equals(msg)) {
                showAlert(Alert.AlertType.WARNING,
                        "Compte en attente de validation",
                        "⏳  Votre demande d'inscription est en cours d'examen.\n\n"
                                + "Un administrateur TripEase doit valider votre compte.\n"
                                + "Merci de patienter et de réessayer ultérieurement.");
            } else if ("STATUT:REFUSE".equals(msg)) {
                showAlert(Alert.AlertType.ERROR, "Accès refusé",
                        "✖  Votre demande d'inscription a été refusée "
                                + "par l'administrateur.\n\n"
                                + "Pour plus d'informations, "
                                + "contactez le support TripEase.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur technique",
                        "Un problème est survenu : " + msg);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  REDIRECTION ADMIN  →  HomeAdmin.fxml
    // ─────────────────────────────────────────────────────────────────
    private void navigateToAdmin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/HomeAdmin.fxml"));
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase — Panneau d'administration");
            stage.show();
            System.out.println("✅ Redirection vers HomeAdmin réussie");
        } catch (IOException e) {
            System.err.println("❌ Erreur HomeAdmin.fxml : "
                    + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible d'accéder au panneau d'administration.");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  REDIRECTION USER  →  DashboardUser.fxml
    //  Personne extends Utilisateur → on passe directement l'objet
    // ─────────────────────────────────────────────────────────────────
    private void navigateToUser(ActionEvent event, Personne personne) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/DashboardUser.fxml"));
            Parent root = loader.load();

            // Récupérer le controller et lui passer l'utilisateur connecté
            DashboardUserController dashCtrl = loader.getController();
            if (dashCtrl != null) {
                // ✅ Personne IS-A Utilisateur (héritage)
                dashCtrl.setUtilisateur(personne);
            }

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase — Tableau de bord");
            stage.show();
            System.out.println("✅ Redirection vers DashboardUser réussie");

        } catch (IOException e) {
            System.err.println("❌ Erreur DashboardUser.fxml : "
                    + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible d'accéder au tableau de bord.\n"
                            + "Vérifiez que DashboardUser.fxml "
                            + "existe dans resources/");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  HYPERLINK "Mot de passe oublié ?"
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        navigateTo("/MotDePasseOublie.fxml", event);
    }

    // ─────────────────────────────────────────────────────────────────
    //  HYPERLINK "Créer un compte"
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void showInscription(ActionEvent event) {
        navigateTo("/Inscription.fxml", event);
    }

    // ─────────────────────────────────────────────────────────────────
    //  Navigation générique (Mot de passe oublié, Inscription...)
    // ─────────────────────────────────────────────────────────────────
    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Erreur redirection vers "
                    + fxmlPath + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Alerte stylisée orange
    // ─────────────────────────────────────────────────────────────────
    private void showAlert(Alert.AlertType type,
                           String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle(
                "-fx-background-color: white;"   +
                        "-fx-border-color: #FF9800;"      +
                        "-fx-border-width: 2;"            +
                        "-fx-border-radius: 10;"          +
                        "-fx-background-radius: 10;"
        );
        alert.showAndWait();
    }
}