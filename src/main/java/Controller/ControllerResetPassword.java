package Controller;

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

public class ControllerResetPassword implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // Données transmises depuis la page précédente
    private static String emailUtilisateur;
    private static String codeEnvoye;

    private ServicePersonne servicePersonne = new ServicePersonne();

    /**
     * Méthode appelée par le contrôleur précédent pour passer les infos
     */
    public static void setDonnees(String email, String code) {
        emailUtilisateur = email;
        codeEnvoye = code;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> rootPane.requestFocus());
    }

    @FXML
    private void handleReset(ActionEvent event) {
        String codeSaisi = codeField.getText();
        String mdp = newPasswordField.getText();
        String mdpConf = confirmPasswordField.getText();

        // 1. Vérification des champs vides
        if (codeSaisi.isEmpty() || mdp.isEmpty() || mdpConf.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        // 2. Vérification du code
        if (!codeSaisi.equals(codeEnvoye)) {
            showAlert(Alert.AlertType.ERROR, "Code incorrect", "Le code de vérification est invalide.");
            return;
        }

        // 3. Vérification de la correspondance des mots de passe
        if (!mdp.equals(mdpConf)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            // 4. Mise à jour en Base de données
            if (servicePersonne.updatePassword(emailUtilisateur, mdp)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre mot de passe a été modifié avec succès !");
                showLogin(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour le mot de passe.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BDD", "Erreur SQL : " + e.getMessage());
        }
    }

    @FXML
    private void showLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur redirection Login : " + e.getMessage());
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

    @FXML
    private void handleValiderChangement() {
        String codeSaisi = codeField.getText();

        // On utilise codeEnvoye car c'est le nom que tu as défini en haut de ta classe
        if (codeSaisi != null && codeEnvoye != null && codeSaisi.trim().equals(codeEnvoye.trim())) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Code valide ! Vous pouvez changer votre mot de passe.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Code incorrect", "Le code de vérification est invalide.");
        }
    }
}
