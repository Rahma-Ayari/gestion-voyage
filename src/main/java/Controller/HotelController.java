package Controller;

import Entite.Hotel;
import Service.ServiceHotel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class HotelController {

    @FXML
    private TextField nomField, villeField, adresseField, searchField;
    @FXML
    private Button ajouterBtn, modifierBtn, supprimerBtn, clearBtn, refreshBtn;
    @FXML
    private TableView<Hotel> tableView;
    @FXML
    private TableColumn<Hotel, Integer> idCol;
    @FXML
    private TableColumn<Hotel, String> nomCol, villeCol, adresseCol;
    @FXML
    private Label countLabel;

    private ServiceHotel serviceHotel;
    private ObservableList<Hotel> hotelList;

    @FXML
    public void initialize() {
        serviceHotel = new ServiceHotel();

        // Configurer les colonnes de la table
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        villeCol.setCellValueFactory(new PropertyValueFactory<>("ville"));
        adresseCol.setCellValueFactory(new PropertyValueFactory<>("adresse"));

        // Charger les données
        loadHotels();

        // Listener pour la recherche
        searchField.textProperty().addListener((obs, oldText, newText) -> searchHotels(newText));

        // Listener pour sélectionner un hôtel dans la table
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                nomField.setText(newSel.getNom());
                villeField.setText(newSel.getVille());
                adresseField.setText(newSel.getAdresse());
            }
        });
    }

    private void loadHotels() {
        try {
            List<Hotel> hotels = serviceHotel.readAll();
            hotelList = FXCollections.observableArrayList(hotels);
            tableView.setItems(hotelList);
            countLabel.setText(String.valueOf(hotels.size()));
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les hôtels : " + e.getMessage());
        }
    }

    @FXML
    private void ajouterHotel() {
        String nom = nomField.getText().trim();
        String ville = villeField.getText().trim();
        String adresse = adresseField.getText().trim();

        if (nom.isEmpty() || ville.isEmpty() || adresse.isEmpty()) {
            showAlert("Attention", "Veuillez remplir tous les champs !");
            return;
        }

        Hotel hotel = new Hotel(0, nom, ville, adresse);
        try {
            if (serviceHotel.ajouter(hotel)) {
                showAlert("Succès", "Hôtel ajouté avec succès !");
                clearFields();
                loadHotels();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ajouter l'hôtel : " + e.getMessage());
        }
    }

    @FXML
    private void modifierHotel() {
        Hotel selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Sélectionnez un hôtel pour modifier !");
            return;
        }

        selected.setNom(nomField.getText().trim());
        selected.setVille(villeField.getText().trim());
        selected.setAdresse(adresseField.getText().trim());

        try {
            if (serviceHotel.modifier(selected)) {
                showAlert("Succès", "Hôtel modifié avec succès !");
                clearFields();
                loadHotels();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de modifier l'hôtel : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerHotel() {
        Hotel selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Sélectionnez un hôtel pour supprimer !");
            return;
        }

        try {
            if (serviceHotel.supprimer(selected)) {
                showAlert("Succès", "Hôtel supprimé avec succès !");
                clearFields();
                loadHotels();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de supprimer l'hôtel : " + e.getMessage());
        }
    }

    @FXML
    private void clearFields() {
        nomField.clear();
        villeField.clear();
        adresseField.clear();
        tableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void refreshTable() {
        loadHotels();
        searchField.clear();
    }

    private void searchHotels(String query) {
        if (query == null || query.isEmpty()) {
            tableView.setItems(hotelList);
            return;
        }

        ObservableList<Hotel> filtered = FXCollections.observableArrayList();
        for (Hotel h : hotelList) {
            if (h.getNom().toLowerCase().contains(query.toLowerCase()) ||
                    h.getVille().toLowerCase().contains(query.toLowerCase()) ||
                    h.getAdresse().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(h);
            }
        }
        tableView.setItems(filtered);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}