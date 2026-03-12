package Controller;

import Entite.Utilisateur;
import Entite.Voyage;
import Service.ServiceVoyage;
import Utils.SessionManager;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

public class MesVoyagesController implements Initializable {

    @FXML private VBox   voyagesContainer;
    @FXML private Label  voyageCountLabel;
    @FXML private Label  completeCountLabel;
    @FXML private Label  pendingCountLabel;
    @FXML private Button newVoyageButton;
    @FXML private Button logoutButton;
    @FXML private Label  userNameLabel;
    @FXML private Button activeTabButton;
    @FXML private Button archiveTabButton;
    @FXML private Label  sectionTitleLabel;
    @FXML private Button btnRetour;

    private final ServiceVoyage     serviceVoyage = new ServiceVoyage();
    private final DateTimeFormatter fmt           = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final DateTimeFormatter fmtShort      = DateTimeFormatter.ofPattern("dd MMM");

    // État pour tracker quel onglet est actif
    private boolean isActiveTabSelected = true;
    private List<Voyage> currentVoyagesList;

    // ══════════════════════════════════════════════════════════════════
    // INITIALISATION
    // ══════════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!SessionManager.isAuthenticated()) { redirectToLogin(); return; }
        userNameLabel.setText(SessionManager.getUserName());
        newVoyageButton.setOnAction(e -> navigateToConfigVoyage());
        logoutButton.setOnAction(e -> handleLogout());
        btnRetour.setOnAction(e -> retourDashboard());  // ← AJOUT


        // Gestion des onglets
        activeTabButton.setOnAction(e -> switchToActiveTab());
        archiveTabButton.setOnAction(e -> switchToArchiveTab());

