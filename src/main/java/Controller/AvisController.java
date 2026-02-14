package Controller;

import Entite.Avis;
import Service.ServiceAvis;
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

    @FXML private TableView<Avis> tableView;
    @FXML private TableColumn<Avis, Integer> idCol, noteCol, userCol;
    @FXML private TableColumn<Avis, String> commentaireCol;
    @FXML private TableColumn<Avis, java.sql.Date> dateCol;
    @FXML private Button btnPublier, btnSupprimer;

    private final ServiceAvis serviceAvis = new ServiceAvis();
    private ObservableList<Avis> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configuration professionnelle des colonnes
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userCol.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateAvis"));
        commentaireCol.setCellValueFactory(new PropertyValueFactory<>("commentaire"));

        // État initial des boutons (Désactivés sans sélection)
        btnPublier.setDisable(true);
        btnSupprimer.setDisable(true);

        // Gestion intelligente de la sélection
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = (newSelection != null);
            btnPublier.setDisable(!hasSelection);
            btnSupprimer.setDisable(!hasSelection);
        });

        refreshTable();
    }

    @FXML
    private void handlePublier() {
        Avis selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Logique de modération professionnelle
            afficherMessage(Alert.AlertType.INFORMATION, "Validation",
                    "L'avis #" + selected.getId() + " a été validé et publié avec succès.");
        }
    }

    @FXML
    private void handleSupprimer() {
        Avis selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Êtes-vous sûr de vouloir supprimer définitivement cet avis ?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Confirmation de suppression");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        if (serviceAvis.supprimer(selected)) {
                            refreshTable();
                            afficherMessage(Alert.AlertType.INFORMATION, "Suppression", "L'avis a été retiré de la base de données.");
                        }
                    } catch (SQLException e) {
                        afficherMessage(Alert.AlertType.ERROR, "Erreur Critique", "Erreur lors de la suppression : " + e.getMessage());
                    }
                }
            });
        }
    }

    @FXML
    public void refreshTable() {
        try {
            masterData.setAll(serviceAvis.readAll());
            tableView.setItems(masterData);
        } catch (SQLException e) {
            afficherMessage(Alert.AlertType.ERROR, "Erreur de Connexion", "Impossible de charger les données.");
        }
    }

    private void afficherMessage(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}