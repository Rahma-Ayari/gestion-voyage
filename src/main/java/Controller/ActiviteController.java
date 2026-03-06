package Controller;

import Entite.Activite;
import Entite.Destination;
import Entite.TypeActivite;
import Service.ServiceActivite;
import Service.ServiceDestination;
import Service.ServiceTypeActivite;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class ActiviteController {

    @FXML
    private TextField nomField, prixField, dureeField, categorieField, horaireField, searchField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ComboBox<TypeActivite> cbTypeActivite;
    @FXML
    private ComboBox<Destination> cbDestination;
    @FXML
    private Button ajouterBtn, modifierBtn, supprimerBtn, clearBtn, refreshBtn;
    @FXML
    private TableView<Activite> tableView;
    @FXML
    private TableColumn<Activite, Integer> idCol;
    @FXML
    private TableColumn<Activite, String> nomCol, descriptionCol, categorieCol, horaireCol, typeCol;
    @FXML
    private TableColumn<Activite, Double> prixCol;
    @FXML
    private TableColumn<Activite, Integer> dureeCol;
    @FXML
    private Label countLabel;

    @FXML
    private TableColumn<Activite, String> destinationCol;

    private ServiceActivite serviceActivite;
    private ServiceTypeActivite serviceTypeActivite;
    private ServiceDestination serviceDestination;
    private ObservableList<Activite> activiteList;
    private ObservableList<TypeActivite> typeList;
    private ObservableList<Destination> destinationList;

    @FXML
    public void initialize() {
        serviceActivite = new ServiceActivite();
        serviceTypeActivite = new ServiceTypeActivite();
        serviceDestination = new ServiceDestination();

        // Initialiser les colonnes
        idCol.setCellValueFactory(new PropertyValueFactory<>("idActivite"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
        dureeCol.setCellValueFactory(new PropertyValueFactory<>("dureeEnHeure"));
        categorieCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        horaireCol.setCellValueFactory(new PropertyValueFactory<>("horaire"));
        typeCol.setCellValueFactory(cell -> {
            TypeActivite type = cell.getValue().getTypeAct();
            String libelle = (type != null) ? type.getLibelle() : "";
            return new javafx.beans.property.SimpleStringProperty(libelle);
        });

        // Lier la colonne Destination à la destination de l'activité
        destinationCol.setCellValueFactory(cell -> {
            int idDest = cell.getValue().getIdDestination();
            Destination d = null;

            if (destinationList != null) {
                for (Destination dest : destinationList) {
                    if (dest.getIdDestination() == idDest) {
                        d = dest;
                        break;
                    }
                }
            }

            String nomDestination = (d != null) ? d.getVille() + " (" + d.getPays() + ")" : "";
            return new javafx.beans.property.SimpleStringProperty(nomDestination);
        });

        loadTypeActivites();
        loadDestinations();
        loadActivites();

        // Sélection dans la table
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                nomField.setText(newSel.getNom());
                descriptionField.setText(newSel.getDescription());
                prixField.setText(String.valueOf(newSel.getPrix()));
                dureeField.setText(String.valueOf(newSel.getDureeEnHeure()));
                categorieField.setText(newSel.getCategorie());
                horaireField.setText(newSel.getHoraire());
                cbTypeActivite.getSelectionModel().select(newSel.getTypeAct());

                // Sélectionner la destination
                if (destinationList != null) {
                    for (Destination d : destinationList) {
                        if (d.getIdDestination() == newSel.getIdDestination()) {
                            cbDestination.getSelectionModel().select(d);
                            break;
                        }
                    }
                }
            }
        });

        // Recherche
        searchField.textProperty().addListener((obs, oldText, newText) -> searchActivites(newText));
    }

    private void loadTypeActivites() {
        try {
            List<TypeActivite> list = serviceTypeActivite.readAll();
            typeList = FXCollections.observableArrayList(list);
            cbTypeActivite.setItems(typeList);
            System.out.println("✅ " + list.size() + " types chargés");
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement types: " + e.getMessage());
            showAlert("Erreur", e.getMessage());
        }
    }

    private void loadDestinations() {
        try {
            List<Destination> list = serviceDestination.readAll();
            destinationList = FXCollections.observableArrayList(list);
            cbDestination.setItems(destinationList);
            System.out.println("✅ " + list.size() + " destinations chargées");
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement destinations: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger les destinations : " + e.getMessage());
        }
    }

    private void loadActivites() {
        try {
            List<Activite> list = serviceActivite.readAll();
            activiteList = FXCollections.observableArrayList(list);
            tableView.setItems(activiteList);
            countLabel.setText(String.valueOf(list.size()));
            System.out.println("✅ " + list.size() + " activités chargées");
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void ajouterActivite() {
        if (nomField.getText().isEmpty() || cbTypeActivite.getSelectionModel().isEmpty()) {
            showAlert("Attention", "Veuillez remplir le nom et le type !");
            return;
        }

        if (cbDestination.getSelectionModel().isEmpty()) {
            showAlert("Attention", "Veuillez sélectionner une destination !");
            return;
        }

        Activite a = new Activite();
        a.setNom(nomField.getText());
        a.setDescription(descriptionField.getText());
        try { a.setPrix(Double.parseDouble(prixField.getText())); } catch(Exception e){ a.setPrix(0); }
        try { a.setDureeEnHeure(Integer.parseInt(dureeField.getText())); } catch(Exception e){ a.setDureeEnHeure(0); }
        a.setCategorie(categorieField.getText());
        a.setHoraire(horaireField.getText());
        a.setTypeAct(cbTypeActivite.getSelectionModel().getSelectedItem());

        Destination selectedDest = cbDestination.getSelectionModel().getSelectedItem();
        a.setIdDestination(selectedDest.getIdDestination());

        System.out.println("Ajout activité - Destination: " + selectedDest.getPays() + " (" + a.getIdDestination() + ")");

        try {
            serviceActivite.ajouter(a);
            System.out.println("✅ Activité ajoutée avec succès");
            clearFields();
            loadActivites();
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void modifierActivite() {
        Activite selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Sélectionnez une activité !");
            return;
        }

        if (cbDestination.getSelectionModel().isEmpty()) {
            showAlert("Attention", "Veuillez sélectionner une destination !");
            return;
        }

        selected.setNom(nomField.getText());
        selected.setDescription(descriptionField.getText());
        try { selected.setPrix(Double.parseDouble(prixField.getText())); } catch(Exception e){ selected.setPrix(0); }
        try { selected.setDureeEnHeure(Integer.parseInt(dureeField.getText())); } catch(Exception e){ selected.setDureeEnHeure(0); }
        selected.setCategorie(categorieField.getText());
        selected.setHoraire(horaireField.getText());
        selected.setTypeAct(cbTypeActivite.getSelectionModel().getSelectedItem());

        Destination selectedDest = cbDestination.getSelectionModel().getSelectedItem();
        selected.setIdDestination(selectedDest.getIdDestination());

        try {
            serviceActivite.modifier(selected);
            System.out.println("✅ Activité modifiée avec succès");
            clearFields();
            loadActivites();
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void supprimerActivite() {
        Activite selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Sélectionnez une activité !");
            return;
        }

        try {
            serviceActivite.supprimer(selected);
            System.out.println("✅ Activité supprimée avec succès");
            clearFields();
            loadActivites();
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void clearFields() {
        nomField.clear();
        descriptionField.clear();
        prixField.clear();
        dureeField.clear();
        categorieField.clear();
        horaireField.clear();
        cbTypeActivite.getSelectionModel().clearSelection();
        cbDestination.getSelectionModel().clearSelection();
        tableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void refreshTable() {
        loadActivites();
        searchField.clear();
    }

    private void searchActivites(String query) {
        if (query.isEmpty()) {
            tableView.setItems(activiteList);
            return;
        }

        ObservableList<Activite> filtered = FXCollections.observableArrayList();
        for (Activite a : activiteList) {
            if (a.getNom().toLowerCase().contains(query.toLowerCase()) ||
                    a.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                    (a.getTypeAct() != null && a.getTypeAct().getLibelle().toLowerCase().contains(query.toLowerCase()))) {
                filtered.add(a);
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