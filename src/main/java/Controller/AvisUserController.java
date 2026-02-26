package Controller;

import Entite.Avis;
import Entite.Utilisateur;
import Entite.Voyage;
import Service.ServiceAvis;
import Service.ServiceVoyage;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.util.StringConverter;
import java.sql.SQLException;
import java.util.List;

public class AvisUserController {

    // ─── FXML Bindings ────────────────────────────────────────────────────────
    @FXML private Button btnAnnuler, btnPublier;
    @FXML private Label lblRating, lblCharCount;
    @FXML private Label lblHebergement, lblTransport, lblActivites, lblQualitePrix;
    @FXML private Button star1, star2, star3, star4, star5;
    @FXML private Slider sliderHebergement, sliderTransport, sliderActivites, sliderQualitePrix;
    @FXML private TextField txtTitre;
    @FXML private TextArea txtCommentaire;
    @FXML private ToggleButton btnOui, btnNon;

    // ── New : user info labels & voyage selector ───────────────────────────────
    @FXML private Label lblUserName;       // e.g. "Bonjour, alice@mail.com"
    @FXML private Label lblUserAvatar;     // initiale de l'email
    @FXML private ComboBox<Voyage> cmbVoyage;  // sélecteur de voyage

    // ─── State ────────────────────────────────────────────────────────────────
    private int currentRating = 0;
    private ToggleGroup recommendationGroup;

    /** Utilisateur actuellement connecté – injecté depuis l'écran de connexion */
    private Utilisateur utilisateurConnecte;

    // ─── Services ─────────────────────────────────────────────────────────────
    private final ServiceAvis    serviceAvis    = new ServiceAvis();
    private final ServiceVoyage  serviceVoyage  = new ServiceVoyage();

