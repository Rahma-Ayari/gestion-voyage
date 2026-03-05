package Controller;

import Entite.*;
import Service.ServiceOffre;
import Service.ServiceReservation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ReservationUserController implements Initializable {

    @FXML private FlowPane  offresGrid;
    @FXML private VBox      emptyBox;
    @FXML private TextField searchField;

    private final ServiceOffre       serviceOffre       = new ServiceOffre();
    private final ServiceReservation serviceReservation = new ServiceReservation();
    private List<Offre> toutesLesOffres;

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_DT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadOffres();
        setupSearch();
    }

    // ══════════════════════════════════════════════════════
    //  CHARGEMENT
    // ══════════════════════════════════════════════════════

    private void loadOffres() {
        try {
            toutesLesOffres = serviceOffre.readAll().stream()
                    .filter(Offre::isDisponibilite)
                    .collect(Collectors.toList());
            afficherOffres(toutesLesOffres);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur chargement : " + e.getMessage());
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, o, n) -> appliquerRecherche());
    }

    private void appliquerRecherche() {
        if (toutesLesOffres == null) return;
        String s = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        List<Offre> filtrees = toutesLesOffres.stream().filter(o ->
                s.isEmpty()
                        || (o.getType() != null && o.getType().toLowerCase().contains(s))
                        || (o.getDescription() != null && o.getDescription().toLowerCase().contains(s))
        ).collect(Collectors.toList());
        afficherOffres(filtrees);
    }

    private void afficherOffres(List<Offre> offres) {
        offresGrid.getChildren().clear();
        if (offres.isEmpty()) {
            emptyBox.setVisible(true);  emptyBox.setManaged(true);
        } else {
            emptyBox.setVisible(false); emptyBox.setManaged(false);
            for (Offre o : offres) offresGrid.getChildren().add(creerCarte(o));
        }
    }

    // ══════════════════════════════════════════════════════
    //  CARTE OFFRE
    // ══════════════════════════════════════════════════════

    private VBox creerCarte(Offre offre) {
        VBox carte = new VBox(0);
        carte.setPrefWidth(270);
        carte.setStyle(
                "-fx-background-color: white; -fx-background-radius: 14;" +
                        "-fx-border-color: #ECECEC; -fx-border-radius: 14; -fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        // Image
        StackPane imagePane = new StackPane();
        imagePane.setPrefHeight(160);
        imagePane.setStyle("-fx-background-color: #FFF3E0; -fx-background-radius: 14 14 0 0;");

        Label imgPh = new Label("🏖"); imgPh.setStyle("-fx-font-size: 40px;");
        ImageView iv = new ImageView();
        iv.setFitWidth(270); iv.setFitHeight(160); iv.setPreserveRatio(false);
        Rectangle clip = new Rectangle(270, 160);
        clip.setArcWidth(28); clip.setArcHeight(28);
        iv.setClip(clip);

        Image img = chargerImage(offre.getImagePath(), 270, 160);
        if (img != null) { iv.setImage(img); iv.setVisible(true); imgPh.setVisible(false); }
        else             { iv.setVisible(false); imgPh.setVisible(true); }

        // Destination badge sur l'image
        if (offre.getDestination() != null) {
            Destination d = offre.getDestination();
            Label destBadge = new Label("📍 " + d.getVille() + ", " + d.getPays());
            destBadge.setStyle(
                    "-fx-background-color: rgba(0,0,0,0.55); -fx-text-fill: white;" +
                            "-fx-font-size: 10px; -fx-background-radius: 20; -fx-padding: 4 10;");
            StackPane.setAlignment(destBadge, Pos.BOTTOM_LEFT);
            StackPane.setMargin(destBadge, new Insets(0, 0, 8, 8));
            imagePane.getChildren().addAll(imgPh, iv, destBadge);
        } else {
            imagePane.getChildren().addAll(imgPh, iv);
        }

        // Corps
        VBox corps = new VBox(6);
        corps.setPadding(new Insets(14, 16, 16, 16));

        Label typeL = new Label(offre.getType() != null ? offre.getType().toUpperCase() : "OFFRE");
        typeL.setStyle("-fx-text-fill: #FF6B35; -fx-font-size: 9.5px; -fx-font-weight: bold;" +
                "-fx-background-color: #FFF3E0; -fx-background-radius: 4; -fx-padding: 2 7;");

        Label descL = new Label(offre.getDescription() != null ? offre.getDescription() : "");
        descL.setWrapText(true);
        descL.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        descL.setMaxHeight(38);

        // Dates si présentes
        if (offre.getDateDebut() != null && offre.getDateFin() != null) {
            Label datesL = new Label("📅 " + offre.getDateDebut().format(FMT_DATE)
                    + "  →  " + offre.getDateFin().format(FMT_DATE));
            datesL.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
            corps.getChildren().addAll(typeL, descL, datesL);
        } else {
            corps.getChildren().addAll(typeL, descL);
        }

        // Prix + boutons
        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(footer, new Insets(6, 0, 0, 0));

        Label prixL = new Label(String.format("%.0f €", offre.getPrix()));
        prixL.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FF6B35;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnPlus = new Button("Voir plus");
        btnPlus.setStyle(
                "-fx-background-color: white; -fx-text-fill: #FF6B35;" +
                        "-fx-border-color: #FF6B35; -fx-border-radius: 8; -fx-background-radius: 8;" +
                        "-fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 6 12;");
        btnPlus.setOnAction(e -> ouvrirDetail(offre));

        Button btnRes = new Button("Réserver");
        btnRes.setStyle(
                "-fx-background-color: #FF6B35; -fx-text-fill: white;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 12;");
        btnRes.setOnAction(e -> ouvrirReservation(offre));

        footer.getChildren().addAll(prixL, sp, btnPlus, btnRes);
        corps.getChildren().add(footer);
        carte.getChildren().addAll(imagePane, corps);
        return carte;
    }

    // ══════════════════════════════════════════════════════
    //  POPUP DÉTAIL COMPLET & MODERNE
    // ══════════════════════════════════════════════════════

    private void ouvrirDetail(Offre offre) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Détail de l'offre");
        dialog.setResizable(false);

        // ── Scroll global ─────────────────────────────────
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F4F6F9;");

        // ── HEADER ────────────────────────────────────────
        StackPane headerPane = new StackPane();
        headerPane.setPrefHeight(220);

        // Image de fond
        ImageView headerImg = new ImageView();
        headerImg.setFitWidth(620); headerImg.setFitHeight(220);
        headerImg.setPreserveRatio(false);
        Image img = chargerImage(offre.getImagePath(), 620, 220);
        if (img != null) headerImg.setImage(img);

        // Overlay sombre
        Region overlay = new Region();
        overlay.setPrefSize(620, 220);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");

        // Contenu header
        VBox headerContent = new VBox(8);
        headerContent.setAlignment(Pos.BOTTOM_LEFT);
        headerContent.setPadding(new Insets(0, 24, 24, 24));

        Label typeBadge = new Label(offre.getType() != null ? offre.getType().toUpperCase() : "OFFRE");
        typeBadge.setStyle(
                "-fx-background-color: #FF6B35; -fx-text-fill: white;" +
                        "-fx-font-size: 10px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 4; -fx-padding: 3 10;");

        Label prixGrand = new Label(String.format("%.0f €", offre.getPrix()));
        prixGrand.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label descCourt = new Label(offre.getDescription() != null ? offre.getDescription() : "");
        descCourt.setWrapText(true);
        descCourt.setMaxWidth(560);
        descCourt.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.85);");

        headerContent.getChildren().addAll(typeBadge, prixGrand, descCourt);
        headerPane.getChildren().addAll(headerImg, overlay, headerContent);

        // ── CORPS ─────────────────────────────────────────
        VBox corps = new VBox(14);
        corps.setPadding(new Insets(20, 24, 24, 24));

        // ── Section Dates ──────────────────────────────────
        if (offre.getDateDebut() != null || offre.getDateFin() != null) {
            HBox datesBox = new HBox(12);
            datesBox.getChildren().add(titreSection("📅", "Période de l'offre"));
            corps.getChildren().add(titreSection("📅", "Période de l'offre"));

            HBox dateCards = new HBox(12);
            if (offre.getDateDebut() != null)
                dateCards.getChildren().add(carteInfo("🟢 Départ", offre.getDateDebut().format(FMT_DATE), "#E8F5E9", "#27AE60"));
            if (offre.getDateFin() != null)
                dateCards.getChildren().add(carteInfo("🔴 Retour", offre.getDateFin().format(FMT_DATE), "#FFEBEE", "#E74C3C"));

            // Durée
            if (offre.getDateDebut() != null && offre.getDateFin() != null) {
                long jours = java.time.temporal.ChronoUnit.DAYS.between(offre.getDateDebut(), offre.getDateFin());
                dateCards.getChildren().add(carteInfo("⏱ Durée", jours + " jours", "#E3F2FD", "#1565C0"));
            }
            corps.getChildren().add(dateCards);
        }

        // ── Section Destination ────────────────────────────
        if (offre.getDestination() != null) {
            Destination d = offre.getDestination();
            corps.getChildren().add(titreSection("🌍", "Destination"));

            VBox destCard = sectionCard();
            destCard.getChildren().addAll(
                    ligneDetail("🗺 Pays",        d.getPays()        != null ? d.getPays()        : "—"),
                    separateurFin(),
                    ligneDetail("🏙 Ville",       d.getVille()       != null ? d.getVille()       : "—"),
                    separateurFin(),
                    ligneDetail("📝 Description", d.getDescription() != null ? d.getDescription() : "—")
            );
            corps.getChildren().add(destCard);
        }

        // ── Section Vol ────────────────────────────────────
        if (offre.getVol() != null) {
            Vol v = offre.getVol();
            corps.getChildren().add(titreSection("✈", "Vol"));

            VBox volCard = sectionCard();
            volCard.getChildren().addAll(
                    ligneDetail("🔢 Numéro de vol", v.getNumeroVol()  != null ? v.getNumeroVol()  : "—"),
                    separateurFin(),
                    ligneDetail("🏢 Compagnie",     v.getCompagnie()  != null ? v.getCompagnie()  : "—"),
                    separateurFin(),
                    ligneDetail("🛫 Départ",         v.getDateDepart()  != null ? v.getDateDepart().format(FMT_DT)  : "—"),
                    separateurFin(),
                    ligneDetail("🛬 Arrivée",        v.getDateArrivee() != null ? v.getDateArrivee().format(FMT_DT) : "—"),
                    separateurFin(),
                    ligneDetail("💰 Prix vol",       String.format("%.0f €", v.getPrix()))
            );
            // Destination du vol
            if (v.getDestination() != null) {
                Destination dv = v.getDestination();
                volCard.getChildren().addAll(
                        separateurFin(),
                        ligneDetail("📍 Vers", dv.getVille() + ", " + dv.getPays())
                );
            }
            corps.getChildren().add(volCard);
        }

        // ── Section Hôtel ──────────────────────────────────
        if (offre.getHotel() != null) {
            Hotel h = offre.getHotel();
            corps.getChildren().add(titreSection("🏨", "Hôtel"));

            // Étoiles
            String etoiles = "★".repeat(Math.max(0, h.getStars())) + "☆".repeat(Math.max(0, 5 - h.getStars()));

            VBox hotelCard = sectionCard();
            hotelCard.getChildren().addAll(
                    ligneDetail("🏷 Nom",          h.getNom()          != null ? h.getNom()          : "—"),
                    separateurFin(),
                    ligneDetail("⭐ Étoiles",       etoiles),
                    separateurFin(),
                    ligneDetail("🏙 Ville",         h.getVille()        != null ? h.getVille()        : "—"),
                    separateurFin(),
                    ligneDetail("📍 Adresse",       h.getAdresse()      != null ? h.getAdresse()      : "—"),
                    separateurFin(),
                    ligneDetail("🛏 Type chambre",  h.getTypeChambre()  != null ? h.getTypeChambre()  : "—"),
                    separateurFin(),
                    ligneDetail("💰 Prix/nuit",     String.format("%.0f €", h.getPrixParNuit())),
                    separateurFin(),
                    ligneDetail("🏠 Capacité",      h.getCapacite() + " chambres")
            );
            corps.getChildren().add(hotelCard);
        }

        // ── Section Activité ───────────────────────────────
        if (offre.getActivite() != null) {
            Activite a = offre.getActivite();
            corps.getChildren().add(titreSection("🎯", "Activité"));

            VBox actCard = sectionCard();
            actCard.getChildren().addAll(
                    ligneDetail("🏷 Nom",         a.getNom()         != null ? a.getNom()         : "—"),
                    separateurFin(),
                    ligneDetail("📂 Catégorie",   a.getCategorie()   != null ? a.getCategorie()   : "—"),
                    separateurFin(),
                    ligneDetail("📝 Description", a.getDescription() != null ? a.getDescription() : "—"),
                    separateurFin(),
                    ligneDetail("⏱ Durée",        a.getDureeEnHeure() + " heure(s)"),
                    separateurFin(),
                    ligneDetail("🕐 Horaire",     a.getHoraire()     != null ? a.getHoraire()     : "—"),
                    separateurFin(),
                    ligneDetail("💰 Prix",        String.format("%.0f €", a.getPrix()))
            );
            corps.getChildren().add(actCard);
        }

        // ── Boutons ────────────────────────────────────────
        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        Button btnFermer = new Button("✕  Fermer");
        btnFermer.setStyle(
                "-fx-background-color: #F0F0F0; -fx-text-fill: #555;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;" +
                        "-fx-font-size: 13px; -fx-padding: 10 24;");
        btnFermer.setOnAction(e -> dialog.close());

        Button btnRes = new Button("🛒  Réserver maintenant");
        btnRes.setStyle(
                "-fx-background-color: linear-gradient(to right, #FF6B35, #F7931E);" +
                        "-fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;" +
                        "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 24;" +
                        "-fx-effect: dropshadow(gaussian, rgba(255,107,53,0.4), 10, 0, 0, 3);");
        btnRes.setOnAction(e -> { dialog.close(); ouvrirReservation(offre); });

        btnRow.getChildren().addAll(btnFermer, btnRes);
        corps.getChildren().add(btnRow);

        // ── Assemblage final ───────────────────────────────
        ScrollPane scroll = new ScrollPane(corps);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;" +
                "-fx-border-color: transparent;");

        root.getChildren().addAll(headerPane, scroll);

        Scene scene = new Scene(root, 620, 700);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ══════════════════════════════════════════════════════
    //  HELPERS UI
    // ══════════════════════════════════════════════════════

    /** Titre de section avec icône */
    private HBox titreSection(String icon, String titre) {
        HBox hb = new HBox(8);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setPadding(new Insets(4, 0, 0, 0));

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(30, 30);
        iconBox.setStyle("-fx-background-color: #FFF3E0; -fx-background-radius: 8;");
        Label ic = new Label(icon); ic.setStyle("-fx-font-size: 13px;");
        iconBox.getChildren().add(ic);

        Label lbl = new Label(titre);
        lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        hb.getChildren().addAll(iconBox, lbl);
        return hb;
    }

    /** Carte blanche arrondie pour regrouper les infos */
    private VBox sectionCard() {
        VBox box = new VBox(0);
        box.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12;" +
                        "-fx-border-color: #ECECEC; -fx-border-radius: 12; -fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);");
        box.setPadding(new Insets(4, 16, 4, 16));
        return box;
    }

    /** Ligne label + valeur */
    private HBox ligneDetail(String label, String valeur) {
        HBox hb = new HBox(12);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setPadding(new Insets(10, 0, 10, 0));

        Label lbl = new Label(label);
        lbl.setMinWidth(140);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #999; -fx-font-weight: bold;");

        Label val = new Label(valeur);
        val.setWrapText(true);
        val.setStyle("-fx-font-size: 13px; -fx-text-fill: #2C2C2C;");
        HBox.setHgrow(val, Priority.ALWAYS);

        hb.getChildren().addAll(lbl, val);
        return hb;
    }

    /** Petite carte colorée pour dates/durée */
    private VBox carteInfo(String titre, String valeur, String bgColor, String textColor) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(12, 16, 12, 16));
        box.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10;");
        HBox.setHgrow(box, Priority.ALWAYS);

        Label t = new Label(titre);
        t.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        Label v = new Label(valeur);
        v.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        box.getChildren().addAll(t, v);
        return box;
    }

    /** Séparateur fin entre lignes */
    private Separator separateurFin() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #F5F5F5; -fx-padding: 0;");
        return sep;
    }

    // ══════════════════════════════════════════════════════
    //  RÉSERVATION & IMAGE
    // ══════════════════════════════════════════════════════

    private void ouvrirReservation(Offre offre) {
        showAlert(Alert.AlertType.INFORMATION,
                "Vous avez choisi de réserver l'offre : " + offre.getType());
    }

    private Image chargerImage(String imagePath, double width, double height) {
        if (imagePath == null || imagePath.trim().isEmpty()) return null;
        try {
            File f = new File(imagePath.replace("\\", "/"));
            if (f.exists()) return new Image(f.toURI().toString(), width, height, false, true);
            URL resUrl = getClass().getResource("/images/" + f.getName());
            if (resUrl != null) return new Image(resUrl.toExternalForm(), width, height, false, true);
        } catch (Exception ignored) {}
        return null;
    }

    @FXML
    private void refreshOffres() {
        searchField.clear();
        loadOffres();
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}