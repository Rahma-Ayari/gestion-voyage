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

    // @FXML signifie : "Cet objet est lié à un élément de mon interface visuelle"
    @FXML private TextField     emailField; // Le champ où l'utilisateur tape son email
    @FXML private PasswordField passwordField; // Le champ où les caractères sont cachés (****)

    // On crée un lien vers les services qui gèrent la base de données
    private final ServicePersonne      servicePersonne      = new ServicePersonne();
    private final ServiceAdministrateur serviceAdministrateur = new ServiceAdministrateur();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cette méthode s'exécute automatiquement à l'ouverture de la page.
        // Ici, on ne fait rien de spécial au départ.
    }

    // ─────────────────────────────────────────────────────────────────
    //  BOUTON "SE CONNECTER"  →  onAction="#handleLogin"  (Login.fxml)
    // ─────────────────────────────────────────────────────────────────
    @FXML
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
            // 3.a D'abord : vérifier si c'est un ADMINISTRATEUR
            Administrateur admin = serviceAdministrateur.authentifier(email, mdp);
            if (admin != null) {
                // ✅ Admin trouvé - Stocker la session et rediriger vers HomeAdmin
                SessionManager.setCurrentAdmin(admin);
                // Afficher le nom dans le tableau de bord admin
                HomeAdminController.setNomAdmin(admin.getEmail());

                showAlert(Alert.AlertType.INFORMATION, "Bienvenue (Admin)",
                        "Connexion réussie en tant qu'administrateur.\n"
                                + "Vous allez être redirigé vers le panneau d'administration.");
                
                // ⏱️ Redirection APRÈS affichage de l'alerte
                navigateToAdmin(event);
                return;
            }

            // 3.b Sinon : tenter l'authentification UTILISATEUR (Personne)
            Personne personne = servicePersonne.authentifier(email, mdp);

            if (personne != null) {
                // ✅ Utilisateur trouvé - Stocker la session et rediriger vers ConfigVoyage
                SessionManager.setCurrentUser(personne);

                showAlert(Alert.AlertType.INFORMATION, "Bienvenue !",
                        "Bonjour " + personne.getPrenom() + " " + personne.getNom() + " !\n"
                                + "Bienvenue sur TripEase. Bon voyage ! ✈");

                // ⏱️ Redirection APRÈS affichage de l'alerte
                navigateToUser(event);

            } else {
                // ❌ Aucun utilisateur ni admin trouvé
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
    //  REDIRECTION ADMIN
    // ─────────────────────────────────────────────────────────────────
    private void navigateToAdmin(ActionEvent event) {
        try {
            // Charge le dashboard admin
            Parent root = FXMLLoader.load(getClass().getResource("/HomeAdmin.fxml"));
            Stage stage  = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
    //  REDIRECTION USER
    // ─────────────────────────────────────────────────────────────────
    private void navigateToUser(ActionEvent event) {
        try {
            // Charge l'écran de configuration du voyage
            Parent root = FXMLLoader.load(getClass().getResource("/MesVoyages.fxml"));
            Stage stage  = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase - Configuration du voyage");
            stage.show();
            
            System.out.println("✅ Redirection vers ConfigVoyageUser réussie");
        } catch (IOException e) {
            System.err.println("❌ Erreur redirection vers ConfigVoyageUser.fxml : " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", 
                     "Impossible d'accéder au formulaire de configuration du voyage.");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  HYPERLINK "Mot de passe oublié ?"  →  onAction="#handleForgotPassword"
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
    //  Navigation générique (pour autres pages)
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
            System.err.println("❌ Erreur redirection vers " + fxmlPath + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ───────────────────────────────────────────────────────────────
    //  Alerte stylisée orange
    // ───────────────────────────────────────────────────────────────
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