    // ══════════════════════════════════════════════════════════════════════════
    //  INJECTION PUBLIQUE  (appelée depuis le contrôleur précédent)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Point d'entrée principal.
     * Appelez cette méthode juste après le chargement du FXML :
     * <pre>
     *   AvisUserController ctrl = loader.getController();
     *   ctrl.setUtilisateur(userConnecte);
     * </pre>
     */
    public void setUtilisateur(Utilisateur user) {
        this.utilisateurConnecte = user;
        refreshUserInfo();
        loadVoyages();
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

    // ──────────────────────────────────────────────────────────────────────────
    //  User info
    // ──────────────────────────────────────────────────────────────────────────

    private void refreshUserInfo() {
        if (utilisateurConnecte == null) return;
        String email = utilisateurConnecte.getEmail();

        // Initiale pour l'avatar
        lblUserAvatar.setText(String.valueOf(email.charAt(0)).toUpperCase());

        // Nom court
        String displayName = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        lblUserName.setText("Bonjour, " + displayName);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Voyage ComboBox
    // ──────────────────────────────────────────────────────────────────────────

    private void setupVoyageComboBox() {
        // Affichage : "Voyage #3 — Paris → Rome (7 jours)"
        cmbVoyage.setConverter(new StringConverter<Voyage>() {
            @Override
            public String toString(Voyage v) {
                if (v == null) return "Sélectionnez votre voyage...";
                return String.format("Voyage #%d  ·  %s → %s  (%d jours)",
                        v.getIdVoyage(),
                        v.getDateDebut(),
                        v.getDateFin(),
                        v.getDuree());
            }
            @Override
            public Voyage fromString(String s) { return null; }
        });

        // Placeholder
        cmbVoyage.setPromptText("Sélectionnez votre voyage...");
    }

    private void loadVoyages() {
        try {
            List<Voyage> voyages = serviceVoyage.readAll();
            cmbVoyage.getItems().setAll(voyages);
            if (!voyages.isEmpty()) cmbVoyage.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            showError("Impossible de charger les voyages : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PUBLICATION
    // ══════════════════════════════════════════════════════════════════════════

    private void handlePublish() {
        if (!validateForm()) return;

        try {
            Avis avis = new Avis();
            avis.setIdUtilisateur(utilisateurConnecte != null
                    ? utilisateurConnecte.getIdUtilisateur()
                    : 1);                                         // fallback dev
            avis.setIdVoyage(cmbVoyage.getValue().getIdVoyage());
            avis.setNote(currentRating);
            avis.setTitre(txtTitre.getText().trim());
            avis.setCommentaire(txtCommentaire.getText().trim());
            avis.setNoteHebergement((int) sliderHebergement.getValue());
            avis.setNoteTransport((int)  sliderTransport.getValue());
            avis.setNoteActivites((int)  sliderActivites.getValue());
            avis.setNoteQualitePrix((int) sliderQualitePrix.getValue());
            avis.setRecommande(btnOui.isSelected());
            avis.setDateAvis(new java.sql.Date(System.currentTimeMillis()));

            if (serviceAvis.ajouter(avis)) {
                showSuccess();
                resetForm();
            } else {
                showError("Une erreur est survenue lors de la publication de votre avis.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de connexion à la base de données : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  VALIDATION
    // ══════════════════════════════════════════════════════════════════════════

    private boolean validateForm() {
        StringBuilder err = new StringBuilder();

        if (utilisateurConnecte == null)
            err.append("• Aucun utilisateur connecté\n");

        if (cmbVoyage.getValue() == null)
            err.append("• Veuillez sélectionner un voyage\n");

        if (currentRating == 0)
            err.append("• Veuillez sélectionner une note (étoiles)\n");

        if (txtTitre.getText().trim().isEmpty())
            err.append("• Le titre de l'avis est obligatoire\n");

        if (txtCommentaire.getText().trim().isEmpty())
            err.append("• Le commentaire est obligatoire\n");

        if (txtCommentaire.getText().length() > 1000)
            err.append("• Le commentaire ne doit pas dépasser 1000 caractères\n");

        if (recommendationGroup.getSelectedToggle() == null)
            err.append("• Veuillez indiquer si vous recommandez cette destination\n");

        if (err.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⚠ Formulaire incomplet");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(err.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private void showSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("✓ Merci !");
        alert.setHeaderText("Votre avis a été publié !");
        Voyage v = cmbVoyage.getValue();
        String voyageInfo = v != null ? " pour le Voyage #" + v.getIdVoyage() : "";
        alert.setContentText("Votre avis" + voyageInfo + " a bien été enregistré.\nIl aidera d'autres voyageurs.");
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("✗ Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Star rating
    // ──────────────────────────────────────────────────────────────────────────

    private void setupStarRating() {
        Button[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            stars[i].setOnAction(e -> { setRating(rating); animateButton(stars[rating - 1]); });
            stars[i].setOnMouseEntered(e -> highlightStars(rating));
            stars[i].setOnMouseExited(e  -> highlightStars(currentRating));
        }
    }

    private void setRating(int rating) {
        currentRating = rating;
        highlightStars(rating);
        if (rating == 0) {
            lblRating.setText("Sélectionnez une note");
            lblRating.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        } else {
            lblRating.setText(rating + "/5 étoiles");
            lblRating.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FF7F50;");
        }
    }

    private void highlightStars(int rating) {
        Button[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            String color = (i < rating) ? "#FF7F50" : "#ddd";
            stars[i].setStyle("-fx-background-color: transparent; -fx-text-fill: " + color + "; -fx-font-size: 50; -fx-cursor: hand;");
        }
    }

    private void animateButton(Button button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(1.3);   st.setToY(1.3);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Sliders
    // ──────────────────────────────────────────────────────────────────────────

    private void setupSliders() {
        setupSlider(sliderHebergement, lblHebergement, "hebergement");
        setupSlider(sliderTransport,   lblTransport,   "transport");
        setupSlider(sliderActivites,   lblActivites,   "activites");
        setupSlider(sliderQualitePrix, lblQualitePrix, "qualiteprix");
    }

    private void setupSlider(Slider slider, Label label, String category) {
        label.setText("0/5");
        slider.getStyleClass().addAll("modern-slider", "slider-" + category);
        slider.setMin(0); slider.setMax(5); slider.setValue(0);
        slider.setBlockIncrement(1); slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0); slider.setSnapToTicks(true);
        slider.setShowTickMarks(false); slider.setShowTickLabels(false);
        slider.valueProperty().addListener((obs, o, n) -> label.setText(n.intValue() + "/5"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Recommendation toggles
    // ──────────────────────────────────────────────────────────────────────────

    private void setupRecommendationButtons() {
        recommendationGroup = new ToggleGroup();
        btnOui.setToggleGroup(recommendationGroup);
        btnNon.setToggleGroup(recommendationGroup);

        btnOui.selectedProperty().addListener((obs, o, sel) ->
                btnOui.setStyle(sel
                        ? "-fx-font-size:15px;-fx-padding:15 40;-fx-background-color:#10b981;-fx-border-color:#10b981;-fx-border-width:2;-fx-border-radius:10;-fx-background-radius:10;-fx-text-fill:white;-fx-font-weight:bold;-fx-cursor:hand;"
                        : "-fx-font-size:15px;-fx-padding:15 40;-fx-background-color:white;-fx-border-color:#10b981;-fx-border-width:2;-fx-border-radius:10;-fx-background-radius:10;-fx-text-fill:#10b981;-fx-font-weight:bold;-fx-cursor:hand;"));

        btnNon.selectedProperty().addListener((obs, o, sel) ->
                btnNon.setStyle(sel
                        ? "-fx-font-size:15px;-fx-padding:15 40;-fx-background-color:#ef4444;-fx-border-color:#ef4444;-fx-border-width:2;-fx-border-radius:10;-fx-background-radius:10;-fx-text-fill:white;-fx-font-weight:bold;-fx-cursor:hand;"
                        : "-fx-font-size:15px;-fx-padding:15 40;-fx-background-color:white;-fx-border-color:#ef4444;-fx-border-width:2;-fx-border-radius:10;-fx-background-radius:10;-fx-text-fill:#ef4444;-fx-font-weight:bold;-fx-cursor:hand;"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Character counter
    // ──────────────────────────────────────────────────────────────────────────

    private void setupCharacterCounter() {
        txtCommentaire.textProperty().addListener((obs, o, n) -> {
            int len = n.length();
            lblCharCount.setText(len + "/1000");
            if (len > 1000)
                lblCharCount.setStyle("-fx-font-size:12px;-fx-text-fill:#ef4444;-fx-font-weight:bold;");
            else if (len > 800)
                lblCharCount.setStyle("-fx-font-size:12px;-fx-text-fill:#f59e0b;");
            else
                lblCharCount.setStyle("-fx-font-size:12px;-fx-text-fill:#95a5a6;");
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Action buttons
    // ──────────────────────────────────────────────────────────────────────────

    private void setupActionButtons() {
        btnPublier.setOnAction(e -> handlePublish());
        btnAnnuler.setOnAction(e -> clearForm());
    }

    private void clearForm() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Annuler votre avis ?");
        confirmation.setContentText("Êtes-vous sûr ? Toutes les informations saisies seront perdues.");

        ButtonType btnConfirmer = new ButtonType("Oui, annuler",  ButtonBar.ButtonData.OK_DONE);
        ButtonType btnRetour    = new ButtonType("Non, continuer", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmation.getButtonTypes().setAll(btnConfirmer, btnRetour);

        confirmation.showAndWait().ifPresent(r -> { if (r == btnConfirmer) resetForm(); });
    }

    private void resetForm() {
        txtTitre.clear();
        txtCommentaire.clear();
        setRating(0);
        sliderHebergement.setValue(0);
        sliderTransport.setValue(0);
        sliderActivites.setValue(0);
        sliderQualitePrix.setValue(0);
        recommendationGroup.selectToggle(null);
        lblHebergement.setText("0/5");
        lblTransport.setText("0/5");
        lblActivites.setText("0/5");
        lblQualitePrix.setText("0/5");
        // NB: on conserve le voyage et l'utilisateur
    }
}