package Controller.ConfigurerVoyage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import Controller.DashboardUserController;
import Entite.Destination;
import Entite.Utilisateur;
import Entite.Voyage;
import Service.ServiceDestination;
import Service.ServiceVoyage;
import Utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfigVoyageController {

    @FXML private DatePicker            dateDebutPicker;
    @FXML private DatePicker            dateFinPicker;
    @FXML private Label                 dureeLabel;
    @FXML private ComboBox<String>      rythmeCombo;
    @FXML private ComboBox<Destination> destinationCombo;
    @FXML private Label                 destinationPreview;
    @FXML private Button                suivantButton;
    @FXML private Button                retourButton;
    @FXML private Button                accueilNavButton;
    @FXML private Button                mesVoyagesNavButton;
    @FXML private Label                 userNameHeader;
    @FXML private Button                logoutButton;

    private final ServiceDestination serviceDestination = new ServiceDestination();
    private final ServiceVoyage      serviceVoyage      = new ServiceVoyage();

    @FXML
    public void initialize() {
        // ✅ Afficher le nom d'utilisateur connecté dans le header
        String userName = SessionManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            userNameHeader.setText("👤 " + userName);
        }

        rythmeCombo.getItems().addAll(
                "Détente", "Aventure", "Culturel", "Gastronomique", "Sport", "Famille");

        dateDebutPicker.valueProperty().addListener((obs, old, nw) -> {
            calculerDuree(); rafraichirDestinations();
        });
        dateFinPicker.valueProperty().addListener((obs, old, nw) -> {
            calculerDuree(); rafraichirDestinations();
        });

        destinationCombo.valueProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                destinationPreview.setText(
                        "🌍 " + nw.getPays() + "  —  " + nw.getVille()
                                + "\n\n" + (nw.getDescription() != null ? nw.getDescription() : ""));
                destinationPreview.setStyle("-fx-text-fill:#555;-fx-font-size:13px;-fx-line-spacing:4px;");
            } else {
                destinationPreview.setText("Sélectionnez une destination pour voir l'aperçu");
                destinationPreview.setStyle("-fx-text-fill:#888;-fx-font-size:13px;");
            }
        });

        destinationCombo.setPromptText("Choisissez d'abord vos dates");
        configurerConverterDestination();
    }

    private void configurerConverterDestination() {
        destinationCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Destination d) {
                return d == null ? "" : d.getPays() + " — " + d.getVille();
            }
            @Override public Destination fromString(String s) { return null; }
        });
    }

    private void rafraichirDestinations() {
        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin   = dateFinPicker.getValue();
        destinationCombo.getItems().clear();
        destinationCombo.setValue(null);
        if (debut == null || fin == null || fin.isBefore(debut)) {
            destinationCombo.setPromptText("Choisissez d'abord des dates valides"); return;
        }
        try {
            List<Destination> dispo = serviceDestination.findByDateRange(debut, fin);
            if (dispo.isEmpty()) {
                destinationCombo.setPromptText("Aucune destination disponible pour ces dates");
            } else {
                destinationCombo.getItems().setAll(dispo);
                destinationCombo.setPromptText("Choisir une destination (" + dispo.size() + " disponible(s))");
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les destinations : " + e.getMessage());
        }
    }

    private void calculerDuree() {
        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin   = dateFinPicker.getValue();
        if (debut != null && fin != null) {
            if (fin.isBefore(debut)) {
                dureeLabel.setText("⚠ Date de retour invalide");
                dureeLabel.setStyle("-fx-font-size:14.5px;-fx-font-weight:bold;-fx-text-fill:#E74C3C;");
            } else {
                long j = ChronoUnit.DAYS.between(debut, fin);
                dureeLabel.setText("Durée : " + j + " jour" + (j > 1 ? "s" : ""));
                dureeLabel.setStyle("-fx-font-size:14.5px;-fx-font-weight:bold;-fx-text-fill:#FF6B35;");
            }
        } else {
            dureeLabel.setText("Durée : 0 jours");
            dureeLabel.setStyle("-fx-font-size:14.5px;-fx-font-weight:bold;-fx-text-fill:#FF6B35;");
        }
    }

    /* ══════════════════════════════════════════
       NAVIGATION → Vol  +  CRÉATION du voyage en BD
    ══════════════════════════════════════════ */
    @FXML
    private void passerEtapeSuivante() {
        if (dateDebutPicker.getValue() == null) {
            showAlert("Champ requis", "Veuillez sélectionner une date de départ."); return;
        }
        if (dateFinPicker.getValue() == null) {
            showAlert("Champ requis", "Veuillez sélectionner une date de retour."); return;
        }
        if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
            showAlert("Dates invalides", "La date de retour doit être après la date de départ."); return;
        }
        if (rythmeCombo.getValue() == null) {
            showAlert("Champ requis", "Veuillez choisir un type de voyage."); return;
        }
        if (destinationCombo.getValue() == null) {
            showAlert("Champ requis", "Veuillez choisir une destination."); return;
        }

        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin   = dateFinPicker.getValue();
        long duree = ChronoUnit.DAYS.between(debut, fin);

        int idVoyage;
        try {
            // ✅ ÉTAPE CLIÉ : Récupérer l'ID de l'utilisateur connecté
            int idUserActuel = SessionManager.getUserId();

            // ✅ Vérification de sécurité (optionnel mais recommandé)
            if (idUserActuel <= 0) {
                showAlert("Erreur", "Utilisateur non authentifié. Veuillez vous reconnecter.");
                return;
            }

            // Afficher dans la console pour déboguer
            System.out.println("═══════════════════════════════════════════");
            System.out.println("🧳 CRÉATION DE VOYAGE");
            System.out.println("═══════════════════════════════════════════");
            System.out.println("👤 Utilisateur ID : " + idUserActuel);
            System.out.println("📍 Destination : " + destinationCombo.getValue().getPays());
            System.out.println("📅 Du " + debut + " au " + fin);
            System.out.println("🎒 Rythme : " + rythmeCombo.getValue());
            System.out.println("═══════════════════════════════════════════");

            Voyage v = new Voyage();
            v.setDuree((int) duree);
            v.setDateDebut(debut);
            v.setDateFin(fin);
            v.setRythme(rythmeCombo.getValue());
            v.setIdDestination(destinationCombo.getValue().getIdDestination());

            // ✅ CRUCIAL : Associer le voyage à l'utilisateur connecté
            v.setIdUser(idUserActuel);

            idVoyage = serviceVoyage.ajouter(v);
            if (idVoyage == -1) {
                showAlert("Erreur", "Impossible de créer le voyage."); return;
            }
        } catch (SQLException e) {
            showAlert("Erreur BD", "Erreur lors de la création du voyage : " + e.getMessage()); return;
        }

        URL url = getClass().getClassLoader().getResource("ConfigurerVoyage/Vol.fxml");
        if (url == null) url = getClass().getResource("/ConfigurerVoyage/Vol.fxml");
        if (url == null) { showAlert("Erreur", "Vol.fxml introuvable."); return; }

        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            VolController volCtrl = loader.getController();
            volCtrl.initDonnees(destinationCombo.getValue(), debut, fin, idVoyage);

            Stage stage = (Stage) suivantButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase — Choisir un Vol");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger Vol.fxml : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // NAVIGATION - BOUTONS
    // ══════════════════════════════════════════════════════════════════



    @FXML
    private void retourMesVoyages() {
        try {
            URL url = getClass().getResource("/DashboardUser.fxml");
            if (url == null) {
                showAlert("Erreur", "DashboardUser.fxml introuvable.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // ✅ CRUCIAL : Récupérer le contrôleur et passer l'utilisateur connecté
            DashboardUserController controller = loader.getController();
            Utilisateur utilisateurConnecte = SessionManager.getCurrentUser();

            if (utilisateurConnecte != null) {
                controller.setUtilisateur(utilisateurConnecte);
            } else {
                showAlert("Erreur", "Session expirée. Veuillez vous reconnecter.");
                handleLogout();
                return;
            }

            Stage stage = (Stage) retourButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase - Dashboard");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le Dashboard : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void allerAccueil() {
        try {
            URL url = getClass().getResource("/Accueil.fxml");
            if (url == null) {
                showAlert("Erreur", "Accueil.fxml introuvable."); return;
            }
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) accueilNavButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase - Accueil");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger Accueil : " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        // Nettoyer la session et revenir à l'écran de connexion
        SessionManager.clearSession();
        URL url = getClass().getResource("/Login.fxml");
        if (url == null) {
            showAlert("Erreur", "Login.fxml introuvable."); return;
        }
        try {
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase - Connexion");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger Login.fxml : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // EFFECTS DE SOURIS
    // ══════════════════════════════════════════════════════════════════

    @FXML private void onMouseEnteredButton(javafx.scene.input.MouseEvent e)  {
        ((Button)e.getSource()).setOpacity(0.85);
    }
    @FXML private void onMouseExitedButton(javafx.scene.input.MouseEvent e)   {
        ((Button)e.getSource()).setOpacity(1.0);
    }

    @FXML private void onMouseEnteredRetourButton(javafx.scene.input.MouseEvent e) {
        ((Button)e.getSource()).setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 7 16;" +
                        "-fx-background-color: rgba(255,255,255,0.35);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: rgba(255,255,255,0.6);" +
                        "-fx-border-width: 1.5; -fx-border-radius: 18;" +
                        "-fx-cursor: hand;");
    }

    @FXML private void onMouseExitedRetourButton(javafx.scene.input.MouseEvent e) {
        ((Button)e.getSource()).setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 7 16;" +
                        "-fx-background-color: rgba(255,255,255,0.22);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: rgba(255,255,255,0.45);" +
                        "-fx-border-width: 1.5; -fx-border-radius: 18;" +
                        "-fx-cursor: hand;");
    }

    @FXML private void onMouseEnteredSuivantButton(javafx.scene.input.MouseEvent e) {
        ((Button)e.getSource()).setStyle(
                "-fx-background-color:linear-gradient(to right,#E8622F,#E08519);" +
                        "-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;" +
                        "-fx-background-radius:10;-fx-cursor:hand;" +
                        "-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.6),16,0,0,5);");
    }
    @FXML private void onMouseExitedSuivantButton(javafx.scene.input.MouseEvent e) {
        ((Button)e.getSource()).setStyle(
                "-fx-background-color:linear-gradient(to right,#FF6B35,#F7931E);" +
                        "-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;" +
                        "-fx-background-radius:10;-fx-cursor:hand;" +
                        "-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.45),12,0,0,4);");
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}