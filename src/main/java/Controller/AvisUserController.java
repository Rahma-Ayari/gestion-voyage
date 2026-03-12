package Controller;

import Entite.Avis;
import Entite.Utilisateur;
import Entite.Voyage;
import Service.ServiceAvis;
import Service.ServiceUtilisateur;
import Service.ServiceVoyage;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import javafx.util.StringConverter;
import java.sql.SQLException;

public class AvisUserController {

    // ── FXML ──────────────────────────────────────────────────────────────────
    @FXML private Button       btnAnnuler, btnPublier;
    @FXML private Label        lblRating, lblCharCount;
    @FXML private Label        lblHebergement, lblTransport,
            lblActivites, lblQualitePrix;
    @FXML private Button       star1, star2, star3, star4, star5;
    @FXML private Slider       sliderHebergement, sliderTransport,
            sliderActivites, sliderQualitePrix;
    @FXML private TextField    txtTitre;
    @FXML private TextArea     txtCommentaire;
    @FXML private ToggleButton btnOui, btnNon;
    @FXML private Label        lblUserName, lblUserAvatar;
    @FXML private ComboBox<Voyage> cmbVoyage;
    @FXML private Button btnRetour;

    // ── Navigation ────────────────────────────────────────────────────────────
    private javafx.stage.Stage stage;

    public void setStage(javafx.stage.Stage stage) {
        this.stage = stage;
    }

    // ── État ──────────────────────────────────────────────────────────────────
    private int         currentRating = 0;
    private ToggleGroup recommendationGroup;
    private Utilisateur utilisateurConnecte;

    // ── Services ──────────────────────────────────────────────────────────────
    private final ServiceAvis        serviceAvis   = new ServiceAvis();
    private final ServiceVoyage      serviceVoyage = new ServiceVoyage();
    private final ServiceUtilisateur serviceUser   = new ServiceUtilisateur();

    // ══════════════════════════════════════════════════════════════════════════
    //  POINT D'ENTRÉE
    // ══════════════════════════════════════════════════════════════════════════

    private void handleRetour() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/DashboardUser.fxml"));
            javafx.scene.Parent root = loader.load();

            // ✅ CRUCIAL : Passer l'utilisateur au contrôleur du Dashboard
            DashboardUserController ctrl = loader.getController();
            ctrl.setUtilisateur(utilisateurConnecte);  // ← Transférer la session

