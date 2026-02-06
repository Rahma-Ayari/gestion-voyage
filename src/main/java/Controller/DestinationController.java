package Controller;

import Entite.Destination;
import Service.ServiceDestination;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class DestinationController {

    @FXML
    private TextField paysField, villeField, searchField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private Button ajouterBtn, modifierBtn, supprimerBtn, clearBtn, refreshBtn;
    @FXML
    private TableView<Destination> tableView;
    @FXML
    private TableColumn<Destination, Integer> idCol;
    @FXML
    private TableColumn<Destination, String> paysCol, villeCol, descriptionCol;
    @FXML
    private Label countLabel;

    private ServiceDestination serviceDestination;
    private ObservableList<Destination> destinationList;

    @FXML
    public void initialize() {
        serviceDestination = new ServiceDestination();

        idCol.setCellValueFactory(new PropertyValueFactory<>("idDestination"));
        paysCol.setCellValueFactory(new PropertyValueFactory<>("pays"));
        villeCol.setCellValueFactory(new PropertyValueFactory<>("ville"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        loadDestinations();

        searchField.textProperty().addListener((obs, oldText, newText) -> searchDestinations(newText));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                paysField.setText(newSel.getPays());
                villeField.setText(newSel.getVille());
                descriptionField.setText(newSel.getDescription());
            }
        });
    }

    private void loadDestinations() {
        try {
            List<Destination> list = serviceDestination.readAll();
            destinationList = FXCollections.observableArrayList(list);
            tableView.setItems(destinationList);
            countLabel.setText(String.valueOf(list.size()));
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void ajouterDestination() {
        if (paysField.getText().isEmpty() || villeField.getText().isEmpty()) {
            showAlert("Attention", "Veuillez remplir tous les champs !");
            return;
        }

        Destination d = new Destination(
                0,
                paysField.getText(),
                villeField.getText(),
                descriptionField.getText()
        );

        try {
            serviceDestination.ajouter(d);
            clearFields();
            loadDestinations();
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void modifierDestination() {
        Destination selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Sélectionnez une destination !");
            return;
        }

        selected.setPays(paysField.getText());
        selected.setVille(villeField.getText());
        selected.setDescription(descriptionField.getText());

        try {
            serviceDestination.modifier(selected);
            clearFields();
            loadDestinations();
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void supprimerDestination() {
        Destination selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Sélectionnez une destination !");
            return;
        }

        try {
            serviceDestination.supprimer(selected);
            clearFields();
            loadDestinations();
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void clearFields() {
        paysField.clear();
        villeField.clear();
        descriptionField.clear();
        tableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void refreshTable() {
        loadDestinations();
        searchField.clear();
    }

    private void searchDestinations(String query) {
        if (query.isEmpty()) {
            tableView.setItems(destinationList);
            return;
        }

        ObservableList<Destination> filtered = FXCollections.observableArrayList();
        for (Destination d : destinationList) {
            if (d.getPays().toLowerCase().contains(query.toLowerCase()) ||
                    d.getVille().toLowerCase().contains(query.toLowerCase()) ||
                    d.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(d);
            }
        }
        tableView.setItems(filtered);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
