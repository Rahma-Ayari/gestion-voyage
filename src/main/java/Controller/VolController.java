package Controller;

import Entite.Destination;
import Entite.Vol;
import Service.ServiceVol;
import Service.AmadeusFlightService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class VolController {

    private AmadeusFlightService amadeusService = new AmadeusFlightService();

    @FXML private ComboBox<String> compagnieCombo;
    @FXML private DatePicker datedepartPicker;
    @FXML private DatePicker datearriveePicker;
    @FXML private TextField prix;
    @FXML private ComboBox<String> destinationCombo;
    @FXML private ComboBox<String> villeDepartCombo;

    @FXML private ComboBox<String> filterCompagnie;
    @FXML private ComboBox<String> filterDestination;
    @FXML private ComboBox<String> filterVilleDepart;
    @FXML private DatePicker filterDateDepart;
    @FXML private DatePicker filterDateArrivee;

    @FXML private TableView<Vol> table;

    @FXML private TableColumn<Vol,String> colcompagnie;
    @FXML private TableColumn<Vol,String> coldatedepart;
    @FXML private TableColumn<Vol,String> coldatearrivee;
    @FXML private TableColumn<Vol,Double> colprix;
    @FXML private TableColumn<Vol,Integer> colidvol;
    @FXML private TableColumn<Vol,String> colVilleDepart;
    @FXML private TableColumn<Vol,String> colDestination;
    @FXML private Label count;

    private ServiceVol serviceVol;
    private ObservableList<Vol> list = FXCollections.observableArrayList();

    public VolController() throws SQLException {
        serviceVol = new ServiceVol();
    }

    @FXML
    public void initialize() {
        // Table columns mapping
        colcompagnie.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCompagnie()));
        coldatedepart.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDateDepart().toString()));
        coldatearrivee.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDateArrivee().toString()));
        colprix.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("prix"));
        colidvol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIdVol()));
        colVilleDepart.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getVilleDepart() != null ? data.getValue().getVilleDepart().getVille() : ""
        ));
        colDestination.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDestination() != null ? data.getValue().getDestination().getVille() : ""
        ));
        try {
            // Populate add-form ComboBoxes
            compagnieCombo.setItems(FXCollections.observableArrayList(serviceVol.getAllCompanies()));
            destinationCombo.setItems(FXCollections.observableArrayList(serviceVol.getAllDestinations()));
            villeDepartCombo.setItems(FXCollections.observableArrayList(serviceVol.getAllDepartures()));

            // Populate filter ComboBoxes
            filterCompagnie.setItems(FXCollections.observableArrayList(serviceVol.getAllCompanies()));
            filterDestination.setItems(FXCollections.observableArrayList(serviceVol.getAllDestinations()));
            filterVilleDepart.setItems(FXCollections.observableArrayList(serviceVol.getAllDepartures()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        loadFromDatabase();
    }


    private void loadFromDatabase() {
        try {
            list.clear();
            List<Vol> vols = serviceVol.readAll();
            list.addAll(vols);
            table.setItems(list);
            count.setText(String.valueOf(list.size()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void ajouterVol() {
        try {
            Vol v = new Vol();
            v.setCompagnie(compagnieCombo.getValue());
            v.setDateDepart(datedepartPicker.getValue().atStartOfDay());
            v.setDateArrivee(datearriveePicker.getValue().atStartOfDay());

            int depId = serviceVol.getDestinationIdByVille(villeDepartCombo.getValue());
            int destId = serviceVol.getDestinationIdByVille(destinationCombo.getValue());

            Destination dep = new Destination();
            dep.setIdDestination(depId);
            dep.setVille(villeDepartCombo.getValue());
            v.setVilleDepart(dep);

            Destination dest = new Destination();
            dest.setIdDestination(destId);
            dest.setVille(destinationCombo.getValue());
            v.setDestination(dest);

            v.setPrix(Double.parseDouble(prix.getText()));

            serviceVol.ajouter(v);
            loadFromDatabase();
            clearFields();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void modifierVol() {
        Vol selected = table.getSelectionModel().getSelectedItem();
        if(selected != null) {
            try {
                selected.setCompagnie(compagnieCombo.getValue());
                selected.setDateDepart(datedepartPicker.getValue().atStartOfDay());
                selected.setDateArrivee(datearriveePicker.getValue().atStartOfDay());

                selected.getVilleDepart().setVille(villeDepartCombo.getValue());
                selected.getDestination().setVille(destinationCombo.getValue());
                selected.setPrix(Double.parseDouble(prix.getText()));

                serviceVol.modifier(selected);
                loadFromDatabase();
                clearFields();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void supprimerVol() {
        Vol selected = table.getSelectionModel().getSelectedItem();
        if(selected != null) {
            try {
                serviceVol.supprimer(selected);
                loadFromDatabase();
                clearFields();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void clearFields() {

        prix.clear();
        compagnieCombo.setValue(null);
        datedepartPicker.setValue(null);
        datearriveePicker.setValue(null);
        destinationCombo.setValue(null);
        villeDepartCombo.setValue(null);
    }


    @FXML
    private void searchFlights() {
        try {
            List<Vol> vols = serviceVol.readAll();

            if(filterCompagnie.getValue() != null) {
                vols = vols.stream()
                        .filter(v -> v.getCompagnie().equals(filterCompagnie.getValue()))
                        .collect(Collectors.toList());
            }
            if(filterDestination.getValue() != null) {
                vols = vols.stream()
                        .filter(v -> v.getDestination().getVille().equals(filterDestination.getValue()))
                        .collect(Collectors.toList());
            }
            if(filterVilleDepart.getValue() != null) {
                vols = vols.stream()
                        .filter(v -> v.getVilleDepart().getVille().equals(filterVilleDepart.getValue()))
                        .collect(Collectors.toList());
            }
            if(filterDateDepart.getValue() != null) {
                vols = vols.stream()
                        .filter(v -> v.getDateDepart().toLocalDate().equals(filterDateDepart.getValue()))
                        .collect(Collectors.toList());
            }
            if(filterDateArrivee.getValue() != null) {
                vols = vols.stream()
                        .filter(v -> v.getDateArrivee().toLocalDate().equals(filterDateArrivee.getValue()))
                        .collect(Collectors.toList());
            }

            list.clear();
            list.addAll(vols);
            table.setItems(list);
            count.setText(String.valueOf(list.size()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void tableClicked(MouseEvent event) {
        Vol selected = table.getSelectionModel().getSelectedItem();
        if(selected != null) {
            // Fill left form
            compagnieCombo.setValue(selected.getCompagnie());
            datedepartPicker.setValue(selected.getDateDepart().toLocalDate());
            datearriveePicker.setValue(selected.getDateArrivee().toLocalDate());

            if(selected.getDestination() != null)
                destinationCombo.setValue(selected.getDestination().getVille());
            if(selected.getVilleDepart() != null)
                villeDepartCombo.setValue(selected.getVilleDepart().getVille());

            prix.setText(String.valueOf(selected.getPrix()));
        }
    }

    @FXML
    private void refreshAllTable() {
        filterCompagnie.setValue(null);
        filterDestination.setValue(null);
        filterVilleDepart.setValue(null);
        filterDateDepart.setValue(null);
        filterDateArrivee.setValue(null);
        loadFromDatabase();
    }
    @FXML
    private void searchFlightsFromAPI() {

        try {

            String dep = filterVilleDepart.getValue();
            String dest = filterDestination.getValue();

            if(dep == null || dest == null) {
                System.out.println("Select departure and destination");
                return;
            }

            List<Vol> apiFlights = serviceVol.fetchFlightsFromAPI(
                    dep,
                    dest,
                    LocalDateTime.now()
            );

            list.clear();
            list.addAll(apiFlights);
            table.setItems(list);

            count.setText(String.valueOf(list.size()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void syncFlightsAPI() {

        serviceVol.syncFlightsFromAPI();
        loadFromDatabase();

    }
}
