package Controller;

import Entite.Vol;
import Entite.Destination;
import Service.ServiceVol;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class VolController {

    @FXML private TextField numero;
    @FXML private TextField compagnie;
    @FXML private TextField datedepart;
    @FXML private TextField datearrivee;
    @FXML private TextField prix;
    @FXML private TextField searchField;


    @FXML private TableView<Vol> table;
    @FXML private TableColumn<Vol, Integer> colidvol;
    @FXML private TableColumn<Vol, String> colnumvol;
    @FXML private TableColumn<Vol, String> colstatut;
    @FXML private TableColumn<Vol, String> colcompagnie;
    @FXML private TableColumn<Vol, LocalDateTime> coldatedepart;
    @FXML private TableColumn<Vol, LocalDateTime> coldatearrivee;
    @FXML private TableColumn<Vol, Double> colprix;

    @FXML private Label count;

    private ServiceVol serviceVol = new ServiceVol();
    private ObservableList<Vol> volList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colidvol.setCellValueFactory(new PropertyValueFactory<>("idVol"));
        colnumvol.setCellValueFactory(new PropertyValueFactory<>("numeroVol"));
        colcompagnie.setCellValueFactory(new PropertyValueFactory<>("compagnie"));
        coldatedepart.setCellValueFactory(new PropertyValueFactory<>("dateDepart"));
        coldatearrivee.setCellValueFactory(new PropertyValueFactory<>("dateArrivee"));
        colprix.setCellValueFactory(new PropertyValueFactory<>("prix"));

        loadTable();

        table.setOnMouseClicked(event -> {
            Vol v = table.getSelectionModel().getSelectedItem();
            if (v != null) {
                numero.setText(v.getNumeroVol());
                compagnie.setText(v.getCompagnie());
                datedepart.setText(v.getDateDepart().toString());
                datearrivee.setText(v.getDateArrivee().toString());
                prix.setText(String.valueOf(v.getPrix()));
            }
        });

        // Live search
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            searchVol(newValue);
        });
        colstatut.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateDepart() != null) {
                if (cellData.getValue().getDateDepart().isAfter(LocalDateTime.now())) {
                    return new javafx.beans.property.SimpleStringProperty("À venir");
                } else {
                    return new javafx.beans.property.SimpleStringProperty("Passé");
                }
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

    }

    private void loadTable() {
        try {
            volList.clear();
            volList.addAll(serviceVol.readAll());
            table.setItems(volList);
            updateCount();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateCount() {
        count.setText(String.valueOf(table.getItems().size()));
    }

    @FXML
    public void ajouterVol() {
        try {
            Vol v = new Vol();
            v.setNumeroVol(numero.getText());
            v.setCompagnie(compagnie.getText());
            v.setDateDepart(LocalDateTime.parse(datedepart.getText()));
            v.setDateArrivee(LocalDateTime.parse(datearrivee.getText()));
            v.setPrix(Double.parseDouble(prix.getText()));

            Destination d = new Destination();
            d.setIdDestination(1); // change if needed
            v.setDestination(d);

            serviceVol.ajouter(v);
            loadTable();
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void supprimerVol() {
        try {
            Vol v = table.getSelectionModel().getSelectedItem();
            if (v != null) {
                serviceVol.supprimer(v);
                loadTable();
                clearFields();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void modifierVol() {
        try {
            Vol v = table.getSelectionModel().getSelectedItem();
            if (v != null) {
                v.setNumeroVol(numero.getText());
                v.setCompagnie(compagnie.getText());
                v.setDateDepart(LocalDateTime.parse(datedepart.getText()));
                v.setDateArrivee(LocalDateTime.parse(datearrivee.getText()));
                v.setPrix(Double.parseDouble(prix.getText()));

                serviceVol.modifier(v);
                loadTable();
                clearFields();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void clearFields() {
        numero.clear();
        compagnie.clear();
        datedepart.clear();
        datearrivee.clear();
        prix.clear();
    }

    @FXML
    public void refreshTable() {
        loadTable();
    }

    private void searchVol(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            table.setItems(volList);
        } else {
            table.setItems(
                    volList.stream()
                            .filter(v ->
                                    v.getNumeroVol().toLowerCase().contains(keyword.toLowerCase())
                                            || v.getCompagnie().toLowerCase().contains(keyword.toLowerCase())
                            )
                            .collect(Collectors.toCollection(FXCollections::observableArrayList))
            );
        }
        updateCount();
    }
}
