package Controller;

import Entite.Hotel;
import Service.ServiceHotel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.util.Callback;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HotelController implements Initializable {

    // ─────────────────────────────────────────────
    // FXML - Formulaire
    // ─────────────────────────────────────────────
    @FXML private TextField nomField;
    @FXML private TextField villeField;
    @FXML private TextField adresseField;
    @FXML private TextField starsField;
    @FXML private TextField capaciteField;
    @FXML private TextField typeChambreField;
    @FXML private TextField prixParNuitField;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private CheckBox  disponibiliteCheckBox;

    // ─────────────────────────────────────────────
    // FXML - Table
    // ─────────────────────────────────────────────
    @FXML private TableView<Hotel>            tableView;
    @FXML private TableColumn<Hotel, String>  nomCol;
    @FXML private TableColumn<Hotel, String>  villeCol;
    @FXML private TableColumn<Hotel, String>  adresseCol;
    @FXML private TableColumn<Hotel, Integer> starsCol;
    @FXML private TableColumn<Hotel, Integer> capaciteCol;
    @FXML private TableColumn<Hotel, String>  typeChambreCol;
    @FXML private TableColumn<Hotel, Double>  prixParNuitCol;
    @FXML private TableColumn<Hotel, Double>  latitudeCol;
    @FXML private TableColumn<Hotel, Double>  longitudeCol;
    @FXML private TableColumn<Hotel, Boolean> disponibiliteCol;
    @FXML private TableColumn<Hotel, Void>    carteCol;

    // ─────────────────────────────────────────────
    // FXML - Autres
    // ─────────────────────────────────────────────
    @FXML private TextField searchField;
    @FXML private WebView   mapView;
    @FXML private Label     countLabel;

    // ─────────────────────────────────────────────
    // SERVICE & DONNÉES
    // ─────────────────────────────────────────────
    private ServiceHotel service = new ServiceHotel();
    private ObservableList<Hotel> hotelList = FXCollections.observableArrayList();
    private Hotel hotelSelectionne = null;

    // ─────────────────────────────────────────────
    // INITIALIZE
    // ─────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Activer JavaScript et user agent pour WebView
        mapView.getEngine().setJavaScriptEnabled(true);
        mapView.getEngine().setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "Chrome/100.0.4896.127 Safari/537.36"
        );

        // Logger les erreurs WebView
        mapView.getEngine().setOnError(event ->
                System.out.println("WebView error: " + event.getMessage())
        );
        mapView.getEngine().getLoadWorker().stateProperty().addListener(
                (obs, oldState, newState) -> {
                    System.out.println("WebView state: " + newState);
                    if (newState == javafx.concurrent.Worker.State.FAILED) {
                        System.out.println("Echec: " +
                                mapView.getEngine().getLoadWorker().getException());
                    }
                }
        );

        // Lier les colonnes aux propriétés
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        villeCol.setCellValueFactory(new PropertyValueFactory<>("ville"));
        adresseCol.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        starsCol.setCellValueFactory(new PropertyValueFactory<>("stars"));
        capaciteCol.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        typeChambreCol.setCellValueFactory(new PropertyValueFactory<>("typeChambre"));
        prixParNuitCol.setCellValueFactory(new PropertyValueFactory<>("prixParNuit"));
        latitudeCol.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        longitudeCol.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        disponibiliteCol.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));

        // Colonne bouton carte
        ajouterColonneCarte();

        // Charger les données
        chargerHotels();

        // Sélection dans la table → remplir formulaire + afficher carte
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, ancien, selectionne) -> {
                    if (selectionne != null) {
                        hotelSelectionne = selectionne;
                        remplirFormulaire(selectionne);
                        mapView.getEngine().loadContent(
                                service.genererHtmlCarte(selectionne), "text/html"
                        );
                    }
                }
        );

        // Recherche en temps réel
        searchField.textProperty().addListener(
                (obs, ancien, nouveau) -> filtrerHotels(nouveau)
        );
    }

    // ─────────────────────────────────────────────
    // COLONNE BOUTON CARTE
    // ─────────────────────────────────────────────
    private void ajouterColonneCarte() {
        Callback<TableColumn<Hotel, Void>, TableCell<Hotel, Void>> cellFactory =
                param -> new TableCell<>() {
                    private final Button btn = new Button("🗺️ Carte");
                    {
                        btn.setStyle(
                                "-fx-background-color: #FF9800;" +
                                        "-fx-text-fill: white;" +
                                        "-fx-background-radius: 6;" +
                                        "-fx-padding: 4 10;" +
                                        "-fx-font-size: 12px;" +
                                        "-fx-cursor: hand;"
                        );
                        btn.setOnAction(event -> {
                            Hotel h = getTableView().getItems().get(getIndex());
                            mapView.getEngine().loadContent(
                                    service.genererHtmlCarte(h), "text/html"
                            );
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                };

        carteCol.setCellFactory(cellFactory);
    }

    // ─────────────────────────────────────────────
    // CHARGER tous les hôtels
    // ─────────────────────────────────────────────
    private void chargerHotels() {
        try {
            hotelList.setAll(service.readAll());
            tableView.setItems(hotelList);
            countLabel.setText(String.valueOf(hotelList.size()));
        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les hôtels : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // FILTRER par recherche
    // ─────────────────────────────────────────────
    private void filtrerHotels(String recherche) {
        if (recherche == null || recherche.trim().isEmpty()) {
            tableView.setItems(hotelList);
            countLabel.setText(String.valueOf(hotelList.size()));
            return;
        }
        String lower = recherche.toLowerCase();
        List<Hotel> filtres = hotelList.stream()
                .filter(h ->
                        h.getNom().toLowerCase().contains(lower) ||
                                h.getVille().toLowerCase().contains(lower) ||
                                h.getAdresse().toLowerCase().contains(lower) ||
                                h.getTypeChambre().toLowerCase().contains(lower)
                )
                .collect(Collectors.toList());

        tableView.setItems(FXCollections.observableArrayList(filtres));
        countLabel.setText(String.valueOf(filtres.size()));
    }

    // ─────────────────────────────────────────────
    // AJOUTER
    // ─────────────────────────────────────────────
    @FXML
    private void ajouterHotel() {
        if (!validerFormulaire()) return;

        Hotel h = construireHotelDepuisFormulaire(0);

        if (h.getLatitude() == 0.0 && h.getLongitude() == 0.0) {
            double[] coords = service.geocoderAdresse(h.getAdresse(), h.getVille());
            if (coords != null) {
                h.setLatitude(coords[0]);
                h.setLongitude(coords[1]);
                latitudeField.setText(String.valueOf(coords[0]));
                longitudeField.setText(String.valueOf(coords[1]));
            } else {
                afficherAlerte(Alert.AlertType.WARNING, "Carte",
                        "Impossible de géolocaliser l'adresse. L'hôtel sera ajouté sans position.");
            }
        }

        try {
            if (service.ajouter(h)) {
                afficherAlerte(Alert.AlertType.INFORMATION, "Succès",
                        "Hôtel \"" + h.getNom() + "\" ajouté avec succès !");
                chargerHotels();
                clearFields();
            }
        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Ajout échoué : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // MODIFIER
    // ─────────────────────────────────────────────
    @FXML
    private void modifierHotel() {
        if (hotelSelectionne == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner un hôtel dans la liste.");
            return;
        }
        if (!validerFormulaire()) return;

        Hotel h = construireHotelDepuisFormulaire(hotelSelectionne.getIdHotel());

        if (h.getLatitude() == 0.0 && h.getLongitude() == 0.0) {
            double[] coords = service.geocoderAdresse(h.getAdresse(), h.getVille());
            if (coords != null) {
                h.setLatitude(coords[0]);
                h.setLongitude(coords[1]);
                latitudeField.setText(String.valueOf(coords[0]));
                longitudeField.setText(String.valueOf(coords[1]));
            }
        }

        try {
            if (service.modifier(h)) {
                afficherAlerte(Alert.AlertType.INFORMATION, "Succès",
                        "Hôtel \"" + h.getNom() + "\" modifié avec succès !");
                chargerHotels();
                clearFields();
                hotelSelectionne = null;
            }
        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Modification échouée : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // SUPPRIMER
    // ─────────────────────────────────────────────
    @FXML
    private void supprimerHotel() {
        if (hotelSelectionne == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner un hôtel dans la liste.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'hôtel \"" + hotelSelectionne.getNom() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            try {
                if (service.supprimer(hotelSelectionne)) {
                    afficherAlerte(Alert.AlertType.INFORMATION, "Succès",
                            "Hôtel supprimé avec succès !");
                    chargerHotels();
                    clearFields();
                    mapView.getEngine().loadContent("", "text/html");
                    hotelSelectionne = null;
                }
            } catch (SQLException e) {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                        "Suppression échouée : " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────
    // REFRESH TABLE
    // ─────────────────────────────────────────────
    @FXML
    private void refreshTable() {
        searchField.clear();
        chargerHotels();
    }

    // ─────────────────────────────────────────────
    // EFFACER le formulaire
    // ─────────────────────────────────────────────
    @FXML
    private void clearFields() {
        nomField.clear();
        villeField.clear();
        adresseField.clear();
        starsField.clear();
        capaciteField.clear();
        typeChambreField.clear();
        prixParNuitField.clear();
        latitudeField.clear();
        longitudeField.clear();
        disponibiliteCheckBox.setSelected(false);
        hotelSelectionne = null;
    }

    // ─────────────────────────────────────────────
    // REMPLIR formulaire depuis hôtel sélectionné
    // ─────────────────────────────────────────────
    private void remplirFormulaire(Hotel h) {
        nomField.setText(h.getNom());
        villeField.setText(h.getVille());
        adresseField.setText(h.getAdresse());
        starsField.setText(String.valueOf(h.getStars()));
        capaciteField.setText(String.valueOf(h.getCapacite()));
        typeChambreField.setText(h.getTypeChambre());
        prixParNuitField.setText(String.valueOf(h.getPrixParNuit()));
        latitudeField.setText(String.valueOf(h.getLatitude()));
        longitudeField.setText(String.valueOf(h.getLongitude()));
        disponibiliteCheckBox.setSelected(h.isDisponibilite());
    }

    // ─────────────────────────────────────────────
    // CONSTRUIRE Hotel depuis le formulaire
    // ─────────────────────────────────────────────
    private Hotel construireHotelDepuisFormulaire(int id) {
        double lat = 0.0, lng = 0.0;
        try {
            if (!latitudeField.getText().trim().isEmpty())
                lat = Double.parseDouble(latitudeField.getText().trim());
            if (!longitudeField.getText().trim().isEmpty())
                lng = Double.parseDouble(longitudeField.getText().trim());
        } catch (NumberFormatException ignored) {}

        return new Hotel(
                id,
                nomField.getText().trim(),
                villeField.getText().trim(),
                adresseField.getText().trim(),
                Integer.parseInt(starsField.getText().trim()),
                Integer.parseInt(capaciteField.getText().trim()),
                typeChambreField.getText().trim(),
                Double.parseDouble(prixParNuitField.getText().trim()),
                disponibiliteCheckBox.isSelected(),
                lat,
                lng
        );
    }

    // ─────────────────────────────────────────────
    // VALIDER le formulaire
    // ─────────────────────────────────────────────
    private boolean validerFormulaire() {
        if (nomField.getText().trim().isEmpty() ||
                villeField.getText().trim().isEmpty() ||
                adresseField.getText().trim().isEmpty() ||
                starsField.getText().trim().isEmpty() ||
                capaciteField.getText().trim().isEmpty() ||
                typeChambreField.getText().trim().isEmpty() ||
                prixParNuitField.getText().trim().isEmpty()) {
            afficherAlerte(Alert.AlertType.WARNING, "Validation",
                    "Veuillez remplir tous les champs obligatoires.");
            return false;
        }
        try {
            int stars = Integer.parseInt(starsField.getText().trim());
            if (stars < 1 || stars > 5) {
                afficherAlerte(Alert.AlertType.WARNING, "Validation",
                        "Le nombre d'étoiles doit être entre 1 et 5.");
                return false;
            }
            Integer.parseInt(capaciteField.getText().trim());
            Double.parseDouble(prixParNuitField.getText().trim());
        } catch (NumberFormatException e) {
            afficherAlerte(Alert.AlertType.WARNING, "Validation",
                    "Stars, capacité et prix doivent être des nombres valides.");
            return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────
    // HELPER - Afficher une alerte
    // ─────────────────────────────────────────────
    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}