package Controller.ConfigurerVoyage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

    private final ServiceDestination serviceDestination = new ServiceDestination();
    private final ServiceVoyage      serviceVoyage      = new ServiceVoyage();

    // ← L'utilisateur connecté, reçu depuis ControllerLogin
    private Utilisateur utilisateurConnecte;

    // ══════════════════════════════════════════════════════════════
    //  Reçoit l'utilisateur connecté depuis ControllerLogin
    //  Cette méthode est appelée APRÈS loader.load()
    // ══════════════════════════════════════════════════════════════
    public void setUtilisateur(Utilisateur user) {
        this.utilisateurConnecte = user;
        System.out.println("✅ Utilisateur reçu dans ConfigVoyage : " + user.getEmail());
    }

    @FXML
    public void initialize() {
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

    // ══════════════════════════════════════════════════════════════
    //  BOUTON SUIVANT → créer le voyage et passer à l'écran Vol
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void passerEtapeSuivante() {
        // ── Validations ────────────────────────────────────────────
        if (utilisateurConnecte == null) {
            showAlert("Erreur", "Aucun utilisateur connecté. Veuillez vous reconnecter."); return;
        }
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

        // ── Créer le voyage en BDD avec l'id_utilisateur ──────────
        int idVoyage;
        try {
            Voyage v = new Voyage();
            v.setDuree((int) duree);
            v.setDateDebut(debut);
            v.setDateFin(fin);
            v.setRythme(rythmeCombo.getValue());
            v.setIdDestination(destinationCombo.getValue().getIdDestination());
            v.setIdUtilisateur(utilisateurConnecte.getIdUtilisateur()); // ← clé du problème !

            idVoyage = serviceVoyage.ajouter(v);
            if (idVoyage == -1) {
                showAlert("Erreur", "Impossible de créer le voyage."); return;
            }
            System.out.println("✅ Voyage créé avec id=" + idVoyage
                    + " pour utilisateur=" + utilisateurConnecte.getIdUtilisateur());
        } catch (SQLException e) {
            showAlert("Erreur BD", "Erreur lors de la création du voyage : " + e.getMessage()); return;
        }

        // ── Navigation vers l'écran Vol ────────────────────────────
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

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        URL url = getClass().getResource("/Login.fxml");
        if (url == null) { showAlert("Erreur", "Login.fxml introuvable."); return; }
        try {
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) suivantButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase - Connexion");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger Login.fxml : " + e.getMessage());
        }
    }

    @FXML private void onMouseEnteredButton(javafx.scene.input.MouseEvent e)  { ((Button)e.getSource()).setOpacity(0.85); }
    @FXML private void onMouseExitedButton(javafx.scene.input.MouseEvent e)   { ((Button)e.getSource()).setOpacity(1.0); }
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