package Controller.ConfigurerVoyage;



import Entite.Activite;
import Entite.Destination;
import Service.ServiceActivite;
import Service.ServiceVoyage;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ActiviteUserController {

    /* ── FXML refs ── */
    @FXML private Label  destinationLabel;
    @FXML private Label  datesLabel;
    @FXML private Label  dureeLabel;
    @FXML private Label  headerDestLabel;
    @FXML private Label  headerDatesLabel;
    @FXML private Label  countLabel;
    @FXML private Label  selectionCountLabel;
    @FXML private Label  totalCoutLabel;
    @FXML private Label  totalDureeLabel;
    @FXML private VBox   activiteListContainer;
    @FXML private VBox   selectionContainer;
    @FXML private Button suivantButton;

    /* ── Filtres ── */
    @FXML private TextField        searchNomField;
    @FXML private ComboBox<String> filterTypeBox;
    @FXML private ComboBox<String> filterRythmeBox;

    private final ServiceActivite serviceActivite = new ServiceActivite();
    private final ServiceVoyage   serviceVoyage   = new ServiceVoyage();
    private final DateTimeFormatter fmt           = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ── State ── */
    private Destination      destination;
    private LocalDate        dateDebut;
    private LocalDate        dateFin;
    private String           rythme;
    private int              idVoyage = -1;
    private List<Activite>   allActivites    = new ArrayList<>();
    private List<Activite>   activitesSelectionnees = new ArrayList<>();

    // ══════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ══════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        if (filterTypeBox != null) {
            filterTypeBox.getItems().addAll("Tous les types", "Randonnée", "Visite culturelle",
                    "Sport nautique", "Ski / Snowboard", "Spa / Bien-être", "Plongée sous-marine",
                    "Excursion en montagne", "Tour guidé en ville", "Vélo / VTT", "Canoë / Kayak",
                    "Observation de la faune", "Atelier culinaire", "Concert / Spectacle",
                    "Parc d'attractions", "Parapente / Aventure", "Yoga / Méditation",
                    "Safari / Excursion nature", "Plage / Baignade", "Escalade", "Croisière / Bateau");
            filterTypeBox.setValue("Tous les types");
            filterTypeBox.setOnAction(e -> appliquerFiltres());
        }
        if (filterRythmeBox != null) {
            filterRythmeBox.getItems().addAll("Tous", "Détendu", "Modéré", "Intense");
            filterRythmeBox.setValue("Tous");
            filterRythmeBox.setOnAction(e -> appliquerFiltres());
        }
        if (searchNomField != null)
            searchNomField.textProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    // ══════════════════════════════════════════════════════════════
    //  INIT DONNÉES
    // ══════════════════════════════════════════════════════════════
    public void initDonnees(Destination destination, LocalDate dateDebut,
                            LocalDate dateFin, String rythme, int idVoyage) {
        this.destination = destination;
        this.dateDebut   = dateDebut;
        this.dateFin     = dateFin;
        this.rythme      = rythme;
        this.idVoyage    = idVoyage;

        // Pré-sélectionner le filtre rythme selon le voyage
        if (filterRythmeBox != null) {
            if (rythme != null && !rythme.isBlank()) {
                String r = rythme.substring(0,1).toUpperCase() + rythme.substring(1).toLowerCase();
                if (filterRythmeBox.getItems().contains(r)) filterRythmeBox.setValue(r);
            }
        }

        mettreAJourResume();
        chargerActivites();
    }

    // ══════════════════════════════════════════════════════════════
    //  RÉSUMÉ EN-TÊTE
    // ══════════════════════════════════════════════════════════════
    private void mettreAJourResume() {
        destinationLabel.setText("🌍 " + destination.getPays() + " — " + destination.getVille());
        datesLabel.setText(dateDebut.format(fmt) + "  →  " + dateFin.format(fmt));
        long jours = java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin);
        dureeLabel.setText(jours + " jour" + (jours > 1 ? "s" : ""));
        if (headerDestLabel  != null) headerDestLabel.setText(destination.getPays() + " — " + destination.getVille());
        if (headerDatesLabel != null) headerDatesLabel.setText(dateDebut.format(fmt) + " → " + dateFin.format(fmt));
    }

    // ══════════════════════════════════════════════════════════════
    //  CHARGEMENT
    // ══════════════════════════════════════════════════════════════
    private void chargerActivites() {
        try {
            allActivites = serviceActivite.findByDestination(destination.getIdDestination());
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les activités : " + e.getMessage());
            return;
        }
        appliquerFiltres();
    }

    // ══════════════════════════════════════════════════════════════
    //  FILTRES
    // ══════════════════════════════════════════════════════════════
    private void appliquerFiltres() {
        activiteListContainer.getChildren().clear();

        if (allActivites.isEmpty()) {
            countLabel.setText("Aucune activité pour cette destination");
            countLabel.setStyle("-fx-font-size:11.5px;-fx-text-fill:#E74C3C;");
            activiteListContainer.getChildren().add(
                    msgLabel("😕 Aucune activité enregistrée pour " + destination.getVille(), "#999"));
            return;
        }

        String recherche = searchNomField != null ? searchNomField.getText().trim().toLowerCase() : "";
        String typeFiltre  = filterTypeBox  != null ? filterTypeBox.getValue()  : "Tous les types";
        String rythmeFiltre = filterRythmeBox != null ? filterRythmeBox.getValue() : "Tous";

        List<Activite> filtrées = allActivites.stream()
                .filter(a -> recherche.isEmpty() || a.getNom().toLowerCase().contains(recherche))
                .filter(a -> "Tous les types".equals(typeFiltre) || typeFiltre.equals(a.getLibelleType()))
                .filter(a -> {
                    if ("Tous".equals(rythmeFiltre)) return true;
                    return correspondRythme(a.getIdTypeActivite(), rythmeFiltre);
                })
                .collect(Collectors.toList());

        if (filtrées.isEmpty()) {
            countLabel.setText("Aucun résultat");
            countLabel.setStyle("-fx-font-size:11.5px;-fx-text-fill:#E74C3C;");
            activiteListContainer.getChildren().add(
                    msgLabel("🔍 Aucune activité ne correspond à votre recherche.", "#999"));
            return;
        }

        countLabel.setText(filtrées.size() + " activité(s) disponible(s)");
        countLabel.setStyle("-fx-font-size:11.5px;-fx-text-fill:#27AE60;");

        for (Activite a : filtrées)
            activiteListContainer.getChildren().add(creerCarteActivite(a));
    }

    private boolean correspondRythme(int idType, String rythme) {
        return switch (rythme) {
            case "Détendu" -> List.of(2,5,8,12,13,16,18,20).contains(idType);
            case "Intense" -> List.of(1,3,4,6,7,9,10,15,19).contains(idType);
            default        -> true; // Modéré = tout
        };
    }

    // ══════════════════════════════════════════════════════════════
    //  CARTE ACTIVITÉ
    // ══════════════════════════════════════════════════════════════
    private VBox creerCarteActivite(Activite activite) {
        boolean dejaSel = activitesSelectionnees.stream()
                .anyMatch(a -> a.getIdActivite() == activite.getIdActivite());

        VBox carte = new VBox(0);
        carte.setStyle(getStyleCarte(dejaSel));

        HBox mainRow = new HBox(12);
        mainRow.setPadding(new Insets(14, 16, 12, 16));
        mainRow.setAlignment(Pos.CENTER_LEFT);

        // Icône type
        StackPane icon = new StackPane();
        icon.setPrefSize(42, 42); icon.setMinSize(42, 42);
        icon.setStyle("-fx-background-color:" + (dejaSel ? "#FF6B35" : "#FFF3E0") + ";-fx-background-radius:10;");
        icon.getChildren().add(labelStyle(getEmoji(activite.getLibelleType()), "-fx-font-size:18px;"));

        // Infos
        VBox infos = new VBox(4);
        HBox.setHgrow(infos, Priority.ALWAYS);

        HBox nomRow = new HBox(8);
        nomRow.setAlignment(Pos.CENTER_LEFT);
        Label nomLbl = labelStyle(activite.getNom(),
                "-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" + (dejaSel ? "#FF6B35" : "#2C2C2C") + ";");
        nomRow.getChildren().add(nomLbl);
        if (dejaSel) nomRow.getChildren().add(badgeLabel("✓ Sélectionné", "#27AE60", "white"));

        Label typeLbl = labelStyle("🏷 " + activite.getLibelleType(),
                "-fx-font-size:11px;-fx-text-fill:#888;");
        Label descLbl = labelStyle(activite.getDescription(),
                "-fx-font-size:11.5px;-fx-text-fill:#666;");
        descLbl.setWrapText(true);

        HBox detailRow = new HBox(16);
        detailRow.setAlignment(Pos.CENTER_LEFT);
        detailRow.getChildren().addAll(
                labelStyle("⏱ " + activite.getDureeEnHeure() + "h", "-fx-font-size:11.5px;-fx-text-fill:#888;"),
                labelStyle("🕐 " + activite.getHoraire(), "-fx-font-size:11.5px;-fx-text-fill:#888;")
        );

        infos.getChildren().addAll(nomRow, typeLbl, descLbl, detailRow);

        // Prix + bouton
        VBox droite = new VBox(6);
        droite.setAlignment(Pos.CENTER_RIGHT);
        droite.setMinWidth(100);
        droite.getChildren().addAll(
                labelStyle(String.format("%.0f TND", activite.getPrix()),
                        "-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:" + (dejaSel ? "#27AE60" : "#FF6B35") + ";"),
                labelStyle("/ personne", "-fx-font-size:10px;-fx-text-fill:#BBB;")
        );

        Button btn = new Button(dejaSel ? "✕ Retirer" : "+ Ajouter");
        btn.setStyle(dejaSel
                ? "-fx-background-color:#FDECEA;-fx-text-fill:#E74C3C;-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:7;-fx-cursor:hand;-fx-padding:6 14;"
                : "-fx-background-color:linear-gradient(to right,#FF6B35,#F7931E);-fx-text-fill:white;-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:7;-fx-cursor:hand;-fx-padding:6 14;");
        btn.setOnAction(e -> toggleActivite(activite));
        droite.getChildren().add(btn);

        mainRow.getChildren().addAll(icon, infos, droite);
        carte.getChildren().add(mainRow);

        // Hover
        if (!dejaSel) {
            carte.setOnMouseEntered(e -> carte.setStyle(
                    "-fx-background-color:#FFFAF7;-fx-background-radius:12;-fx-border-color:#FFCDB0;-fx-border-radius:12;-fx-border-width:1.5;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.15),12,0,0,4);"));
            carte.setOnMouseExited(e -> carte.setStyle(getStyleCarte(false)));
        }

        return carte;
    }

    // ══════════════════════════════════════════════════════════════
    //  TOGGLE SÉLECTION
    // ══════════════════════════════════════════════════════════════
    private void toggleActivite(Activite activite) {
        boolean dejaSel = activitesSelectionnees.stream()
                .anyMatch(a -> a.getIdActivite() == activite.getIdActivite());
        if (dejaSel) {
            activitesSelectionnees.removeIf(a -> a.getIdActivite() == activite.getIdActivite());
        } else {
            activitesSelectionnees.add(activite);
        }
        appliquerFiltres();
        mettreAJourRecapitulatif();
    }

    // ══════════════════════════════════════════════════════════════
    //  RÉCAPITULATIF SÉLECTION
    // ══════════════════════════════════════════════════════════════
    private void mettreAJourRecapitulatif() {
        int    nb     = activitesSelectionnees.size();
        double total  = activitesSelectionnees.stream().mapToDouble(Activite::getPrix).sum();
        int    duree  = activitesSelectionnees.stream().mapToInt(Activite::getDureeEnHeure).sum();

        selectionCountLabel.setText(nb + " activité" + (nb > 1 ? "s" : "") + " sélectionnée" + (nb > 1 ? "s" : ""));
        totalCoutLabel.setText(String.format("💰 Total : %.0f TND", total));
        totalDureeLabel.setText("⏱ Durée totale : " + duree + "h");

        selectionContainer.getChildren().clear();
        if (nb == 0) {
            selectionContainer.getChildren().add(
                    msgLabel("Aucune activité sélectionnée", "#BBB"));
            return;
        }
        for (Activite a : activitesSelectionnees) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(4, 0, 4, 0));
            Label nomL  = labelStyle("• " + a.getNom(), "-fx-font-size:12px;-fx-text-fill:#444;");
            HBox.setHgrow(nomL, Priority.ALWAYS);
            Label prixL = labelStyle(String.format("%.0f TND", a.getPrix()),
                    "-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#FF6B35;");
            row.getChildren().addAll(nomL, prixL);
            selectionContainer.getChildren().add(row);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void passerEtapeSuivante() {
        if (activitesSelectionnees.isEmpty()) {
            showAlert("Aucune sélection", "Veuillez sélectionner au moins une activité."); return;
        }
        if (idVoyage > 0) {
            try {
                List<Integer> ids = activitesSelectionnees.stream()
                        .map(Activite::getIdActivite).collect(Collectors.toList());
                serviceVoyage.mettreAJourActivites(idVoyage, ids);
            } catch (SQLException e) {
                showAlert("Erreur BD", "Impossible d'enregistrer les activités : " + e.getMessage()); return;
            }
        }
        showAlert("✅ Enregistré !",
                activitesSelectionnees.size() + " activité(s) enregistrée(s) pour le voyage #" + idVoyage
                        + "\nTotal : " + String.format("%.0f TND",
                        activitesSelectionnees.stream().mapToDouble(Activite::getPrix).sum())
                        + "\n\nProchaine étape : Budget");
    }

    @FXML
    private void retourEtapePrecedente() {
        URL url = getClass().getClassLoader().getResource("ConfigurerVoyage/Hotel.fxml");
        if (url == null) url = getClass().getResource("/ConfigurerVoyage/Hotel.fxml");
        if (url == null) { showAlert("Erreur", "Hotel.fxml introuvable."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            ((HotelController) loader.getController())
                    .initDonnees(destination, dateDebut, dateFin, idVoyage);
            Stage stage = (Stage) suivantButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase — Choisir un Hôtel");
            stage.show();
        } catch (IOException ex) {
            showAlert("Erreur", "Impossible de recharger Hotel.fxml : " + ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════
    private String getStyleCarte(boolean selected) {
        return selected
                ? "-fx-background-color:#FFF3E0;-fx-background-radius:12;-fx-border-color:#FF6B35;-fx-border-radius:12;-fx-border-width:2;-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.2),12,0,0,4);"
                : "-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#ECECEC;-fx-border-radius:12;-fx-border-width:1.5;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);";
    }

    private String getEmoji(String libelle) {
        if (libelle == null) return "🎯";
        return switch (libelle) {
            case "Randonnée"             -> "🥾";
            case "Visite culturelle"     -> "🏛";
            case "Sport nautique"        -> "🏄";
            case "Ski / Snowboard"       -> "⛷";
            case "Spa / Bien-être"       -> "💆";
            case "Plongée sous-marine"   -> "🤿";
            case "Excursion en montagne" -> "⛰";
            case "Tour guidé en ville"   -> "🗺";
            case "Vélo / VTT"            -> "🚴";
            case "Canoë / Kayak"         -> "🛶";
            case "Observation de la faune" -> "🦁";
            case "Atelier culinaire"     -> "👨‍🍳";
            case "Concert / Spectacle"   -> "🎭";
            case "Parc d'attractions"    -> "🎡";
            case "Parapente / Aventure"  -> "🪂";
            case "Yoga / Méditation"     -> "🧘";
            case "Safari / Excursion nature" -> "🌿";
            case "Plage / Baignade"      -> "🏖";
            case "Escalade"              -> "🧗";
            case "Croisière / Bateau"    -> "⛵";
            default -> "🎯";
        };
    }

    @FXML private void onMouseEnteredButton(javafx.scene.input.MouseEvent e)  { ((Button)e.getSource()).setOpacity(0.85); }
    @FXML private void onMouseExitedButton(javafx.scene.input.MouseEvent e)   { ((Button)e.getSource()).setOpacity(1.0); }
    @FXML private void onMouseEnteredSuivantButton(javafx.scene.input.MouseEvent e) {
        ((Button)e.getSource()).setStyle("-fx-background-color:linear-gradient(to right,#E8622F,#E08519);-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.6),16,0,0,5);");
    }
    @FXML private void onMouseExitedSuivantButton(javafx.scene.input.MouseEvent e) {
        ((Button)e.getSource()).setStyle("-fx-background-color:linear-gradient(to right,#FF6B35,#F7931E);-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.45),12,0,0,4);");
    }

    private Label labelStyle(String text, String style) {
        Label l = new Label(text); l.setStyle(style); l.setWrapText(true); return l;
    }
    private Label badgeLabel(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color:"+bg+";-fx-text-fill:"+fg+";-fx-font-size:9px;-fx-font-weight:bold;-fx-background-radius:4;-fx-padding:2 6;");
        return l;
    }
    private Label msgLabel(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:"+color+";-fx-font-size:13px;-fx-padding:20;");
        l.setWrapText(true); return l;
    }
    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}