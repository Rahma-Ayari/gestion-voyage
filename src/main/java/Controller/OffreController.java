package Controller;

import Entite.Offre;
import Entite.Voyage;
import Entite.Vol;
import Entite.Hotel;
import Entite.Destination;
import Entite.Activite;
import Service.ServiceOffre;
import Service.ServiceVoyage;
import Service.ServiceVol;
import Service.ServiceHotel;
import Service.ServiceDestination;
import Service.ServiceActivite;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class OffreController {

    // Form fields
    @FXML private TextField typeField;
    @FXML private TextField prixField;
    @FXML private TextArea descriptionArea;
    @FXML private CheckBox disponibiliteCheck;
    @FXML private ComboBox<Voyage> voyageCombo;
    @FXML private ComboBox<Destination> destinationCombo;  // Nouveau
    @FXML private ComboBox<Vol> volCombo;
    @FXML private ComboBox<Hotel> hotelCombo;
    @FXML private ComboBox<Activite> activiteCombo;        // Nouveau

    // Buttons
    @FXML private Button ajouterBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button clearBtn;
    @FXML private Button refreshBtn;

    // Table
    @FXML private TableView<Offre> tableView;
    @FXML private TableColumn<Offre, String> typeCol;
    @FXML private TableColumn<Offre, Double> prixCol;
    @FXML private TableColumn<Offre, String> descriptionCol;
    @FXML private TableColumn<Offre, String> disponibiliteCol;
    @FXML private TableColumn<Offre, String> voyageCol;
    @FXML private TableColumn<Offre, String> destinationCol;  // Nouveau
    @FXML private TableColumn<Offre, String> volCol;
    @FXML private TableColumn<Offre, String> hotelCol;
    @FXML private TableColumn<Offre, String> activiteCol;     // Nouveau

    // Search and stats
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private Label disponiblesLabel;
    @FXML private Label nonDisponiblesLabel;

    // Services
    private ServiceOffre offreService;
    private ServiceVoyage voyageService;
    private ServiceDestination destinationService;  // Nouveau
    private ServiceVol volService;
    private ServiceHotel hotelService;
    private ServiceActivite activiteService;        // Nouveau

    // Data
    private ObservableList<Offre> offreList;
    private Offre selectedOffre;

    @FXML
    public void initialize() {
        // Initialiser les services
        offreService = new ServiceOffre();
        voyageService = new ServiceVoyage();
        destinationService = new ServiceDestination();
        volService = new ServiceVol();
        hotelService = new ServiceHotel();
        activiteService = new ServiceActivite();

        // Configurer les colonnes du tableau
        setupTableColumns();

        // Charger les données
        chargerVoyages();
        chargerDestinations();  // Nouveau
        chargerVols();
        chargerHotels();
        chargerActivites();     // Nouveau
        refreshTable();

        // Configurer les listeners
        setupListeners();
    }

    private void setupTableColumns() {
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Colonne disponibilité avec formatage
        disponibiliteCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isDisponibilite() ? "✅ Oui" : "❌ Non")
        );

        // Colonne voyage avec TOUS les détails
        voyageCol.setCellValueFactory(cellData -> {
            Voyage voyage = cellData.getValue().getvoyage();
            if (voyage != null) {
                StringBuilder voyageDetails = new StringBuilder();

                // Destination (ville, pays)
                if (voyage.getDestination() != null) {
                    voyageDetails.append(voyage.getDestination().getVille())
                            .append(", ")
                            .append(voyage.getDestination().getPays());
                } else {
                    voyageDetails.append("Destination inconnue");
                }

                // Dates
                voyageDetails.append(" | ")
                        .append(voyage.getDateDebut())
                        .append(" → ")
                        .append(voyage.getDateFin());

                // Durée
                voyageDetails.append(" | ")
                        .append(voyage.getDuree())
                        .append(" jours");

                // Rythme
                voyageDetails.append(" | Rythme: ")
                        .append(voyage.getRythme());

                return new SimpleStringProperty(voyageDetails.toString());
            }
            return new SimpleStringProperty("N/A");
        });

        // Colonne Destination (Nouveau)
        destinationCol.setCellValueFactory(cellData -> {
            Destination destination = cellData.getValue().getDestination();
            if (destination != null) {
                StringBuilder destDetails = new StringBuilder();
                destDetails.append(destination.getPays())
                        .append(" - ")
                        .append(destination.getVille());
                return new SimpleStringProperty(destDetails.toString());
            }
            return new SimpleStringProperty("N/A");
        });

        // Colonne Vol
        volCol.setCellValueFactory(cellData -> {
            Vol vol = cellData.getValue().getVol();
            if (vol != null) {
                StringBuilder volDetails = new StringBuilder();
                volDetails.append(vol.getNumeroVol())
                        .append(" - ")
                        .append(vol.getCompagnie())
                        .append(" | ")
                        .append(vol.getDateDepart())
                        .append(" → ")
                        .append(vol.getDateArrivee());
                return new SimpleStringProperty(volDetails.toString());
            }
            return new SimpleStringProperty("N/A");
        });

        // Colonne Hotel
        hotelCol.setCellValueFactory(cellData -> {
            Hotel hotel = cellData.getValue().getHotel();
            if (hotel != null) {
                StringBuilder hotelDetails = new StringBuilder();
                hotelDetails.append(hotel.getNom())
                        .append(" - ")
                        .append(hotel.getVille())
                        .append(" | ")
                        .append(hotel.getAdresse());
                return new SimpleStringProperty(hotelDetails.toString());
            }
            return new SimpleStringProperty("N/A");
        });

        // Colonne Activité (Nouveau)
        activiteCol.setCellValueFactory(cellData -> {
            Activite activite = cellData.getValue().getActivite();
            if (activite != null) {
                StringBuilder actDetails = new StringBuilder();
                actDetails.append(activite.getNom())
                        .append(" - ")
                        .append(activite.getCategorie())
                        .append(" | ")
                        .append(activite.getPrix())
                        .append(" €");
                return new SimpleStringProperty(actDetails.toString());
            }
            return new SimpleStringProperty("N/A");
        });

        // Style pour le prix
        prixCol.setCellFactory(column -> new TableCell<Offre, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", item));
                    setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void setupListeners() {
        // Double-clic sur une ligne pour sélectionner
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                selectedOffre = tableView.getSelectionModel().getSelectedItem();
                if (selectedOffre != null) {
                    remplirFormulaire(selectedOffre);
                }
            }
        });

        // Recherche en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrerOffres(newValue);
        });
    }

    private void chargerVoyages() {
        try {
            voyageCombo.getItems().clear();
            voyageCombo.getItems().addAll(voyageService.readAll());

            voyageCombo.setCellFactory(param -> new ListCell<Voyage>() {
                @Override
                protected void updateItem(Voyage item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        StringBuilder display = new StringBuilder();

                        if (item.getDestination() != null) {
                            display.append(item.getDestination().getVille())
                                    .append(", ")
                                    .append(item.getDestination().getPays());
                        } else {
                            display.append("Destination inconnue");
                        }

                        display.append(" | ")
                                .append(item.getDateDebut())
                                .append(" → ")
                                .append(item.getDateFin())
                                .append(" (")
                                .append(item.getDuree())
                                .append(" jours)");

                        display.append(" | ")
                                .append(item.getRythme());

                        setText(display.toString());
                    }
                }
            });

            voyageCombo.setButtonCell(new ListCell<Voyage>() {
                @Override
                protected void updateItem(Voyage item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        StringBuilder display = new StringBuilder();

                        if (item.getDestination() != null) {
                            display.append(item.getDestination().getVille())
                                    .append(", ")
                                    .append(item.getDestination().getPays());
                        } else {
                            display.append("Destination inconnue");
                        }

                        display.append(" | ")
                                .append(item.getDateDebut())
                                .append(" → ")
                                .append(item.getDateFin())
                                .append(" (")
                                .append(item.getDuree())
                                .append(" jours)");

                        setText(display.toString());
                    }
                }
            });
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les voyages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Nouveau : Charger les destinations
    private void chargerDestinations() {
        try {
            destinationCombo.getItems().clear();
            destinationCombo.getItems().add(null); // Option "Aucune"
            destinationCombo.getItems().addAll(destinationService.readAll());

            destinationCombo.setCellFactory(param -> new ListCell<Destination>() {
                @Override
                protected void updateItem(Destination item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Aucune destination");
                    } else {
                        setText(item.getPays() + " - " + item.getVille());
                    }
                }
            });

            destinationCombo.setButtonCell(new ListCell<Destination>() {
                @Override
                protected void updateItem(Destination item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Aucune destination");
                    } else {
                        setText(item.getPays() + " - " + item.getVille());
                    }
                }
            });
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les destinations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerVols() {
        try {
            volCombo.getItems().clear();
            volCombo.getItems().add(null); // Option "Aucun"
            volCombo.getItems().addAll(volService.readAll());

            volCombo.setCellFactory(param -> new ListCell<Vol>() {
                @Override
                protected void updateItem(Vol item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Aucun vol");
                    } else {
                        setText(item.getNumeroVol() + " - " + item.getCompagnie() +
                                " (" + item.getPrix() + " €)");
                    }
                }
            });

            volCombo.setButtonCell(new ListCell<Vol>() {
                @Override
                protected void updateItem(Vol item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Aucun vol");
                    } else {
                        setText(item.getNumeroVol() + " - " + item.getCompagnie());
                    }
                }
            });
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les vols: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerHotels() {
        try {
            hotelCombo.getItems().clear();
            hotelCombo.getItems().add(null); // Option "Aucun"
            hotelCombo.getItems().addAll(hotelService.readAll());

            hotelCombo.setCellFactory(param -> new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Aucun hôtel");
                    } else {
                        setText(item.getNom() + " - " + item.getVille() +
                                " (" + item.getAdresse() + ")");
                    }
                }
            });

            hotelCombo.setButtonCell(new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Aucun hôtel");
                    } else {
                        setText(item.getNom() + " - " + item.getVille());
                    }
                }
            });
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les hôtels: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Nouveau : Charger les activités
    private void chargerActivites() {
        try {
            activiteCombo.getItems().clear();
            activiteCombo.getItems().add(null); // Option "Aucune"
            activiteCombo.getItems().addAll(activiteService.readAll());

            activiteCombo.setCellFactory(param -> new ListCell<Activite>() {
                @Override
                protected void updateItem(Activite item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Aucune activité");
                    } else {
                        setText(item.getNom() + " - " + item.getCategorie() +
                                " (" + item.getPrix() + " €)");
                    }
                }
            });

            activiteCombo.setButtonCell(new ListCell<Activite>() {
                @Override
                protected void updateItem(Activite item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Aucune activité");
                    } else {
                        setText(item.getNom() + " - " + item.getCategorie());
                    }
                }
            });
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les activités: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void refreshTable() {
        try {
            offreList = FXCollections.observableArrayList(offreService.readAll());
            tableView.setItems(offreList);
            updateStats();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les offres: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStats() {
        int total = offreList.size();
        long disponibles = offreList.stream().filter(Offre::isDisponibilite).count();
        long nonDisponibles = total - disponibles;

        countLabel.setText(String.valueOf(total));
        disponiblesLabel.setText(String.valueOf(disponibles));
        nonDisponiblesLabel.setText(String.valueOf(nonDisponibles));
    }

    @FXML
    public void ajouterOffre() {
        try {
            // Validation
            if (!validerFormulaire()) {
                return;
            }

            // Créer l'offre
            Offre offre = new Offre();
            offre.setType(typeField.getText().trim());
            offre.setPrix(Double.parseDouble(prixField.getText().trim()));
            offre.setDescription(descriptionArea.getText().trim());
            offre.setDisponibilite(disponibiliteCheck.isSelected());
            offre.setvoyage(voyageCombo.getValue());
            offre.setDestination(destinationCombo.getValue());  // Nouveau
            offre.setVol(volCombo.getValue());
            offre.setHotel(hotelCombo.getValue());
            offre.setActivite(activiteCombo.getValue());        // Nouveau

            boolean success = offreService.ajouter(offre);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre ajoutée avec succès!");
                refreshTable();
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout de l'offre!");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le prix doit être un nombre valide!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ajouter l'offre: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void modifierOffre() {
        if (selectedOffre == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner une offre à modifier!");
            return;
        }

        try {
            if (!validerFormulaire()) {
                return;
            }

            selectedOffre.setType(typeField.getText().trim());
            selectedOffre.setPrix(Double.parseDouble(prixField.getText().trim()));
            selectedOffre.setDescription(descriptionArea.getText().trim());
            selectedOffre.setDisponibilite(disponibiliteCheck.isSelected());
            selectedOffre.setvoyage(voyageCombo.getValue());
            selectedOffre.setDestination(destinationCombo.getValue());  // Nouveau
            selectedOffre.setVol(volCombo.getValue());
            selectedOffre.setHotel(hotelCombo.getValue());
            selectedOffre.setActivite(activiteCombo.getValue());        // Nouveau

            boolean success = offreService.modifier(selectedOffre);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre modifiée avec succès!");
                refreshTable();
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification de l'offre!");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le prix doit être un nombre valide!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de modifier l'offre: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void supprimerOffre() {
        selectedOffre = tableView.getSelectionModel().getSelectedItem();

        if (selectedOffre == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner une offre à supprimer!");
            return;
        }

        // Confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'offre");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette offre?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                boolean success = offreService.supprimer(selectedOffre.getId_offre());

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre supprimée avec succès!");
                    refreshTable();
                    clearFields();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression de l'offre!");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de supprimer l'offre: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void clearFields() {
        typeField.clear();
        prixField.clear();
        descriptionArea.clear();
        disponibiliteCheck.setSelected(false);
        voyageCombo.setValue(null);
        destinationCombo.setValue(null);  // Nouveau
        volCombo.setValue(null);
        hotelCombo.setValue(null);
        activiteCombo.setValue(null);     // Nouveau
        selectedOffre = null;
        tableView.getSelectionModel().clearSelection();
    }

    private void remplirFormulaire(Offre offre) {
        typeField.setText(offre.getType());
        prixField.setText(String.valueOf(offre.getPrix()));
        descriptionArea.setText(offre.getDescription());
        disponibiliteCheck.setSelected(offre.isDisponibilite());
        voyageCombo.setValue(offre.getvoyage());
        destinationCombo.setValue(offre.getDestination());  // Nouveau
        volCombo.setValue(offre.getVol());
        hotelCombo.setValue(offre.getHotel());
        activiteCombo.setValue(offre.getActivite());        // Nouveau
    }

    private boolean validerFormulaire() {
        if (typeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le type est obligatoire!");
            return false;
        }
        if (prixField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le prix est obligatoire!");
            return false;
        }

        // Vérifier que le prix est un nombre valide
        try {
            double prix = Double.parseDouble(prixField.getText().trim());
            if (prix < 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le prix ne peut pas être négatif!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le prix doit être un nombre valide!");
            return false;
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La description est obligatoire!");
            return false;
        }
        if (voyageCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un voyage!");
            return false;
        }
        return true;
    }

    private void filtrerOffres(String recherche) {
        if (recherche == null || recherche.trim().isEmpty()) {
            tableView.setItems(offreList);
        } else {
            ObservableList<Offre> filteredList = offreList.filtered(offre -> {
                String searchLower = recherche.toLowerCase();

                // Recherche dans le type
                if (offre.getType().toLowerCase().contains(searchLower)) {
                    return true;
                }

                // Recherche dans la description
                if (offre.getDescription().toLowerCase().contains(searchLower)) {
                    return true;
                }

                // Recherche dans le voyage
                Voyage voyage = offre.getvoyage();
                if (voyage != null) {
                    if (voyage.getDestination() != null) {
                        if (voyage.getDestination().getVille().toLowerCase().contains(searchLower) ||
                                voyage.getDestination().getPays().toLowerCase().contains(searchLower)) {
                            return true;
                        }
                    }
                    if (voyage.getRythme().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                }

                // Recherche dans la destination (Nouveau)
                Destination destination = offre.getDestination();
                if (destination != null) {
                    if (destination.getPays().toLowerCase().contains(searchLower) ||
                            destination.getVille().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                }

                // Recherche dans le vol
                Vol vol = offre.getVol();
                if (vol != null) {
                    if (vol.getNumeroVol().toLowerCase().contains(searchLower) ||
                            vol.getCompagnie().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                }

                // Recherche dans l'hotel
                Hotel hotel = offre.getHotel();
                if (hotel != null) {
                    if (hotel.getNom().toLowerCase().contains(searchLower) ||
                            hotel.getVille().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                }

                // Recherche dans l'activité (Nouveau)
                Activite activite = offre.getActivite();
                if (activite != null) {
                    if (activite.getNom().toLowerCase().contains(searchLower) ||
                            activite.getCategorie().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                }

                return false;
            });
            tableView.setItems(filteredList);
        }
        updateStats();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}