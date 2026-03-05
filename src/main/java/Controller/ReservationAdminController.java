package Controller;

import Entite.*;
import Service.ServiceReservation;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationAdminController implements Initializable {

    // ===== TABLE ET COLONNES =====
    @FXML private TableView<Reservation>            tableView;
    @FXML private TableColumn<Reservation, Integer> idCol;
    @FXML private TableColumn<Reservation, String>  clientCol;
    @FXML private TableColumn<Reservation, String>  offreCol;
    @FXML private TableColumn<Reservation, String>  dateReservationCol;
    @FXML private TableColumn<Reservation, String>  dateVoyageCol;
    @FXML private TableColumn<Reservation, String>  nbPersonnesCol;
    @FXML private TableColumn<Reservation, Double>  montantCol;
    @FXML private TableColumn<Reservation, String>  statutCol;
    @FXML private TableColumn<Reservation, String>  commentaireCol;

    // ===== STATS =====
    @FXML private Label totalLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label accepteesLabel;
    @FXML private Label refuseesLabel;
    @FXML private Label annuleesLabel;

    // ===== LEFT PANEL =====
    @FXML private VBox             detailsBox;
    @FXML private ComboBox<String> statutFilter;
    @FXML private TextField        searchField;

    // ===== BOUTONS =====
    @FXML private Button accepterBtn;
    @FXML private Button refuserBtn;
    @FXML private Button annulerBtn;

    private final ServiceReservation    service        = new ServiceReservation();
    private ObservableList<Reservation> observableList = FXCollections.observableArrayList();

    // ══════════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnes();
        configurerFiltres();
        configurerRecherche();
        configurerSelection();
        loadData();
    }

    // ══════════════════════════════════════════════════════
    //  COLONNES
    // ══════════════════════════════════════════════════════

    private void configurerColonnes() {

        idCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getId_reservation()).asObject());

        clientCol.setCellValueFactory(data -> {
            Personne p = data.getValue().getId_personne();
            if (p == null) return new SimpleStringProperty("—");
            String nom = ((p.getNom() != null ? p.getNom() : "") + " "
                    + (p.getPrenom() != null ? p.getPrenom() : "")).trim();
            return new SimpleStringProperty(nom.isBlank() ? "Client #" + p.getIdUtilisateur() : nom);
        });

        offreCol.setCellValueFactory(data -> {
            Offre o = data.getValue().getId_offre();
            if (o == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(
                    o.getType() != null ? o.getType() : "Offre #" + o.getId_offre());
        });

        dateReservationCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getDate_reservation() != null
                                ? data.getValue().getDate_reservation().toString() : "—"));

        dateVoyageCol.setCellValueFactory(data -> {
            Voyage v = data.getValue().getId_voyage();
            if (v == null || v.getDateDebut() == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(v.getDateDebut().toString());
        });

        nbPersonnesCol.setCellValueFactory(data -> new SimpleStringProperty("—"));

        montantCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getPrix_reservation()).asObject());

        // Statut coloré
        statutCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getEtat() != null ? data.getValue().getEtat() : "—"));
        statutCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String etat, boolean empty) {
                super.updateItem(etat, empty);
                if (empty || etat == null) { setText(null); setStyle(""); return; }
                setText(etat);
                switch (etat.toLowerCase()) {
                    case "acceptée"   -> setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                    case "refusée"    -> setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                    case "annulée"    -> setStyle("-fx-text-fill: #9E9E9E; -fx-font-weight: bold;");
                    case "en attente" -> setStyle("-fx-text-fill: #F7931E; -fx-font-weight: bold;");
                    default           -> setStyle("-fx-text-fill: #333;");
                }
            }
        });

        commentaireCol.setCellValueFactory(data -> new SimpleStringProperty("—"));
    }

    // ══════════════════════════════════════════════════════
    //  CHARGEMENT
    // ══════════════════════════════════════════════════════

    private void loadData() {
        try {
            List<Reservation> list = service.readAll();
            observableList.setAll(list);
            tableView.setItems(observableList);
            updateStats();
        } catch (SQLException e) {
            showAlert("Erreur chargement : " + e.getMessage());
        }
    }

    private void updateStats() {
        totalLabel.setText(String.valueOf(observableList.size()));

        long enAttente = observableList.stream()
                .filter(r -> "En attente".equalsIgnoreCase(r.getEtat())).count();
        long acceptees = observableList.stream()
                .filter(r -> "Acceptée".equalsIgnoreCase(r.getEtat())).count();
        long refusees  = observableList.stream()
                .filter(r -> "Refusée".equalsIgnoreCase(r.getEtat())).count();
        long annulees  = observableList.stream()
                .filter(r -> "Annulée".equalsIgnoreCase(r.getEtat())).count();

        enAttenteLabel.setText(String.valueOf(enAttente));
        accepteesLabel.setText(String.valueOf(acceptees));
        refuseesLabel.setText(String.valueOf(refusees));
        annuleesLabel.setText(String.valueOf(annulees));
    }

    // ══════════════════════════════════════════════════════
    //  FILTRES, RECHERCHE & SÉLECTION
    // ══════════════════════════════════════════════════════

    private void configurerFiltres() {
        statutFilter.getItems().addAll("Tous", "En attente", "Acceptée", "Refusée", "Annulée");
        statutFilter.setValue("Tous");
        statutFilter.setOnAction(e -> appliquerFiltres());
    }

    private void configurerRecherche() {
        searchField.textProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    private void appliquerFiltres() {
        String statut = statutFilter.getValue();
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();

        ObservableList<Reservation> filtered = observableList.filtered(r -> {
            // Filtre statut
            boolean matchStatut = "Tous".equals(statut)
                    || (r.getEtat() != null && r.getEtat().equalsIgnoreCase(statut));

            // Filtre recherche
            boolean matchSearch = search.isEmpty();
            if (!matchSearch) {
                // Recherche sur client
                if (r.getId_personne() != null) {
                    String nom = ((r.getId_personne().getNom() != null ? r.getId_personne().getNom() : "") + " "
                            + (r.getId_personne().getPrenom() != null ? r.getId_personne().getPrenom() : "")).toLowerCase();
                    if (nom.contains(search)) matchSearch = true;
                }
                // Recherche sur offre
                if (!matchSearch && r.getId_offre() != null && r.getId_offre().getType() != null
                        && r.getId_offre().getType().toLowerCase().contains(search)) {
                    matchSearch = true;
                }
                // Recherche sur statut
                if (!matchSearch && r.getEtat() != null && r.getEtat().toLowerCase().contains(search)) {
                    matchSearch = true;
                }
            }
            return matchStatut && matchSearch;
        });

        tableView.setItems(filtered);
    }

    private void configurerSelection() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> { if (newVal != null) showDetails(newVal); });
    }

    // ══════════════════════════════════════════════════════
    //  PANNEAU DÉTAILS
    // ══════════════════════════════════════════════════════

    private void showDetails(Reservation r) {
        detailsBox.getChildren().clear();

        Label titre = new Label("📋 Réservation #" + r.getId_reservation());
        titre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FF6B35;");
        detailsBox.getChildren().addAll(titre, new Separator());

        // Client
        if (r.getId_personne() != null) {
            Personne p = r.getId_personne();
            detailsBox.getChildren().add(sectionTitre("👤 Client"));
            String nomComplet = ((p.getNom() != null ? p.getNom() : "") + " "
                    + (p.getPrenom() != null ? p.getPrenom() : "")).trim();
            detailsBox.getChildren().add(ligneDetail("Nom",
                    nomComplet.isBlank() ? "Client #" + p.getIdUtilisateur() : nomComplet));
            detailsBox.getChildren().add(new Separator());
        }

        // Offre
        if (r.getId_offre() != null) {
            Offre o = r.getId_offre();
            detailsBox.getChildren().add(sectionTitre("🎫 Offre"));
            detailsBox.getChildren().add(ligneDetail("Type",
                    o.getType() != null ? o.getType() : "—"));
            detailsBox.getChildren().add(ligneDetail("Prix",
                    String.format("%.0f €", o.getPrix())));
            if (o.getDestination() != null) {
                Destination d = o.getDestination();
                detailsBox.getChildren().add(ligneDetail("Destination",
                        (d.getVille() != null ? d.getVille() : "") + ", "
                                + (d.getPays() != null ? d.getPays() : "")));
            }
            if (o.getHotel() != null && o.getHotel().getNom() != null)
                detailsBox.getChildren().add(ligneDetail("Hôtel", o.getHotel().getNom()));
            if (o.getVol() != null && o.getVol().getNumeroVol() != null)
                detailsBox.getChildren().add(ligneDetail("Vol", o.getVol().getNumeroVol()));
            if (o.getActivite() != null && o.getActivite().getNom() != null)
                detailsBox.getChildren().add(ligneDetail("Activité", o.getActivite().getNom()));
            detailsBox.getChildren().add(new Separator());
        }

        // Réservation
        detailsBox.getChildren().add(sectionTitre("📅 Réservation"));
        detailsBox.getChildren().add(ligneDetail("Date",
                r.getDate_reservation() != null ? r.getDate_reservation().toString() : "—"));
        detailsBox.getChildren().add(ligneDetail("Montant",
                String.format("%.0f €", r.getPrix_reservation())));

        // Badge statut
        Label statutBadge = new Label(r.getEtat() != null ? r.getEtat() : "—");
        String style = switch (r.getEtat() != null ? r.getEtat().toLowerCase() : "") {
            case "acceptée"   -> "-fx-background-color:#E8F5E9; -fx-text-fill:#27AE60;";
            case "refusée"    -> "-fx-background-color:#FFEBEE; -fx-text-fill:#E74C3C;";
            case "annulée"    -> "-fx-background-color:#F5F5F5; -fx-text-fill:#9E9E9E;";
            case "en attente" -> "-fx-background-color:#FFF8E1; -fx-text-fill:#F7931E;";
            default           -> "-fx-background-color:#F0F0F0; -fx-text-fill:#333;";
        };
        statutBadge.setStyle(style +
                "-fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:3 10;");
        HBox statutRow = new HBox(8);
        statutRow.setAlignment(Pos.CENTER_LEFT);
        Label statutLbl = new Label("Statut :");
        statutLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#888;");
        statutRow.getChildren().addAll(statutLbl, statutBadge);
        detailsBox.getChildren().add(statutRow);

        // Voyage
        if (r.getId_voyage() != null) {
            Voyage v = r.getId_voyage();
            detailsBox.getChildren().addAll(new Separator(), sectionTitre("✈ Voyage"));
            detailsBox.getChildren().add(ligneDetail("ID", "#" + v.getIdVoyage()));
            if (v.getDateDebut() != null)
                detailsBox.getChildren().add(ligneDetail("Départ", v.getDateDebut().toString()));
            if (v.getDateFin() != null)
                detailsBox.getChildren().add(ligneDetail("Retour", v.getDateFin().toString()));
            if (v.getDuree() > 0)
                detailsBox.getChildren().add(ligneDetail("Durée", v.getDuree() + " jours"));
        }
    }

    // ══════════════════════════════════════════════════════
    //  ACTIONS ADMIN
    // ══════════════════════════════════════════════════════

    @FXML private void accepterReservation() { changeEtat("Acceptée"); }
    @FXML private void refuserReservation()  { changeEtat("Refusée");  }
    @FXML private void annulerReservation()  { changeEtat("Annulée");  }

    private void changeEtat(String nouvelEtat) {
        Reservation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("⚠ Veuillez sélectionner une réservation.");
            return;
        }
        selected.setEtat(nouvelEtat);
        try {
            service.modifier(selected);
            loadData();
            showDetails(selected);
        } catch (SQLException e) {
            showAlert("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void refreshTable() {
        searchField.clear();
        statutFilter.setValue("Tous");
        loadData();
    }

    // ══════════════════════════════════════════════════════
    //  HELPERS UI
    // ══════════════════════════════════════════════════════

    private Label sectionTitre(String texte) {
        Label lbl = new Label(texte);
        lbl.setStyle("-fx-font-size:11.5px; -fx-font-weight:bold; -fx-text-fill:#FF6B35; -fx-padding:4 0 2 0;");
        return lbl;
    }

    private HBox ligneDetail(String label, String valeur) {
        HBox hb = new HBox(6);
        hb.setAlignment(Pos.TOP_LEFT);
        hb.setPadding(new Insets(2, 0, 2, 0));
        Label lbl = new Label(label + " :");
        lbl.setMinWidth(72);
        lbl.setStyle("-fx-font-size:11px; -fx-text-fill:#AAA;");
        Label val = new Label(valeur);
        val.setWrapText(true);
        val.setStyle("-fx-font-size:11.5px; -fx-text-fill:#333; -fx-font-weight:bold;");
        HBox.setHgrow(val, Priority.ALWAYS);
        hb.getChildren().addAll(lbl, val);
        return hb;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}