        // Charger les voyages actifs par défaut
        loadData();
    }

    // ══════════════════════════════════════════════════════════════════
    // GESTION DES ONGLETS
    // ══════════════════════════════════════════════════════════════════
    private void switchToActiveTab() {
        if (isActiveTabSelected) return; // Déjà sur cet onglet

        isActiveTabSelected = true;

        // Mise à jour visuelle des boutons
        activeTabButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FF6B35, #F7931E);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-border-radius: 10 10 0 0;" +
                        "-fx-background-radius: 10 10 0 0;" +
                        "-fx-cursor: hand;");

        archiveTabButton.setStyle(
                "-fx-background-color: #E8ECF2;" +
                        "-fx-text-fill: #64748B;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-border-radius: 10 10 0 0;" +
                        "-fx-background-radius: 10 10 0 0;" +
                        "-fx-cursor: hand;");

        sectionTitleLabel.setText("VOS VOYAGES ACTIFS");
        loadActiveVoyages();
    }

    private void switchToArchiveTab() {
        if (!isActiveTabSelected) return; // Déjà sur cet onglet

        isActiveTabSelected = false;

        // Mise à jour visuelle des boutons
        activeTabButton.setStyle(
                "-fx-background-color: #E8ECF2;" +
                        "-fx-text-fill: #64748B;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-border-radius: 10 10 0 0;" +
                        "-fx-background-radius: 10 10 0 0;" +
                        "-fx-cursor: hand;");

        archiveTabButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FF6B35, #F7931E);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-border-radius: 10 10 0 0;" +
                        "-fx-background-radius: 10 10 0 0;" +
                        "-fx-cursor: hand;");

        sectionTitleLabel.setText("VOS VOYAGES ARCHIVÉS");
        loadArchivedVoyages();
    }

    // ══════════════════════════════════════════════════════════════════
    // CHARGEMENT DES DONNÉES
    // ══════════════════════════════════════════════════════════════════
    private void loadData() {
        loadActiveVoyages();
    }

    private void loadActiveVoyages() {
        try {
            List<Voyage> voyages = serviceVoyage.getVoyagesByUser(SessionManager.getUserId());
            currentVoyagesList = voyages;

            voyagesContainer.getChildren().clear();

            long complete = voyages.stream()
                    .filter(v -> v.getIdVol() > 0 && v.getIdHotel() > 0).count();
            long pending  = voyages.size() - complete;

            voyageCountLabel.setText(String.valueOf(voyages.size()));
            if (completeCountLabel != null) completeCountLabel.setText(String.valueOf(complete));
            if (pendingCountLabel  != null) pendingCountLabel .setText(String.valueOf(pending));

            if (voyages.isEmpty()) displayEmptyState("Aucun voyage actif créé", "Commencez par créer votre premier voyage !");
            else for (Voyage v : voyages) voyagesContainer.getChildren().add(createTravelCard(v, false));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les voyages : " + e.getMessage());
        }
    }

    private void loadArchivedVoyages() {
        try {
            List<Voyage> voyages = serviceVoyage.getVoyagesArchivesByUser(SessionManager.getUserId());
            currentVoyagesList = voyages;

            voyagesContainer.getChildren().clear();

            if (voyages.isEmpty()) {
                displayEmptyState("Aucun voyage archivé", "Vous n'avez pas encore archivé de voyage");
            } else {
                for (Voyage v : voyages) {
                    voyagesContainer.getChildren().add(createTravelCard(v, true));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les voyages archivés : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // CARTE VOYAGE
    // ══════════════════════════════════════════════════════════════════
    private HBox createTravelCard(Voyage voyage, boolean isArchived) {
        boolean complete = voyage.getIdVol() > 0 && voyage.getIdHotel() > 0;
        boolean partial  = (voyage.getIdVol() > 0 || voyage.getIdHotel() > 0) && !complete;
        long    days     = ChronoUnit.DAYS.between(voyage.getDateDebut(), voyage.getDateFin());
        int     pct      = complete ? 100 : partial ? 50 : 0;

        Region accent = new Region();
        accent.setPrefWidth(6); accent.setMinWidth(6);
        accent.setStyle("-fx-background-color: " +
                (isArchived ? "linear-gradient(to bottom, #95A5A6, #7F8C8D)"
                        : (complete ? "linear-gradient(to bottom, #27AE60, #2ECC71)"
                        : partial ? "linear-gradient(to bottom, #FF6B35, #F7931E)"
                        : "linear-gradient(to bottom, #F39C12, #F1C40F)")) + ";");

        VBox body = new VBox(12);
        body.setPadding(new Insets(18, 22, 18, 18));
        HBox.setHgrow(body, Priority.ALWAYS);

        HBox topRow = new HBox(); topRow.setAlignment(Pos.CENTER_LEFT);
        String badgeBg, badgeFg, badgeTx;

        if (isArchived) {
            badgeBg = "#F0F0F0";
            badgeFg = "#7F8C8D";
            badgeTx = "📦 Archivé";
        } else {
            badgeBg = complete ? "#E6F7ED" : partial ? "#EBF5FB" : "#FFF0E6";
            badgeFg = complete ? "#27AE60"  : partial ? "#3498DB" : "#F39C12";
            badgeTx = complete ? "✅ Complet" : partial ? "🔄 En cours" : "⏳ À configurer";
        }

        Label badge = new Label(badgeTx);
        badge.setStyle(String.format(
                "-fx-background-color:%s;-fx-text-fill:%s;" +
                        "-fx-padding:4 12;-fx-background-radius:20;-fx-font-weight:bold;-fx-font-size:10;",
                badgeBg, badgeFg));
        Region spTop = new Region(); HBox.setHgrow(spTop, Priority.ALWAYS);
        topRow.getChildren().addAll(badge, spTop);

        Label title = new Label("🧳 Voyage " + voyage.getRythme());
        title.setStyle("-fx-font-size:20;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");

        HBox dateRow = new HBox(10); dateRow.setAlignment(Pos.CENTER_LEFT);
        Label dates = new Label("📅  " + voyage.getDateDebut().format(fmtShort) + " → " + voyage.getDateFin().format(fmtShort));
        dates.setStyle("-fx-text-fill:#94A3B8;-fx-font-size:13;");
        Label pill = new Label(days + " Jours");
        pill.setStyle("-fx-background-color:#F1F5F9;-fx-text-fill:#475569;" +
                "-fx-padding:4 12;-fx-background-radius:20;-fx-font-size:11;-fx-font-weight:bold;");
        dateRow.getChildren().addAll(dates, pill);

        HBox pillRow = new HBox(8); pillRow.setAlignment(Pos.CENTER_LEFT);
        pillRow.getChildren().addAll(
                statusPill("✈️ Vol",   voyage.getIdVol()   > 0),
                statusPill("🏨 Hôtel", voyage.getIdHotel() > 0));

        VBox prog = progressBar(pct, complete);

        Region sep = new Region(); sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color:#F1F5F9;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(4, 0, 0, 0));

        if (isArchived) {
            // Boutons pour les voyages archivés
            Button unarchBtn = styledBtn("♻️ Désarchiver", "#EBF5FB", "#3498DB", "#BFDFFF", "#3498DB", "white");
            unarchBtn.setOnAction(e -> { e.consume(); unarchiveVoyage(voyage); });

            Button delBtn = styledBtn("🗑️ Supprimer", "#FEF9F0", "#F39C12", "#FFD699", "#F39C12", "white");
            delBtn.setOnAction(e -> { e.consume(); deleteVoyage(voyage); });

            Region spAct = new Region(); HBox.setHgrow(spAct, Priority.ALWAYS);

            Button detailBtn = new Button("📖 Détails →");
            detailBtn.setStyle("-fx-background-color:linear-gradient(to right,#FF6B35,#F7931E);" +
                    "-fx-text-fill:white;-fx-font-size:12;-fx-font-weight:bold;" +
                    "-fx-padding:8 18;-fx-background-radius:10;-fx-cursor:hand;");
            detailBtn.setOnAction(e -> { e.consume(); showDetailsPopup(voyage, true); });

            actions.getChildren().addAll(unarchBtn, delBtn, spAct, detailBtn);
        } else {
            // Boutons pour les voyages actifs
            Button evalBtn = styledBtn("⭐ Évaluer", "#FFF0E6", "#FF6B35", "#FFDCCB", "#FF6B35", "white");
            evalBtn.setOnAction(e -> { e.consume(); navigateToAvis(voyage); });

            Button archBtn = styledBtn("📦 Archiver", "#F1F5F9", "#64748B", "#E2E8F0", "#64748B", "white");
            archBtn.setOnAction(e -> { e.consume(); archiveVoyage(voyage); });

            Region spAct = new Region(); HBox.setHgrow(spAct, Priority.ALWAYS);

            Button detailBtn = new Button("📖 Détails →");
            detailBtn.setStyle("-fx-background-color:linear-gradient(to right,#FF6B35,#F7931E);" +
                    "-fx-text-fill:white;-fx-font-size:12;-fx-font-weight:bold;" +
                    "-fx-padding:8 18;-fx-background-radius:10;-fx-cursor:hand;");
            detailBtn.setOnAction(e -> { e.consume(); showDetailsPopup(voyage, false); });

            actions.getChildren().addAll(evalBtn, archBtn, spAct, detailBtn);
        }

        body.getChildren().addAll(topRow, title, dateRow, pillRow, prog, sep, actions);

        HBox card = new HBox(0);
        String ns = "-fx-background-color:white;-fx-background-radius:18;" +
                "-fx-border-color:#EAEEF3;-fx-border-radius:18;-fx-border-width:1.5;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),12,0,0,4);-fx-cursor:hand;";
        card.setStyle(ns);
        card.setOnMouseClicked(e -> showDetailsPopup(voyage, isArchived));
        card.setOnMouseEntered(e -> card.setStyle(
                ns.replace("-fx-border-color:#EAEEF3;", "-fx-border-color:#FFCDB0;")
                        .replace("rgba(0,0,0,0.05)", "rgba(255,107,53,0.10)")));
        card.setOnMouseExited(e -> card.setStyle(ns));

        Rectangle clip = new Rectangle();
        clip.setArcWidth(36); clip.setArcHeight(36);
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        card.setClip(clip);

        card.getChildren().addAll(accent, body);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════
    // POPUP DÉTAILS — GLASSMORPHISM avec fond flouté
    // ══════════════════════════════════════════════════════════════════
    private void showDetailsPopup(Voyage voyage, boolean isArchived) {

        Stage owner = (Stage) voyagesContainer.getScene().getWindow();

        boolean complete = voyage.getIdVol() > 0 && voyage.getIdHotel() > 0;
        boolean partial  = (voyage.getIdVol() > 0 || voyage.getIdHotel() > 0) && !complete;
        long    days     = ChronoUnit.DAYS.between(voyage.getDateDebut(), voyage.getDateFin());
        int     pct      = complete ? 100 : partial ? 50 : 0;

        // ── 1. Snapshot de la scène principale → image floutée en fond ──
        Parent mainRoot = owner.getScene().getRoot();
        WritableImage snapshot = mainRoot.snapshot(new SnapshotParameters(), null);
        ImageView blurredBg = new ImageView(snapshot);
        blurredBg.setFitWidth(owner.getWidth());
        blurredBg.setFitHeight(owner.getHeight());
        blurredBg.setPreserveRatio(false);
        blurredBg.setEffect(new GaussianBlur(18)); // ← intensité du flou

        // Voile semi-transparent par-dessus le fond flouté
        Region dimLayer = new Region();
        dimLayer.setPrefSize(owner.getWidth(), owner.getHeight());
        dimLayer.setStyle("-fx-background-color: rgba(15, 20, 50, 0.45);");

        // ── 2. HEADER ──
        VBox header = new VBox(8);
        String headerGradient = isArchived
                ? "linear-gradient(to bottom right,#95A5A6,#7F8C8D)"
                : "linear-gradient(to bottom right,#FF6B35,#F7931E)";
        header.setStyle("-fx-background-color:" + headerGradient + ";" +
                "-fx-padding:20 24 0 24;");

        HBox hTop = new HBox(10); hTop.setAlignment(Pos.CENTER_LEFT);
        String bTx;
        if (isArchived) {
            bTx = "📦 Archivé";
        } else {
            bTx = complete ? "✅ Complet" : partial ? "🔄 En cours" : "⏳ À configurer";
        }
        Label popBadge = new Label(bTx);
        popBadge.setStyle("-fx-background-color:rgba(255,255,255,0.25);" +
                "-fx-border-color:rgba(255,255,255,0.4);-fx-border-width:1;-fx-border-radius:20;" +
                "-fx-background-radius:20;-fx-text-fill:white;" +
                "-fx-font-size:11;-fx-font-weight:bold;-fx-padding:4 12;");
        Region hSp = new Region(); HBox.setHgrow(hSp, Priority.ALWAYS);
        Button closeX = new Button("✕");
        closeX.setStyle("-fx-background-color:rgba(255,255,255,0.2);-fx-text-fill:white;" +
                "-fx-font-weight:bold;-fx-font-size:13;" +
                "-fx-border-color:rgba(255,255,255,0.3);-fx-border-width:1;" +
                "-fx-border-radius:8;-fx-background-radius:8;" +
                "-fx-pref-width:30;-fx-pref-height:30;-fx-cursor:hand;");
        hTop.getChildren().addAll(popBadge, hSp, closeX);

        Label popTitle = new Label("🧳 Voyage " + voyage.getRythme());
        popTitle.setStyle("-fx-font-size:21;-fx-font-weight:bold;-fx-text-fill:white;");
        Label popSub = new Label("Voyage #" + voyage.getIdVoyage() + "  ·  Rythme " + voyage.getRythme());
        popSub.setStyle("-fx-font-size:11;-fx-text-fill:rgba(255,255,255,0.8);");

        Region wave = new Region(); wave.setPrefHeight(22);
        wave.setStyle("-fx-background-color:#F8FAFC;-fx-background-radius:20 20 0 0;");
        header.getChildren().addAll(hTop, popTitle, popSub, wave);

        // ── 3. BODY scrollable ──
        VBox body = new VBox(14);
        body.setStyle("-fx-background-color:#F8FAFC;-fx-padding:4 24 16 24;");

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        ColumnConstraints col = new ColumnConstraints(); col.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col, col);
        grid.add(infoCell("📅 Départ",  voyage.getDateDebut().format(fmt), false), 0, 0);
        grid.add(infoCell("📅 Retour",  voyage.getDateFin().format(fmt),   false), 1, 0);
        grid.add(infoCell("⏱️ Durée",   days + " jours",                   true),  0, 1);
        grid.add(infoCell("🎒 Rythme",  voyage.getRythme(),                 false), 1, 1);

        // Barre de progression dynamique
        VBox progBox = new VBox(6);
        HBox pTop = new HBox(); pTop.setAlignment(Pos.CENTER_LEFT);
        Label pLbl = new Label("Progression de configuration");
        pLbl.setStyle("-fx-font-size:11;-fx-text-fill:#94A3B8;-fx-font-weight:bold;");
        Region pSp = new Region(); HBox.setHgrow(pSp, Priority.ALWAYS);
        Label pPct = new Label(pct + "%");
        pPct.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:#FF6B35;");
        pTop.getChildren().addAll(pLbl, pSp, pPct);

        StackPane barBg = new StackPane();
        barBg.setPrefHeight(10); barBg.setMinHeight(10);
        barBg.setStyle("-fx-background-color:#E8ECF2;-fx-background-radius:10;");
        StackPane barFill = new StackPane();
        barFill.setPrefHeight(10); barFill.setMinHeight(10);
        barFill.setStyle("-fx-background-color:" +
                (isArchived ? "linear-gradient(to right,#95A5A6,#7F8C8D)"
                        : (complete ? "linear-gradient(to right,#27AE60,#2ECC71)"
                        : partial ? "linear-gradient(to right,#FF6B35,#F7931E)"
                        : "#F39C12")) + ";-fx-background-radius:10;");
        barFill.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(barFill, Pos.CENTER_LEFT);
        barBg.widthProperty().addListener((obs, o, n) ->
                barFill.setPrefWidth(n.doubleValue() * pct / 100.0));
        barBg.getChildren().add(barFill);
        progBox.getChildren().addAll(pTop, barBg);

        HBox secRow = new HBox(10); secRow.setAlignment(Pos.CENTER_LEFT);
        Label secLbl = new Label("ÉTAT DE CONFIGURATION");
        secLbl.setStyle("-fx-font-size:10;-fx-font-weight:bold;-fx-text-fill:#B0B8C8;");
        Region secLine = new Region(); HBox.setHgrow(secLine, Priority.ALWAYS);
        secLine.setStyle("-fx-background-color:#EAEEF3;-fx-pref-height:1;-fx-max-height:1;");
        secRow.getChildren().addAll(secLbl, secLine);

        HBox volRow   = configRow("✈️", "Vol",   voyage.getIdVol()   > 0);
        HBox hotelRow = configRow("🏨", "Hôtel", voyage.getIdHotel() > 0);

        body.getChildren().addAll(grid, progBox, secRow, volRow, hotelRow);

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color:#F8FAFC;-fx-background:#F8FAFC;" +
                "-fx-border-color:transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // ── 4. BOUTONS fixes en bas ──
        HBox acts = new HBox(10);
        acts.setPadding(new Insets(14, 24, 20, 24));
        acts.setStyle("-fx-background-color:white;-fx-border-color:#EAEEF3;-fx-border-width:1 0 0 0;");

        // ── 7. Stage TRANSPARENT plein écran ──
        Stage popup = new Stage(StageStyle.TRANSPARENT);
        popup.initOwner(owner);

        if (isArchived) {
            Button unarchBtn = new Button("♻️ Désarchiver");
            unarchBtn.setStyle("-fx-background-color:#EBF5FB;-fx-text-fill:#3498DB;" +
                    "-fx-border-color:#BFDFFF;-fx-border-width:1.5;-fx-border-radius:10;" +
                    "-fx-background-radius:10;-fx-font-size:12;-fx-font-weight:bold;" +
                    "-fx-padding:11 0;-fx-cursor:hand;");
            HBox.setHgrow(unarchBtn, Priority.ALWAYS); unarchBtn.setMaxWidth(Double.MAX_VALUE);

            Button delBtn = new Button("🗑️ Supprimer");
            delBtn.setStyle("-fx-background-color:#FEF9F0;-fx-text-fill:#F39C12;" +
                    "-fx-border-color:#FFD699;-fx-border-width:1.5;-fx-border-radius:10;" +
                    "-fx-background-radius:10;-fx-font-size:12;-fx-font-weight:bold;" +
                    "-fx-padding:11 0;-fx-cursor:hand;");
            HBox.setHgrow(delBtn, Priority.ALWAYS); delBtn.setMaxWidth(Double.MAX_VALUE);

            Button cpBtn = new Button("✕ Fermer");
            cpBtn.setStyle("-fx-background-color:linear-gradient(to right,#FF6B35,#F7931E);" +
                    "-fx-text-fill:white;-fx-font-size:12;-fx-font-weight:bold;" +
                    "-fx-padding:11 0;-fx-background-radius:10;-fx-cursor:hand;" +
                    "-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.3),6,0,0,2);");
            HBox.setHgrow(cpBtn, Priority.ALWAYS); cpBtn.setMaxWidth(Double.MAX_VALUE);

            acts.getChildren().addAll(unarchBtn, delBtn, cpBtn);
        } else {
            Button ep = new Button("⭐ Évaluer");
            ep.setStyle("-fx-background-color:#FFF0E6;-fx-text-fill:#FF6B35;" +
                    "-fx-border-color:#FFDCCB;-fx-border-width:1.5;-fx-border-radius:10;" +
                    "-fx-background-radius:10;-fx-font-size:12;-fx-font-weight:bold;" +
                    "-fx-padding:11 0;-fx-cursor:hand;");
            HBox.setHgrow(ep, Priority.ALWAYS); ep.setMaxWidth(Double.MAX_VALUE);

            Button ap = new Button("📦 Archiver");
            ap.setStyle("-fx-background-color:#F1F5F9;-fx-text-fill:#64748B;" +
                    "-fx-border-color:#E2E8F0;-fx-border-width:1.5;-fx-border-radius:10;" +
                    "-fx-background-radius:10;-fx-font-size:12;-fx-font-weight:bold;" +
                    "-fx-padding:11 0;-fx-cursor:hand;");
            HBox.setHgrow(ap, Priority.ALWAYS); ap.setMaxWidth(Double.MAX_VALUE);

            Button cp = new Button("✕ Fermer");
            cp.setStyle("-fx-background-color:linear-gradient(to right,#FF6B35,#F7931E);" +
                    "-fx-text-fill:white;-fx-font-size:12;-fx-font-weight:bold;" +
                    "-fx-padding:11 0;-fx-background-radius:10;-fx-cursor:hand;" +
                    "-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.3),6,0,0,2);");
            HBox.setHgrow(cp, Priority.ALWAYS); cp.setMaxWidth(Double.MAX_VALUE);

            acts.getChildren().addAll(ep, ap, cp);
        }

        // ── 5. Carte popup (header + scroll + boutons) ──
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:22;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.30),50,0,0,12);");
        card.setPrefWidth(520);
        card.setMaxWidth(520);
        card.setPrefHeight(570);
        card.setMaxHeight(570);
        card.getChildren().addAll(header, scroll, acts);

        Rectangle cardClip = new Rectangle(520, 570);
        cardClip.setArcWidth(44); cardClip.setArcHeight(44);
        card.setClip(cardClip);

        // ── 6. Assemblage en couches : fond flouté + voile + carte centrée ──
        StackPane root = new StackPane();
        root.setPrefSize(owner.getWidth(), owner.getHeight());

        // Couche 1 : screenshot flouté
        root.getChildren().add(blurredBg);

        // Couche 2 : voile sombre
        root.getChildren().add(dimLayer);

        // Couche 3 : la carte popup centrée
        StackPane.setAlignment(card, Pos.CENTER);
        root.getChildren().add(card);

        // Clic sur le fond → fermer
        root.setOnMouseClicked(e -> {
            if (e.getTarget() == root || e.getTarget() == dimLayer) {
                closePopupWithAnimation(card, popup);
            }
        });

        // Maintenant définir les actions des boutons avec la popup correctement définie
        if (isArchived) {
            Button unarchBtn = (Button) acts.getChildren().get(0);
            Button delBtn = (Button) acts.getChildren().get(1);
            Button cpBtn = (Button) acts.getChildren().get(2);

            unarchBtn.setOnAction(e -> { closePopupWithAnimation(card, popup); unarchiveVoyage(voyage); });
            delBtn.setOnAction(e -> { closePopupWithAnimation(card, popup); deleteVoyage(voyage); });
            cpBtn.setOnAction(e -> closePopupWithAnimation(card, popup));
            closeX.setOnAction(e -> closePopupWithAnimation(card, popup));
        } else {
            Button ep = (Button) acts.getChildren().get(0);
            Button ap = (Button) acts.getChildren().get(1);
            Button cp = (Button) acts.getChildren().get(2);

            ep.setOnAction(e -> { closePopupWithAnimation(card, popup); navigateToAvis(voyage); });
            ap.setOnAction(e -> { closePopupWithAnimation(card, popup); archiveVoyage(voyage); });
            cp.setOnAction(e -> closePopupWithAnimation(card, popup));
            closeX.setOnAction(e -> closePopupWithAnimation(card, popup));
        }

        Scene scene = new Scene(root, owner.getWidth(), owner.getHeight());
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.setX(owner.getX());
        popup.setY(owner.getY());

        // ── 8. Animation d'ouverture (fade + scale) ──
        card.setOpacity(0);
        card.setScaleX(0.88);
        card.setScaleY(0.88);
        popup.show();

        FadeTransition fade = new FadeTransition(Duration.millis(220), card);
        fade.setFromValue(0); fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(220), card);
        scale.setFromX(0.88); scale.setToX(1.0);
        scale.setFromY(0.88); scale.setToY(1.0);

        new ParallelTransition(fade, scale).play();
    }

    // ── Animation de fermeture - CORRIGÉE ──
    private void closePopupWithAnimation(VBox card, Stage popup) {
        FadeTransition fade = new FadeTransition(Duration.millis(160), card);
        fade.setFromValue(1);
        fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(160), card);
        scale.setFromX(1.0);
        scale.setToX(0.90);
        scale.setFromY(1.0);
        scale.setToY(0.90);

        ParallelTransition pt = new ParallelTransition(fade, scale);
        pt.setOnFinished(e -> popup.close());  // ← Ferme la popup après l'animation
        pt.play();
    }

    // ══════════════════════════════════════════════════════════════════
    // HELPERS UI
    // ══════════════════════════════════════════════════════════════════
    private VBox infoCell(String label, String value, boolean accent) {
        VBox c = new VBox(5);
        c.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
                "-fx-border-color:#EAEEF3;-fx-border-radius:12;-fx-border-width:1;-fx-padding:12 14;");
        Label l = new Label(label);
        l.setStyle("-fx-font-size:10;-fx-text-fill:#B0B8C8;-fx-font-weight:bold;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:" +
                (accent ? "#FF6B35" : "#1E2433") + ";");
        c.getChildren().addAll(l, v);
        return c;
    }

    private HBox configRow(String icon, String name, boolean ok) {
        HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
                "-fx-border-color:#EAEEF3;-fx-border-radius:12;-fx-border-width:1;-fx-padding:12 14;");
        VBox.setMargin(row, new Insets(0, 0, 6, 0));

        StackPane iBox = new StackPane();
        iBox.setPrefSize(36, 36); iBox.setMinSize(36, 36);
        iBox.setStyle("-fx-background-color:" + (ok ? "#EAFAF1" : "#FEF9F0") + ";-fx-background-radius:10;");
        Label iLbl = new Label(icon); iLbl.setStyle("-fx-font-size:15;");
        iBox.getChildren().add(iLbl);

        VBox txt = new VBox(2); HBox.setHgrow(txt, Priority.ALWAYS);
        Label nm = new Label(name);
        nm.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:#1E2433;");
        Label st = new Label(ok ? "Sélectionné et confirmé" : "Non sélectionné");
        st.setStyle("-fx-font-size:11;-fx-text-fill:#94A3B8;");
        txt.getChildren().addAll(nm, st);

        Label chk = new Label(ok ? "✅" : "⚠️"); chk.setStyle("-fx-font-size:15;");
        row.getChildren().addAll(iBox, txt, chk);
        return row;
    }

    private Label statusPill(String text, boolean ok) {
        Label p = new Label(ok ? text + " ✓" : text + " ✗");
        p.setStyle("-fx-background-color:" + (ok ? "#EAFAF1" : "#FEF9F0") + ";" +
                "-fx-text-fill:" + (ok ? "#27AE60" : "#F39C12") + ";" +
                "-fx-padding:5 12;-fx-background-radius:20;-fx-font-size:11;-fx-font-weight:bold;");
        return p;
    }

    private VBox progressBar(int pct, boolean complete) {
        VBox box = new VBox(5);
        HBox top = new HBox(); top.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label("Configuration"); l.setStyle("-fx-font-size:10;-fx-text-fill:#B0B8C8;");
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        String pc = complete ? "#27AE60" : pct > 0 ? "#FF6B35" : "#F39C12";
        Label p = new Label(pct + "%"); p.setStyle("-fx-font-size:11;-fx-font-weight:bold;-fx-text-fill:" + pc + ";");
        top.getChildren().addAll(l, s, p);

        StackPane bg = new StackPane(); bg.setPrefHeight(5); bg.setMinHeight(5);
        bg.setStyle("-fx-background-color:#F1F5F9;-fx-background-radius:10;");
        StackPane fill = new StackPane(); fill.setPrefHeight(5); fill.setMinHeight(5);
        fill.setStyle("-fx-background-color:" +
                (complete ? "linear-gradient(to right,#27AE60,#2ECC71)"
                        : pct > 0 ? "linear-gradient(to right,#FF6B35,#F7931E)"
                        : "#F39C12") + ";-fx-background-radius:10;");
        fill.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        bg.widthProperty().addListener((obs, o, n) ->
                fill.setPrefWidth(n.doubleValue() * pct / 100.0));
        bg.getChildren().add(fill);
        box.getChildren().addAll(top, bg);
        return box;
    }

    private Button styledBtn(String text, String bg, String fg,
                             String border, String hoverBg, String hoverFg) {
        Button b = new Button(text);
        String base = String.format(
                "-fx-background-color:%s;-fx-text-fill:%s;-fx-border-color:%s;" +
                        "-fx-border-width:1.5;-fx-border-radius:9;-fx-background-radius:10;" +
                        "-fx-font-size:12;-fx-font-weight:bold;-fx-padding:8 14;-fx-cursor:hand;",
                bg, fg, border);
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(base
                .replace("background-color:" + bg, "background-color:" + hoverBg)
                .replace("text-fill:" + fg,         "text-fill:" + hoverFg)));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    private void displayEmptyState(String title, String subtitle) {
        VBox e = new VBox(16); e.setAlignment(Pos.CENTER); e.setPadding(new Insets(60));
        e.setStyle("-fx-background-color:white;-fx-background-radius:18;-fx-border-color:#EAEEF3;" +
                "-fx-border-radius:18;-fx-border-width:2;-fx-border-style:dashed;");
        Label i = new Label("✈️"); i.setStyle("-fx-font-size:56;-fx-opacity:0.2;");
        Label t = new Label(title); t.setStyle("-fx-font-size:18;-fx-font-weight:bold;-fx-text-fill:#CCC;");
        Label d = new Label(subtitle); d.setStyle("-fx-font-size:13;-fx-text-fill:#AAA;");
        e.getChildren().addAll(i, t, d);
        voyagesContainer.getChildren().add(e);
    }

    // ══════════════════════════════════════════════════════════════════
    // NAVIGATION & ACTIONS
    // ══════════════════════════════════════════════════════════════════
    private void navigateToAvis(Voyage voyage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AvisUser.fxml"));
            Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl != null) {
                try {
                    ctrl.getClass().getMethod("setVoyage", Voyage.class).invoke(ctrl, voyage);
                } catch (NoSuchMethodException ignored) {
                } catch (Exception ex) { ex.printStackTrace(); }
            }
            Stage stage = (Stage) voyagesContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase - Évaluer ce voyage");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir /Avis/AjouterAvis.fxml\n" + e.getMessage());
        }
    }

    private void archiveVoyage(Voyage voyage) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Archiver le voyage");
        confirm.setHeaderText("📦 Archiver ce voyage ?");
        confirm.setContentText("Le voyage #" + voyage.getIdVoyage() + " — " + voyage.getRythme() + " sera archivé.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    serviceVoyage.archiverVoyage(voyage.getIdVoyage());
                    loadData();
                    showAlert("✅ Archivé", "Le voyage #" + voyage.getIdVoyage() + " a été archivé.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Erreur", "Impossible d'archiver : " + ex.getMessage());
                }
            }
        });
    }

    private void unarchiveVoyage(Voyage voyage) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Désarchiver le voyage");
        confirm.setHeaderText("♻️ Désarchiver ce voyage ?");
        confirm.setContentText("Le voyage #" + voyage.getIdVoyage() + " — " + voyage.getRythme() + " sera restauré.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    serviceVoyage.desarchiverVoyage(voyage.getIdVoyage());
                    if (isActiveTabSelected) {
                        loadActiveVoyages();
                    } else {
                        loadArchivedVoyages();
                    }
                    showAlert("✅ Restauré", "Le voyage #" + voyage.getIdVoyage() + " a été restauré.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Erreur", "Impossible de désarchiver : " + ex.getMessage());
                }
            }
        });
    }

    private void deleteVoyage(Voyage voyage) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le voyage");
        confirm.setHeaderText("⚠️ Supprimer définitivement ?");
        confirm.setContentText("Cette action est irréversible. Le voyage #" + voyage.getIdVoyage() + " sera supprimé.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    serviceVoyage.supprimerVoyage(voyage.getIdVoyage());
                    loadArchivedVoyages();
                    showAlert("✅ Supprimé", "Le voyage a été supprimé définitivement.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Erreur", "Impossible de supprimer : " + ex.getMessage());
                }
            }
        });
    }

    private void navigateToConfigVoyage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ConfigurerVoyage/ConfigVoyageUser.fxml"));
            Stage stage = (Stage) newVoyageButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase - Créer un voyage");
            stage.show();
        } catch (IOException e) { showAlert("Erreur", e.getMessage()); }
    }

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion"); confirm.setHeaderText("Êtes-vous sûr ?");
        confirm.setContentText("Voulez-vous vraiment vous déconnecter ?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) { SessionManager.clearSession(); redirectToLogin(); }
        });
    }

    private void redirectToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase - Connexion");
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void retourDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/DashboardUser.fxml")
            );
            Parent root = loader.load();

            // ✅ CRUCIAL : Récupérer le contrôleur et passer l'utilisateur connecté
            DashboardUserController controller = loader.getController();
            Utilisateur utilisateurConnecte = SessionManager.getCurrentUser();

            if (utilisateurConnecte != null) {
                controller.setUtilisateur(utilisateurConnecte);
            } else {
                showAlert("Erreur", "Session expirée. Veuillez vous reconnecter.");
                redirectToLogin();
                return;
            }

            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase - Dashboard");
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de revenir au dashboard : " + e.getMessage());
            e.printStackTrace();
        }
    }
}