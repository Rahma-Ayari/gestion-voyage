package Controller.ConfigurerVoyage;

import Entite.Destination;
import Entite.Voyage;
import Service.ServiceDestination;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ConfigVoyageController {

    @FXML
    private ComboBox<Destination> destinationCombo;

    @FXML
    private ComboBox<String> rythmeCombo;

    @FXML
    private DatePicker dateDebutPicker, dateFinPicker;

    @FXML
    private Label dureeLabel, destinationPreview;

    @FXML
    private Button suivantButton;

    private Voyage voyage = new Voyage();
    private ServiceDestination serviceDestination = new ServiceDestination();

    @FXML
    public void initialize() {
        configurerComboDestination();
        chargerDestinations();
        chargerRythmes();
        initListeners();
    }

    /* ================= DESTINATIONS ================= */

    private void configurerComboDestination() {
        destinationCombo.setConverter(new StringConverter<Destination>() {
            @Override
            public String toString(Destination d) {
                if (d == null) return "";
                return d.getPays() + " - " + d.getVille();
            }

            @Override
            public Destination fromString(String string) {
                return null;
            }
        });
    }

    private void chargerDestinations() {
        try {
            List<Destination> destinations = serviceDestination.readAll();
            destinationCombo.getItems().setAll(destinations);
        } catch (SQLException e) {
            showAlert("Erreur chargement destinations");
        }
    }

    /* ================= RYTHMES ================= */

    private void chargerRythmes() {
        rythmeCombo.getItems().addAll("Détendu", "Normal", "Intensif");
    }

    /* ================= LISTENERS ================= */

    private void initListeners() {

        destinationCombo.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                destinationPreview.setText(
                        "📍 " + newV.getVille() + ", " + newV.getPays() + "\n\n" +
                                newV.getDescription()
                );
                voyage.setDestination(newV);
            }
        });

        dateDebutPicker.valueProperty().addListener((o, a, b) -> calculerDuree());
        dateFinPicker.valueProperty().addListener((o, a, b) -> calculerDuree());

        rythmeCombo.valueProperty().addListener((obs, o, n) -> voyage.setRythme(n));
    }

    /* ================= DURÉE ================= */

    private void calculerDuree() {
        if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null) {

            if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
                showAlert("La date de retour doit être après la date de départ");
                return;
            }

            long jours = ChronoUnit.DAYS.between(
                    dateDebutPicker.getValue(),
                    dateFinPicker.getValue()
            ) + 1;

            dureeLabel.setText("Durée : " + jours + " jours");

            voyage.setDuree((int) jours);
            voyage.setDateDebut(dateDebutPicker.getValue());
            voyage.setDateFin(dateFinPicker.getValue());
        }
    }

    /* ================= NAVIGATION ================= */

    @FXML
    private void passerEtapeSuivante() {
        if (voyage.getDestination() == null ||
                voyage.getDateDebut() == null ||
                voyage.getDateFin() == null ||
                voyage.getRythme() == null) {

            showAlert("Veuillez compléter tous les champs !");
            return;
        }

        System.out.println("Voyage en cours : " + voyage);
        // ➜ Charger interface Activités
    }

    /* ================= EFFETS HOVER - Colors updated to match screenshot ================= */

    @FXML
    private void onMouseEnteredButton(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle(btn.getStyle() + "-fx-background-color: #F8F9FA;");
    }

    @FXML
    private void onMouseExitedButton(MouseEvent event) {
        Button btn = (Button) event.getSource();
        String style = btn.getStyle().replace("-fx-background-color: #F8F9FA;", "");
        btn.setStyle(style);
    }

    @FXML
    private void onMouseEnteredSubButton(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle(btn.getStyle() + "-fx-background-color: #F8F9FA; -fx-text-fill: #666;");
    }

    @FXML
    private void onMouseExitedSubButton(MouseEvent event) {
        Button btn = (Button) event.getSource();
        String style = btn.getStyle()
                .replace("-fx-background-color: #F8F9FA;", "")
                .replace("-fx-text-fill: #666;", "-fx-text-fill: #999;");
        btn.setStyle(style);
    }

    @FXML
    private void onMouseEnteredHeaderButton(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle(btn.getStyle()
                .replace("rgba(255, 255, 255, 0.25)", "rgba(255, 255, 255, 0.4)"));
    }

    @FXML
    private void onMouseExitedHeaderButton(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle(btn.getStyle()
                .replace("rgba(255, 255, 255, 0.4)", "rgba(255, 255, 255, 0.25)"));
    }

    @FXML
    private void onMouseEnteredSuivantButton(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle(btn.getStyle()
                .replace("-fx-background-color: #FF6B47;", "-fx-background-color: #FF8563;")
                .replace("dropshadow(gaussian, rgba(255,107,71,0.3), 10, 0, 0, 3)",
                        "dropshadow(gaussian, rgba(255,107,71,0.5), 12, 0, 0, 4)"));
    }

    @FXML
    private void onMouseExitedSuivantButton(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle(btn.getStyle()
                .replace("-fx-background-color: #FF8563;", "-fx-background-color: #FF6B47;")
                .replace("dropshadow(gaussian, rgba(255,107,71,0.5), 12, 0, 0, 4)",
                        "dropshadow(gaussian, rgba(255,107,71,0.3), 10, 0, 0, 3)"));
    }

    /* ================= ALERT ================= */

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}