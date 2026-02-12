package Controller;

import Entite.Hotel;
import Service.ServiceHotel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.sql.SQLException;
import java.util.List;

public class HotelController {

    @FXML
    private TextField nomField, villeField, adresseField, starsField, capaciteField, typeChambreField, prixParNuitField, searchField;
    @FXML
    private CheckBox disponibiliteCheckBox;
    @FXML
    private Button ajouterBtn, modifierBtn, supprimerBtn, clearBtn, refreshBtn;
    @FXML
    private TableView<Hotel> tableView;
    @FXML
    private TableColumn<Hotel, String> nomCol, villeCol, adresseCol, typeChambreCol;
    @FXML
    private TableColumn<Hotel, Integer> starsCol, capaciteCol;
    @FXML
    private TableColumn<Hotel, Double> prixParNuitCol;
    @FXML
    private TableColumn<Hotel, Boolean> disponibiliteCol;
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
        starsCol.setCellValueFactory(new PropertyValueFactory<>("stars"));
        capaciteCol.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        typeChambreCol.setCellValueFactory(new PropertyValueFactory<>("typeChambre"));
        prixParNuitCol.setCellValueFactory(new PropertyValueFactory<>("prixParNuit"));

        // Colonne Disponibilité avec Oui / Non
        disponibiliteCol.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));
        disponibiliteCol.setCellFactory(col -> new TableCell<Hotel, Boolean>() {
            @Override
            protected void updateItem(Boolean dispo, boolean empty) {
                super.updateItem(dispo, empty);
                if (empty || dispo == null) {
                    setText(null);
                } else {
                    setText(dispo ? "Oui" : "Non");
                }
            }
        });

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
                starsField.setText(String.valueOf(newSel.getStars()));
                capaciteField.setText(String.valueOf(newSel.getCapacite()));
                typeChambreField.setText(newSel.getTypeChambre());
                prixParNuitField.setText(String.valueOf(newSel.getPrixParNuit()));
                disponibiliteCheckBox.setSelected(newSel.isDisponibilite());
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
        try {
            Hotel hotel = new Hotel(
                    0,
                    nomField.getText().trim(),
                    villeField.getText().trim(),
                    adresseField.getText().trim(),
                    Integer.parseInt(starsField.getText().trim()),
                    Integer.parseInt(capaciteField.getText().trim()),
                    typeChambreField.getText().trim(),
                    Double.parseDouble(prixParNuitField.getText().trim()),
                    disponibiliteCheckBox.isSelected()
            );

            if (serviceHotel.ajouter(hotel)) {
                showAlert("Succès", "Hôtel ajouté avec succès !");
                clearFields();
                loadHotels();
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Vérifiez les champs numériques (étoiles, capacité, prix).");
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

        try {
            selected.setNom(nomField.getText().trim());
            selected.setVille(villeField.getText().trim());
            selected.setAdresse(adresseField.getText().trim());
            selected.setStars(Integer.parseInt(starsField.getText().trim()));
            selected.setCapacite(Integer.parseInt(capaciteField.getText().trim()));
            selected.setTypeChambre(typeChambreField.getText().trim());
            selected.setPrixParNuit(Double.parseDouble(prixParNuitField.getText().trim()));
            selected.setDisponibilite(disponibiliteCheckBox.isSelected());

            if (serviceHotel.modifier(selected)) {
                showAlert("Succès", "Hôtel modifié avec succès !");
                clearFields();
                loadHotels();
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Vérifiez les champs numériques (étoiles, capacité, prix).");
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
        starsField.clear();
        capaciteField.clear();
        typeChambreField.clear();
        prixParNuitField.clear();
        disponibiliteCheckBox.setSelected(false);
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
                    h.getAdresse().toLowerCase().contains(query.toLowerCase()) ||
                    h.getTypeChambre().toLowerCase().contains(query.toLowerCase())) {
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