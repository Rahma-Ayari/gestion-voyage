package Controller;

import Entite.Reservation;
import Service.ServiceReservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationAdminController implements Initializable {

    // ===== TABLE ET COLONNES =====
    @FXML private TableView<Reservation> tableView;
    @FXML private TableColumn<Reservation, Integer> idCol;
    @FXML private TableColumn<Reservation, String> clientCol;
    @FXML private TableColumn<Reservation, String> offreCol;
    @FXML private TableColumn<Reservation, String> dateReservationCol;
    @FXML private TableColumn<Reservation, String> dateVoyageCol;
    @FXML private TableColumn<Reservation, String> nbPersonnesCol;
    @FXML private TableColumn<Reservation, Double> montantCol;
    @FXML private TableColumn<Reservation, String> statutCol;
    @FXML private TableColumn<Reservation, String> commentaireCol;

    // ===== LABELS STATS =====
    @FXML private Label totalLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label accepteesLabel;
    @FXML private Label refuseesLabel;
    @FXML private Label annuleesLabel;

    // ===== LEFT PANEL =====
    @FXML private VBox detailsBox;
    @FXML private ComboBox<String> statutFilter;
    @FXML private ComboBox<String> periodeFilter;

    // ===== BOUTONS =====
    @FXML private Button accepterBtn;
    @FXML private Button refuserBtn;
    @FXML private Button annulerBtn;

    private ServiceReservation service = new ServiceReservation();
    private ObservableList<Reservation> observableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // ===== COLONNES =====
        idCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId_reservation()).asObject());

        clientCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        "Client ID: " + (data.getValue().getId_personne() != null ?
                                data.getValue().getId_personne().getIdUtilisateur() : "N/A")));

        offreCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        "Voyage ID: " + (data.getValue().getId_voyage() != null ?
                                data.getValue().getId_voyage().getIdVoyage() : "N/A")));

        dateReservationCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDate_reservation() != null ?
                                data.getValue().getDate_reservation().toString() : "N/A"));

        dateVoyageCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty("N/A"));

        nbPersonnesCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty("-"));

        montantCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPrix_reservation()).asObject());

        statutCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEtat()));

        commentaireCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty("-"));

        // ===== CHARGER LES DONNÉES =====
        loadData();

        // ===== LISTENER POUR DETAILS =====
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showDetails(newVal);
        });

        // ===== INITIALISER FILTRES =====
        statutFilter.getItems().addAll("Tous", "En attente", "Acceptée", "Refusée", "Annulée");
        statutFilter.setValue("Tous");
        statutFilter.setOnAction(e -> filterTable());


    }

    private void loadData() {
        try {
            List<Reservation> list = service.readAll();
            observableList.setAll(list);
            tableView.setItems(observableList);
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStats() {
        totalLabel.setText(String.valueOf(observableList.size()));

        long enAttente = observableList.stream().filter(r -> "En attente".equalsIgnoreCase(r.getEtat())).count();
        long acceptees = observableList.stream().filter(r -> "Acceptée".equalsIgnoreCase(r.getEtat())).count();
        long refusees = observableList.stream().filter(r -> "Refusée".equalsIgnoreCase(r.getEtat())).count();
        long annulees = observableList.stream().filter(r -> "Annulée".equalsIgnoreCase(r.getEtat())).count();

        enAttenteLabel.setText(String.valueOf(enAttente));
        accepteesLabel.setText(String.valueOf(acceptees));
        refuseesLabel.setText(String.valueOf(refusees));
        annuleesLabel.setText(String.valueOf(annulees));
    }

    private void showDetails(Reservation r) {
        detailsBox.getChildren().clear();
        detailsBox.getChildren().addAll(
                new Label("ID: " + r.getId_reservation()),
                new Label("Date Réservation: " + r.getDate_reservation()),
                new Label("Prix: " + r.getPrix_reservation()),
                new Label("Statut: " + r.getEtat()),
                new Label("Client ID: " + (r.getId_personne() != null ? r.getId_personne().getIdUtilisateur() : "N/A")),
                new Label("Voyage ID: " + (r.getId_voyage() != null ? r.getId_voyage().getIdVoyage() : "N/A"))
        );
    }

    // ===== ACTIONS =====
    @FXML
    private void accepterReservation() { changeEtat("Acceptée"); }

    @FXML
    private void refuserReservation() { changeEtat("Refusée"); }

    @FXML
    private void annulerReservation() { changeEtat("Annulée"); }

    private void changeEtat(String nouvelEtat) {
        Reservation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner une réservation !");
            return;
        }
        selected.setEtat(nouvelEtat);
        try {
            service.modifier(selected);
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshTable() { loadData(); }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void filterTable() {
        String statut = statutFilter.getValue();
        // TODO: ajouter filtre par période si nécessaire
        ObservableList<Reservation> filtered = observableList.filtered(r -> {
            if ("Tous".equals(statut)) return true;
            return r.getEtat().equalsIgnoreCase(statut);
        });
        tableView.setItems(filtered);
        updateStats();
    }
}