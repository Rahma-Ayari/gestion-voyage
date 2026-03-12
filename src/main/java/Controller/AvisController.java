package Controller;

import Entite.Avis;
import Entite.Utilisateur;
import Service.ServiceAvis;
import Service.ServiceUtilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AvisController implements Initializable {

    // ── TableView Avis ────────────────────────────────────────────────────────
    @FXML private TableView<Avis>              tableAvis;
    @FXML private TableColumn<Avis, Integer>   idCol, noteCol, userCol, voyageCol;
    @FXML private TableColumn<Avis, String>    titreCol, commentaireCol;
    @FXML private TableColumn<Avis, java.sql.Date> dateCol;
    @FXML private TableColumn<Avis, Integer>   noteHebergementCol, noteTransportCol;
    @FXML private TableColumn<Avis, Integer>   noteActivitesCol, noteQualitePrixCol;
    @FXML private TableColumn<Avis, Boolean>   recommandeCol;

    // ── TableView Utilisateurs ────────────────────────────────────────────────
    @FXML private TableView<Utilisateur>             tableUtilisateurs;
    @FXML private TableColumn<Utilisateur, Integer>  colUserId, colUserVoyage;
    @FXML private TableColumn<Utilisateur, String>   colUserEmail, colUserEtat;

    // ── Boutons ───────────────────────────────────────────────────────────────
    @FXML private Button btnPublier, btnSupprimer;
    @FXML private Button btnMarquerDone, btnMarquerNotDone;

    // ── Services ──────────────────────────────────────────────────────────────
    private final ServiceAvis        serviceAvis = new ServiceAvis();
    private final ServiceUtilisateur serviceUser = new ServiceUtilisateur();

    private final ObservableList<Avis>        listeAvis  =
            FXCollections.observableArrayList();
    private final ObservableList<Utilisateur> listeUsers =
            FXCollections.observableArrayList();

    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnesAvis();
        configurerColonnesUtilisateurs();
        configurerSelectionAvis();
        configurerSelectionUtilisateurs();
        rafraichirTout();
    }

    // ── Colonnes Avis ─────────────────────────────────────────────────────────

    private void configurerColonnesAvis() {
        idCol             .setCellValueFactory(new PropertyValueFactory<>("id"));
        userCol           .setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        voyageCol         .setCellValueFactory(new PropertyValueFactory<>("idVoyage"));
        titreCol          .setCellValueFactory(new PropertyValueFactory<>("titre"));
        noteCol           .setCellValueFactory(new PropertyValueFactory<>("note"));
        dateCol           .setCellValueFactory(new PropertyValueFactory<>("dateAvis"));
        commentaireCol    .setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        noteHebergementCol.setCellValueFactory(new PropertyValueFactory<>("noteHebergement"));
        noteTransportCol  .setCellValueFactory(new PropertyValueFactory<>("noteTransport"));
        noteActivitesCol  .setCellValueFactory(new PropertyValueFactory<>("noteActivites"));
        noteQualitePrixCol.setCellValueFactory(new PropertyValueFactory<>("noteQualitePrix"));
        recommandeCol     .setCellValueFactory(new PropertyValueFactory<>("recommande"));

        // "✓ Oui" en vert / "✗ Non" en rouge
        recommandeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item ? "✓ Oui" : "✗ Non");
                setStyle(item
                        ? "-fx-text-fill:#10b981;-fx-font-weight:bold;"
                        : "-fx-text-fill:#ef4444;-fx-font-weight:bold;");
            }
        });
    }

    // ── Colonnes Utilisateurs ─────────────────────────────────────────────────

    private void configurerColonnesUtilisateurs() {
        colUserId   .setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        // ← id_voyage (nom du champ dans Utilisateur.java)
        colUserVoyage.setCellValueFactory(new PropertyValueFactory<>("idVoyage"));
        colUserEtat .setCellValueFactory(new PropertyValueFactory<>("etat"));

        // Colorier la colonne état
        colUserEtat.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                if ("done".equals(item)) {
                    setText("✅ done");
                    setStyle("-fx-text-fill:#10b981;-fx-font-weight:bold;");
                } else {
                    setText("⏳ notDone");
                    setStyle("-fx-text-fill:#f59e0b;-fx-font-weight:bold;");
                }
            }
        });
    }

    // ── Sélection ─────────────────────────────────────────────────────────────

    private void configurerSelectionAvis() {
        btnPublier .setDisable(true);
        btnSupprimer.setDisable(true);
        tableAvis.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> {
                    btnPublier .setDisable(n == null);
                    btnSupprimer.setDisable(n == null);
                });
    }

    private void configurerSelectionUtilisateurs() {
        btnMarquerDone   .setDisable(true);
        btnMarquerNotDone.setDisable(true);
        tableUtilisateurs.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> {
                    btnMarquerDone   .setDisable(n == null);
                    btnMarquerNotDone.setDisable(n == null);
                });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ACTIONS AVIS
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handlePublier() {
        Avis sel = tableAvis.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        afficherMessage(Alert.AlertType.INFORMATION, "✔ Publié",
                "L'avis #" + sel.getId() + " a été validé avec succès.");
    }

    @FXML
    private void handleSupprimer() {
        Avis sel = tableAvis.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer définitivement cet avis ?",
                ButtonType.YES, ButtonType.NO);
        c.setHeaderText("Confirmation");
        c.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    if (serviceAvis.supprimer(sel)) {
                        rafraichirTout();
                        afficherMessage(Alert.AlertType.INFORMATION,
                                "Supprimé", "L'avis a été supprimé.");
                    }
                } catch (SQLException e) {
                    afficherMessage(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ACTIONS ÉTAT — Admin change "done" / "notDone"
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleMarquerDone() {
        Utilisateur sel = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        // Vérifier que l'utilisateur a bien un voyage lié
        if (sel.getIdVoyage() == 0) {
            afficherMessage(Alert.AlertType.WARNING, "⚠ Attention",
                    "Cet utilisateur n'a pas encore de voyage configuré.\n"
                            + "Il doit d'abord terminer la configuration de son voyage.");
            return;
        }

        try {
            if (serviceUser.mettreAJourEtat(sel.getIdUtilisateur(), "done")) {
                rafraichirTout();
                afficherMessage(Alert.AlertType.INFORMATION, "✅ Succès",
                        "L'utilisateur " + sel.getEmail()
                                + " peut maintenant laisser un avis\n"
                                + "pour le Voyage #" + sel.getIdVoyage());
            }
        } catch (SQLException e) {
            afficherMessage(Alert.AlertType.ERROR, "Erreur BD", e.getMessage());
        }
    }

    @FXML
    private void handleMarquerNotDone() {
        Utilisateur sel = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        try {
            if (serviceUser.mettreAJourEtat(sel.getIdUtilisateur(), "notDone")) {
                rafraichirTout();
                afficherMessage(Alert.AlertType.INFORMATION, "⏳ Mis à jour",
                        "L'état de " + sel.getEmail()
                                + " est maintenant : notDone");
            }
        } catch (SQLException e) {
            afficherMessage(Alert.AlertType.ERROR, "Erreur BD", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  RAFRAÎCHISSEMENT
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void rafraichirTout() {
        try {
            listeAvis.setAll(serviceAvis.readAll());
            tableAvis.setItems(listeAvis);

            listeUsers.setAll(serviceUser.readAll());
            tableUtilisateurs.setItems(listeUsers);
        } catch (SQLException e) {
            afficherMessage(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les données : " + e.getMessage());
        }
    }

    // ── Utilitaire ────────────────────────────────────────────────────────────

    private void afficherMessage(Alert.AlertType type, String titre, String msg) {
        Alert a = new Alert(type);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}