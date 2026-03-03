package Controller.ConfigurerVoyage;

import Entite.Destination;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ServicesSuppUserController {

    /* ── Header & résumé ── */
    @FXML private Label headerDestLabel;
    @FXML private Label headerDatesLabel;
    @FXML private Label destinationLabel;
    @FXML private Label datesLabel;
    @FXML private Label dureeLabel;

    /* ── Résumé sélection ── */
    @FXML private Label selectionCountLabel;
    @FXML private Label totalCoutLabel;
    @FXML private VBox  selectionContainer;

    /* ── Services (checkBox simples, côté FXML) ── */
    @FXML private CheckBox cbAssurance;
    @FXML private CheckBox cbTransfert;
    @FXML private CheckBox cbBagage;
    @FXML private CheckBox cbVoiture;
    @FXML private CheckBox cbWifi;

    @FXML private Button suivantButton;

    private Destination destination;
    private LocalDate   dateDebut;
    private LocalDate   dateFin;
    private int         idVoyage = -1;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        // Met à jour le récap dès qu'on coche / décoche un service
        if (cbAssurance != null) cbAssurance.selectedProperty().addListener((o, ov, nv) -> mettreAJourRecapitulatif());
        if (cbTransfert != null) cbTransfert.selectedProperty().addListener((o, ov, nv) -> mettreAJourRecapitulatif());
        if (cbBagage   != null) cbBagage.selectedProperty().addListener((o, ov, nv) -> mettreAJourRecapitulatif());
        if (cbVoiture  != null) cbVoiture.selectedProperty().addListener((o, ov, nv) -> mettreAJourRecapitulatif());
        if (cbWifi     != null) cbWifi.selectedProperty().addListener((o, ov, nv) -> mettreAJourRecapitulatif());
    }

    /**
     * Appelée depuis l'écran précédent pour transmettre le contexte du voyage.
     */
    public void initDonnees(Destination destination, LocalDate dateDebut,
                            LocalDate dateFin, int idVoyage) {
        this.destination = destination;
        this.dateDebut   = dateDebut;
        this.dateFin     = dateFin;
        this.idVoyage    = idVoyage;

        mettreAJourResume();
        mettreAJourRecapitulatif();
    }

    private void mettreAJourResume() {
        if (destination == null || dateDebut == null || dateFin == null) return;

        String destTxt = destination.getPays() + " — " + destination.getVille();
        String datesTxt = dateDebut.format(fmt) + "  →  " + dateFin.format(fmt);
        long jours = java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin);

        if (destinationLabel != null) destinationLabel.setText("🌍 " + destTxt);
        if (datesLabel       != null) datesLabel.setText(datesTxt);
        if (dureeLabel       != null) dureeLabel.setText(jours + " jour" + (jours > 1 ? "s" : ""));

        if (headerDestLabel  != null) headerDestLabel.setText(destTxt);
        if (headerDatesLabel != null) headerDatesLabel.setText(datesTxt);
    }

    private void mettreAJourRecapitulatif() {
        if (selectionContainer == null || selectionCountLabel == null || totalCoutLabel == null) return;

        selectionContainer.getChildren().clear();

        int count = 0;
        double total = 0;

        count += ajouterServiceSiCoché(cbAssurance, "Assurance voyage", 120, selectionContainer);
        total += cbAssurance != null && cbAssurance.isSelected() ? 120 : 0;

        count += ajouterServiceSiCoché(cbTransfert, "Transfert aéroport", 80, selectionContainer);
        total += cbTransfert != null && cbTransfert.isSelected() ? 80 : 0;

        count += ajouterServiceSiCoché(cbBagage, "Bagage supplémentaire", 60, selectionContainer);
        total += cbBagage != null && cbBagage.isSelected() ? 60 : 0;

        count += ajouterServiceSiCoché(cbVoiture, "Location de voiture", 200, selectionContainer);
        total += cbVoiture != null && cbVoiture.isSelected() ? 200 : 0;

        count += ajouterServiceSiCoché(cbWifi, "Wi-Fi portable", 50, selectionContainer);
        total += cbWifi != null && cbWifi.isSelected() ? 50 : 0;

        if (count == 0) {
            selectionCountLabel.setText("Aucun service sélectionné");
            Label l = new Label("Ajoutez des services pour améliorer votre expérience.");
            l.setStyle("-fx-text-fill:#BBB;-fx-font-size:12.5px;-fx-padding:4 0 0 0;");
            l.setWrapText(true);
            selectionContainer.getChildren().add(l);
        } else {
            selectionCountLabel.setText(count + " service" + (count > 1 ? "s" : "") + " sélectionné" + (count > 1 ? "s" : ""));
        }

        totalCoutLabel.setText(String.format("💰 Total estimé : %.0f TND", total));
    }

    private int ajouterServiceSiCoché(CheckBox cb, String libelle, double prix, VBox container) {
        if (cb == null || container == null) return 0;
        if (!cb.isSelected()) return 0;

        Label l = new Label("• " + libelle + " — " + String.format("%.0f TND", prix));
        l.setStyle("-fx-font-size:12.5px;-fx-text-fill:#444;");
        l.setWrapText(true);
        container.getChildren().add(l);
        return 1;
    }

    @FXML
    private void passerEtapeSuivante() {
        // Navigation vers l'écran Budget avec les infos de services sélectionnés
        URL url = getClass().getClassLoader().getResource("ConfigurerVoyage/Budget.fxml");
        if (url == null) url = getClass().getResource("/ConfigurerVoyage/Budget.fxml");
        if (url == null) {
            showInfo("Erreur", "Budget.fxml introuvable.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            BudgetController ctrl = loader.getController();
            ctrl.initDonnees(
                    destination,
                    dateDebut,
                    dateFin,
                    idVoyage,
                    cbAssurance != null && cbAssurance.isSelected(),
                    cbTransfert != null && cbTransfert.isSelected(),
                    cbBagage   != null && cbBagage.isSelected(),
                    cbVoiture  != null && cbVoiture.isSelected(),
                    cbWifi     != null && cbWifi.isSelected()
            );
            Stage stage = (Stage) suivantButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase — Budget");
            stage.show();
        } catch (IOException e) {
            showInfo("Erreur", "Impossible de charger Budget.fxml : " + e.getMessage());
        }
    }

    @FXML
    private void retourEtapePrecedente() {
        URL url = getClass().getClassLoader().getResource("ConfigurerVoyage/ActiviteUser.fxml");
        if (url == null) url = getClass().getResource("/ConfigurerVoyage/ActiviteUser.fxml");
        if (url == null) {
            showInfo("Erreur", "ActiviteUser.fxml introuvable.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            ActiviteUserController ctrl = loader.getController();
            // On repasse les mêmes informations de base
            ctrl.initDonnees(destination, dateDebut, dateFin, null, idVoyage);
            Stage stage = (Stage) suivantButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase — Choisir des Activités");
            stage.show();
        } catch (IOException e) {
            showInfo("Erreur", "Impossible de charger ActiviteUser.fxml : " + e.getMessage());
        }
    }

    @FXML
    private void onMouseEnteredButton(javafx.scene.input.MouseEvent e)  {
        ((Button)e.getSource()).setOpacity(0.85);
    }

    @FXML
    private void onMouseExitedButton(javafx.scene.input.MouseEvent e)   {
        ((Button)e.getSource()).setOpacity(1.0);
    }

    @FXML
    private void onMouseEnteredSuivantButton(javafx.scene.input.MouseEvent e) {
        ((Button)e.getSource()).setStyle(
                "-fx-background-color:linear-gradient(to right,#E8622F,#E08519);-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.6),16,0,0,5);"
        );
    }

    @FXML
    private void onMouseExitedSuivantButton(javafx.scene.input.MouseEvent e) {
        ((Button)e.getSource()).setStyle(
                "-fx-background-color:linear-gradient(to right,#FF6B35,#F7931E);-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.45),12,0,0,4);"
        );
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

