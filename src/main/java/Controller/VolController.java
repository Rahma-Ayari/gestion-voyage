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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller for Vol (Flight) Management Interface
 * Manages all CRUD operations and UI interactions for flight management
 */
public class VolController {

    // ==================== FXML UI Components ====================
    
    @FXML private TextField numero;
    @FXML private TextField datedepart;
    @FXML private TextField datearrivee;
    @FXML private TextField prix;
    @FXML private TextField searchField;
    
    @FXML private Button ajouterBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button clearBtn;
    @FXML private Button refreshBtn;
    
    @FXML private TableView<Vol> table;
    @FXML private TableColumn<Vol, Integer> colidvol;
    @FXML private TableColumn<Vol, String> colnumvol;
    @FXML private TableColumn<Vol, LocalDateTime> coldatedepart;
    @FXML private TableColumn<Vol, LocalDateTime> coldatearrivee;
    @FXML private TableColumn<Vol, Double> colprix;
    @FXML private TableColumn<Vol, String> colstatut;
    
    @FXML private Label count;
    
    // ==================== Service and Data ====================
    
    private ServiceVol serviceVol;
    private ObservableList<Vol> volList;
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private static final DateTimeFormatter DISPLAY_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ==================== Initialization ====================
    
    @FXML
    public void initialize() {
        System.out.println("VolController initialized");
        serviceVol = new ServiceVol();

        // Configure table columns
        colidvol.setCellValueFactory(new PropertyValueFactory<>("idVol"));
        colnumvol.setCellValueFactory(new PropertyValueFactory<>("numeroVol"));
        coldatedepart.setCellValueFactory(new PropertyValueFactory<>("dateDepart"));
        coldatearrivee.setCellValueFactory(new PropertyValueFactory<>("dateArrivee"));
        colprix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        
        colstatut.setCellValueFactory(cellData -> {
            Vol vol = cellData.getValue();
            String status = determineFlightStatus(vol);
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        
        formatDateColumn(coldatedepart);
        formatDateColumn(coldatearrivee);
        formatPriceColumn(colprix);

        loadVols();

        searchField.textProperty().addListener((obs, old, newVal) -> searchVols(newVal));

        table.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, newVal) -> {
                if (newVal != null) populateFields(newVal);
            }
        );
        
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                populateFields(table.getSelectionModel().getSelectedItem());
            }
        });
    }

    // ==================== Data Loading ====================
    
    private void loadVols() {
        try {
            List<Vol> list = serviceVol.readAll();
            volList = FXCollections.observableArrayList(list);
            table.setItems(volList);
            updateCount(list.size());
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les vols: " + e.getMessage(), 
                     Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void populateFields(Vol vol) {
        numero.setText(vol.getNumeroVol());
        if (vol.getDateDepart() != null) {
            datedepart.setText(vol.getDateDepart().format(DATE_FORMATTER));
        }
        if (vol.getDateArrivee() != null) {
            datearrivee.setText(vol.getDateArrivee().format(DATE_FORMATTER));
        }
        prix.setText(String.valueOf(vol.getPrix()));
    }

    // ==================== CRUD Operations ====================
    
    @FXML
    private void ajouterDestination() {
        if (!validateInputs()) return;

        try {
            LocalDateTime departDate = LocalDateTime.parse(datedepart.getText().trim(), DATE_FORMATTER);
            LocalDateTime arriveeDate = LocalDateTime.parse(datearrivee.getText().trim(), DATE_FORMATTER);
            
            if (arriveeDate.isBefore(departDate) || arriveeDate.isEqual(departDate)) {
                showAlert("Erreur", "La date d'arrivée doit être postérieure à la date de départ!", 
                         Alert.AlertType.WARNING);
                return;
            }

            double prixValue = Double.parseDouble(prix.getText().trim());
            if (prixValue <= 0) {
                showAlert("Erreur", "Le prix doit être supérieur à 0!", Alert.AlertType.WARNING);
                return;
            }

            Destination destination = new Destination();
            destination.setIdDestination(1);

            Vol nouveauVol = new Vol(0, numero.getText().trim(), "Compagnie Aérienne", 
                                    departDate, arriveeDate, prixValue, destination);

            if (serviceVol.ajouter(nouveauVol)) {
                showAlert("Succès", "Vol ajouté avec succès!", Alert.AlertType.INFORMATION);
                clearFields();
                loadVols();
            }
            
        } catch (DateTimeParseException e) {
            showAlert("Erreur", "Format de date invalide! Format: yyyy-MM-dd HH:mm", 
                     Alert.AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le prix doit être un nombre valide!", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void modifierDestination() {
        Vol selectedVol = table.getSelectionModel().getSelectedItem();
        if (selectedVol == null) {
            showAlert("Attention", "Veuillez sélectionner un vol à modifier!", 
                     Alert.AlertType.WARNING);
            return;
        }

        if (!validateInputs()) return;

        try {
            LocalDateTime departDate = LocalDateTime.parse(datedepart.getText().trim(), DATE_FORMATTER);
            LocalDateTime arriveeDate = LocalDateTime.parse(datearrivee.getText().trim(), DATE_FORMATTER);
            
            if (arriveeDate.isBefore(departDate)) {
                showAlert("Erreur", "La date d'arrivée doit être postérieure à la date de départ!", 
                         Alert.AlertType.WARNING);
                return;
            }

            double prixValue = Double.parseDouble(prix.getText().trim());
            if (prixValue <= 0) {
                showAlert("Erreur", "Le prix doit être supérieur à 0!", Alert.AlertType.WARNING);
                return;
            }

            selectedVol.setNumeroVol(numero.getText().trim());
            selectedVol.setDateDepart(departDate);
            selectedVol.setDateArrivee(arriveeDate);
            selectedVol.setPrix(prixValue);

            if (serviceVol.modifier(selectedVol)) {
                showAlert("Succès", "Vol modifié avec succès!", Alert.AlertType.INFORMATION);
                clearFields();
                loadVols();
            }
            
        } catch (DateTimeParseException e) {
            showAlert("Erreur", "Format de date invalide! Format: yyyy-MM-dd HH:mm", 
                     Alert.AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le prix doit être un nombre valide!", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage(), 
                     Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void supprimerDestination() {
        Vol selectedVol = table.getSelectionModel().getSelectedItem();
        if (selectedVol == null) {
            showAlert("Attention", "Veuillez sélectionner un vol à supprimer!", 
                     Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le vol " + selectedVol.getNumeroVol() + "?");
        confirmation.setContentText("Cette action est irréversible.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (serviceVol.supprimer(selectedVol)) {
                        showAlert("Succès", "Vol supprimé avec succès!", 
                                 Alert.AlertType.INFORMATION);
                        clearFields();
                        loadVols();
                    }
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), 
                             Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ==================== UI Helpers ====================
    
    @FXML
    private void clearFields() {
        numero.clear();
        datedepart.clear();
        datearrivee.clear();
        prix.clear();
        table.getSelectionModel().clearSelection();
    }

    @FXML
    private void refreshTable() {
        loadVols();
        searchField.clear();
        clearFields();
    }

    private void searchVols(String query) {
        if (query == null || query.trim().isEmpty()) {
            table.setItems(volList);
            updateCount(volList.size());
            return;
        }

        ObservableList<Vol> filtered = FXCollections.observableArrayList();
        String lowerQuery = query.toLowerCase().trim();
        
        for (Vol vol : volList) {
            if ((vol.getNumeroVol() != null && vol.getNumeroVol().toLowerCase().contains(lowerQuery)) ||
                (vol.getCompagnie() != null && vol.getCompagnie().toLowerCase().contains(lowerQuery)) ||
                String.valueOf(vol.getPrix()).contains(lowerQuery)) {
                filtered.add(vol);
            }
        }
        
        table.setItems(filtered);
        updateCount(filtered.size());
    }

    // ==================== Validation ====================
    
    private boolean validateInputs() {
        if (numero.getText() == null || numero.getText().trim().isEmpty() ||
            datedepart.getText() == null || datedepart.getText().trim().isEmpty() ||
            datearrivee.getText() == null || datearrivee.getText().trim().isEmpty() ||
            prix.getText() == null || prix.getText().trim().isEmpty()) {
            showAlert("Champs Manquants", "Veuillez remplir tous les champs!", 
                     Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    // ==================== Formatting ====================
    
    private void formatDateColumn(TableColumn<Vol, LocalDateTime> column) {
        column.setCellFactory(col -> new TableCell<Vol, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DISPLAY_FORMATTER.format(item));
            }
        });
    }
    
    private void formatPriceColumn(TableColumn<Vol, Double> column) {
        column.setCellFactory(col -> new TableCell<Vol, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f TND", item));
            }
        });
    }

    private String determineFlightStatus(Vol vol) {
        if (vol.getDateDepart() == null) return "Inconnu";
        LocalDateTime now = LocalDateTime.now();
        if (vol.getDateDepart().isAfter(now)) return "Programmé";
        if (vol.getDateArrivee() != null && vol.getDateArrivee().isBefore(now)) return "Terminé";
        return "En cours";
    }

    private void updateCount(int total) {
        count.setText(String.valueOf(total));
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
