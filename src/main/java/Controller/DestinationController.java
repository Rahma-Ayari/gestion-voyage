package Controller;

import Entite.Destination;
import Service.ServiceDestination;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DestinationController {

    /* ── Formulaire ── */
    @FXML private TextField  paysField, villeField, searchField;
    @FXML private TextArea   descriptionField;
    @FXML private DatePicker dateDebutPicker;  // null si pas encore dans le FXML
    @FXML private DatePicker dateFinPicker;    // null si pas encore dans le FXML

    /* ── Boutons ── */
    @FXML private Button ajouterBtn, modifierBtn, supprimerBtn, clearBtn, refreshBtn;

    /* ── Table ── */
    @FXML private TableView<Destination>            tableView;
    @FXML private TableColumn<Destination, Integer> idCol;
    @FXML private TableColumn<Destination, String>  paysCol, villeCol, descriptionCol;
    @FXML private TableColumn<Destination, String>  dateDebutCol;  // null si pas dans le FXML
    @FXML private TableColumn<Destination, String>  dateFinCol;    // null si pas dans le FXML

    @FXML private Label countLabel;

    private ServiceDestination serviceDestination;
    private ObservableList<Destination> destinationList;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ══════════════════════════════════════════
       INIT
    ══════════════════════════════════════════ */
    @FXML
    public void initialize() {
        serviceDestination = new ServiceDestination();

        // Colonnes de base (toujours présentes)
        idCol.setCellValueFactory(new PropertyValueFactory<>("idDestination"));
        paysCol.setCellValueFactory(new PropertyValueFactory<>("pays"));
        villeCol.setCellValueFactory(new PropertyValueFactory<>("ville"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Colonnes dates — seulement si elles existent dans le FXML (null-safe)
        if (dateDebutCol != null) {
            dateDebutCol.setCellValueFactory(data -> {
                LocalDate d = data.getValue().getDateDebut();
                return new javafx.beans.property.SimpleStringProperty(
                        d != null ? d.format(FMT) : "—");
            });
        }
        if (dateFinCol != null) {
            dateFinCol.setCellValueFactory(data -> {
                LocalDate d = data.getValue().getDateFin();
                return new javafx.beans.property.SimpleStringProperty(
                        d != null ? d.format(FMT) : "—");
            });
        }

        loadDestinations();

        searchField.textProperty().addListener((obs, old, nw) -> searchDestinations(nw));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                paysField.setText(sel.getPays());
                villeField.setText(sel.getVille());
                descriptionField.setText(sel.getDescription());
                if (dateDebutPicker != null) dateDebutPicker.setValue(sel.getDateDebut());
                if (dateFinPicker   != null) dateFinPicker.setValue(sel.getDateFin());
            }
        });
    }

    private void loadDestinations() {
        try {
            List<Destination> list = serviceDestination.readAll();
            destinationList = FXCollections.observableArrayList(list);
            tableView.setItems(destinationList);
            if (countLabel != null) countLabel.setText(String.valueOf(list.size()));
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    /* ══════════════════════════════════════════
       CRUD
    ══════════════════════════════════════════ */
    @FXML
    private void ajouterDestination() {
        if (!validerChamps()) return;
        Destination d = new Destination(
                0,
                paysField.getText().trim(),
                villeField.getText().trim(),
                descriptionField.getText().trim(),
                dateDebutPicker != null ? dateDebutPicker.getValue() : null,
                dateFinPicker   != null ? dateFinPicker.getValue()   : null
        );
        try {
            serviceDestination.ajouter(d);
            clearFields();
            loadDestinations();
            showInfo("Destination ajoutée !");
        } catch (SQLException e) { showAlert("Erreur", e.getMessage()); }
    }

    @FXML
    private void modifierDestination() {
        Destination sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Attention", "Sélectionnez une destination !"); return; }
        if (!validerChamps()) return;

        sel.setPays(paysField.getText().trim());
        sel.setVille(villeField.getText().trim());
        sel.setDescription(descriptionField.getText().trim());
        if (dateDebutPicker != null) sel.setDateDebut(dateDebutPicker.getValue());
        if (dateFinPicker   != null) sel.setDateFin(dateFinPicker.getValue());

        try {
            serviceDestination.modifier(sel);
            clearFields();
            loadDestinations();
            showInfo("Destination modifiée !");
        } catch (SQLException e) { showAlert("Erreur", e.getMessage()); }
    }

    @FXML
    private void supprimerDestination() {
        Destination sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Attention", "Sélectionnez une destination !"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer « " + sel.getVille() + " » ?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    serviceDestination.supprimer(sel);
                    clearFields();
                    loadDestinations();
                } catch (SQLException e) { showAlert("Erreur", e.getMessage()); }
            }
        });
    }

    @FXML
    private void clearFields() {
        paysField.clear();
        villeField.clear();
        descriptionField.clear();
        if (dateDebutPicker != null) dateDebutPicker.setValue(null);
        if (dateFinPicker   != null) dateFinPicker.setValue(null);
        tableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void refreshTable() {
        loadDestinations();
        searchField.clear();
    }

    private void searchDestinations(String query) {
        if (query == null || query.isEmpty()) { tableView.setItems(destinationList); return; }
        String q = query.toLowerCase();
        ObservableList<Destination> filtered = FXCollections.observableArrayList();
        for (Destination d : destinationList) {
            if (d.getPays().toLowerCase().contains(q)
                    || d.getVille().toLowerCase().contains(q)
                    || (d.getDescription() != null && d.getDescription().toLowerCase().contains(q)))
                filtered.add(d);
        }
        tableView.setItems(filtered);
    }

    private boolean validerChamps() {
        if (paysField.getText().trim().isEmpty() || villeField.getText().trim().isEmpty()) {
            showAlert("Champs requis", "Pays et Ville sont obligatoires."); return false;
        }
        if (dateDebutPicker != null && dateFinPicker != null) {
            if (dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
                showAlert("Dates requises", "Veuillez sélectionner une date de début et de fin."); return false;
            }
            if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
                showAlert("Dates invalides", "La date de fin doit être après la date de début."); return false;
            }
        }
        return true;
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Succès"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}