package Controller;

import Entite.*;
import Service.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class OffreController implements Initializable {

    // ── Champs formulaire ──────────────────────────────────────────────────
    @FXML private TextField  typeField;
    @FXML private TextField  prixField;
    @FXML private TextArea   descriptionArea;
    @FXML private CheckBox   disponibiliteCheck;

    @FXML private ComboBox<Voyage>      voyageCombo;
    @FXML private ComboBox<Destination> destinationCombo;
    @FXML private ComboBox<Vol>         volCombo;
    @FXML private ComboBox<Hotel>       hotelCombo;
    @FXML private ComboBox<Activite>    activiteCombo;

    // ── Image ──────────────────────────────────────────────────────────────
    @FXML private TextField  imagePathField;
    @FXML private Button     browseImageBtn;
    @FXML private Button     clearImageBtn;
    @FXML private StackPane  imagePreviewPane;
    @FXML private ImageView  imagePreview;
    @FXML private Label      imagePlaceholderLabel;

    // ── Table ──────────────────────────────────────────────────────────────
    @FXML private TableView<Offre>           tableView;
    @FXML private TableColumn<Offre, String>  typeCol;
    @FXML private TableColumn<Offre, Double>  prixCol;
    @FXML private TableColumn<Offre, String>  descriptionCol;
    @FXML private TableColumn<Offre, Boolean> disponibiliteCol;
    @FXML private TableColumn<Offre, String>  voyageCol;
    @FXML private TableColumn<Offre, String>  destinationCol;
    @FXML private TableColumn<Offre, String>  volCol;
    @FXML private TableColumn<Offre, String>  hotelCol;
    @FXML private TableColumn<Offre, String>  activiteCol;
    @FXML private TableColumn<Offre, String>  imageCol;

    // ── Recherche & Stats ──────────────────────────────────────────────────
    @FXML private TextField searchField;
    @FXML private Label     countLabel;
    @FXML private Label     disponiblesLabel;
    @FXML private Label     nonDisponiblesLabel;

    // ── Services ──────────────────────────────────────────────────────────
    private final ServiceOffre       serviceOffre       = new ServiceOffre();
    private final ServiceVoyage      serviceVoyage      = new ServiceVoyage();
    private final ServiceVol         serviceVol         = new ServiceVol();
    private final ServiceHotel       serviceHotel       = new ServiceHotel();
    private final ServiceDestination serviceDestination = new ServiceDestination();
    private final ServiceActivite    serviceActivite    = new ServiceActivite();

    // ── État interne ───────────────────────────────────────────────────────
    private ObservableList<Offre> offreList = FXCollections.observableArrayList();
    private Offre selectedOffre = null;

    // ══════════════════════════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureColumns();
        loadCombos();
        loadTable();
        setupSearch();
        setupTableSelection();
    }

    // ── Configuration des colonnes ─────────────────────────────────────────
    private void configureColumns() {
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        disponibiliteCol.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));

        voyageCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getvoyage() != null
                                ? cell.getValue().getvoyage().toString() : "—"));

        destinationCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getDestination() != null
                                ? cell.getValue().getDestination().toString() : "—"));

        volCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getVol() != null
                                ? cell.getValue().getVol().toString() : "—"));

        hotelCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getHotel() != null
                                ? cell.getValue().getHotel().toString() : "—"));

        activiteCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getActivite() != null
                                ? cell.getValue().getActivite().toString() : "—"));

        // Colonne image : affiche "✔" si une image est définie, "—" sinon
        imageCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getImagePath() != null
                                && !cell.getValue().getImagePath().isEmpty()
                                ? "✔" : "—"));
    }

    // ── Chargement des ComboBoxes ──────────────────────────────────────────
    private void loadCombos() {
        try {
            voyageCombo.setItems(FXCollections.observableArrayList(
                    serviceVoyage.readAll()));
            volCombo.setItems(FXCollections.observableArrayList(
                    serviceVol.readAll()));
            hotelCombo.setItems(FXCollections.observableArrayList(
                    serviceHotel.readAll()));
            destinationCombo.setItems(FXCollections.observableArrayList(
                    serviceDestination.readAll()));
            activiteCombo.setItems(FXCollections.observableArrayList(
                    serviceActivite.readAll()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur chargement des listes : " + e.getMessage());
        }
    }

    // ── Chargement de la table ─────────────────────────────────────────────
    private void loadTable() {
        try {
            List<Offre> list = serviceOffre.readAll();
            offreList.setAll(list);
            tableView.setItems(offreList);
            updateStats();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur chargement des offres : " + e.getMessage());
        }
    }

    // ── Recherche en temps réel ────────────────────────────────────────────
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal == null ? "" : newVal.toLowerCase().trim();
            if (filter.isEmpty()) {
                tableView.setItems(offreList);
            } else {
                ObservableList<Offre> filtered = FXCollections.observableArrayList();
                for (Offre o : offreList) {
                    if ((o.getType() != null && o.getType().toLowerCase().contains(filter))
                            || (o.getDescription() != null && o.getDescription().toLowerCase().contains(filter))) {
                        filtered.add(o);
                    }
                }
                tableView.setItems(filtered);
            }
        });
    }

    // ── Sélection dans la table → remplissage formulaire ──────────────────
    private void setupTableSelection() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedOffre = newVal;
                        fillForm(newVal);
                    }
                });
    }

    private void fillForm(Offre o) {
        typeField.setText(o.getType());
        prixField.setText(String.valueOf(o.getPrix()));
        descriptionArea.setText(o.getDescription());
        disponibiliteCheck.setSelected(o.isDisponibilite());
        voyageCombo.setValue(o.getvoyage());
        volCombo.setValue(o.getVol());
        hotelCombo.setValue(o.getHotel());
        destinationCombo.setValue(o.getDestination());
        activiteCombo.setValue(o.getActivite());

        if (o.getImagePath() != null && !o.getImagePath().isEmpty()) {
            imagePathField.setText(o.getImagePath());
            showImagePreview(o.getImagePath());
        } else {
            imagePathField.clear();
            hideImagePreview();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ACTIONS BOUTONS
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    private void browseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
        File file = fc.showOpenDialog(browseImageBtn.getScene().getWindow());
        if (file != null) {
            imagePathField.setText(file.getAbsolutePath());
            showImagePreview(file.getAbsolutePath());
        }
    }

    @FXML
    private void clearImage() {
        imagePathField.clear();
        imagePreview.setImage(null);
        imagePreview.setVisible(false);
        imagePlaceholderLabel.setVisible(true);
    }

    @FXML
    private void ajouterOffre() {
        if (!validateForm()) return;
        try {
            Offre o = buildOffreFromForm();
            if (serviceOffre.ajouter(o)) {
                showAlert(Alert.AlertType.INFORMATION, "✅ Offre ajoutée avec succès !");
                clearFields();
                loadTable();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @FXML
    private void modifierOffre() {
        if (selectedOffre == null) {
            showAlert(Alert.AlertType.WARNING, "⚠ Veuillez sélectionner une offre à modifier.");
            return;
        }
        if (!validateForm()) return;
        try {
            Offre o = buildOffreFromForm();
            o.setId_offre(selectedOffre.getId_offre());
            if (serviceOffre.modifier(o)) {
                showAlert(Alert.AlertType.INFORMATION, "✅ Offre modifiée avec succès !");
                clearFields();
                loadTable();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la modification : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerOffre() {
        if (selectedOffre == null) {
            showAlert(Alert.AlertType.WARNING, "⚠ Veuillez sélectionner une offre à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer la suppression de cette offre ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    if (serviceOffre.supprimer(selectedOffre.getId_offre())) {
                        showAlert(Alert.AlertType.INFORMATION, "✅ Offre supprimée avec succès.");
                        clearFields();
                        loadTable();
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur lors de la suppression : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void refreshTable() {
        searchField.clear();
        loadTable();
    }

    @FXML
    public void clearFields() {
        typeField.clear();
        prixField.clear();
        descriptionArea.clear();
        disponibiliteCheck.setSelected(false);
        voyageCombo.setValue(null);
        volCombo.setValue(null);
        hotelCombo.setValue(null);
        destinationCombo.setValue(null);
        activiteCombo.setValue(null);
        imagePathField.clear();
        hideImagePreview();
        selectedOffre = null;
        tableView.getSelectionModel().clearSelection();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HELPERS PRIVÉS
    // ══════════════════════════════════════════════════════════════════════

    /** Construit un objet Offre à partir des champs du formulaire. */
    private Offre buildOffreFromForm() {
        Offre o = new Offre();
        o.setType(typeField.getText().trim());
        o.setPrix(Double.parseDouble(prixField.getText().trim()));
        o.setDescription(descriptionArea.getText().trim());
        o.setDisponibilite(disponibiliteCheck.isSelected());
        o.setvoyage(voyageCombo.getValue());
        o.setVol(volCombo.getValue());
        o.setHotel(hotelCombo.getValue());
        o.setDestination(destinationCombo.getValue());
        o.setActivite(activiteCombo.getValue());
        String imgPath = imagePathField.getText().trim();
        o.setImagePath(imgPath.isEmpty() ? null : imgPath);
        return o;
    }

    /** Affiche l'image dans le StackPane de prévisualisation. */
    private void showImagePreview(String path) {
        try {
            File f = new File(path);
            if (f.exists()) {
                Image img = new Image(f.toURI().toString());
                imagePreview.setImage(img);
                imagePreview.setVisible(true);
                imagePlaceholderLabel.setVisible(false);
            } else {
                hideImagePreview();
            }
        } catch (Exception e) {
            hideImagePreview();
        }
    }

    /** Masque l'image et affiche le placeholder. */
    private void hideImagePreview() {
        imagePreview.setImage(null);
        imagePreview.setVisible(false);
        imagePlaceholderLabel.setVisible(true);
    }

    /** Valide les champs obligatoires du formulaire. */
    private boolean validateForm() {
        if (typeField.getText() == null || typeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "⚠ Le champ 'Type' est obligatoire.");
            typeField.requestFocus();
            return false;
        }
        try {
            double prix = Double.parseDouble(prixField.getText().trim());
            if (prix < 0) {
                showAlert(Alert.AlertType.WARNING, "⚠ Le prix ne peut pas être négatif.");
                prixField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "⚠ Le prix doit être un nombre valide (ex: 199.99).");
            prixField.requestFocus();
            return false;
        }
        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "⚠ Le champ 'Description' est obligatoire.");
            descriptionArea.requestFocus();
            return false;
        }
        if (voyageCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "⚠ Veuillez sélectionner un voyage.");
            voyageCombo.requestFocus();
            return false;
        }
        return true;
    }

    /** Met à jour les labels de statistiques. */
    private void updateStats() {
        long dispo    = offreList.stream().filter(Offre::isDisponibilite).count();
        long nonDispo = offreList.size() - dispo;
        countLabel.setText(String.valueOf(offreList.size()));
        disponiblesLabel.setText(String.valueOf(dispo));
        nonDisponiblesLabel.setText(String.valueOf(nonDispo));
    }

    /** Affiche une boîte de dialogue. */
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}