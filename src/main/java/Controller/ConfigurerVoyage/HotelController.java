package Controller.ConfigurerVoyage;

import Entite.Destination;
import Entite.Hotel;
import Service.ServiceHotel;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class HotelController {

    /* ── FXML refs ── */
    @FXML private Label      destinationLabel;
    @FXML private Label      datesLabel;
    @FXML private Label      headerDestLabel;
    @FXML private Label      headerDatesLabel;
    @FXML private Label      dureeLabel;
    @FXML private Label      selectedHotelLabel;
    @FXML private Label      hotelDetailNom;
    @FXML private Label      hotelDetailVille;
    @FXML private Label      hotelDetailAdresse;
    @FXML private Label      hotelDetailPrix;
    @FXML private DatePicker checkInPicker;
    @FXML private DatePicker checkOutPicker;
    @FXML private Label      nuitLabel;
    @FXML private VBox       hotelListContainer;
    @FXML private VBox       hotelDetailPanel;
    @FXML private Button     suivantButton;
    @FXML private Label      countLabel;

    /* ── Filtres ── */
    @FXML private TextField        searchNomField;
    @FXML private ComboBox<String> filterStarsBox;
    @FXML private CheckBox         showCompletCheckBox;
    @FXML private ComboBox<String> filterTypeChambreBox;
    @FXML private ComboBox<String> filterTypeReservationBox;

    private final ServiceHotel     serviceHotel  = new ServiceHotel();
    private final ServiceVoyage    serviceVoyage = new ServiceVoyage();
    private final DateTimeFormatter fmt           = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ── State ── */
    private Destination destination;
    private LocalDate   dateDebut;
    private LocalDate   dateFin;
    private Hotel       hotelSelectionne;
    private List<Hotel> allHotels = new java.util.ArrayList<>();
    private int         idVoyage  = -1;   // ← reçu de VolController

    // ══════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ══════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        if (filterStarsBox != null) {
            filterStarsBox.getItems().addAll(
                    "Toutes", "1 ★", "2 ★★", "3 ★★★", "4 ★★★★", "5 ★★★★★"
            );
            filterStarsBox.setValue("Toutes");
            filterStarsBox.setOnAction(e -> appliquerFiltres());
        }

        if (filterTypeChambreBox != null) {
            filterTypeChambreBox.getItems().addAll(
                    "Tous", "Individuelle", "Double", "Twin", "Triple",
                    "Suite", "Suite Présidentielle", "Familiale", "Studio"
            );
            filterTypeChambreBox.setValue("Tous");
            filterTypeChambreBox.setOnAction(e -> appliquerFiltres());
        }

        if (filterTypeReservationBox != null) {
            filterTypeReservationBox.getItems().addAll(
                    "Tous",
                    "Logement Seul",
                    "Accès Piscine Seulement",
                    "Bed and Breakfast",
                    "Demi-Pension",
                    "Pension Complète",
                    "Tout Inclus",
                    "All Inclusive Premium",
                    "Chambre + Petit-Déjeuner + Dîner"
            );
            filterTypeReservationBox.setValue("Tous");
            filterTypeReservationBox.setOnAction(e -> appliquerFiltres());
        }

        if (searchNomField != null)
            searchNomField.textProperty().addListener((obs, o, n) -> appliquerFiltres());
        if (showCompletCheckBox != null)
            showCompletCheckBox.setOnAction(e -> appliquerFiltres());

        javafx.beans.value.ChangeListener<LocalDate> dl = (obs, old, nw) -> {
            calculerNuits();
            if (destination != null) chargerHotels();
        };
        checkInPicker.valueProperty().addListener(dl);
        checkOutPicker.valueProperty().addListener(dl);
    }

    // ══════════════════════════════════════════════════════════════
    //  INIT DONNÉES
    // ══════════════════════════════════════════════════════════════

    // Ancienne signature (rétro-compatibilité)
    public void initDonnees(Destination destination, LocalDate dateDebut, LocalDate dateFin) {
        initDonnees(destination, dateDebut, dateFin, -1);
    }

    // Nouvelle signature avec idVoyage
    public void initDonnees(Destination destination, LocalDate dateDebut, LocalDate dateFin, int idVoyage) {
        this.destination = destination;
        this.dateDebut   = dateDebut;
        this.dateFin     = dateFin;
        this.idVoyage    = idVoyage;
        mettreAJourResume();
        checkInPicker.setValue(dateDebut);
        checkOutPicker.setValue(dateFin);
        chargerHotels();
    }

    // ══════════════════════════════════════════════════════════════
    //  RÉSUMÉ EN-TÊTE
    // ══════════════════════════════════════════════════════════════
    private void mettreAJourResume() {
        destinationLabel.setText("🌍 " + destination.getPays() + " — " + destination.getVille());
        datesLabel.setText(dateDebut.format(fmt) + "  →  " + dateFin.format(fmt));
        long jours = ChronoUnit.DAYS.between(dateDebut, dateFin);
        dureeLabel.setText(jours + " nuit" + (jours > 1 ? "s" : ""));
        if (headerDestLabel  != null) headerDestLabel.setText(destination.getPays() + " — " + destination.getVille());
        if (headerDatesLabel != null) headerDatesLabel.setText(dateDebut.format(fmt) + " → " + dateFin.format(fmt));
    }

    // ══════════════════════════════════════════════════════════════
    //  CALCUL NUITS
    // ══════════════════════════════════════════════════════════════
    private void calculerNuits() {
        LocalDate ci = checkInPicker.getValue();
        LocalDate co = checkOutPicker.getValue();
        if (ci != null && co != null) {
            if (co.isBefore(ci) || co.isEqual(ci)) {
                nuitLabel.setText("⚠ Date de check-out invalide");
                nuitLabel.setStyle("-fx-font-size:11.5px;-fx-text-fill:#E74C3C;-fx-font-weight:bold;");
            } else {
                long nuits = ChronoUnit.DAYS.between(ci, co);
                nuitLabel.setText("🌙 " + nuits + " nuit" + (nuits > 1 ? "s" : ""));
                nuitLabel.setStyle("-fx-font-size:11.5px;-fx-text-fill:#FF6B35;-fx-font-weight:bold;");
                if (hotelSelectionne != null) {
                    double total = hotelSelectionne.getPrixParNuit() * nuits;
                    hotelDetailPrix.setText(String.format("💰 %.0f TND/nuit  ·  Total : %.0f TND",
                            hotelSelectionne.getPrixParNuit(), total));
                }
            }
        } else {
            nuitLabel.setText("");
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  CHARGEMENT HÔTELS
    // ══════════════════════════════════════════════════════════════
    private void chargerHotels() {
        reinitialiserSelection();
        LocalDate ci = checkInPicker.getValue();
        LocalDate co = checkOutPicker.getValue();
        if (ci == null || co == null || !co.isAfter(ci)) {
            countLabel.setText("Saisissez des dates valides pour filtrer");
            countLabel.setStyle("-fx-font-size:11.5px;-fx-text-fill:#E74C3C;");
            hotelListContainer.getChildren().setAll(
                    msgLabel("⚠ Veuillez saisir des dates de check-in et check-out valides.", "#E74C3C"));
            return;
        }
        try {
            allHotels = serviceHotel.findByDestination(destination.getIdDestination());
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les hôtels : " + e.getMessage());
            return;
        }
        appliquerFiltres();
    }

    // ══════════════════════════════════════════════════════════════
    //  FILTRES  (capacite > 0 = disponible)
    // ══════════════════════════════════════════════════════════════
    private void appliquerFiltres() {
        hotelListContainer.getChildren().clear();

        if (allHotels.isEmpty()) {
            countLabel.setText("Aucun hôtel pour cette destination");
            countLabel.setStyle("-fx-font-size:11.5px;-fx-text-fill:#E74C3C;");
            hotelListContainer.getChildren().add(
                    msgLabel("😕 Aucun hôtel enregistré pour " + destination.getVille(), "#999"));
            return;
        }

        String recherche = (searchNomField != null)
                ? searchNomField.getText().trim().toLowerCase() : "";

        int starFilter = 0;
        if (filterStarsBox != null && filterStarsBox.getValue() != null) {
            String val = filterStarsBox.getValue();
            if (!val.equals("Toutes")) {
                try { starFilter = Integer.parseInt(val.substring(0, 1)); }
                catch (NumberFormatException ignored) {}
            }
        }

        boolean afficherComplets = showCompletCheckBox != null && showCompletCheckBox.isSelected();
        final int sf = starFilter;

        // ── NOUVEAU : lire les deux nouveaux filtres ──────────────────
        String filterChambre = (filterTypeChambreBox != null && filterTypeChambreBox.getValue() != null
                && !filterTypeChambreBox.getValue().equals("Tous"))
                ? filterTypeChambreBox.getValue() : "";

        String filterResa = (filterTypeReservationBox != null && filterTypeReservationBox.getValue() != null
                && !filterTypeReservationBox.getValue().equals("Tous"))
                ? filterTypeReservationBox.getValue() : "";
        // ─────────────────────────────────────────────────────────────

        // ── REMPLACE l'ancienne List<Hotel> tousFiltrés ───────────────
        List<Hotel> tousFiltrés = allHotels.stream()
                .filter(h -> recherche.isEmpty() || h.getNom().toLowerCase().contains(recherche))
                .filter(h -> sf == 0 || h.getStars() == sf)
                .filter(h -> filterChambre.isEmpty() || filterChambre.equals(h.getTypeChambre()))
                .filter(h -> filterResa.isEmpty()    || filterResa.equals(h.getTypeReservation()))
                .collect(Collectors.toList());
        // ─────────────────────────────────────────────────────────────

        // (le reste ne change pas)
        List<Hotel> disponibles = tousFiltrés.stream()
                .filter(h -> h.getCapacite() > 0)
                .collect(Collectors.toList());

        List<Hotel> complets = tousFiltrés.stream()
                .filter(h -> h.getCapacite() == 0)
                .collect(Collectors.toList());

        int total = disponibles.size() + (afficherComplets ? complets.size() : 0);

        if (total == 0) {
            countLabel.setText("Aucun résultat pour ces critères");
            countLabel.setStyle("-fx-font-size:11.5px;-fx-text-fill:#E74C3C;");
            hotelListContainer.getChildren().add(
                    msgLabel("🔍 Aucun hôtel ne correspond à votre recherche.", "#999"));
            return;
        }

        LocalDate ci = checkInPicker.getValue();
        LocalDate co = checkOutPicker.getValue();
        String dateInfo = (ci != null && co != null && co.isAfter(ci))
                ? " · " + ci.format(fmt) + " → " + co.format(fmt) : "";
        countLabel.setText(disponibles.size() + " hôtel(s) avec chambres disponibles" + dateInfo);
        countLabel.setStyle("-fx-font-size:11.5px;-fx-text-fill:#27AE60;");

        for (Hotel h : disponibles)
            hotelListContainer.getChildren().add(creerCarteHotel(h, false));

        if (afficherComplets && !complets.isEmpty()) {
            Label sep = msgLabel("— Hôtels complets (0 chambre disponible) —", "#BBB");
            sep.setAlignment(Pos.CENTER);
            sep.setMaxWidth(Double.MAX_VALUE);
            hotelListContainer.getChildren().add(sep);
            for (Hotel h : complets)
                hotelListContainer.getChildren().add(creerCarteHotel(h, true));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  CARTE HÔTEL
    // ══════════════════════════════════════════════════════════════
    private VBox creerCarteHotel(Hotel hotel, boolean complet) {
        VBox carte = new VBox(0);
        carte.setStyle(
                "-fx-background-color:" + (complet ? "#F9F9F9" : "white") + ";" +
                        "-fx-background-radius:12;-fx-border-color:" + (complet ? "#DCDCDC" : "#ECECEC") + ";" +
                        "-fx-border-radius:12;-fx-border-width:1.5;" +
                        (complet ? "" : "-fx-cursor:hand;") +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");

        HBox mainRow = new HBox(12);
        mainRow.setPadding(new Insets(16, 18, 14, 18));
        mainRow.setAlignment(Pos.CENTER_LEFT);

        StackPane icon = new StackPane();
        icon.setPrefSize(44, 44); icon.setMinSize(44, 44);
        icon.setStyle("-fx-background-color:" + (complet ? "#F0F0F0" : "#FFF3E0") + ";-fx-background-radius:11;");
        icon.getChildren().add(labelStyle(complet ? "🔒" : "🏨", "-fx-font-size:19px;"));

        VBox infos = new VBox(4);
        HBox.setHgrow(infos, Priority.ALWAYS);

        HBox nomRow = new HBox(8);
        nomRow.setAlignment(Pos.CENTER_LEFT);
        Label nomLbl = labelStyle(hotel.getNom(),
                "-fx-font-size:14.5px;-fx-font-weight:bold;-fx-text-fill:" + (complet ? "#AAAAAA" : "#2C2C2C") + ";");

        if (complet) {
            nomRow.getChildren().addAll(nomLbl, badgeLabel("COMPLET", "#E74C3C", "white"));
        } else {
            nomRow.getChildren().addAll(nomLbl,
                    badgeLabel("✓ " + hotel.getCapacite() + " chambre(s) libre(s)", "#27AE60", "white"));
        }

        HBox starsRow = new HBox(2);
        starsRow.setAlignment(Pos.CENTER_LEFT);
        int e = Math.max(1, Math.min(5, hotel.getStars()));
        for (int i = 0; i < 5; i++)
            starsRow.getChildren().add(labelStyle("★",
                    "-fx-font-size:13px;-fx-text-fill:" + (i < e ? (complet ? "#CCC" : "#F7931E") : "#DDD") + ";"));
        starsRow.getChildren().add(labelStyle("  " + e + " étoile" + (e > 1 ? "s" : ""),
                "-fx-font-size:11px;-fx-text-fill:#AAA;"));

        infos.getChildren().addAll(nomRow, starsRow,
                labelStyle("📍 " + hotel.getVille(),
                        "-fx-font-size:12px;-fx-text-fill:" + (complet ? "#BBB" : "#888") + ";"));

        VBox prixBox = new VBox(2);
        prixBox.setAlignment(Pos.CENTER_RIGHT);
        prixBox.getChildren().addAll(
                labelStyle(String.format("%.0f TND", hotel.getPrixParNuit()),
                        "-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:" + (complet ? "#CCC" : "#FF6B35") + ";"),
                labelStyle("/ nuit", "-fx-font-size:10px;-fx-text-fill:#BBB;"));

        mainRow.getChildren().addAll(icon, infos, prixBox);
        carte.getChildren().add(mainRow);

        VBox detailPanel = new VBox(10);
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
        detailPanel.setPadding(new Insets(0, 18, 14, 18));
        detailPanel.setStyle("-fx-border-color:#F0F0F0;-fx-border-width:1 0 0 0;");

        Label adresseLbl = labelStyle("🏠 " + hotel.getAdresse(), "-fx-font-size:12px;-fx-text-fill:#666;");
        adresseLbl.setWrapText(true);

        HBox tagsRow = new HBox(10);
        tagsRow.setAlignment(Pos.CENTER_LEFT);
        tagsRow.getChildren().addAll(
                chipLabel("🛏 " + hotel.getTypeChambre()),
                chipLabel("👥 " + hotel.getCapacite() + " chambre(s) libre(s)"));
        detailPanel.getChildren().addAll(new Separator(), adresseLbl, tagsRow);

        LocalDate ci = checkInPicker.getValue();
        LocalDate co = checkOutPicker.getValue();
        if (ci != null && co != null && co.isAfter(ci)) {
            long nuits = ChronoUnit.DAYS.between(ci, co);
            double total = hotel.getPrixParNuit() * nuits;
            detailPanel.getChildren().add(labelStyle(
                    String.format("💰 %.0f TND/nuit × %d nuit%s = %.0f TND total",
                            hotel.getPrixParNuit(), nuits, nuits > 1 ? "s" : "", total),
                    "-fx-font-size:12.5px;-fx-text-fill:#FF6B35;-fx-font-weight:bold;"));
        }

        if (!complet) {
            Button choisirBtn = new Button("✓ Choisir cet hôtel");
            choisirBtn.setStyle(
                    "-fx-background-color:linear-gradient(to right,#FF6B35,#F7931E);" +
                            "-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;" +
                            "-fx-background-radius:8;-fx-cursor:hand;-fx-padding:8 20;");
            choisirBtn.setOnAction(ev -> selectionnerHotel(hotel, carte));
            HBox btnRow = new HBox();
            btnRow.setAlignment(Pos.CENTER_RIGHT);
            btnRow.getChildren().add(choisirBtn);
            detailPanel.getChildren().add(btnRow);
        }

        carte.getChildren().add(detailPanel);

        HBox footer = new HBox();
        footer.setPadding(new Insets(0, 18, 12, 18));
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button voirPlusBtn = new Button("▼  Voir plus");
        voirPlusBtn.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:#FF6B35;" +
                        "-fx-font-size:11.5px;-fx-font-weight:bold;-fx-cursor:hand;" +
                        "-fx-border-color:#FFCDB0;-fx-border-radius:6;-fx-border-width:1;-fx-padding:4 12;");
        voirPlusBtn.setOnAction(ev -> {
            boolean open = detailPanel.isVisible();
            detailPanel.setVisible(!open);
            detailPanel.setManaged(!open);
            voirPlusBtn.setText(open ? "▼  Voir plus" : "▲  Voir moins");
        });
        footer.getChildren().add(voirPlusBtn);
        carte.getChildren().add(footer);

        if (!complet) {
            String styleNormal = "-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#ECECEC;-fx-border-radius:12;-fx-border-width:1.5;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);";
            String styleHover  = "-fx-background-color:#FFFAF7;-fx-background-radius:12;-fx-border-color:#FFCDB0;-fx-border-radius:12;-fx-border-width:1.5;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.15),12,0,0,4);";
            carte.setOnMouseEntered(ev -> { if (hotelSelectionne == null || hotelSelectionne.getIdHotel() != hotel.getIdHotel()) carte.setStyle(styleHover); });
            carte.setOnMouseExited (ev -> { if (hotelSelectionne == null || hotelSelectionne.getIdHotel() != hotel.getIdHotel()) carte.setStyle(styleNormal); });
        }

        return carte;
    }

    // ══════════════════════════════════════════════════════════════
    //  SÉLECTION HÔTEL
    // ══════════════════════════════════════════════════════════════
    private void selectionnerHotel(Hotel hotel, VBox carte) {
        hotelListContainer.getChildren().forEach(node -> {
            if (node instanceof VBox)
                node.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#ECECEC;-fx-border-radius:12;-fx-border-width:1.5;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");
        });
        carte.setStyle("-fx-background-color:#FFF3E0;-fx-background-radius:12;-fx-border-color:#FF6B35;-fx-border-radius:12;-fx-border-width:2;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.25),14,0,0,5);");

        hotelSelectionne = hotel;
        selectedHotelLabel.setText("✅ " + hotel.getNom() + " sélectionné");
        selectedHotelLabel.setStyle("-fx-text-fill:#27AE60;-fx-font-size:13px;-fx-font-weight:bold;");
        hotelDetailNom.setText(hotel.getNom());
        hotelDetailVille.setText("📍 " + hotel.getVille() + "  " + "★".repeat(Math.max(1, Math.min(5, hotel.getStars()))));
        hotelDetailAdresse.setText("🏠 " + hotel.getAdresse());

        LocalDate ci = checkInPicker.getValue();
        LocalDate co = checkOutPicker.getValue();
        if (ci != null && co != null && co.isAfter(ci)) {
            long nuits = ChronoUnit.DAYS.between(ci, co);
            double total = hotel.getPrixParNuit() * nuits;
            hotelDetailPrix.setText(String.format("💰 %.0f TND/nuit  ·  Total : %.0f TND", hotel.getPrixParNuit(), total));
        } else {
            hotelDetailPrix.setText(String.format("💰 %.0f TND / nuit", hotel.getPrixParNuit()));
        }
        hotelDetailPanel.setVisible(true);
        hotelDetailPanel.setManaged(true);
    }

    private void reinitialiserSelection() {
        hotelSelectionne = null;
        selectedHotelLabel.setText("Aucun hôtel sélectionné");
        selectedHotelLabel.setStyle("-fx-text-fill:#BBB;-fx-font-size:13px;-fx-font-style:italic;");
        hotelDetailPanel.setVisible(false);
        hotelDetailPanel.setManaged(false);
    }

    // ══════════════════════════════════════════════════════════════
    //  NAVIGATION  →  enregistre en BD puis passe à l'étape suivante
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void passerEtapeSuivante() {
        if (hotelSelectionne == null) {
            showAlert("Champ requis", "Veuillez sélectionner un hôtel avant de continuer."); return;
        }
        if (checkInPicker.getValue() == null) {
            showAlert("Champ requis", "Veuillez sélectionner une date de check-in."); return;
        }
        if (checkOutPicker.getValue() == null) {
            showAlert("Champ requis", "Veuillez sélectionner une date de check-out."); return;
        }
        if (!checkOutPicker.getValue().isAfter(checkInPicker.getValue())) {
            showAlert("Dates invalides", "La date de check-out doit être après le check-in."); return;
        }

        // ── Enregistrement en BD ──────────────────────────────────
        if (idVoyage > 0) {
            try {
                serviceVoyage.mettreAJourHotel(
                        idVoyage,
                        hotelSelectionne.getIdHotel(),
                        checkInPicker.getValue(),
                        checkOutPicker.getValue()
                );
            } catch (SQLException e) {
                showAlert("Erreur BD", "Impossible d'enregistrer l'hôtel : " + e.getMessage()); return;
            }
        }
        // ─────────────────────────────────────────────────────────

        // ── Navigation vers Activités ─────────────────────────────
        URL url = getClass().getClassLoader().getResource("ConfigurerVoyage/ActiviteUser.fxml");
        if (url == null) url = getClass().getResource("/ConfigurerVoyage/ActiviteUser.fxml");
        if (url == null) { showAlert("Erreur", "ActiviteUser.fxml introuvable."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            // Récupérer le rythme depuis la BD car Destination ne l'a pas
            String rythme = "";
            try {
                rythme = serviceVoyage.findbyId(idVoyage).getRythme();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            ((ActiviteUserController) loader.getController())
                    .initDonnees(destination, dateDebut, dateFin, rythme, idVoyage);
            Stage stage = (Stage) suivantButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase — Choisir des Activités");
            stage.show();
        } catch (IOException ex) {
            showAlert("Erreur", "Impossible de charger ActiviteUser.fxml : " + ex.getMessage());
        }
        // ─────────────────────────────────────────────────────────
    }

    @FXML
    private void retourEtapePrecedente() {
        URL url = getClass().getClassLoader().getResource("ConfigurerVoyage/Vol.fxml");
        if (url == null) url = getClass().getResource("/ConfigurerVoyage/Vol.fxml");
        if (url == null) { showAlert("Erreur", "Vol.fxml introuvable."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            ((VolController) loader.getController()).initDonnees(destination, dateDebut, dateFin, idVoyage);
            Stage stage = (Stage) suivantButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase — Choisir un Vol");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Impossible de recharger Vol.fxml : " + ex.getMessage());
        }
    }

    @FXML private void onMouseEnteredButton(javafx.scene.input.MouseEvent e)  { ((Button)e.getSource()).setOpacity(0.85); }
    @FXML private void onMouseExitedButton(javafx.scene.input.MouseEvent e)   { ((Button)e.getSource()).setOpacity(1.0); }
    @FXML private void onMouseEnteredSuivantButton(javafx.scene.input.MouseEvent e) {
        ((Button)e.getSource()).setStyle("-fx-background-color:linear-gradient(to right,#E8622F,#E08519);-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.6),16,0,0,5);");
    }
    @FXML private void onMouseExitedSuivantButton(javafx.scene.input.MouseEvent e) {
        ((Button)e.getSource()).setStyle("-fx-background-color:linear-gradient(to right,#FF6B35,#F7931E);-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.45),12,0,0,4);");
    }

    private Label labelStyle(String text, String style) { Label l = new Label(text); l.setStyle(style); l.setWrapText(true); return l; }
    private Label badgeLabel(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";-fx-font-size:9px;-fx-font-weight:bold;-fx-background-radius:4;-fx-padding:2 6;");
        return l;
    }
    private Label chipLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color:#F5F5F5;-fx-text-fill:#555;-fx-font-size:11.5px;-fx-background-radius:6;-fx-padding:4 10;");
        return l;
    }
    private Label msgLabel(String text, String color) {
        Label l = new Label(text); l.setStyle("-fx-text-fill:" + color + ";-fx-font-size:13px;-fx-padding:20;"); l.setWrapText(true); return l;
    }
    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}