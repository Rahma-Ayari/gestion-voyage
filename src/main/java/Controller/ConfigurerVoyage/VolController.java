package Controller.ConfigurerVoyage;

import Entite.Destination;
import Entite.Vol;
import Service.ServiceVol;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VolController {

    @FXML private VBox         volsContainer;
    @FXML private VBox         emptyState;
    @FXML private Label        selectedVolLabel;
    @FXML private Label        headerDestLabel;
    @FXML private Label        nbVolsLabel;
    @FXML private Button       suivantBtn;
    @FXML private ToggleButton btnAllerRetour;
    @FXML private ToggleButton btnAllerSimple;
    @FXML private ToggleButton btnRetourSimple;
    @FXML private VBox         vboxDateAller;
    @FXML private VBox         vboxDateRetour;
    @FXML private DatePicker   dateAllerPicker;
    @FXML private DatePicker   dateRetourPicker;

    private final ServiceVol serviceVol = new ServiceVol();

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter FMT_TIME = DateTimeFormatter.ofPattern("HH:mm");

    private Destination destination;
    private LocalDate   dateDebut;
    private LocalDate   dateFin;
    private Vol         volSelectionne = null;

    // ⚠️ Ce champ représente le choix du USER, pas ce qui est en BD
    private String typeVolActuel = "ALLER_RETOUR";

    private static final String CARD_NORMAL =
            "-fx-background-color:white; -fx-background-radius:14;" +
                    "-fx-border-color:#ECECEC; -fx-border-radius:14; -fx-border-width:1.5;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),10,0,0,2); -fx-cursor:hand;";
    private static final String CARD_HOVER =
            "-fx-background-color:#FFFAF7; -fx-background-radius:14;" +
                    "-fx-border-color:#FFCDB0; -fx-border-radius:14; -fx-border-width:1.5;" +
                    "-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.10),12,0,0,3); -fx-cursor:hand;";
    private static final String CARD_SELECTED =
            "-fx-background-color:#FFF8F5; -fx-background-radius:14;" +
                    "-fx-border-color:#FF6B35; -fx-border-radius:14; -fx-border-width:2.5;" +
                    "-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.22),16,0,0,4); -fx-cursor:hand;";

    /* ══════════════ INIT ══════════════ */
    @FXML
    public void initialize() {
        emptyState.setVisible(false);
        emptyState.setManaged(false);

        ToggleGroup group = new ToggleGroup();
        btnAllerRetour.setToggleGroup(group);
        btnAllerSimple.setToggleGroup(group);
        btnRetourSimple.setToggleGroup(group);
        btnAllerRetour.setSelected(true);

        group.selectedToggleProperty().addListener((obs, old, nw) -> {
            if (nw == btnAllerSimple) {
                typeVolActuel = "ALLER_SIMPLE";
                // Afficher seulement la date d'aller
                vboxDateAller.setVisible(true);   vboxDateAller.setManaged(true);
                vboxDateRetour.setVisible(false);  vboxDateRetour.setManaged(false);
            } else if (nw == btnRetourSimple) {
                typeVolActuel = "RETOUR_SIMPLE";
                // Afficher seulement la date de retour
                vboxDateAller.setVisible(false);  vboxDateAller.setManaged(false);
                vboxDateRetour.setVisible(true);   vboxDateRetour.setManaged(true);
            } else {
                typeVolActuel = "ALLER_RETOUR";
                // Afficher les deux dates
                vboxDateAller.setVisible(true);   vboxDateAller.setManaged(true);
                vboxDateRetour.setVisible(true);   vboxDateRetour.setManaged(true);
            }
        });
    }

    /* ══════════════ DONNÉES ÉTAPE 1 ══════════════ */
    public void initDonnees(Destination destination, LocalDate dateDebut, LocalDate dateFin) {
        this.destination = destination;
        this.dateDebut   = dateDebut;
        this.dateFin     = dateFin;

        dateAllerPicker.setValue(dateDebut);
        dateRetourPicker.setValue(dateFin);

        if (headerDestLabel != null) {
            headerDestLabel.setText(
                    "📍 " + destination.getVille() + ", " + destination.getPays()
                            + "   📅 " + dateDebut.format(FMT_DATE) + " → " + dateFin.format(FMT_DATE)
            );
        }
        chargerVols();
    }

    /* ══════════════ BOUTON RECHERCHER ══════════════ */
    @FXML
    private void onRechercherVols() {
        chargerVols();
    }

    /* ══════════════ CHARGER LES VOLS ══════════════ */
    private void chargerVols() {
        volSelectionne = null;
        selectedVolLabel.setText("Aucun vol sélectionné");
        selectedVolLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#AAA;");

        LocalDate aller  = dateAllerPicker.getValue();
        LocalDate retour = dateRetourPicker.getValue();

        if (typeVolActuel.equals("ALLER_SIMPLE") && aller == null) {
            showAlert("Date manquante", "Veuillez sélectionner une date d'aller."); return;
        }
        if (typeVolActuel.equals("RETOUR_SIMPLE") && retour == null) {
            showAlert("Date manquante", "Veuillez sélectionner une date de retour."); return;
        }
        if (typeVolActuel.equals("ALLER_RETOUR")) {
            if (aller == null || retour == null) {
                showAlert("Dates manquantes", "Veuillez sélectionner les deux dates."); return;
            }
            if (retour.isBefore(aller)) {
                showAlert("Dates invalides", "La date de retour doit être après la date d'aller."); return;
            }
        }

        try {
            List<Vol> vols = serviceVol.findByTypeAndDates(
                    destination.getIdDestination(), typeVolActuel, aller, retour);

            if (nbVolsLabel != null) {
                nbVolsLabel.setText(vols.size() + " vol(s) trouvé(s)");
                nbVolsLabel.setStyle(vols.isEmpty()
                        ? "-fx-font-size:13px; -fx-text-fill:#E74C3C; -fx-font-weight:bold;"
                        : "-fx-font-size:13px; -fx-text-fill:#4CAF50; -fx-font-weight:bold;");
            }

            volsContainer.getChildren().clear();

            if (vols.isEmpty()) {
                volsContainer.setVisible(false); volsContainer.setManaged(false);
                emptyState.setVisible(true);     emptyState.setManaged(true);
                return;
            }

            volsContainer.setVisible(true);  volsContainer.setManaged(true);
            emptyState.setVisible(false);    emptyState.setManaged(false);

            for (Vol vol : vols) {
                volsContainer.getChildren().add(creerCarteVol(vol));
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les vols : " + e.getMessage());
        }
    }

    /* ══════════════ CRÉER CARTE VOL ══════════════ */
    private VBox creerCarteVol(Vol vol) {
        VBox card = new VBox(0);
        card.setStyle(CARD_NORMAL);
        card.setPadding(new Insets(20, 24, 20, 24));

        // ⚠️ CORRECTION CLÉ : on utilise typeVolActuel (choix du USER)
        //    et NON vol.getTypeVol() (valeur en BD)
        String badgeTexte;
        String symbole;
        if (typeVolActuel.equals("ALLER_SIMPLE")) {
            badgeTexte = "→ Aller simple";
            symbole    = "→";
        } else if (typeVolActuel.equals("RETOUR_SIMPLE")) {
            badgeTexte = "← Retour simple";
            symbole    = "←";
        } else {
            badgeTexte = "⇄ Aller-Retour";
            symbole    = "⇄";
        }

        // ── Ligne 1 : icône + compagnie + badge + prix ──
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane icon = new StackPane();
        icon.setPrefSize(44, 44); icon.setMinSize(44, 44);
        icon.setStyle("-fx-background-color:linear-gradient(to bottom right,#FF6B35,#F7931E); -fx-background-radius:12;");
        Label plane = new Label("✈");
        plane.setStyle("-fx-font-size:19px; -fx-text-fill:white;");
        icon.getChildren().add(plane);

        VBox info = new VBox(3);
        Label compagnie = new Label(vol.getCompagnie());
        compagnie.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1A1A2E;");
        Label numero = new Label("Vol " + vol.getNumeroVol());
        numero.setStyle("-fx-font-size:12px; -fx-text-fill:#AAA;");
        info.getChildren().addAll(compagnie, numero);

        Label badge = new Label(badgeTexte);
        badge.setStyle("-fx-background-color:#E8F5E9; -fx-background-radius:8;" +
                "-fx-border-color:#C8E6C9; -fx-border-radius:8; -fx-border-width:1;" +
                "-fx-text-fill:#2E7D32; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox prix = new VBox(2);
        prix.setAlignment(Pos.CENTER);
        prix.setStyle("-fx-background-color:#FFF3E0; -fx-background-radius:10;" +
                "-fx-border-color:#FFCDB0; -fx-border-radius:10; -fx-border-width:1; -fx-padding:8 18;");
        Label prixVal = new Label(String.format("%.0f DT", vol.getPrix()));
        prixVal.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#FF6B35;");
        Label prixSub = new Label("par personne");
        prixSub.setStyle("-fx-font-size:10px; -fx-text-fill:#BBAA99;");
        prix.getChildren().addAll(prixVal, prixSub);

        topRow.getChildren().addAll(icon, info, badge, spacer, prix);

        Separator sep = new Separator();
        VBox.setMargin(sep, new Insets(14, 0, 14, 0));

        // ── Ligne 2 : itinéraire ──
        // ⚠️ Affichage conditionnel basé sur typeVolActuel (choix du USER)
        HBox itineraireRow = new HBox(0);
        itineraireRow.setAlignment(Pos.CENTER);

        // Côté GAUCHE : date de départ
        // → visible si Aller simple ou Aller-Retour
        // → caché si Retour simple
        VBox depart = new VBox(3);
        depart.setAlignment(Pos.CENTER_LEFT);
        if (!typeVolActuel.equals("RETOUR_SIMPLE")) {
            Label dTime = new Label(vol.getDateDepart() != null ? vol.getDateDepart().format(FMT_TIME) : "--:--");
            dTime.setStyle("-fx-font-size:24px; -fx-font-weight:bold; -fx-text-fill:#1A1A2E;");
            Label dDate = new Label(vol.getDateDepart() != null ? vol.getDateDepart().format(FMT_DATE) : "");
            dDate.setStyle("-fx-font-size:11px; -fx-text-fill:#AAA;");
            Label dVille = new Label(vol.getDestination() != null ? vol.getDestination().getVille() : destination.getVille());
            dVille.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#666;");
            depart.getChildren().addAll(dTime, dDate, dVille);
        } else {
            Label txt = new Label("Depuis l'origine");
            txt.setStyle("-fx-font-size:12px; -fx-text-fill:#CCC; -fx-font-style:italic;");
            depart.getChildren().add(txt);
        }

        // Flèche
        VBox arrow = new VBox(4);
        arrow.setAlignment(Pos.CENTER);
        HBox.setHgrow(arrow, Priority.ALWAYS);
        Label arrowLine = new Label("────── " + symbole + " ──────");
        arrowLine.setStyle("-fx-text-fill:#FFCDB0; -fx-font-size:13px;");
        Label arrowSub = new Label(badgeTexte);
        arrowSub.setStyle("-fx-font-size:10px; -fx-text-fill:#CCC;");
        arrow.getChildren().addAll(arrowLine, arrowSub);

        // Côté DROIT : date d'arrivée
        // → visible si Retour simple ou Aller-Retour
        // → caché si Aller simple
        VBox arrivee = new VBox(3);
        arrivee.setAlignment(Pos.CENTER_RIGHT);
        if (!typeVolActuel.equals("ALLER_SIMPLE")) {
            Label aTime = new Label(vol.getDateArrivee() != null ? vol.getDateArrivee().format(FMT_TIME) : "--:--");
            aTime.setStyle("-fx-font-size:24px; -fx-font-weight:bold; -fx-text-fill:#1A1A2E;");
            Label aDate = new Label(vol.getDateArrivee() != null ? vol.getDateArrivee().format(FMT_DATE) : "");
            aDate.setStyle("-fx-font-size:11px; -fx-text-fill:#AAA;");
            Label aPays = new Label(vol.getDestination() != null ? vol.getDestination().getPays() : destination.getPays());
            aPays.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#666;");
            arrivee.getChildren().addAll(aTime, aDate, aPays);
        } else {
            Label txt = new Label("Vers la destination");
            txt.setStyle("-fx-font-size:12px; -fx-text-fill:#CCC; -fx-font-style:italic;");
            arrivee.getChildren().add(txt);
        }

        itineraireRow.getChildren().addAll(depart, arrow, arrivee);
        card.getChildren().addAll(topRow, sep, itineraireRow);

        card.setOnMouseClicked(e -> selectionnerVol(vol, card));
        card.setOnMouseEntered(e -> { if (!vol.equals(volSelectionne)) card.setStyle(CARD_HOVER); });
        card.setOnMouseExited(e  -> { if (!vol.equals(volSelectionne)) card.setStyle(CARD_NORMAL); });

        return card;
    }

    /* ══════════════ SÉLECTION ══════════════ */
    private void selectionnerVol(Vol vol, VBox carteCliquee) {
        volsContainer.getChildren().forEach(n -> n.setStyle(CARD_NORMAL));
        carteCliquee.setStyle(CARD_SELECTED);
        volSelectionne = vol;
        selectedVolLabel.setText("✅ " + vol.getCompagnie()
                + " — Vol " + vol.getNumeroVol()
                + " | " + String.format("%.0f DT", vol.getPrix()));
        selectedVolLabel.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#FF6B35;");
    }

    /* ══════════════ NAVIGATION ══════════════ */
    @FXML
    private void passerEtapeSuivante() {
        if (volSelectionne == null) {
            showAlert("Aucun vol sélectionné", "Veuillez choisir un vol avant de continuer.");
            return;
        }
        showAlert("Étape suivante",
                "Vol confirmé : " + volSelectionne.getNumeroVol()
                        + " (" + volSelectionne.getCompagnie() + ")\nPassage à l'étape Hôtels...");
    }

    @FXML
    private void retourEtapePrecedente() {
        URL url = getClass().getClassLoader().getResource("ConfigurerVoyage/ConfigVoyageUser.fxml");
        if (url == null) url = getClass().getResource("/ConfigurerVoyage/ConfigVoyageUser.fxml");
        if (url == null) { showAlert("Erreur", "ConfigVoyageUser.fxml introuvable"); return; }
        try {
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) suivantBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}