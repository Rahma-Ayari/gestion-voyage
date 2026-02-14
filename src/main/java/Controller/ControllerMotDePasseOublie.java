package Controller;

import Service.ServicePersonne;
import Utils.EmailSender;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ControllerMotDePasseOublie implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private TextField emailRecupField;

    private ServicePersonne servicePersonne = new ServicePersonne();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Empêche le focus automatique sur le champ pour voir le prompt text
        Platform.runLater(() -> rootPane.requestFocus());
    }

    @FXML
    private void handleSendCode(ActionEvent event) {
        String email = emailRecupField.getText();

        if (email.isEmpty() || !email.contains("@")) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez saisir une adresse email valide.");
            return;
        }

        try {
            // 1. Vérifier si l'email existe en base de données
            if (servicePersonne.verifierEmailExiste(email)) {

                // 2. Générer un code à 6 chiffres
                String codeGenere = String.valueOf((int)(Math.random() * 900000) + 100000);

                // 3. Envoyer le mail (dans un thread séparé pour ne pas bloquer l'interface)
                new Thread(() -> {
                    try {
                        EmailSender.sendResetCode(email, codeGenere);

                        // 4. Une fois envoyé, on passe à l'interface de réinitialisation
                        Platform.runLater(() -> {
                            try {
                                ControllerResetPassword.setDonnees(email, codeGenere);
                                loadResetPage(event);
                            } catch (IOException e) {
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page suivante.");
                            }
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erreur d'envoi", "Impossible d'envoyer l'email. Vérifiez votre connexion."));
                        e.printStackTrace();
                    }
                }).start();

            } else {
                showAlert(Alert.AlertType.ERROR, "Email introuvable", "Cette adresse email n'est pas enregistrée sur TripEase.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BDD", "Erreur lors de la vérification : " + e.getMessage());
        }
    }

    private void loadResetPage(ActionEvent event) throws IOException {
        // 1. Charger le FXML manuellement
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ResetPassword.fxml"));
        Parent root = loader.load();

        // 2. Récupérer l'instance du contrôleur de la page suivante
        ControllerResetPassword controller = loader.getController();

        // 3. Envoyer l'email et le code à cette instance précise
        String email = emailRecupField.getText();
        // Nous récupérons le code stocké temporairement ou passé en paramètre
        // Pour simplifier, passons les données ici si tu ne l'as pas fait dans le thread

        // 4. Afficher la scène
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
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
}
