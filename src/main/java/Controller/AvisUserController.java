package Controller;

import Entite.Avis;
import Service.ServiceAvis;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.layout.Region;
import java.sql.SQLException;

public class AvisUserController {

    @FXML private Button btnAnnuler, btnPublier;
    @FXML private Label lblRating, lblCharCount;
    @FXML private Label lblHebergement, lblTransport, lblActivites, lblQualitePrix;
    @FXML private Button star1, star2, star3, star4, star5;
    @FXML private Slider sliderHebergement, sliderTransport, sliderActivites, sliderQualitePrix;
    @FXML private TextField txtTitre;
    @FXML private TextArea txtCommentaire;
    @FXML private ToggleButton btnOui, btnNon;

    private int currentRating = 0;
    private ToggleGroup recommendationGroup;
    private ServiceAvis serviceAvis = new ServiceAvis();

    @FXML
    public void initialize() {
        setupStarRating();
        setupSliders();
        setupCharacterCounter();
        setupRecommendationButtons();
        setupActionButtons();
        setRating(0);
    }

    private void setupSliders() {
        setupSlider(sliderHebergement, lblHebergement, "hebergement");
        setupSlider(sliderTransport, lblTransport, "transport");
        setupSlider(sliderActivites, lblActivites, "activites");
        setupSlider(sliderQualitePrix, lblQualitePrix, "qualiteprix");
    }

    private void setupSlider(Slider slider, Label label, String category) {
        // Initialiser la valeur
        label.setText("0/5");

        // Ajouter les classes CSS
        slider.getStyleClass().add("modern-slider");
        slider.getStyleClass().add("slider-" + category);

        // Configuration du slider
        slider.setMin(0);
        slider.setMax(5);
        slider.setValue(0);
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setShowTickMarks(false);
        slider.setShowTickLabels(false);

        // Listener pour mettre à jour le label
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            label.setText(value + "/5");
        });
    }

    private void setupStarRating() {
        Button[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            stars[i].setOnAction(e -> {
                setRating(rating);
                animateButton(stars[rating - 1]);
            });
            stars[i].setOnMouseEntered(e -> highlightStars(rating));
            stars[i].setOnMouseExited(e -> highlightStars(currentRating));
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
            if (i < rating) {
                stars[i].setStyle("-fx-background-color: transparent; -fx-text-fill: #FF7F50; -fx-font-size: 50; -fx-cursor: hand;");
            } else {
                stars[i].setStyle("-fx-background-color: transparent; -fx-text-fill: #ddd; -fx-font-size: 50; -fx-cursor: hand;");
            }
        }
    }

    private void animateButton(Button button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.3);
        st.setToY(1.3);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    private void setupRecommendationButtons() {
        recommendationGroup = new ToggleGroup();
        btnOui.setToggleGroup(recommendationGroup);
        btnNon.setToggleGroup(recommendationGroup);

        btnOui.selectedProperty().addListener((obs, old, isSelected) -> {
            if (isSelected) {
                btnOui.setStyle("-fx-font-size: 15px; -fx-padding: 15 40; -fx-background-color: #10b981; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            } else {
                btnOui.setStyle("-fx-font-size: 15px; -fx-padding: 15 40; -fx-background-color: white; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-text-fill: #10b981; -fx-font-weight: bold; -fx-cursor: hand;");
            }
        });

        btnNon.selectedProperty().addListener((obs, old, isSelected) -> {
            if (isSelected) {
                btnNon.setStyle("-fx-font-size: 15px; -fx-padding: 15 40; -fx-background-color: #ef4444; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            } else {
                btnNon.setStyle("-fx-font-size: 15px; -fx-padding: 15 40; -fx-background-color: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-cursor: hand;");
            }
        });
    }

    private void setupCharacterCounter() {
        txtCommentaire.textProperty().addListener((obs, oldV, newV) -> {
            int length = newV.length();
            lblCharCount.setText(length + "/1000");
            if (length > 1000) {
                lblCharCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
            } else if (length > 800) {
                lblCharCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #f59e0b;");
            } else {
                lblCharCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");
            }
        });
    }

    private void setupActionButtons() {
        btnPublier.setOnAction(e -> handlePublish());
        btnAnnuler.setOnAction(e -> clearForm());
    }

    private void handlePublish() {
        if (!validateForm()) {
            return;
        }

        try {
            Avis avis = new Avis();
            avis.setIdUtilisateur(1);
            avis.setNote(currentRating);
            avis.setTitre(txtTitre.getText().trim());
            avis.setCommentaire(txtCommentaire.getText().trim());
            avis.setNoteHebergement((int) sliderHebergement.getValue());
            avis.setNoteTransport((int) sliderTransport.getValue());
            avis.setNoteActivites((int) sliderActivites.getValue());
            avis.setNoteQualitePrix((int) sliderQualitePrix.getValue());
            avis.setRecommande(btnOui.isSelected());
            avis.setDateAvis(new java.sql.Date(System.currentTimeMillis()));

            if (serviceAvis.ajouter(avis)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("✓ Succès");
                alert.setHeaderText("Merci pour votre avis !");
                alert.setContentText("Votre avis a été publié avec succès et aidera d'autres voyageurs.");
                alert.showAndWait();
                resetForm();
            } else {
                showError("Une erreur est survenue lors de la publication de votre avis.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de connexion à la base de données : " + e.getMessage());
        }
    }

    private boolean validateForm() {
        StringBuilder erreurs = new StringBuilder();

        if (currentRating == 0) {
            erreurs.append("• Veuillez sélectionner une note avec les étoiles\n");
        }

        if (txtTitre.getText().trim().isEmpty()) {
            erreurs.append("• Le titre de l'avis est obligatoire\n");
        }

        if (txtCommentaire.getText().trim().isEmpty()) {
            erreurs.append("• Le commentaire est obligatoire\n");
        }

        if (recommendationGroup.getSelectedToggle() == null) {
            erreurs.append("• Veuillez indiquer si vous recommandez cette destination\n");
        }

        if (txtCommentaire.getText().length() > 1000) {
            erreurs.append("• Le commentaire ne doit pas dépasser 1000 caractères\n");
        }

        if (erreurs.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⚠ Formulaire incomplet");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(erreurs.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("✗ Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Annuler votre avis ?");
        confirmation.setContentText("Êtes-vous sûr ? Toutes les informations saisies seront perdues.");

        ButtonType btnConfirmer = new ButtonType("Oui, annuler", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnRetour = new ButtonType("Non, continuer", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmation.getButtonTypes().setAll(btnConfirmer, btnRetour);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == btnConfirmer) {
                resetForm();
            }
        });
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
    }
}