            // Récupérer le stage depuis le bouton retour
            javafx.stage.Stage currentStage =
                    (javafx.stage.Stage) btnRetour.getScene().getWindow();

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            currentStage.setScene(scene);
            currentStage.show();

        } catch (Exception e) {
            afficherErreur("Impossible de revenir : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUtilisateur(Utilisateur user) {
        this.utilisateurConnecte = user;
        afficherInfosUser();
        chargerVoyageUtilisateur();   // ← vérifie etat et charge le bon voyage
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  INITIALISATION FXML
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        setupStarRating();
        setupSliders();
        setupCharacterCounter();
        setupRecommendationButtons();
        setupActionButtons();
        setupVoyageComboBox();
        setRating(0);
    }

    // ── Header utilisateur ────────────────────────────────────────────────────

    private void afficherInfosUser() {
        if (utilisateurConnecte == null) return;
        String email = utilisateurConnecte.getEmail();
        lblUserAvatar.setText(String.valueOf(email.charAt(0)).toUpperCase());
        String nom = email.contains("@")
                ? email.substring(0, email.indexOf('@')) : email;
        lblUserName.setText("Bonjour, " + nom);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LOGIQUE PRINCIPALE : charger le voyage selon l'état
    // ══════════════════════════════════════════════════════════════════════════

    private void chargerVoyageUtilisateur() {
        if (utilisateurConnecte == null) return;

        try {
            // Relire depuis la BD pour avoir l'état le plus récent
            Utilisateur uFrais =
                    serviceUser.findbyId(utilisateurConnecte.getIdUtilisateur());

            if (uFrais == null) {
                afficherErreur("Utilisateur introuvable.");
                return;
            }

            if ("done".equals(uFrais.getEtat())) {

                // ✅ Voyage terminé → charger le voyage via id_voyage
                Voyage voyage = serviceVoyage.findbyId(uFrais.getIdVoyage());

                if (voyage != null) {
                    cmbVoyage.getItems().setAll(voyage);
                    cmbVoyage.getSelectionModel().selectFirst();
                    cmbVoyage.setDisable(true); // 1 seul voyage, pas modifiable
                } else {
                    bloquerFormulaire(
                            "Voyage introuvable (id=" + uFrais.getIdVoyage() + ").");
                }

            } else {
                // ❌ Voyage pas encore terminé → bloquer tout
                bloquerFormulaire(
                        "Votre voyage n'est pas encore terminé.\n"
                                + "Revenez ici après votre retour pour laisser votre avis ✈");
            }

        } catch (SQLException e) {
            afficherErreur("Erreur BD : " + e.getMessage());
        }
    }

    // ── Bloquer tout le formulaire ────────────────────────────────────────────

    private void bloquerFormulaire(String message) {
        cmbVoyage.setDisable(true);
        cmbVoyage.setPromptText("Voyage non disponible");
        btnPublier.setDisable(true);
        star1.setDisable(true); star2.setDisable(true);
        star3.setDisable(true); star4.setDisable(true); star5.setDisable(true);
        sliderHebergement.setDisable(true);
        sliderTransport  .setDisable(true);
        sliderActivites  .setDisable(true);
        sliderQualitePrix.setDisable(true);
        txtTitre        .setDisable(true);
        txtCommentaire  .setDisable(true);
        btnOui.setDisable(true);
        btnNon.setDisable(true);

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("⏳ Pas encore disponible");
        info.setHeaderText("Vous ne pouvez pas encore laisser un avis");
        info.setContentText(message);
        info.showAndWait();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PUBLICATION
    // ══════════════════════════════════════════════════════════════════════════

    private void handlePublish() {
        if (!validerFormulaire()) return;

        try {
            Avis avis = new Avis();
            avis.setIdUtilisateur(utilisateurConnecte.getIdUtilisateur());
            avis.setIdVoyage(cmbVoyage.getValue().getIdVoyage());
            avis.setNote(currentRating);
            avis.setTitre(txtTitre.getText().trim());
            avis.setCommentaire(txtCommentaire.getText().trim());
            avis.setNoteHebergement((int) sliderHebergement.getValue());
            avis.setNoteTransport  ((int) sliderTransport.getValue());
            avis.setNoteActivites  ((int) sliderActivites.getValue());
            avis.setNoteQualitePrix((int) sliderQualitePrix.getValue());
            avis.setRecommande(btnOui.isSelected());
            avis.setDateAvis(new java.sql.Date(System.currentTimeMillis()));

            if (serviceAvis.ajouter(avis)) {
                afficherSucces();
                resetFormulaire();
            } else {
                afficherErreur("Erreur lors de la publication de votre avis.");
            }
        } catch (SQLException e) {
            afficherErreur("Erreur BD : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  VALIDATION
    // ══════════════════════════════════════════════════════════════════════════

    private boolean validerFormulaire() {
        StringBuilder err = new StringBuilder();

        if (utilisateurConnecte == null)
            err.append("• Aucun utilisateur connecté\n");
        if (cmbVoyage.getValue() == null)
            err.append("• Aucun voyage sélectionné\n");
        if (currentRating == 0)
            err.append("• Veuillez sélectionner une note (étoiles)\n");
        if (txtTitre.getText().trim().isEmpty())
            err.append("• Le titre est obligatoire\n");
        if (txtCommentaire.getText().trim().isEmpty())
            err.append("• Le commentaire est obligatoire\n");
        if (txtCommentaire.getText().length() > 1000)
            err.append("• Le commentaire dépasse 1000 caractères\n");
        if (recommendationGroup.getSelectedToggle() == null)
            err.append("• Veuillez indiquer si vous recommandez\n");

        if (err.length() > 0) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("⚠ Formulaire incomplet");
            a.setHeaderText("Veuillez corriger :");
            a.setContentText(err.toString());
            a.showAndWait();
            return false;
        }
        return true;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SETUP UI
    // ══════════════════════════════════════════════════════════════════════════

    private void setupVoyageComboBox() {
        cmbVoyage.setConverter(new StringConverter<>() {
            @Override
            public String toString(Voyage v) {
                if (v == null) return "Sélectionnez votre voyage...";
                return String.format("Voyage #%d  ·  %s → %s  (%d jours)",
                        v.getIdVoyage(), v.getDateDebut(),
                        v.getDateFin(),  v.getDuree());
            }
            @Override public Voyage fromString(String s) { return null; }
        });
    }

    private void setupStarRating() {
        Button[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            final int r = i + 1;
            stars[i].setOnAction    (e -> { setRating(r); animateButton(stars[r-1]); });
            stars[i].setOnMouseEntered(e -> highlightStars(r));
            stars[i].setOnMouseExited (e -> highlightStars(currentRating));
        }
    }

    private void setRating(int rating) {
        currentRating = rating;
        highlightStars(rating);
        if (rating == 0) {
            lblRating.setText("Cliquez pour noter");
            lblRating.setStyle(
                    "-fx-font-size:13.5px;-fx-font-weight:bold;-fx-text-fill:#C0A090;");
        } else {
            lblRating.setText(rating + " / 5 étoiles");
            lblRating.setStyle(
                    "-fx-font-size:13.5px;-fx-font-weight:bold;-fx-text-fill:#FF7F50;");
        }
    }

    private void highlightStars(int rating) {
        Button[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            String c = (i < rating) ? "#FF7F50" : "#E8D8D0";
            stars[i].setStyle("-fx-background-color:transparent;-fx-text-fill:" + c
                    + ";-fx-font-size:46;-fx-cursor:hand;-fx-padding:2;");
        }
    }

    private void animateButton(Button btn) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(1.3);   st.setToY(1.3);
        st.setAutoReverse(true); st.setCycleCount(2); st.play();
    }

    private void setupSliders() {
        setupSlider(sliderHebergement, lblHebergement);
        setupSlider(sliderTransport,   lblTransport);
        setupSlider(sliderActivites,   lblActivites);
        setupSlider(sliderQualitePrix, lblQualitePrix);
    }

    private void setupSlider(Slider slider, Label label) {
        slider.setMin(0); slider.setMax(5); slider.setValue(0);
        slider.setBlockIncrement(1); slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0); slider.setSnapToTicks(true);
        label.setText("0/5");
        slider.valueProperty().addListener(
                (obs, o, n) -> label.setText(n.intValue() + "/5"));
    }

    private void setupRecommendationButtons() {
        recommendationGroup = new ToggleGroup();
        btnOui.setToggleGroup(recommendationGroup);
        btnNon.setToggleGroup(recommendationGroup);

        btnOui.selectedProperty().addListener((obs, o, sel) ->
                btnOui.setStyle(sel
                        ? "-fx-font-size:14px;-fx-padding:12 34;-fx-background-color:#10b981;"
                        + "-fx-border-color:#10b981;-fx-border-width:2;-fx-border-radius:12;"
                        + "-fx-background-radius:12;-fx-text-fill:white;"
                        + "-fx-font-weight:bold;-fx-cursor:hand;"
                        : "-fx-font-size:14px;-fx-padding:12 34;-fx-background-color:white;"
                        + "-fx-border-color:#10b981;-fx-border-width:2;-fx-border-radius:12;"
                        + "-fx-background-radius:12;-fx-text-fill:#10b981;"
                        + "-fx-font-weight:bold;-fx-cursor:hand;"));

        btnNon.selectedProperty().addListener((obs, o, sel) ->
                btnNon.setStyle(sel
                        ? "-fx-font-size:14px;-fx-padding:12 34;-fx-background-color:#ef4444;"
                        + "-fx-border-color:#ef4444;-fx-border-width:2;-fx-border-radius:12;"
                        + "-fx-background-radius:12;-fx-text-fill:white;"
                        + "-fx-font-weight:bold;-fx-cursor:hand;"
                        : "-fx-font-size:14px;-fx-padding:12 34;-fx-background-color:white;"
                        + "-fx-border-color:#ef4444;-fx-border-width:2;-fx-border-radius:12;"
                        + "-fx-background-radius:12;-fx-text-fill:#ef4444;"
                        + "-fx-font-weight:bold;-fx-cursor:hand;"));
    }

    private void setupCharacterCounter() {
        txtCommentaire.textProperty().addListener((obs, o, n) -> {
            int len = n.length();
            lblCharCount.setText(len + "/1000");
            if      (len > 1000) lblCharCount.setStyle(
                    "-fx-font-size:11px;-fx-text-fill:#ef4444;-fx-font-weight:bold;");
            else if (len > 800)  lblCharCount.setStyle(
                    "-fx-font-size:11px;-fx-text-fill:#f59e0b;");
            else                 lblCharCount.setStyle(
                        "-fx-font-size:11px;-fx-text-fill:#95a5a6;");
        });
    }

    private void setupActionButtons() {
        btnPublier .setOnAction(e -> handlePublish());
        btnAnnuler .setOnAction(e -> demanderAnnulation());
        btnRetour.setOnAction(e -> handleRetour());

    }

    private void demanderAnnulation() {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Annuler ?");
        c.setHeaderText("Êtes-vous sûr ?");
        c.setContentText("Toutes les informations saisies seront perdues.");
        ButtonType oui = new ButtonType("Oui, annuler",   ButtonBar.ButtonData.OK_DONE);
        ButtonType non = new ButtonType("Non, continuer", ButtonBar.ButtonData.CANCEL_CLOSE);
        c.getButtonTypes().setAll(oui, non);
        c.showAndWait().ifPresent(r -> { if (r == oui) resetFormulaire(); });
    }

    private void resetFormulaire() {
        txtTitre.clear();
        txtCommentaire.clear();
        setRating(0);
        sliderHebergement.setValue(0);
        sliderTransport  .setValue(0);
        sliderActivites  .setValue(0);
        sliderQualitePrix.setValue(0);
        recommendationGroup.selectToggle(null);
    }

    // ── Alertes ───────────────────────────────────────────────────────────────

    private void afficherSucces() {
        Voyage v = cmbVoyage.getValue();
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("✓ Merci !");
        a.setHeaderText("Votre avis a été publié !");
        a.setContentText("Merci pour votre retour sur le Voyage #"
                + (v != null ? v.getIdVoyage() : "?") + ".\n"
                + "Il aidera d'autres voyageurs ✈");
        a.showAndWait();
    }

    private void afficherErreur(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("✗ Erreur"); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }
}