package Controller.ConfigurerVoyage;

import Entite.Destination;
import Service.ServiceDestination;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ConfigVoyageController {

    @FXML private DatePicker          dateDebutPicker;
    @FXML private DatePicker          dateFinPicker;
    @FXML private Label               dureeLabel;
    @FXML private ComboBox<String>    rythmeCombo;
    @FXML private ComboBox<Destination> destinationCombo;
    @FXML private Label               destinationPreview;
    @FXML private Button              suivantButton;

    private final ServiceDestination serviceDestination = new ServiceDestination();

    /* ══════════════════════════════════════════
       INIT
    ══════════════════════════════════════════ */
    @FXML
    public void initialize() {
        rythmeCombo.getItems().addAll(
                "Détente", "Aventure", "Culturel", "Gastronomique", "Sport", "Famille");

        // Les dates pilotent la liste de destinations
        dateDebutPicker.valueProperty().addListener((obs, old, nw) -> {
            calculerDuree();
            rafraichirDestinations();
        });
        dateFinPicker.valueProperty().addListener((obs, old, nw) -> {
            calculerDuree();
            rafraichirDestinations();
        });

        // Aperçu quand la destination change
        destinationCombo.valueProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                destinationPreview.setText(
                        "🌍 " + nw.getPays() + "  —  " + nw.getVille()
                                + "\n\n" + (nw.getDescription() != null ? nw.getDescription() : ""));
                destinationPreview.setStyle(
                        "-fx-text-fill: #555; -fx-font-size: 13px; -fx-line-spacing: 4px;");
            } else {
                destinationPreview.setText("Sélectionnez une destination pour voir l'aperçu");
                destinationPreview.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");
            }
        });

        // Afficher message par défaut dans le combo
        destinationCombo.setPromptText("Choisissez d'abord vos dates");
        configurerConverterDestination();
    }

    /* ── Converter affiché dans le ComboBox ── */
    private void configurerConverterDestination() {
        destinationCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Destination d) {
                return d == null ? "" : d.getPays() + " — " + d.getVille();
            }
            @Override public Destination fromString(String s) { return null; }
        });
    }

    /* ── Recharge les destinations filtrées selon les dates saisies ── */
    private void rafraichirDestinations() {
        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin   = dateFinPicker.getValue();

        destinationCombo.getItems().clear();
        destinationCombo.setValue(null);

        if (debut == null || fin == null || fin.isBefore(debut)) {
            destinationCombo.setPromptText("Choisissez d'abord des dates valides");
            return;
        }

        try {
            // ← Requête filtrée : destinations dont la période contient les dates user
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

    /* ── Calcul durée ── */
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
       NAVIGATION → Vol
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

        URL url = getClass().getClassLoader().getResource("ConfigurerVoyage/Vol.fxml");
        if (url == null) url = getClass().getResource("/ConfigurerVoyage/Vol.fxml");
        if (url == null) {
            showAlert("Erreur", "Vol.fxml introuvable."); return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // ── Transmission des données à VolController ──
            VolController volCtrl = loader.getController();
            volCtrl.initDonnees(
                    destinationCombo.getValue(),
                    dateDebutPicker.getValue(),
                    dateFinPicker.getValue()
            );

            Stage stage = (Stage) suivantButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase — Choisir un Vol");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger Vol.fxml : " + e.getMessage());
        }
    }

    /* ── Hover effects ── */
    @FXML private void onMouseEnteredButton(javafx.scene.input.MouseEvent e) {
        ((Button) e.getSource()).setOpacity(0.85);
    }
    @FXML private void onMouseExitedButton(javafx.scene.input.MouseEvent e) {
        ((Button) e.getSource()).setOpacity(1.0);
    }
    @FXML private void onMouseEnteredSuivantButton(javafx.scene.input.MouseEvent e) {
        ((Button) e.getSource()).setStyle(
                "-fx-background-color:linear-gradient(to right,#E8622F,#E08519);" +
                        "-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;" +
                        "-fx-background-radius:10;-fx-cursor:hand;" +
                        "-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.6),16,0,0,5);");
    }
    @FXML private void onMouseExitedSuivantButton(javafx.scene.input.MouseEvent e) {
        ((Button) e.getSource()).setStyle(
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