package Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HomeAdminController implements Initializable {

    @FXML private Label adminNameLabel;
    @FXML private Label welcomeLabel;

    // Cards (pour l'effet hover)
    @FXML private VBox cardInscriptions;
    @FXML private VBox cardAvis;
    @FXML private VBox cardDestinations;
    @FXML private VBox cardActivites;
    @FXML private VBox cardHotels;
    @FXML private VBox cardReservations;
    @FXML private VBox cardOffres;
    @FXML private VBox cardVols;
    @FXML private VBox cardPaiements;
    @FXML private VBox cardNotifications;

    // Nom de l'admin connecté (à injecter depuis la page de login)
    private static String nomAdmin = "Administrateur";

    /** Appelé depuis le controller de login pour passer le nom de l'admin */
    public static void setNomAdmin(String nom) {
        nomAdmin = nom;
    }

    private static final String CARD_STYLE_BASE =
            "-fx-background-color: white; -fx-background-radius: 16; " +
                    "-fx-border-color: #EBEBEB; -fx-border-radius: 16; -fx-border-width: 1; " +
                    "-fx-padding: 24 22; -fx-cursor: hand;";

    private static final String CARD_STYLE_HOVER =
            "-fx-background-color: #FFF8F2; -fx-background-radius: 16; " +
                    "-fx-border-color: #FF6B00; -fx-border-radius: 16; -fx-border-width: 1.5; " +
                    "-fx-padding: 24 22; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(255,107,0,0.18), 18, 0, 0, 6);";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Affichage du nom admin
        adminNameLabel.setText(nomAdmin);
        welcomeLabel.setText("Bienvenue, " + nomAdmin + " 👋");

        // Ajout des effets hover sur les cartes
        VBox[] cards = {
                cardInscriptions, cardAvis, cardDestinations, cardActivites,
                cardHotels, cardReservations, cardOffres, cardVols,
                cardPaiements, cardNotifications
        };

        for (VBox card : cards) {
            if (card != null) {
                card.setOnMouseEntered(e -> card.setStyle(CARD_STYLE_HOVER)); // Survol → orange
                card.setOnMouseExited(e  -> card.setStyle(CARD_STYLE_BASE)); // Quitte → blanc
            }
        }
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @FXML
    private void handleInscriptions(MouseEvent e) {
        ouvrirFenetre("/DashboardAdmin.fxml", "Gestion des Inscriptions");
    }

    @FXML
    private void handleAvis(MouseEvent e) {
        ouvrirFenetre("/Avis.fxml", "Gestion des Avis");
    }
    @FXML
    private void handleDestinations(MouseEvent e) {
        ouvrirFenetre("/DestinationView.fxml", "Gestion des Destinations");
    }

    @FXML
    private void handleActivites(MouseEvent e) {
        // Nom réel du fichier FXML : ActiviteView.fxml
        ouvrirFenetre("/ActiviteView.fxml", "Gestion des Activités");
    }

    @FXML
    private void handleHotels(MouseEvent e) {
        ouvrirFenetre("/Hotels.fxml", "Gestion des Hôtels");
    }

    @FXML
    private void handleReservations(MouseEvent e) {
        // Nom réel du fichier FXML : ReservationAdmin.fxml
        ouvrirFenetre("/ReservationAdmin.fxml", "Gestion des Réservations");
    }

    @FXML
    private void handleOffres(MouseEvent e) {
        // Nom réel du fichier FXML : Offre.fxml
        ouvrirFenetre("/Offre.fxml", "Gestion des Offres");
    }

    @FXML
    private void handleVols(MouseEvent e) {
        // Nom réel du fichier FXML : VolView.fxml
        ouvrirFenetre("/VolView.fxml", "Gestion des Vols");
    }

    @FXML
    private void handlePaiements(MouseEvent e) {
        ouvrirFenetre("/Paiements.fxml", "Gestion des Paiements");
    }

    @FXML
    private void handleNotifications(MouseEvent e) {
        ouvrirFenetre("/Notifications.fxml", "Gestion des Notifications");
    }

    @FXML
    private void handleLogout() {
        ouvrirFenetre("/Login.fxml", "TripEase – Connexion");
    }

    // ─── Utilitaire d'ouverture de fenêtre ────────────────────────────────────

    private void ouvrirFenetre(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("TripEase Admin – " + titre);
            stage.setScene(new Scene(root));
            stage.show();


        } catch (IOException ex) {
            System.err.println("Impossible d'ouvrir : " + fxmlPath);
            ex.printStackTrace(); // Affiche les détails de l'erreur
        }
    }
}